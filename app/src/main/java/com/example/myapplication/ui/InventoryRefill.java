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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import com.example.myapplication.health.*;
import com.example.myapplication.models.*;

public class InventoryRefill extends AppCompatActivity {

    private TextInputEditText etPurchaseDate, etExpiryDate, etCapacity, etRemainingAmount;
    private Button btnSave;
    private ImageButton btnBack;

    private MedicineLabel label;
    private boolean isEdit;
    private Child child;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_create);

        // --- Initialize views ---
        etPurchaseDate = findViewById(R.id.etPurchaseDate);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etCapacity = findViewById(R.id.etCapacity);
        etRemainingAmount = findViewById(R.id.etRemainingAmount);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        label = MedicineLabel.valueOf(getIntent().getStringExtra("label"));
        isEdit = getIntent().getBooleanExtra("isEdit", false);

        // TODO: Load child from Firebase here

        // --- Disable save button initially ---
        btnSave.setEnabled(false);
        btnSave.setAlpha(0.5f);

        // --- Add validation watchers ---
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
            @Override public void afterTextChanged(Editable e) {
                updateSaveButtonState();
            }
        };

        etPurchaseDate.addTextChangedListener(watcher);
        etExpiryDate.addTextChangedListener(watcher);
        etCapacity.addTextChangedListener(watcher);
        etRemainingAmount.addTextChangedListener(watcher);

        // --- Date pickers ---
        etPurchaseDate.setOnClickListener(v -> showDatePicker(etPurchaseDate));
        etExpiryDate.setOnClickListener(v -> showDatePicker(etExpiryDate));

        // --- Pre-fill when editing ---
        if (isEdit) {
            InventoryItem item = child.getInventory().getMedicine(label);
            etPurchaseDate.setText(item.getPurchaseDate().format(dateFormatter));
            etExpiryDate.setText(item.getExpiryDate().format(dateFormatter));
            etCapacity.setText(String.valueOf(item.getCapacity()));
            etRemainingAmount.setText(String.valueOf(item.getAmount()));
        }

        // --- Back button ---
        btnBack.setOnClickListener(v -> finish());

        // --- Save button ---
        btnSave.setOnClickListener(v -> {
            LocalDate purchase = LocalDate.parse(etPurchaseDate.getText(), dateFormatter);
            LocalDate expiry = LocalDate.parse(etExpiryDate.getText(), dateFormatter);
            double capacity = Double.parseDouble(etCapacity.getText().toString());
            double amount = Double.parseDouble(etRemainingAmount.getText().toString());

            InventoryItem newItem = new InventoryItem(amount, capacity, purchase, expiry);
            child.getInventory().setMedicine(label, newItem);

            setResult(RESULT_OK);
            finish();
        });
    }

    // --- Validation logic ---

    private void updateSaveButtonState() {
        boolean valid =
                isValidPurchaseDate(etPurchaseDate.getText().toString()) &&
                        isValidExpiryDate(
                                etPurchaseDate.getText().toString(),
                                etExpiryDate.getText().toString()
                        ) &&
                        isValidCapacity(etCapacity.getText().toString()) &&
                        isValidRemainingAmount(
                                etCapacity.getText().toString(),
                                etRemainingAmount.getText().toString()
                        );

        btnSave.setEnabled(valid);
        btnSave.setAlpha(valid ? 1.0f : 0.5f);
    }

    private boolean isValidPurchaseDate(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            LocalDate purchase = LocalDate.parse(text, dateFormatter);
            return !purchase.isAfter(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidExpiryDate(String purchaseText, String expiryText) {
        if (TextUtils.isEmpty(purchaseText) || TextUtils.isEmpty(expiryText)) return false;
        try {
            LocalDate purchase = LocalDate.parse(purchaseText, dateFormatter);
            LocalDate expiry = LocalDate.parse(expiryText, dateFormatter);
            return expiry.isAfter(purchase);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidCapacity(String text) {
        if (TextUtils.isEmpty(text)) return false;
        try {
            double value = Double.parseDouble(text);
            return value > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidRemainingAmount(String capText, String amtText) {
        if (TextUtils.isEmpty(capText) || TextUtils.isEmpty(amtText)) return false;
        try {
            double capacity = Double.parseDouble(capText);
            double amount = Double.parseDouble(amtText);
            return amount >= 0 && amount <= capacity;
        } catch (Exception e) {
            return false;
        }
    }

    // --- Date picker utility ---
    private void showDatePicker(TextInputEditText target) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    target.setText(String.format("%02d/%02d/%d", month + 1, day, year));
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }
}