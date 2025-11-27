package com.example.myapplication.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.ui.ParentHomeActivity;
import com.example.myapplication.ui.ParentManagement;
import com.example.myapplication.ui.ParentTutorial;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class SignOut extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private CardView medicationHistoryCard, inviteCard, reportCard;
    private Button logoutButton;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signout_page);

        mAuth = FirebaseAuth.getInstance();

        // Initialize CardViews
        medicationHistoryCard = findViewById(R.id.medicationHistoryCard);
        inviteCard = findViewById(R.id.inviteCard);
        reportCard = findViewById(R.id.reportCard);
        logoutButton = findViewById(R.id.LogoutButton);
        bottomNavigationView = findViewById(R.id.menuBar);

        // Set up bottom navigation - ONLY if it exists
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }

        // Set click listeners for cards (for future features)
        if (medicationHistoryCard != null) {
            medicationHistoryCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Medication History", Toast.LENGTH_SHORT).show();
                // TODO: Implement medication history
            });
        }

        if (inviteCard != null) {
            inviteCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Invite", Toast.LENGTH_SHORT).show();
                // TODO: Implement invite feature
            });
        }

        if (reportCard != null) {
            reportCard.setOnClickListener(v -> {
                Toast.makeText(this, "Opening Report", Toast.LENGTH_SHORT).show();
                // TODO: Implement report feature
            });
        }

        // Logout button - Manual logout
        if (logoutButton != null) {
            logoutButton.setOnClickListener(v -> {
                manualLogout();
            });
        }
    }

    private void setupBottomNavigation() {
        try {
            // Set the current item as selected
            bottomNavigationView.setSelectedItemId(R.id.moreButton);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        // Navigate to Parent Home
                        startActivity(new Intent(SignOut.this, ParentHomeActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.fileButton) {
                        // Navigate to Parent Management
                        startActivity(new Intent(SignOut.this, ParentManagement.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.nav_profile) {
                        // Navigate to Parent Tutorial
                        startActivity(new Intent(SignOut.this, ParentTutorial.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.moreButton) {
                        // Already on Sign Out Page - do nothing
                        return true;
                    }

                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Manual logout - when user clicks the logout button
    private void manualLogout() {
        try {
            // Sign out from Firebase
            mAuth.signOut();

            // Clear saved credentials from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate back to Login page and clear all previous activities
            Intent intent = new Intent(this, LoginPage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Logout error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                // User not logged in, redirect to MainActivity
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}