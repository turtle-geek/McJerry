package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;

public class ParentRegisterChild extends AppCompatActivity {

    private TextInputEditText nameET, mailET, passwordET, confirmPasswordET;
    private Button nextButton;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_registerchild);

        // Initialize UI components
        nameET = findViewById(R.id.nameETRegister);
        mailET = findViewById(R.id.mailETRegister);
        passwordET = findViewById(R.id.passwordETRegister);
        confirmPasswordET = findViewById(R.id.confirmPasswordETRegister);
        nextButton = findViewById(R.id.loginBotton);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Next button - validate and go to health info page
        nextButton.setOnClickListener(v -> proceedToHealthInfo());
    }

    private void proceedToHealthInfo() {
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

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mailET.setError("Enter a valid email address");
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

        // All validation passed, proceed to health info page
        Intent intent = new Intent(ParentRegisterChild.this, ParentRegisterLogin.class);
        intent.putExtra("childName", name);
        intent.putExtra("childEmail", email);
        intent.putExtra("childPassword", password);
        startActivity(intent);
    }
}