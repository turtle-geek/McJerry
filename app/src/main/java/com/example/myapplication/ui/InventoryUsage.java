package com.example.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.text.TextWatcher;
import android.text.Editable;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.R;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import com.example.myapplication.health.*;
        import com.example.myapplication.models.*;
        import com.google.firebase.firestore.FirebaseFirestore;
import android.util.Log;

public class InventoryUsage extends AppCompatActivity {

    private static final String TAG = "InventoryUsage";
    private TextInputEditText etDate, etTime, etDosage;
    private Button btnSaveMedicine;
    private ImageButton btnBack;
    private MedicineLabel label;
    private Child child;
    private FirebaseFirestore db;
    private String childId;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    private void loadChild() {
        db.collection("users").document(childId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        child = snapshot.toObject(Child.class);
                        Log.d(TAG, "Child loaded successfully");
                        // Re-validate after child loads
                        updateSaveButtonState();
                    } else {
                        Log.e(TAG, "Child document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load child", e);
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_usage);

        Log.d(TAG, "onCreate started");

        // --- Initialize views ---
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDosage = findViewById(R.id.etDosage);
        btnSaveMedicine = findViewById(R.id.btnSaveMedicine);
        btnBack = findViewById(R.id.btnBack);

        // Get label with null check
        String labelString = getIntent().getStringExtra("label");
        if (labelString == null) {
            Log.e(TAG, "No label provided in intent!");
            android.widget.Toast.makeText(this, "Error: No medicine type provided", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        label = MedicineLabel.valueOf(labelString);
        Log.d(TAG, "Medicine label: " + label);

        // Load child from firebase
        db = FirebaseFirestore.getInstance();
        childId = getIntent().getStringExtra("childId");
        if (childId == null) {
            Log.e(TAG, "No childId provided in intent!");
            android.widget.Toast.makeText(this, "Error: No child ID provided", android.widget.Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Loading child: " + childId);
        loadChild();

        // --- Disable save button initially ---
        btnSaveMedicine.setEnabled(false);
        btnSaveMedicine.setAlpha(0.5f);

        // --- Add validation watcher ---
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable s) {
                updateSaveButtonState();
            }
        };

        etDate.addTextChangedListener(watcher);
        etTime.addTextChangedListener(watcher);
        etDosage.addTextChangedListener(watcher);

        // --- Back button ---
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // --- Date/time pickers ---
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        // --- Save button ---
        btnSaveMedicine.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");

            // Add null check for child
            if (child == null) {
                Log.e(TAG, "Child not loaded yet!");
                android.widget.Toast.makeText(this, "Please wait, loading data...", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // Parse date and time
                LocalDate date = LocalDate.parse(etDate.getText().toString(), dateFormatter);
                LocalTime time = LocalTime.parse(etTime.getText().toString(), timeFormatter);
                LocalDateTime timestamp = LocalDateTime.of(date, time);

                double amount = Double.parseDouble(etDosage.getText().toString());
                Log.d(TAG, "Parsed data - Date: " + date + ", Time: " + time + ", Amount: " + amount);

                // Determine technique quality
                TechniqueQuality quality =
                        (label == MedicineLabel.CONTROLLER) ? TechniqueQuality.HIGH : TechniqueQuality.NA;

                // Use medicine from inventory
                boolean success = child.getInventory().useMedicine(label, amount, timestamp.toString());

                if (success) {
                    Log.d(TAG, "Medicine usage successful, updating Firebase...");

                    // Update streak count
                    child.getStreakCount().countStreaks();

                    // Get the medicine item for passing to next activities
                    InventoryItem medicineItem = child.getInventory().getMedicine(label);

                    // Get proper medicine name
                    String medicineName = getMedicineName(label);

                    Log.d(TAG, "Medicine details - Label: " + label + ", Name: " + medicineName + ", Dosage: " + amount);

                    // Save to Firebase
                    db.collection("users").document(childId)
                            .set(child)
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Firebase save successful");

                                // Navigate to ParentTutorial with proper data
                                Intent tutorialIntent = new Intent(InventoryUsage.this, ParentTutorial.class);
                                tutorialIntent.putExtra("medicineLabel", label.toString());
                                tutorialIntent.putExtra("medicineName", medicineName);
                                tutorialIntent.putExtra("dosage", amount);

                                Log.d(TAG, "Starting ParentTutorial activity...");
                                startActivity(tutorialIntent);

                                setResult(RESULT_OK);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Firebase save failed", e);
                                android.widget.Toast.makeText(this,
                                        "Failed to save usage: " + e.getMessage(),
                                        android.widget.Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.e(TAG, "Medicine usage failed - insufficient amount or invalid data");
                    android.widget.Toast.makeText(this,
                            "Failed to use medicine. Check dosage amount.",
                            android.widget.Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error during save", e);
                android.widget.Toast.makeText(this,
                        "Error: " + e.getMessage(),
                        android.widget.Toast.LENGTH_LONG).show();
            }
        });

        Log.d(TAG, "onCreate completed");
    }

    // Helper method to get user-friendly medicine name
    private String getMedicineName(MedicineLabel label) {
        switch (label) {
            case CONTROLLER:
                return "Controller Inhaler";
            case RESCUE:
                return "Rescue Inhaler";
            default:
                return label.toString();
        }
    }

    // --- Validation logic ---

    private void updateSaveButtonState() {
        String dateText = etDate.getText().toString();
        String timeText = etTime.getText().toString();
        String dosageText = etDosage.getText().toString();

        boolean dateValid = isValidDate(dateText);
        boolean timeValid = isValidTime(timeText);
        boolean dosageValid = isValidDosage(dosageText);

        // Debug logging
        Log.d(TAG, "Validation check:");
        Log.d(TAG, "  Date: '" + dateText + "' valid=" + dateValid);
        Log.d(TAG, "  Time: '" + timeText + "' valid=" + timeValid);
        Log.d(TAG, "  Dosage: '" + dosageText + "' valid=" + dosageValid);
        Log.d(TAG, "  Child loaded: " + (child != null));

        boolean isValid = dateValid && timeValid && dosageValid;

        btnSaveMedicine.setEnabled(isValid);
        btnSaveMedicine.setAlpha(isValid ? 1.0f : 0.5f);

        Log.d(TAG, "Save button enabled: " + isValid);
    }

    private boolean isValidDate(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            LocalDate date = LocalDate.parse(text, dateFormatter);
            return !date.isAfter(LocalDate.now());  // cannot be future
        } catch (Exception e) {
            Log.e(TAG, "Date validation failed", e);
            return false;
        }
    }

    private boolean isValidTime(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            // Use formatter that matches what the picker outputs (HH:mm)
            LocalTime.parse(text, timeFormatter);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Time validation failed for: '" + text + "'", e);
            return false;
        }
    }

    private boolean isValidDosage(String text) {
        if (TextUtils.isEmpty(text)) return false;

        // Allow basic number validation if child isn't loaded yet
        if (child == null) {
            try {
                double dosage = Double.parseDouble(text);
                return dosage > 0;
            } catch (Exception e) {
                return false;
            }
        }

        try {
            double dosage = Double.parseDouble(text);
            InventoryItem item = child.getInventory().getMedicine(label);

            // If inventory item not available yet, just check positive number
            if (item == null) {
                return dosage > 0;
            }

            return dosage > 0 && dosage <= item.getAmount() + 1e-9;
        } catch (Exception e) {
            Log.e(TAG, "Dosage validation failed", e);
            return false;
        }
    }

    // --- Date and Time Pickers ---

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    etDate.setText(String.format("%02d/%02d/%d", month + 1, day, year));
                    Log.d(TAG, "Date selected: " + etDate.getText().toString());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        TimePickerDialog dialog = new TimePickerDialog(
                this,
                (view, hour, min) -> {
                    etTime.setText(String.format("%02d:%02d", hour, min));
                    Log.d(TAG, "Time selected: " + etTime.getText().toString());
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }
}