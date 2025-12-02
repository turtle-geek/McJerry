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
import com.google.firebase.auth.FirebaseAuth;

import jp.wasabeef.blurry.Blurry;

public class UserRole extends AppCompatActivity {
    private Button parentButton, doctorButton;
    private FirebaseAuth fAuth;
    private String userName, userEmail, userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity_choosing);

        fAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
        userPassword = intent.getStringExtra("userPassword");


        parentButton = findViewById(R.id.parentButton);
        doctorButton = findViewById(R.id.doctorButton);

        ViewGroup loginContainer = findViewById(R.id.loginContainerIdentity);
        Blurry.with(this)
                .radius(25)
                .sampling(2)
                .color(Color.argb(66, 255, 255, 255))
                .async()
                .animate(500);

        parentButton.setOnClickListener(v -> saveUserRole("Parent"));
        doctorButton.setOnClickListener(v -> saveUserRole("Provider"));
    }

    private void saveUserRole(String role) {
        Toast.makeText(UserRole.this, "Signing up as " + role + ". Please wait...", Toast.LENGTH_LONG).show();

        // This call to AuthManager handles:
        // 1. Firebase Auth user creation.
        // 2. Firestore profile creation.
        // 3. Local session update.
        AuthManager.signUp(userEmail, userPassword, userName, role, task -> {
            if (task.isSuccessful()) {
                Toast.makeText(UserRole.this,
                        "Welcome! Account setup complete.", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(UserRole.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}