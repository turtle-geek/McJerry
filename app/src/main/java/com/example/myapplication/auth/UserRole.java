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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class UserRole extends AppCompatActivity {

    private Button parentButton, doctorButton;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity_choosing);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Get user info from Register activity
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");

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
        String userId = fAuth.getCurrentUser().getUid();

        // Create user document in Firestore with selected role
        Map<String, Object> user = new HashMap<>();
        user.put("name", userName);
        user.put("email", userEmail);
        user.put("role", role);

        fStore.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(UserRole.this,
                            "Welcome! Setting up your account...", Toast.LENGTH_SHORT).show();

                    // Navigate to MainActivity which will redirect to appropriate home page
                    Intent intent = new Intent(UserRole.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(UserRole.this,
                            "Failed to save role: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}