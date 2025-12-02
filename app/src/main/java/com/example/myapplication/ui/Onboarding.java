package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.ChildUI.ChildHomeActivity;
import com.example.myapplication.ui.ParentUI.ParentHomeActivity;
import com.example.myapplication.ui.ProviderUI.ProviderHomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions; // <-- IMPORTANT NEW IMPORT

import java.util.HashMap;
import java.util.Map;

public class Onboarding extends AppCompatActivity {
    private static final String TAG = "OnboardingActivity";
    private int currentScreen = 0;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    // Array of onboarding layout resources
    private final int[] onboardingLayouts = {
            R.layout.onboarding1,
            R.layout.onboarding2,
            R.layout.onboarding3,
            R.layout.onboarding4,
            R.layout.onboarding5,
            R.layout.onboarding6,
            R.layout.onboarding7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // Show first onboarding screen
        showScreen(currentScreen);
    }

    private void showScreen(int screenIndex) {
        setContentView(onboardingLayouts[screenIndex]);

        Button nextButton = findViewById(R.id.nextButton);

        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                if (currentScreen < onboardingLayouts.length - 1) {
                    currentScreen++;
                    showScreen(currentScreen);
                } else {
                    finishOnboarding();
                }
            });
        }
    }

    private void finishOnboarding() {
        FirebaseUser user = fAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // 1. Prepare data to mark onboarding complete
            Map<String, Object> updates = new HashMap<>();
            updates.put("onboardingCompleted", true);

            // 2. FIX: Use set with merge option to reliably update the flag
            fStore.collection("users").document(userId)
                    .set(updates, SetOptions.merge()) // <-- FIX FOR UPDATE FAILURES
                    .addOnSuccessListener(aVoid -> {
                        // After successful update, fetch the user's role
                        fetchUserRoleAndRedirect(userId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update onboarding status (using set.merge).", e);
                        // If update fails, still try to redirect based on existing role
                        fetchUserRoleAndRedirect(userId);
                    });
        } else {
            // No user logged in, go to main activity (which should handle sign-in)
            navigateToTarget(MainActivity.class);
        }
    }

    private void fetchUserRoleAndRedirect(String userId) {
        fStore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");

                        // Add this line to log the role before redirection
                        Log.d(TAG, "Onboarding complete. User role: " + role);

                        if ("parent".equalsIgnoreCase(role)) {
                            navigateToTargetWithId(ParentHomeActivity.class, userId); // **FIXED**
                        } else if ("child".equalsIgnoreCase(role)) {
                            // Example:
                            navigateToTargetWithId(ChildHomeActivity.class, userId); // **FIXED**
                            // navigateToTarget(MainActivity.class); // REMOVED redundant fallback
                        } else if ("provider".equalsIgnoreCase(role)) {
                            // Example:
                            navigateToTargetWithId(ProviderHomeActivity.class, userId); // **FIXED**
                            // navigateToTarget(MainActivity.class); // REMOVED redundant fallback
                        }
                        else {
                            // Default fallback (should ideally sign out or go to role selection)
                            Log.w(TAG, "Role is missing or unknown: " + role);
                            navigateToTarget(MainActivity.class);
                        }
                    } else {
                        Log.w(TAG, "User document not found.");
                        navigateToTarget(MainActivity.class);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch user role.", e);
                    navigateToTarget(MainActivity.class);
                });
    }

    private void navigateToTarget(Class<?> targetActivity) {
        Intent intent = new Intent(Onboarding.this, targetActivity);
        startActivity(intent);
        finish();
    }

    // **NEW HELPER METHOD to include the user ID in the intent**
    private void navigateToTargetWithId(Class<?> targetActivity, String userId) {
        Intent intent = new Intent(Onboarding.this, targetActivity);
        intent.putExtra("id", userId);
        startActivity(intent);
        finish();
    }
}