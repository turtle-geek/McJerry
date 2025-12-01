package com.example.myapplication.ui;

// Standard Android UI Imports
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Java Utility Imports
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;
import androidx.annotation.NonNull;

// Project Imports
import com.example.myapplication.R;
import com.example.myapplication.models.DailyCheckIn;

// Firebase/Firestore Imports
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

public class DailyCheckInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the UI (R.layout.activity_daily_check_in must exist)
        setContentView(R.layout.activity_daily_check_in);

        // Call the function to set the date
        setDynamicDate();

        // Runs handleSubmitCheckIn() when the button is clicked
        Button submitButton = findViewById(R.id.btnSubmitCheckIn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmitCheckIn();
            }
        });
    }

    /**
     * Finds the header TextView and updates it with the current date.
     */
    private void setDynamicDate() {
        try {
            // Find the TextView element using its ID from the XML
            TextView dateTextView = findViewById(R.id.headerDateText);

            // Get the current date and format it (e.g., "November 24, 2025")
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date());

            // Set the dynamic text
            dateTextView.setText("Please complete this entry for " + formattedDate + ".");

        } catch (Exception e) {
            // This will catch the error if R.id.headerDateText is missing,
            // preventing the app from crashing.
            Log.e("DailyCheckInActivity", "Error setting dynamic date.", e);
        }
    }

    /**
     * Saves the completed DailyCheckIn object to the Firestore database.
     */
    private void saveCheckInToFirestore(DailyCheckIn checkIn) {
        // Initialize Firestore instance
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("daily_checkins")
                .add(checkIn)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(DailyCheckInActivity.this, "Daily Check-in Saved!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DailyCheckInActivity", "Error saving check-in to Firestore", e);
                        Toast.makeText(DailyCheckInActivity.this, "Error: Could not save entry.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean isFormValid(List<String> collectedTriggers) {
        // Check all required RadioGroups for selection (-1 means nothing is selected)
        if (((RadioGroup) findViewById(R.id.radioGroupAuthor)).getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select the Entry Author.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (((RadioGroup) findViewById(R.id.radioGroupNightWaking)).getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Night Waking status.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (((RadioGroup) findViewById(R.id.radioGroupActivityLimits)).getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Activity Limits.", Toast.LENGTH_LONG).show();
            return false;
        }

        if (((RadioGroup) findViewById(R.id.radioGroupCoughWheeze)).getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select Cough/Wheeze Severity.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void handleSubmitCheckIn() {
        // 1. Collect Triggers (Checkboxes)
        List<String> selectedTriggers = new ArrayList<>();
        if (((CheckBox) findViewById(R.id.cbExercise)).isChecked())
            selectedTriggers.add("Exercise");
        if (((CheckBox) findViewById(R.id.cbColdAir)).isChecked())
            selectedTriggers.add("Cold Air");
        if (((CheckBox) findViewById(R.id.cbDustPets)).isChecked())
            selectedTriggers.add("Dust/Pets");
        if (((CheckBox) findViewById(R.id.cbSmoke)).isChecked())
            selectedTriggers.add("Smoke");
        if (((CheckBox) findViewById(R.id.cbIllness)).isChecked())
            selectedTriggers.add("Illness");
        if (((CheckBox) findViewById(R.id.cbStrongOdors)).isChecked())
            selectedTriggers.add("Strong Odors/Perfumes");

        // 2. Validate Form
        if(!isFormValid(selectedTriggers)){
            return; // stop execution
        }

        long timestamp = System.currentTimeMillis();

        // 3. Get Entry Author
        RadioGroup authorGroup = findViewById(R.id.radioGroupAuthor);
        int selectedAuthorId = authorGroup.getCheckedRadioButtonId();
        String entryAuthor = (selectedAuthorId == R.id.rbChildEntered) ? "Child" : "Parent";

        // 4. Get Night Waking Status
        RadioGroup wakingGroup = findViewById(R.id.radioGroupNightWaking);
        int selectedWakingId = wakingGroup.getCheckedRadioButtonId();
        boolean nightWaking = selectedWakingId != R.id.rbWakingNo;

        // 5. Get Activity Limits (Mapping ID to 0-2 Scale)
        RadioGroup limitsGroup = findViewById(R.id.radioGroupActivityLimits);
        int selectedLimitId = limitsGroup.getCheckedRadioButtonId();
        int activityLimits;

        if (selectedLimitId == R.id.rbLimit0)
            activityLimits = 0; // No Limits
        else if (selectedLimitId == R.id.rbLimit1)
            activityLimits = 1; // A Bit Limited
        else if (selectedLimitId == R.id.rbLimit2)
            activityLimits = 2; // Very Limited
        else
            activityLimits = 0; // Fallback - should not happen due to validation

        // 6. Get Cough/Wheeze (Mapping ID to 0-4 Scale)
        RadioGroup coughGroup = findViewById(R.id.radioGroupCoughWheeze);
        int selectedCoughId = coughGroup.getCheckedRadioButtonId();
        int coughWheeze = 0; // Default to 0

        if (selectedCoughId == R.id.rbCough1)
            coughWheeze = 0; // None
        else if (selectedCoughId == R.id.rbCough2)
            coughWheeze = 1;
        else if (selectedCoughId == R.id.rbCough3)
            coughWheeze = 2;
        else if (selectedCoughId == R.id.rbCough4)
            coughWheeze = 3;
        else if (selectedCoughId == R.id.rbCough5)
            coughWheeze = 4; // Severe

        // 7. Create DailyCheckIn object
        DailyCheckIn newEntry = new DailyCheckIn(
                timestamp,
                entryAuthor,
                nightWaking,
                activityLimits,
                coughWheeze,
                selectedTriggers
        );

        // 8. Persist data
        saveCheckInToFirestore(newEntry);
    }
}