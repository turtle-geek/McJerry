package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

public class ProviderChildDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private String childId;
    private String childName;
    private String parentId;
    private String email;
    private String dob;

    // UI Components
    private TextView tvDetailName;
    private TextView tvDetailBirthday;
    private TextView tvAge;
    private TextView tvDetailSpecialNote;
    private TextView tvParentInfo;
    private TextView tvEmailInfo;
    private ImageButton btnBack;
    private Button btnViewMedicalRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_management);

        db = FirebaseFirestore.getInstance();

        // Get data from intent
        childId = getIntent().getStringExtra("childId");
        childName = getIntent().getStringExtra("childName");
        parentId = getIntent().getStringExtra("parentId");
        email = getIntent().getStringExtra("email");
        dob = getIntent().getStringExtra("dob");

        // Initialize views
        initializeViews();

        // Setup buttons
        setupButtons();

        // Display basic data
        displayBasicData();

        // Load additional details from Firestore
        loadAdditionalDetails();
    }

    private void initializeViews() {
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailBirthday = findViewById(R.id.tvDetailBirthday);
        tvAge = findViewById(R.id.tvAge);
        tvDetailSpecialNote = findViewById(R.id.tvDetailSpecialNote);
        btnBack = findViewById(R.id.btnBack);
        btnViewMedicalRecords = findViewById(R.id.btnViewMedicalRecords);
    }

    private void setupButtons() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Medical records button
        btnViewMedicalRecords.setOnClickListener(v -> {
            // TODO: Implement medical records viewing
            Toast.makeText(this, "Medical records feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void displayBasicData() {
        // Set name
        tvDetailName.setText(childName);

        // Set birthday and calculate age
        if (dob != null && !dob.isEmpty()) {
            tvDetailBirthday.setText(dob);
            calculateAndDisplayAge(dob);
        } else {
            tvDetailBirthday.setText("Not provided");
            tvAge.setText("Age: Unknown");
        }

        // Set parent info
        if (parentId != null && !parentId.isEmpty()) {
            tvParentInfo.setText("Parent ID: " + parentId);
        } else {
            tvParentInfo.setText("Parent ID: Not specified");
        }

        // Set email
        if (email != null && !email.isEmpty()) {
            tvEmailInfo.setText("Email: " + email);
        } else {
            tvEmailInfo.setText("Email: Not provided");
        }
    }

    private void calculateAndDisplayAge(String dateOfBirth) {
        try {
            // Try parsing with different date formats
            LocalDate birthDate = null;

            // Common date formats
            String[] formats = {
                    "yyyy-MM-dd",
                    "MM/dd/yyyy",
                    "dd/MM/yyyy",
                    "yyyy/MM/dd"
            };

            for (String format : formats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    birthDate = LocalDate.parse(dateOfBirth, formatter);
                    break;
                } catch (Exception e) {
                    // Try next format
                }
            }

            if (birthDate != null) {
                LocalDate today = LocalDate.now();
                Period period = Period.between(birthDate, today);
                int years = period.getYears();
                int months = period.getMonths();

                if (years > 0) {
                    tvAge.setText("Age: " + years + " years old");
                } else if (months > 0) {
                    tvAge.setText("Age: " + months + " months old");
                } else {
                    tvAge.setText("Age: Less than 1 month old");
                }
            } else {
                tvAge.setText("Age: Unable to calculate");
            }
        } catch (Exception e) {
            tvAge.setText("Age: Unable to calculate");
        }
    }

    private void loadAdditionalDetails() {
        db.collection("users").document(childId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get special notes
                        String notes = documentSnapshot.getString("notes");
                        if (notes != null && !notes.isEmpty()) {
                            tvDetailSpecialNote.setText(notes);
                        } else {
                            tvDetailSpecialNote.setText("No special notes recorded");
                        }

                        // You can add more fields here as needed
                        // For example: allergies, medications, emergency contacts, etc.
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load additional details: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}