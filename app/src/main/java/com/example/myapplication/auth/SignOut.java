package com.example.myapplication.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignOut extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private CardView medicationHistoryCard, inviteCard, reportCard;
    private Button logoutButton;

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

        // Set click listeners for cards
        medicationHistoryCard.setOnClickListener(v -> {
            // Navigate to Medication History Activity
            Toast.makeText(this, "Opening Medication History", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, MedicationHistoryActivity.class);
            // startActivity(intent);
        });

        inviteCard.setOnClickListener(v -> {
            // Navigate to Invite Activity
            Toast.makeText(this, "Opening Invite", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, InviteActivity.class);
            // startActivity(intent);
        });

        reportCard.setOnClickListener(v -> {
            // Navigate to Report Activity
            Toast.makeText(this, "Opening Report", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(this, ReportActivity.class);
            // startActivity(intent);
        });

        // Logout button
        logoutButton.setOnClickListener(v -> {
            logout();
        });
    }

    private void logout() {
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}