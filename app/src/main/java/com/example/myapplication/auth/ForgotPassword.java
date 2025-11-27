package com.example.myapplication.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    private TextInputEditText emailET;
    private Button resetButton;
    private TextView backToLoginTextView;
    private FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password_page);

        // Initialize Firebase Auth
        fAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        emailET = findViewById(R.id.forgotPasswordEmailET);
        resetButton = findViewById(R.id.resetPasswordButton);
        backToLoginTextView = findViewById(R.id.backToLoginTextView);

        // Reset button click listener
        resetButton.setOnClickListener(v -> resetPassword());

        // Back to login click listener
        backToLoginTextView.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = emailET.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            emailET.setError("Email is required");
            emailET.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailET.setError("Please enter a valid email");
            emailET.requestFocus();
            return;
        }

        // Send password reset email
        fAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPassword.this,
                                "Password reset email sent! Please check your inbox.",
                                Toast.LENGTH_LONG).show();
                        finish(); // Go back to login page
                    } else {
                        Toast.makeText(ForgotPassword.this,
                                "Failed to send reset email: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}