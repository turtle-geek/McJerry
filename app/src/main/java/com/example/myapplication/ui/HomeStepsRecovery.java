package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut;
import com.example.myapplication.auth.SignOut_child;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

/**
 * HomeStepsRecovery - Tutorial/Recovery Steps Activity
 * Works for BOTH parent and child users
 * Automatically detects user role and navigates accordingly
 */
public class HomeStepsRecovery extends AppCompatActivity {

    private static final String TAG = "HomeStepsRecovery";

    private CheckBox step1, step2, step3, step4;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String userRole = "child"; // Default to child

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_steps_recovery);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize checkboxes
        initializeBoxes();

        // Set up YouTube videos
        setUpVideos();

        // Detect user role and setup navigation
        detectUserRoleAndSetupNavigation();

        // Done button
        findViewById(R.id.doneButton).setOnClickListener(v -> {
            if (checkBoxes()) {
                finish();
            }
        });
    }

    /**
     * Detect if current user is parent or child
     * Then setup appropriate navigation
     */
    private void detectUserRoleAndSetupNavigation() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in");
            setupDefaultNavigation();
            return;
        }

        String userId = currentUser.getUid();

        // Query user's role from Firestore
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        userRole = (role != null) ? role : "child";

                        Log.d(TAG, "User role detected: " + userRole);

                        // Setup navigation based on role
                        setupBottomNavigation();
                    } else {
                        Log.w(TAG, "User document not found, defaulting to child");
                        setupBottomNavigation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading user role", e);
                    setupBottomNavigation();
                });
    }

    private void setupDefaultNavigation() {
        bottomNavigationView = findViewById(R.id.menuBar);
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }
    }

    private void initializeBoxes(){
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);

        if (step1 != null) step1.setChecked(false);
        if (step2 != null) step2.setChecked(false);
        if (step3 != null) step3.setChecked(false);
        if (step4 != null) step4.setChecked(false);
    }

    private void setUpVideos(){
        try {
            // Initialize Rescue Inhaler YouTube Player
            YouTubePlayerView rescuePlayer = findViewById(R.id.rescueYoutubePlayer);
            if (rescuePlayer != null) {
                getLifecycle().addObserver(rescuePlayer);

                rescuePlayer.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        String videoId = "LU-pRbN7AD4";
                        youTubePlayer.cueVideo(videoId, 60);
                    }
                });
            }

            // Initialize Breathing Exercises YouTube Player
            YouTubePlayerView breathingPlayer = findViewById(R.id.breathingPlayer);
            if (breathingPlayer != null) {
                getLifecycle().addObserver(breathingPlayer);

                breathingPlayer.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        String videoId = "FyjZLPmZ534";
                        youTubePlayer.cueVideo(videoId, 0);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up videos", e);
        }
    }

    private void setupBottomNavigation() {
        try {
            bottomNavigationView = findViewById(R.id.menuBar);
            if (bottomNavigationView == null) {
                Log.e(TAG, "Bottom navigation view not found!");
                return;
            }

            // Set nav_profile as selected (we're on tutorial page)
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        navigateToHome();
                        return true;

                    } else if (id == R.id.fileButton) {
                        navigateToManagement();
                        return true;

                    } else if (id == R.id.nav_profile) {
                        // Already on Tutorial - do nothing
                        return true;

                    } else if (id == R.id.moreButton) {
                        navigateToSignOut();
                        return true;
                    }

                    return false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation", e);
        }
    }

    /**
     * Navigate to Home based on user role
     */
    private void navigateToHome() {
        Intent intent;
        if ("parent".equalsIgnoreCase(userRole)) {
            intent = new Intent(this, ParentHomeActivity.class);
            Log.d(TAG, "Navigating to ParentHomeActivity");
        } else {
            intent = new Intent(this, ChildHomeActivity.class);
            Log.d(TAG, "Navigating to ChildHomeActivity");
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Navigate to Management based on user role
     */
    private void navigateToManagement() {
        Intent intent;
        if ("parent".equalsIgnoreCase(userRole)) {
            intent = new Intent(this, ParentManagement.class);
            Log.d(TAG, "Navigating to ParentManagement");
        } else {
            intent = new Intent(this, ChildManagement.class);
            Log.d(TAG, "Navigating to ChildManagement");
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Navigate to SignOut based on user role
     */
    private void navigateToSignOut() {
        Intent intent;
        if ("parent".equalsIgnoreCase(userRole)) {
            intent = new Intent(this, SignOut.class);
            Log.d(TAG, "Navigating to SignOut (Parent)");
        } else {
            intent = new Intent(this, SignOut_child.class);
            Log.d(TAG, "Navigating to SignOut_child");
        }
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private boolean checkBoxes(){
        // Return true only when ALL boxes are checked
        if (step1 != null && step2 != null && step3 != null && step4 != null) {
            if (step1.isChecked() && step2.isChecked() && step3.isChecked() && step4.isChecked()){
                // All steps completed
                return true;
            }
        }

        // Not all steps completed - show message
        Toast.makeText(this, "Please complete all steps", Toast.LENGTH_SHORT).show();
        return false;
    }
}