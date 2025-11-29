package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class ParentChildDetails extends AppCompatActivity {

    private TextView tvDetailName;
    private TextView tvDetailBirthday;
    private TextView tvAge;
    private TextView tvDetailSpecialNote;
    private TextView tvUserId;
    private TextView tvPassword;
    private ImageButton btnBack;
    private ImageButton btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_carddetails);

        // Initialize views
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailBirthday = findViewById(R.id.tvDetailBirthday);
        tvAge = findViewById(R.id.tvAge);
        tvDetailSpecialNote = findViewById(R.id.tvDetailSpecialNote);
        tvUserId = findViewById(R.id.tvIDName);
        tvPassword = findViewById(R.id.tvChildPassword);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);

        // Get data from intent
        String childId = getIntent().getStringExtra("childId");
        String childName = getIntent().getStringExtra("childName");
        String childUserId = getIntent().getStringExtra("childUserId");
        String childEmail = getIntent().getStringExtra("childEmail");
        String childBirthday = getIntent().getStringExtra("childBirthday");
        String childNote = getIntent().getStringExtra("childNote");
        String childPassword = getIntent().getStringExtra("childPassword");

        // Display data
        if (tvDetailName != null) {
            tvDetailName.setText(childName != null ? childName : "Unknown");
        }

        if (tvDetailBirthday != null) {
            tvDetailBirthday.setText(childBirthday != null ? childBirthday : "Not provided");
        }

        if (tvAge != null) {
            // Calculate age from birthday if needed
            // For now, just show a placeholder
            tvAge.setText("Age: Calculating...");
            calculateAge(childBirthday);
        }

        if (tvDetailSpecialNote != null) {
            tvDetailSpecialNote.setText(childNote != null && !childNote.isEmpty() ? childNote : "None");
        }

        if (tvUserId != null) {
            tvUserId.setText(childUserId != null ? childUserId : "Not set");
        }

        if (tvPassword != null) {
            // Display masked password or a placeholder
            if (childPassword != null && !childPassword.isEmpty()) {
                tvPassword.setText(maskPassword(childPassword));
            } else {
                tvPassword.setText("••••••••");
            }
        }

        // Back button
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Edit button
        if (btnEdit != null) {
            btnEdit.setOnClickListener(v -> {
                Toast.makeText(this, "Edit functionality coming soon", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to edit activity
            });
        }
    }

    private String maskPassword(String password) {
        // Create a masked version of the password (show first 2 chars, rest as dots)
        if (password.length() <= 2) {
            return "••••••";
        }
        StringBuilder masked = new StringBuilder();
        masked.append(password.substring(0, 2));
        for (int i = 2; i < password.length(); i++) {
            masked.append("•");
        }
        return masked.toString();
    }

    private void calculateAge(String birthday) {
        if (tvAge == null || birthday == null || birthday.isEmpty()) {
            if (tvAge != null) {
                tvAge.setText("Age: Unknown");
            }
            return;
        }

        try {
            // Parse birthday (format: MM/dd/yyyy)
            String[] parts = birthday.split("/");
            if (parts.length == 3) {
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);

                // Get current date
                java.util.Calendar today = java.util.Calendar.getInstance();
                int currentYear = today.get(java.util.Calendar.YEAR);
                int currentMonth = today.get(java.util.Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
                int currentDay = today.get(java.util.Calendar.DAY_OF_MONTH);

                // Calculate age
                int age = currentYear - year;

                // Adjust if birthday hasn't occurred this year
                if (currentMonth < month || (currentMonth == month && currentDay < day)) {
                    age--;
                }

                if (age >= 0) {
                    tvAge.setText("Age: " + age + " years old");
                } else {
                    tvAge.setText("Age: Invalid date");
                }
            } else {
                tvAge.setText("Age: Invalid format");
            }
        } catch (Exception e) {
            tvAge.setText("Age: Unable to calculate");
        }
    }
}