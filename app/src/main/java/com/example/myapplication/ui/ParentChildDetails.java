package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class ParentChildDetails extends AppCompatActivity {

    private TextView tvLegalName;
    private TextView tvUsername;
    private TextView tvUserEmail;
    private TextView tvChildPassword;
    private TextView tvDetailBirthday;
    private TextView tvDetailSpecialNote;
    private ImageButton btnBack;
    private ImageButton btnEdit;
    private EditText editPB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_carddetails);

        // Initialize views
        tvLegalName = findViewById(R.id.tvLegalName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvDetailBirthday = findViewById(R.id.tvDetailBirthday);
        tvDetailSpecialNote = findViewById(R.id.tvDetailSpecialNote);
        tvUsername = findViewById(R.id.tvUsername);
        tvChildPassword = findViewById(R.id.tvChildPassword);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        editPB = findViewById(R.id.editPB);


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

        if (tvUsername != null) {
            tvUsername.setText(tvUserEmail != null ? childEmail : "Not set");
        }

        if (tvDetailBirthday != null) {
            tvDetailBirthday.setText(childBirthday != null ? childBirthday : "Not provided");
        }


        if (tvDetailSpecialNote != null) {
            tvDetailSpecialNote.setText(childNote != null && !childNote.isEmpty() ? childNote : "None");
        }

        if (tvUsername != null) {
            tvUsername.setText(childId != null ? childId : "Not set");
        }

        if (tvChildPassword != null) {
            // Display masked password or a placeholder
            if (childPassword != null && !childPassword.isEmpty()) {
                tvChildPassword.setText(maskPassword(childPassword));
            } else {
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

        editPB.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String newPB = editPB.getText().toString();
                if (!newPB.isEmpty() && childId != null) {
                    // Query by childId and update the personal best
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("users").document(childId)
                                    .update("personalBest", newPB)
                                    .addOnSuccessListener(aVoid ->
                                        Toast.makeText(this, "New PB set to " + newPB,
                                                Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to update PB",
                                                Toast.LENGTH_SHORT).show());
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