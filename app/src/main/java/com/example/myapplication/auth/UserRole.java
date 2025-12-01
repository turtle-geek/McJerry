package com.example.myapplication.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.User;
import com.example.myapplication.ui.Onboarding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class UserRole extends AppCompatActivity {
    private Button parentButton, doctorButton;
    private FirebaseAuth fAuth;
    private String userName, userEmail, userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity_choosing);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();

        // Get user info from Register activity
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
        userPassword = intent.getStringExtra("userPassword");


        // Initialize buttons
        parentButton = findViewById(R.id.parentButton);
        doctorButton = findViewById(R.id.doctorButton);

        // Apply blur effect
        ViewGroup loginContainer = findViewById(R.id.loginContainerIdentity);
        Blurry.with(this)
                .radius(25)
                .sampling(2)
                .color(Color.argb(66, 255, 255, 255))
                .async()
                .animate(500);

        // Set click listeners
        parentButton.setOnClickListener(v -> saveUserRole("parent"));
        doctorButton.setOnClickListener(v -> saveUserRole("provider"));
    }

    private void saveUserRole(String role) {
        AuthMan.signUp(userEmail, userPassword, userName, role, task -> {
            if (!task.isSuccessful()) {
                Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show();
            }
        });
        String userId = fAuth.getCurrentUser().getUid();
        User user = AuthMan.createUserProfile(userId, userName, userEmail, role);
        AuthMan.addToDatabase(user);

        Toast.makeText(UserRole.this,
                "Welcome! Setting up your account...", Toast.LENGTH_SHORT).show();

        // Navigate to OnboardingActivity for first-time users
        Intent intent = new Intent(UserRole.this, Onboarding.class);
        startActivity(intent);
        finish();
    }
}