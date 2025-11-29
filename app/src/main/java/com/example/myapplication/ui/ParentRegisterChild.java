package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ParentRegisterChild extends AppCompatActivity {

    private TextInputEditText nameET, userIdET, passwordET, confirmPasswordET;
    private TextInputLayout userIdLayout;
    private Button nextButton;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private boolean isUserIdValid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_registerchild);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize UI components - CORRECTED
        nameET = findViewById(R.id.nameETRegister);
        userIdET = findViewById(R.id.userIdETRegister);           // ✅ FIXED - was missing
        userIdLayout = findViewById(R.id.userIdLayoutRegister);   // ✅ FIXED - was wrong ID
        passwordET = findViewById(R.id.passwordETRegister);
        confirmPasswordET = findViewById(R.id.confirmPasswordETRegister);
        nextButton = findViewById(R.id.loginBotton);
        btnBack = findViewById(R.id.btnBack);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Add TextWatcher to check ID availability in real-time
        userIdET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    checkUserIdAvailability(s.toString().trim());
                } else {
                    userIdLayout.setError(null);
                    userIdLayout.setHelperText(null);
                    isUserIdValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Next button - validate and go to health info page
        nextButton.setOnClickListener(v -> proceedToHealthInfo());
    }

    private void checkUserIdAvailability(String userId) {
        // Query Firebase to check if the ID already exists
        db.collection("users")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ID is occupied
                        userIdLayout.setError("ID is occupied, please use another one");
                        userIdLayout.setErrorEnabled(true);
                        isUserIdValid = false;
                    } else {
                        // ID is available
                        userIdLayout.setError(null);
                        userIdLayout.setErrorEnabled(false);
                        userIdLayout.setHelperText("✓ ID is available");
                        isUserIdValid = true;
                    }
                })
                .addOnFailureListener(e -> {
                    userIdLayout.setError("Unable to verify ID availability");
                    isUserIdValid = false;
                });
    }

    private void proceedToHealthInfo() {
        String name = nameET.getText().toString().trim();
        String userId = userIdET.getText().toString().trim();
        String password = passwordET.getText().toString().trim();
        String confirmPassword = confirmPasswordET.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            nameET.setError("Name is required");
            nameET.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(userId)) {
            userIdET.setError("User ID is required");
            userIdET.requestFocus();
            return;
        }

        if (!isUserIdValid) {
            userIdET.setError("Please choose a valid and unique ID");
            userIdET.requestFocus();
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
        intent.putExtra("childUserId", userId);
        intent.putExtra("childPassword", password);
        startActivity(intent);
    }
}