package com.example.myapplication.ui;

// Standard Android UI Imports
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
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
import com.example.myapplication.sosButtonResponse;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DailyCheckInActivity extends AppCompatActivity {

    private String username;
    public static final String EXTRA_USERNAME = "CHILD_USERNAME";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkAndSetupUser();
    }

    /**
     * Checks the current logged-in user, fetches their username, and initializes the views.
     */
    private void checkAndSetupUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: User must be logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = user.getUid();

        // Fetch the user's document to get the username field
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        // 1. Retrieve the full string from the "emailUsername" field
                        String fullEmailUsername = documentSnapshot.getString("emailUsername");

                        if (fullEmailUsername != null && !fullEmailUsername.isEmpty()) {

                            // 2. Extract the identifier (the part before the '@')
                            String cleanUsername;
                            int atIndex = fullEmailUsername.indexOf('@');

                            if (atIndex > 0) {
                                cleanUsername = fullEmailUsername.substring(0, atIndex);
                            } else {
                                // Fallback: use the whole string if no @ is found
                                cleanUsername = fullEmailUsername;
                            }

                            if (!cleanUsername.isEmpty()) {
                                this.username = cleanUsername; // Set the clean identifier
                                setupViews(); // Proceed with activity setup
                            } else {
                                Toast.makeText(this, "Error: Username part not found in profile.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(this, "Error: User data is incomplete.", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(this, "Error: User profile not found.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DailyCheckInActivity", "Failed to fetch user data: ", e);
                    Toast.makeText(this, "Error fetching user data.", Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    /**
     * Sets up all the UI elements once the username is successfully retrieved.
     */
    private void setupViews() {
        setContentView(R.layout.activity_daily_check_in);

        setDynamicDate();
        setupBackButton();

        // Set up SOS Button
        ImageButton sosButton = findViewById(R.id.sosButton);
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(id)
                .get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String role = documentSnapshot.getString("role");
                                if ("parents".equals(role)) {
                                    sosButton.setVisibility(View.GONE);
                                    return;
                                } else if ("child".equals(role)) {
                                    sosButton.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                        });
        sosButton.setOnClickListener(v -> {
            sosButtonResponse action = new sosButtonResponse();
            action.response(id, this);

        });

        // Runs handleSubmitCheckIn() when the button is clicked
        Button submitButton = findViewById(R.id.btnSubmitCheckIn);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSubmitCheckIn();
            }
        });
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                finish();
            });
        }
    }

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
            Log.e("DailyCheckInActivity", "Error setting dynamic date.", e);
        }
    }

    private void saveCheckInToFirestore(DailyCheckIn checkIn) {
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

        if(!isFormValid(selectedTriggers)){
            return;
        }

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Error: Child Username is missing, cannot save entry.", Toast.LENGTH_LONG).show();
            Log.e("DailyCheckInActivity", "Username is null or empty during handleSubmitCheckIn.");
            return;
        }

        long timestamp = System.currentTimeMillis();

        RadioGroup authorGroup = findViewById(R.id.radioGroupAuthor);
        int selectedAuthorId = authorGroup.getCheckedRadioButtonId();
        String entryAuthor = (selectedAuthorId == R.id.rbChildEntered) ? "Child" : "Parent";

        RadioGroup wakingGroup = findViewById(R.id.radioGroupNightWaking);
        int selectedWakingId = wakingGroup.getCheckedRadioButtonId();
        boolean nightWaking = selectedWakingId != R.id.rbWakingNo;

        RadioGroup limitsGroup = findViewById(R.id.radioGroupActivityLimits);
        int selectedLimitId = limitsGroup.getCheckedRadioButtonId();
        int activityLimits;

        if (selectedLimitId == R.id.rbLimit0)
            activityLimits = 0;
        else if (selectedLimitId == R.id.rbLimit1)
            activityLimits = 1;
        else if (selectedLimitId == R.id.rbLimit2)
            activityLimits = 2;
        else
            activityLimits = 0;

        RadioGroup coughGroup = findViewById(R.id.radioGroupCoughWheeze);
        int selectedCoughId = coughGroup.getCheckedRadioButtonId();
        int coughWheeze = 0;

        if (selectedCoughId == R.id.rbCough1)
            coughWheeze = 0;
        else if (selectedCoughId == R.id.rbCough2)
            coughWheeze = 1;
        else if (selectedCoughId == R.id.rbCough3)
            coughWheeze = 2;
        else if (selectedCoughId == R.id.rbCough4)
            coughWheeze = 3;
        else if (selectedCoughId == R.id.rbCough5)
            coughWheeze = 4;

        DailyCheckIn newEntry = new DailyCheckIn(
                username,
                timestamp,
                entryAuthor,
                nightWaking,
                activityLimits,
                coughWheeze,
                selectedTriggers
        );

        saveCheckInToFirestore(newEntry);
    }
}