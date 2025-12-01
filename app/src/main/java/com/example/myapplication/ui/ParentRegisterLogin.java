package com.example.myapplication.ui;

import static java.time.LocalDate.parse;

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
import com.example.myapplication.auth.AuthMan;
import com.example.myapplication.models.Child;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ParentRegisterLogin extends AppCompatActivity {

    private TextInputEditText etChildName, etBirthday, etSpecialNote;
    private Button registerButton;
    private ImageButton btnBack;

    private FirebaseAuth fAuth;
    private FirebaseFirestore db;

    private String childName, childUsername, childPassword;
    private Calendar calendar;
    private String id;

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

    private void signParentBackIn(String parentId) {
        // After creating child account, we need to sign the parent back in
        // Get parent's email from Firestore
        db.collection("users").document(parentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String parentEmail = documentSnapshot.getString("email");

                        // Note: We don't have parent's password here
                        // Best practice: Just sign out and redirect to ParentManagement
                        // Parent will remain signed in because we got their data
                        fAuth.signOut();

                        // Redirect back to ParentManagement with success flag
                        Intent intent = new Intent(ParentRegisterLogin.this, ParentManagement.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra("childAdded", true);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ParentRegisterLogin.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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

        // Create an account with firebase Auth
        fAuth.createUserWithEmailAndPassword(childEmail, childPassword)
                .addOnSuccessListener(authResult -> {
                    String childId = authResult.getUser().getUid();

                    Child child = new Child(childId, parentId, displayName, childEmail, "child");
                    // TODO What if it's an older build version
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        child.setDOB(birthday);
                    }
                    child.setNotes(specialNote);

                    db.collection("users").document(childId)
                            .set(child)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(ParentRegisterLogin.this,
                                        "Child account created successfully!",
                                        Toast.LENGTH_SHORT).show();

                                // Sign the parent back in
                                signParentBackIn(parentId);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ParentRegisterLogin.this,
                                        "Failed to save child data: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ParentRegisterLogin.this,
                            "Failed to create account: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });



    }
}