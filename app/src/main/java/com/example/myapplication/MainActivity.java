package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

// --- ASSUMED IMPORTS ---
import com.example.myapplication.auth.AuthManager;
import com.example.myapplication.auth.LoginPage;
import com.example.myapplication.ui.ChildHomeActivity;
import com.example.myapplication.ui.Onboarding;
import com.example.myapplication.ui.ParentHomeActivity;
import com.example.myapplication.ui.ProviderHomeActivity;
// --- END ASSUMED IMPORTS ---

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.MemoryCacheSettings;
import com.google.firebase.FirebaseApp; // Needed to check if Firebase is initialized

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener {

    private static final String TAG = "MainActivityRouter";
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        AuthManager.signOut();
        try {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(
                            MemoryCacheSettings.newBuilder().build()
                    )
                    .build();
            db.setFirestoreSettings(settings);
            Log.d(TAG, "FirebaseFirestore settings set to disable persistence.");
        } catch (IllegalStateException e) {
            // This is expected if onCreate is called more than once (e.g., orientation change)
            // or if other Firebase methods are called first. We log it but proceed.
            Log.w(TAG, "Firestore settings could not be modified: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firestore settings.", e);
        }

        fAuth = FirebaseAuth.getInstance();

        checkAndRouteUser();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Defensive check: only attach if FirebaseApp is initialized
        if (FirebaseApp.getApps(this).size() > 0) {
            AuthManager.attachAuthStateListener(this);
        } else {
            Log.e(TAG, "FirebaseApp not initialized, cannot attach AuthStateListener.");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        AuthManager.detachAuthStateListener(this);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        // Only route if the activity isn't already finishing or destroyed
        if (!isFinishing()) {
            checkAndRouteUser();
        }
    }

    private void checkAndRouteUser() {
        FirebaseUser currentUser = fAuth.getCurrentUser();

        if (currentUser == null) {
            // User is NOT logged in. Redirect to Login Page.
            Intent intent = new Intent(this, LoginPage.class);
            startActivity(intent);
            finish();
            return;
        }

        // If the user is logged in, check their role and onboarding status.
        checkRoleAndOnboarding(currentUser.getUid());
    }

    private void checkRoleAndOnboarding(String userId) {
        // **SAFETY CHECK:** Ensure userId is not null before attempting Firestore access (though it shouldn't be here)
        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "User ID is null or empty. Forcing sign out.");
            AuthManager.signOut();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(userId).get()
                .addOnSuccessListener(documentInfo -> {
                    if (documentInfo.exists()) {
                        Boolean onboardingCompleted = documentInfo.getBoolean("onboardingCompleted");
                        String role = documentInfo.getString("role");

                        if (onboardingCompleted == null || !onboardingCompleted) {
                            // User is logged in but hasn't completed onboarding.
                            Log.d(TAG, "User logged in, redirecting to Onboarding.");
                            Intent intent = new Intent(this, Onboarding.class);
                            startActivity(intent);
                            finish();
                        } else if (role != null) {
                            // Onboarding complete and role is found.
                            Log.d(TAG, "Onboarding complete. Redirecting to home page for role: " + role);
                            landonSpecificPage(role, userId); // **PASSED userId for consistency**
                        } else {
                            // Onboarding complete but role is missing (data issue).
                            Log.w(TAG, "Onboarding complete but role field is missing for user: " + userId);
                            Toast.makeText(this, "User role is missing, please sign in again.", Toast.LENGTH_LONG).show();
                            AuthManager.signOut();
                        }
                    }
                    else {
                        // Document doesn't exist (user created but document failed to save).
                        // This likely means role isn't set, so we force onboarding/setup.
                        Log.e(TAG, "User document does not exist for ID: " + userId + ". Starting onboarding to ensure creation.");
                        Toast.makeText(this, "Cannot find user information, starting onboarding.", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(this, Onboarding.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(exception -> {
                    // Failed to read Firestore (network/permissions issue).
                    Log.e(TAG, "Failed to fetch user document for ID: " + userId, exception);
                    Toast.makeText(this, "Cannot process user data. Please check connection.", Toast.LENGTH_SHORT).show();
                    AuthManager.signOut();
                });
    }

    // **MODIFIED:** Take userId as an argument to ensure it's used if retrieved.
    private void landonSpecificPage(String role, String userId) {
        Intent intent;

        // **REMOVED REDUNDANT:** User is guaranteed non-null if we reach here.
        /*
        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "Attempted to land on specific page but user is null.");
            AuthManager.signOut(); // Fallback to sign out
            return;
        }
        String userId = currentUser.getUid();
        */

        if (userId == null || userId.isEmpty()) {
            Log.e(TAG, "Cannot redirect, userId is unexpectedly null.");
            AuthManager.signOut();
            return;
        }

        switch (role.toLowerCase()) {
            case "child":
                intent = new Intent(this, ChildHomeActivity.class);
                intent.putExtra("id", userId);
                break;
            case "parent":
                intent = new Intent(this, ParentHomeActivity.class);
                intent.putExtra("id", userId);
                break;
            case "provider":
                intent = new Intent(this, ProviderHomeActivity.class);
                intent.putExtra("id", userId);
                break;
            default:
                Log.w(TAG, "Unknown Character Role: " + role);
                Toast.makeText(this, "Unknown Character Role: " + role, Toast.LENGTH_SHORT).show();
                AuthManager.signOut();
                return;
        }
        startActivity(intent);
        finish();
    }
}