package com.example.myapplication.ui.ChildUI.TriageAndResponse;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast; // Added import for Toast and Log
import android.util.Log;
import android.view.View; // Added import for View components like Switch/Slider if used

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.util.List;

public class TriageActivity extends AppCompatActivity {

    private static final String TAG = "TriageActivity";
    private ChipGroup chipGroup;
    private Button nextButton;
    private EditText peakFlowInput;

    private HealthProfile hp;
    private Child currentChild;
    private int peakFlowValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_triage);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String id = getIntent().getStringExtra("id");
        if (id == null) {
            Toast.makeText(this, "User ID missing, cannot load profile.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        db.collection("users").document(id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) return;

                    currentChild = documentSnapshot.toObject(Child.class);
                    if (currentChild == null) return;

                    hp = currentChild.getHealthProfile();
                    Log.d(TAG, "Child profile loaded successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching child data: ", e);
                    Toast.makeText(this, "Failed to load user profile.", Toast.LENGTH_LONG).show();
                });

        bindViews();
        nextButton.setOnClickListener(v -> redFlagCheck());
    }

    void bindViews(){
        chipGroup = findViewById(R.id.chipGroup);
        nextButton = findViewById(R.id.nextButton);
        // Ensure the ID R.id.TriagePEF exists as an EditText in your XML
        peakFlowInput = findViewById(R.id.TriagePEF);

        // Optional: Check if the back button exists and bind a listener for navigation
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    void redFlagCheck() {
        List<Integer> selected = chipGroup.getCheckedChipIds();

        // 1. Define CRITICAL Red Flags (Based on the assumed sequential chip IDs 1 and 2: Cyanosis/Retractions)
        boolean hasCriticalRedFlags = selected.contains(R.id.chip1) ||
                selected.contains(R.id.chip2);

        // 2. Define Other Red Flags (The remaining chips 3 through 9)
        boolean hasOtherRedFlags = selected.contains(R.id.chip3) ||
                selected.contains(R.id.chip4) ||
                selected.contains(R.id.chip5) ||
                selected.contains(R.id.chip6) ||
                selected.contains(R.id.chip7) ||
                selected.contains(R.id.chip8) ||
                selected.contains(R.id.chip9);

        // 3. Check Peak Flow Status
        String peakFlowZone = processPEF();
        boolean hasRedZonePEF = "red".equals(peakFlowZone);
        boolean hasYellowZonePEF = "yellow".equals(peakFlowZone);

        Intent intent;

        if (hasCriticalRedFlags || hasRedZonePEF) {
            // CRITICAL: Redirect to the Critical Emergency Page
            intent = new Intent(this, TriageCriticalActivity.class);
            Toast.makeText(this, "CRITICAL: Redirecting to Emergency Response.", Toast.LENGTH_LONG).show();
        } else if (hasOtherRedFlags || hasYellowZonePEF) {
            // NON-CRITICAL: Redirect to the Non-Critical Emergency Page
            intent = new Intent(this, TriageNonCriticalActivity.class);
            Toast.makeText(this, "Non-Critical: Redirecting for further action.", Toast.LENGTH_LONG).show();
        } else {
            // No selected chips and Green PEF (if entered) -> Default to Non-Critical as a safe measure
            intent = new Intent(this, TriageNonCriticalActivity.class);
            Toast.makeText(this, "No immediate red flags, following action plan.", Toast.LENGTH_LONG).show();
        }

        // Pass the user ID to the next Activity
        String userId = getIntent().getStringExtra("id");
        if (userId != null) {
            intent.putExtra("id", userId);
        }

        startActivity(intent);
        finish();
    }

    String processPEF() {
        // Handle case where peakFlowInput is null (if R.id.TriagePEF is missing in XML)
        if (peakFlowInput == null) {
            Log.w(TAG, "Peak Flow Input (R.id.TriagePEF) not found in layout.");
            return "green";
        }

        final String text = peakFlowInput.getText().toString().trim();

        if (text.isEmpty()) {
            return "green";
        }

        try {
            peakFlowValue = Integer.parseInt(text);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime submitTime = LocalDateTime.now();
                PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);

                // If user data is loaded, use personalized zones.
                if (hp != null && currentChild != null) {
                    hp.addPEFToLog(pef);
                    // Optional: Update Firestore here if necessary
                    return pef.computeZone(currentChild);
                } else {
                    // Fallback zone calculation (using 400 as PB for safety)
                    final int FALLBACK_PB = 400;
                    if (peakFlowValue >= (int)(0.8 * FALLBACK_PB)) {
                        return "green";
                    } else if (peakFlowValue >= (int)(0.5 * FALLBACK_PB)) {
                        return "yellow";
                    } else {
                        return "red";
                    }
                }
            }
        } catch (NumberFormatException e) {
            Log.e(TAG, "Invalid Peak Flow number entered.", e);
            Toast.makeText(this, "Invalid Peak Flow reading entered.", Toast.LENGTH_SHORT).show();
            return "green";
        }

        return "green";
    }
}