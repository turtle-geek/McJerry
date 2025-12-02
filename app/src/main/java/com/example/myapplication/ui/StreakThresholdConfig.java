package com.example.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.models.Badges;
import com.example.myapplication.models.Child;
import com.google.firebase.firestore.FirebaseFirestore;

public class StreakThresholdConfig extends AppCompatActivity {

    private ImageButton btnBack;
    private Button btnSave;
    private EditText etControllerThreshold, etTechniqueThreshold, etRescueThreshold;

    private FirebaseFirestore db;
    private String childId;
    private Child child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_threshold_config);

        // Initialize UI components
        btnBack = findViewById(R.id.btnBack);
        btnSave = findViewById(R.id.btnSave);
        etControllerThreshold = findViewById(R.id.etControllerThreshold);
        etTechniqueThreshold = findViewById(R.id.etTechniqueThreshold);
        etRescueThreshold = findViewById(R.id.etRescueThreshold);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Get Intent Data
        if (getIntent().hasExtra("childId")) {
            childId = getIntent().getStringExtra("childId");
            loadChildData();
        } else {
            Toast.makeText(this, "Error: No Child ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Listeners
        btnBack.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveConfiguration());
    }

    private void loadChildData() {
        db.collection("users").document(childId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        child = snapshot.toObject(Child.class);
                        if (child != null && child.getBadges() != null) {
                            populateFields(child.getBadges());
                        }
                    } else {
                        Toast.makeText(this, "Child not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void populateFields(Badges badges) {
        etControllerThreshold.setText(String.valueOf(badges.getControllerStreakThreshold()));
        etTechniqueThreshold.setText(String.valueOf(badges.getTechniqueStreakThreshold()));
        etRescueThreshold.setText(String.valueOf(badges.getRescueCountThreshold()));
    }

    private void saveConfiguration() {
        if (child == null) return;

        String controllerStr = etControllerThreshold.getText().toString();
        String techniqueStr = etTechniqueThreshold.getText().toString();
        String rescueStr = etRescueThreshold.getText().toString();

        if (TextUtils.isEmpty(controllerStr) || TextUtils.isEmpty(techniqueStr) || TextUtils.isEmpty(rescueStr)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int controllerVal = Integer.parseInt(controllerStr);
        int techniqueVal = Integer.parseInt(techniqueStr);
        int rescueVal = Integer.parseInt(rescueStr);

        // Update local object
        Badges badges = child.getBadges();
        if (badges == null) badges = new Badges();

        badges.setControllerStreakThreshold(controllerVal);
        badges.setTechniqueStreakThreshold(techniqueVal);
        badges.setRescueCountThreshold(rescueVal);

        // Recalculate badges based on new thresholds
        badges.updateControllerBadge();
        badges.updateTechniqueBadge();
        badges.updateRescueBadge();

        // Save to Firestore
        db.collection("users").document(childId)
                .update("badges", badges)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Thresholds updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Notify calling activity
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error updating settings: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}