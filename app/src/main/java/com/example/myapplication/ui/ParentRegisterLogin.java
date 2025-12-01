package com.example.myapplication.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.Child;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Parent Register Login Activity
 * Final step of child registration - saves child data to Firestore
 * This creates the link between parent and child accounts
 */
public class ParentRegisterLogin extends AppCompatActivity {

    private static final String TAG = "ParentRegisterLogin";

    private TextInputEditText etChildName, etBirthday, etSpecialNote;
    private Button registerButton;
    private ImageButton btnBack;

    private FirebaseAuth fAuth;
    private FirebaseFirestore db;

    private String childName, childUsername, childPassword;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_registerlogin);

        // Initialize Firebase
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        calendar = Calendar.getInstance();

        // Get data from previous activity (ParentRegisterChild)
        Intent intent = getIntent();
        childName = intent.getStringExtra("childName");
        childUsername = intent.getStringExtra("childUsername");
        childPassword = intent.getStringExtra("childPassword");

        // Initialize UI components
        etChildName = findViewById(R.id.etChildName);
        etBirthday = findViewById(R.id.etBirthday);
        etSpecialNote = findViewById(R.id.etSpecialNote);
        registerButton = findViewById(R.id.loginBotton);
        btnBack = findViewById(R.id.btnBack);

        // Pre-fill name from previous screen
        if (childName != null) {
            etChildName.setText(childName);
        }

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Date picker for birthday
        setupDatePicker();

        // Register button
        registerButton.setOnClickListener(v -> registerChild());
    }

    private void setupDatePicker() {
        etBirthday.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    ParentRegisterLogin.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        etBirthday.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );
            datePickerDialog.show();
        });
    }

    private void registerChild() {
        String displayName = etChildName.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String specialNote = etSpecialNote.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(displayName)) {
            etChildName.setError("Child name is required");
            etChildName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(birthday)) {
            etBirthday.setError("Birthday is required");
            etBirthday.requestFocus();
            return;
        }

        // Get current parent ID
        String parentId = fAuth.getCurrentUser().getUid();
        String childEmail = childUsername + "@mcjerry.app";

        // Create child account with Firebase Auth
        fAuth.createUserWithEmailAndPassword(childEmail, childPassword)
                .addOnSuccessListener(authResult -> {
                    String childId = authResult.getUser().getUid();

                    // ✅ CRITICAL: Create complete child document with all fields
                    // This ensures the child can see their profile when they log in
                    Map<String, Object> childData = new HashMap<>();
                    childData.put("id", childId);
                    childData.put("parentID", parentId);
                    childData.put("name", displayName);
                    childData.put("email", childEmail);
                    childData.put("emailUsername", childUsername);
                    childData.put("role", "child");
                    childData.put("dateOfBirth", birthday);
                    childData.put("notes", specialNote != null ? specialNote : "");
                    childData.put("PEF_PB", 400); // Default personal best
                    childData.put("createdAt", com.google.firebase.Timestamp.now());

                    // Save to Firestore
                    db.collection("users").document(childId)
                            .set(childData)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Child account created successfully: " + childId);
                                Toast.makeText(ParentRegisterLogin.this,
                                        "Child account created successfully!",
                                        Toast.LENGTH_SHORT).show();

                                // Sign the parent back in
                                signParentBackIn(parentId);
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to save child data", e);
                                Toast.makeText(ParentRegisterLogin.this,
                                        "Failed to save child data: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create account", e);
                    Toast.makeText(ParentRegisterLogin.this,
                            "Failed to create account: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void signParentBackIn(String parentId) {
        // After creating child account, redirect back to parent management
        // Note: We sign out first to clear the child's auth session
        fAuth.signOut();

        // Redirect back to ParentManagement with success flag
        Intent intent = new Intent(ParentRegisterLogin.this, ParentManagement.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("childAdded", true);
        startActivity(intent);
        finish();
    }
}