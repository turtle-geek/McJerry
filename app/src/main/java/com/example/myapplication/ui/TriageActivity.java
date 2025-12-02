package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.myapplication.R;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.List;

public class TriageActivity extends AppCompatActivity {

    ChipGroup chipGroup;
    static int chipCount = 0;
    Button nextButton;
    EditText peakFlowInput;

    HealthProfile hp;
    Child currentChild;
    int peakFlowValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_triage);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String id = getIntent().getStringExtra("id");
        assert id != null;
        db.collection("users").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    currentChild = documentSnapshot.toObject(Child.class);
                    if (currentChild == null) return;

                    hp = currentChild.getHealthProfile();
                });

        bindViews();
        nextButton.setOnClickListener(v -> redFlagCheck());

    }

    void bindViews(){
        chipGroup = findViewById(R.id.chipGroup);
        nextButton = findViewById(R.id.nextButton);
        peakFlowInput = findViewById(R.id.TriagePEF);
    }

    void redFlagCheck() {
        List<Integer> selected = chipGroup.getCheckedChipIds();

        // Check if any red flag symptoms are selected
        boolean hasRedFlagSymptoms = selected.contains(R.id.chip10) ||
                selected.contains(R.id.chip9) ||
                selected.contains(R.id.chip8) ||
                selected.contains(R.id.chip6) ||
                selected.contains(R.id.chip7);

        // Check peak flow if entered (it's optional)
        String peakFlowZone = processPEF();
        boolean hasRedZonePEF = "red".equals(peakFlowZone);

        Intent intent = new Intent(this, TriageDecisionCard.class);
        if (hasRedFlagSymptoms || hasRedZonePEF) {
            intent.putExtra("DECISION", "SOS");
        } else {
            intent.putExtra("DECISION", "NOT SOS");
        }
        startActivity(intent);
    }

    String processPEF() {
        // Peak flow is optional, so handle empty input gracefully
        final String text = peakFlowInput.getText().toString().trim();

        if (text.isEmpty()) {
            // No peak flow entered, return green (no issue from PEF)
            return "green";
        }

        try {
            peakFlowValue = Integer.parseInt(text);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime submitTime = LocalDateTime.now();
                PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);

                // Only add to log and compute zone if we have the child data
                if (hp != null && currentChild != null) {
                    hp.addPEFToLog(pef);
                    return pef.computeZone(currentChild);
                } else {
                    // If child data isn't loaded yet, use a basic zone calculation
                    // Assume a typical personal best of 400 for safety
                    if (peakFlowValue >= 320) { // 80% of 400
                        return "green";
                    } else if (peakFlowValue >= 200) { // 50% of 400
                        return "yellow";
                    } else {
                        return "red";
                    }
                }
            }
        } catch (NumberFormatException e) {
            // Invalid number entered, treat as no peak flow
            return "green";
        }

        return "green";
    }
}