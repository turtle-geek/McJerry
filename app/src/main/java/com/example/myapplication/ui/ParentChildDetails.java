package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

    public class ParentChildDetails extends AppCompatActivity {

        private TextView tvLegalName;
        private TextView tvUserID;
        private TextView tvUserEmail;
        private TextView tvChildPassword;
        private TextView tvDetailBirthday;
        private TextView tvDetailSpecialNote;
        private ImageButton btnBack;
        private ImageButton btnEdit;
        private EditText editPB;

        private Button btnViewMedicineInventory;
        private Button btnViewMedicalRecords;
        private Button btnViewProgressOverview;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_parent_carddetails);

            // Initialize views
            tvLegalName = findViewById(R.id.tvLegalName);
            tvUserEmail = findViewById(R.id.tvUserEmail);
            tvDetailBirthday = findViewById(R.id.tvDetailBirthday);
            tvDetailSpecialNote = findViewById(R.id.tvDetailSpecialNote);
            tvUserID = findViewById(R.id.tvUsername);
            tvChildPassword = findViewById(R.id.tvChildPassword);
            btnBack = findViewById(R.id.btnBack);
            btnEdit = findViewById(R.id.btnEdit);
            editPB = findViewById(R.id.editPB);

            btnViewMedicineInventory = findViewById(R.id.btnViewMedicineInventory);
            btnViewMedicalRecords = findViewById(R.id.btnViewMedicalRecords);
            btnViewProgressOverview = findViewById(R.id.btnViewProgressOverview);

            // Get data from intent
            String childName = getIntent().getStringExtra("childName");
            String childId = getIntent().getStringExtra("childId");
            String childEmail = getIntent().getStringExtra("childEmail");
            String childBirthday = getIntent().getStringExtra("childBirthday");
            String childNote = getIntent().getStringExtra("childNote");
            String childPassword = getIntent().getStringExtra("childPassword");

            // Display data
            if (tvLegalName != null) {
                tvLegalName.setText(childName != null ? childName : "Unknown");
            }

            // FIX: Use correct variable - tvUserEmail instead of tvUserID
            if (tvUserEmail != null) {
                tvUserEmail.setText(childEmail != null ? childEmail : "Not set");
            }

            if (tvDetailBirthday != null) {
                tvDetailBirthday.setText(childBirthday != null ? childBirthday : "Not provided");
            }

            if (tvDetailSpecialNote != null) {
                tvDetailSpecialNote.setText(childNote != null && !childNote.isEmpty() ? childNote : "None");
            }

            if (tvUserID != null) {
                tvUserID.setText(childId != null ? childId : "Not set");
            }

            if (tvChildPassword != null) {
                // Display masked password or a placeholder
                if (childPassword != null && !childPassword.isEmpty()) {
                    tvChildPassword.setText(maskPassword(childPassword));
                } else {
                    // FIX: Use proper bullet character
                    tvChildPassword.setText("••••••••");
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

            // Medicine Inventory button
            btnViewMedicineInventory.setOnClickListener(v -> {
                Intent intent = new Intent(this, InventoryManagement.class);
                intent.putExtra("childId", childId);
                startActivityForResult(intent, 1);
            });

            // Medicine History button
            btnViewMedicalRecords.setOnClickListener(v -> {
                Intent intent = new Intent(this, InventoryLog.class);
                intent.putExtra("childId", childId);
                startActivityForResult(intent, 1);
            });

            // Progress Overview button
            btnViewProgressOverview.setOnClickListener(v -> {
                Intent intent = new Intent(this, StreakManagement.class);
                intent.putExtra("childId", childId);
                startActivityForResult(intent, 1);
            });

            // FIX: Single focus listener setup (removed duplicate)
            // FIX: Convert to integer and use correct field name
            editPB.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    String newPBText = editPB.getText().toString();
                    if (!newPBText.isEmpty() && childId != null) {
                        try {
                            // FIX: Convert to integer (PB should be a number, not string)
                            int newPB = Integer.parseInt(newPBText);

                            // Query by childId and update the personal best
                            // FIX: Use correct field name "PEF_PB" instead of "personalBest"
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("users").document(childId)
                                    .update("PEF_PB", newPB)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(this, "New PB set to " + newPB,
                                                    Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Failed to update PB: " + e.getMessage(),
                                                    Toast.LENGTH_SHORT).show());
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
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
    }