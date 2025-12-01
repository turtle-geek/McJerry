package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

/**
 * Child Child Details Activity
 * Shows child's own details in READ-ONLY mode
 * - Uses activity_child_carddetails.xml layout
 * - Same fields as parent sees
 * - NO edit button (hidden)
 * - Has medical records button
 */
public class ChildChildDetails extends AppCompatActivity {

    private TextView tvLegalName;
    private TextView tvUserID;
    private TextView tvUserEmail;
    private TextView tvChildPassword;
    private TextView tvDetailBirthday;
    private TextView tvDetailSpecialNote;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Use CHILD-SPECIFIC layout
        setContentView(R.layout.activity_child_carddetails);

        // Initialize views
        initializeViews();

        // Get data from intent
        String childId = getIntent().getStringExtra("childId");
        String childName = getIntent().getStringExtra("childName");
        String childUserId = getIntent().getStringExtra("childUserId");
        String childEmail = getIntent().getStringExtra("childEmail");
        String childBirthday = getIntent().getStringExtra("childBirthday");
        String childNote = getIntent().getStringExtra("childNote");

        // Display data
        displayChildData(childName, childUserId, childEmail, childBirthday, childNote);

        // Setup buttons
        setupButtons();
    }

    private void initializeViews() {
        tvLegalName = findViewById(R.id.tvLegalName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvDetailBirthday = findViewById(R.id.tvDetailBirthday);
        tvDetailSpecialNote = findViewById(R.id.tvDetailSpecialNote);
        tvUserID = findViewById(R.id.tvUserID);
        tvChildPassword = findViewById(R.id.tvChildPassword);
        btnBack = findViewById(R.id.btnBack);
    }

    private void displayChildData(String childName, String childUserId, String childEmail,
                                  String childBirthday, String childNote) {
        // Display name
        if (tvLegalName != null) {
            tvLegalName.setText(childName != null ? childName : "Unknown");
        }

        // Display user ID
        if (tvUserID != null) {
            tvUserID.setText(childUserId != null ? childUserId : "Not set");
        }

        // Display email
        if (tvUserEmail != null) {
            tvUserEmail.setText(childEmail != null ? childEmail : "Not set");
        }

        // Display birthday
        if (tvDetailBirthday != null) {
            tvDetailBirthday.setText(childBirthday != null ? childBirthday : "Not provided");
        }

        // Display special note
        if (tvDetailSpecialNote != null) {
            tvDetailSpecialNote.setText(childNote != null && !childNote.isEmpty() ? childNote : "None");
        }

        // Hide password for child (security)
        if (tvChildPassword != null) {
            tvChildPassword.setText("••••••••");
        }
    }

    private void setupButtons() {
        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }


    }
}