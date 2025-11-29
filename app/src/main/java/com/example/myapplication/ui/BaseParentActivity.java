package com.example.myapplication.ui;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * Base Activity for all parent home activities
 * Handles auto-logout when app is closed
 */
public class BaseParentActivity extends AppCompatActivity {

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Check if the app is finishing (being closed completely)
        // This happens when user presses back button on last activity or closes app from recent apps
        if (isFinishing() && isTaskRoot()) {
            // Auto logout when app closes
            autoLogout();
        }
    }

    /**
     * Auto logout - clears Firebase auth and SharedPreferences
     * Called automatically when app is closed
     */
    private void autoLogout() {
        try {
            // Sign out from Firebase
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            if (mAuth != null) {
                mAuth.signOut();
            }

            // Clear saved credentials from SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            if (sharedPreferences != null) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}