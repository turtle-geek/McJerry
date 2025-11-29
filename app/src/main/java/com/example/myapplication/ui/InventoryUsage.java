package com.example.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
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

public class InventoryUsage extends AppCompatActivity {

    private TextInputEditText etDate, etTime, etDosage;
    private Button btnSaveMedicine;
    private ImageButton btnBack;

    private MedicineLabel label;
    private Child child;

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_usage);

        // --- Initialize views ---
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etDosage = findViewById(R.id.etDosage);
        btnSaveMedicine = findViewById(R.id.btnSaveMedicine);
        btnBack = findViewById(R.id.btnBack);

        label = MedicineLabel.valueOf(getIntent().getStringExtra("label"));

        // TODO: Load child from Firebase here

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
        btnBack.setOnClickListener(v -> finish());

        // --- Date/time pickers ---
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());

        // --- Save button ---
        btnSaveMedicine.setOnClickListener(v -> {
            LocalDate date = LocalDate.parse(etDate.getText().toString(), dateFormatter);
            LocalTime time = LocalTime.parse(etTime.getText().toString());
            LocalDateTime timestamp = LocalDateTime.of(date, time);

            double amount = Double.parseDouble(etDosage.getText().toString());

            // TODO: go to technique session here
            // Placeholder
            TechniqueQuality quality =
                    (label == MedicineLabel.CONTROLLER) ? TechniqueQuality.HIGH : TechniqueQuality.NA;

            boolean success = child.getInventory().useMedicine(label, amount, timestamp);

            if (success) {
                child.getStreakCount().countStreaks();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    // --- Validation logic ---

    private void updateSaveButtonState() {
        boolean isValid =
                isValidDate(etDate.getText().toString()) &&
                        isValidTime(etTime.getText().toString()) &&
                        isValidDosage(etDosage.getText().toString());

        btnSaveMedicine.setEnabled(isValid);
        btnSaveMedicine.setAlpha(isValid ? 1.0f : 0.5f);
    }

    private boolean isValidDate(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            LocalDate date = LocalDate.parse(text, dateFormatter);
            return !date.isAfter(LocalDate.now());  // cannot be future
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidTime(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            LocalTime.parse(text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidDosage(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            double dosage = Double.parseDouble(text);
            InventoryItem item = child.getInventory().getMedicine(label);
            return dosage > 0 && dosage <= item.getAmount() + 1e-9;
        } catch (Exception e) {
            return false;
        }
    }

    // --- Date and Time Pickers ---

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) ->
                        etDate.setText(String.format("%02d/%02d/%d", month + 1, day, year)),
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
                (view, hour, min) ->
                        etTime.setText(String.format("%02d:%02d", hour, min)),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
        );
        dialog.show();
    }
}
