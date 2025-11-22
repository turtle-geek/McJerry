package com.example.myapplication.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class Register extends AppCompatActivity {

    private TextInputEditText nameET, mailET, passwordET, confirmPasswordET;
    private Button registerButton;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Initialize UI components
        TextInputLayout nameLayout = findViewById(R.id.nameLayoutRegister);
        TextInputLayout mailLayout = findViewById(R.id.mailLayoutRegister);
        TextInputLayout passwordLayout = findViewById(R.id.passwordLayoutRegister);
        TextInputLayout confirmPasswordLayout = findViewById(R.id.confirmPasswordLayoutRegister);

        nameET = findViewById(R.id.nameETRegister);
        mailET = findViewById(R.id.mailETRegister);
        passwordET = findViewById(R.id.passwordETRegister);
        confirmPasswordET = findViewById(R.id.confirmPasswordETRegister);
        registerButton = findViewById(R.id.loginBotton); // Note: XML has typo "loginBotton"

        // Apply blur effect to the register container
        ViewGroup registerContainer = findViewById(R.id.registerContainer);
        Blurry.with(this)
                .radius(25)
                .sampling(2)
                .color(Color.argb(66, 255, 255, 255))
                .async()
                .animate(500);

        // Register button click listener
        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = nameET.getText().toString().trim();
        String email = mailET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            nameET.setError("Name is required");
            nameET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            mailET.setError("Email is required");
            mailET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordET.setError("Password is required");
            passwordET.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordET.setError("Password must be at least 6 characters");
            passwordET.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordET.setError("Passwords do not match");
            confirmPasswordET.requestFocus();
            return;
        }

        // Create user with Firebase Auth
        fAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration successful
                        String userId = fAuth.getCurrentUser().getUid();

                        // Store user info in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("role", "child"); // Default role, you can change this based on your needs

                        fStore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(Register.this, "Registration successful!",
                                            Toast.LENGTH_SHORT).show();

                                    // Navigate to LoginPage
                                    Intent intent = new Intent(Register.this, LoginPage.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(Register.this, "Failed to save user data: " +
                                            e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        // Registration failed
                        Toast.makeText(Register.this, "Registration failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}