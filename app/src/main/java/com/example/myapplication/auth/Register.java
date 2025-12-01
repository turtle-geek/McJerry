package com.example.myapplication.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);

        // Initialize UI components
        nameET = findViewById(R.id.nameETRegister);
        mailET = findViewById(R.id.mailETRegister);
        passwordET = findViewById(R.id.passwordETRegister);
        confirmPasswordET = findViewById(R.id.confirmPasswordETRegister);
        registerButton = findViewById(R.id.loginBotton);

        // Apply blur effect to the register container - FIXED VERSION
        Blurry.with(this)
                .radius(25)              // Blur intensity (higher = more blur)
                .sampling(2)             // Down sampling (higher = faster but lower quality)
                .color(Color.argb(66, 255, 255, 255))  // White overlay with transparency
                .async()                 // Do it asynchronously for better performance
                .animate(500);           // Fade in animation (milliseconds)


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

        if (!email.contains("@")) {
            mailET.setError("Invalid email format");
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

        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUpperCase) {
            passwordET.setError("Password must contain at least one uppercase letter");
            passwordET.requestFocus();
            return;
        }
        if (!hasLowerCase) {
            passwordET.setError("Password must contain at least one lowercase letter");
            passwordET.requestFocus();
            return;
        }
        if (!hasDigit) {
            passwordET.setError("Password must contain at least one digit");
            passwordET.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordET.setError("Passwords do not match");
            confirmPasswordET.requestFocus();
            return;
        }

        Intent intent = new Intent(Register.this, UserRole.class);
        intent.putExtra("userName", name);  // Pass name to save later
        intent.putExtra("userEmail", email);  // Pass email to save later
        intent.putExtra("userPassword", password);  // Pass password to save later
        startActivity(intent);
        finish();
    }
}