package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.auth.AuthMan;
import com.example.myapplication.models.Child;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Objects;

public class ParentRegisterChild extends AppCompatActivity {

    private TextInputEditText nameET, userIdET, passwordET, confirmPasswordET;
    private TextInputLayout userIdLayout;
    private Button nextButton;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private boolean isUsernameValid = false;

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
                    checkUsernameAvailability(s.toString().trim());
                } else {
                    userIdLayout.setError(null);
                    userIdLayout.setHelperText(null);
                    isUsernameValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        String parentID = getIntent().getStringExtra("parentID");

        // Next button - validate and go to health info page
        nextButton.setOnClickListener(v -> createChildUnderParent(parentID));
    }

    private void createChildUnderParent(String parentID) {
        db.collection("users").document(parentID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        String name = nameET.getText().toString().trim();
                        String username = userIdET.getText().toString().trim();
                        String password = passwordET.getText().toString().trim();
                        String confirmPassword = confirmPasswordET.getText().toString().trim();

                        // Validation
                        if (TextUtils.isEmpty(name)) {
                            nameET.setError("Name is required");
                            nameET.requestFocus();
                            return;
                        }

                        if (TextUtils.isEmpty(username)) {
                            userIdET.setError("User is required");
                            userIdET.requestFocus();
                            return;
                        }

                        if (!isUsernameValid) {
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
                        intent.putExtra("childUsername", username);
                        intent.putExtra("childPassword", password);
                        startActivity(intent);
                    }
                });
    }

    private void checkUsernameAvailability(String username) {
        // Query Firebase to check if the ID already exists
        db.collection("users")
                .whereEqualTo("emailUsername", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // ID is occupied
                        userIdLayout.setError("Username is occupied, please use another one");
                        userIdLayout.setErrorEnabled(true);
                        isUsernameValid = false;
                    } else if (username.indexOf(' ') >= 0){
                        isUsernameValid = false;
                        userIdLayout.setError("Username cannot contain spaces");
                        userIdLayout.setErrorEnabled(true);
                    }
                    else {
                        // ID is available
                        userIdLayout.setError(null);
                        userIdLayout.setErrorEnabled(false);
                        userIdLayout.setHelperText("✓ Username is available");
                        isUsernameValid = true;
                    }
                })
                .addOnFailureListener(e -> {
                    userIdLayout.setError("Unable to verify Username availability");
                    isUsernameValid = false;
                });
    }
}