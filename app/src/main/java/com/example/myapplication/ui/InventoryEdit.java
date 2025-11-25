package com.example.myapplication.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

import com.example.myapplication.health.Inventory;
import com.example.myapplication.health.InventoryItem;
import com.example.myapplication.health.MedicineLabel;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryEdit extends AppCompatActivity {

    private TextInputEditText etMedicineName, etPurchaseDate, etExpiryDate, etRemainingAmount, etCapacity;
    private MaterialAutoCompleteTextView etLabel; // Dropdown for CONTROLLER / RESCUE

    private Button btnSave, btnDelete;
    private ImageButton btnBack;

    private boolean isEditMode = false;
    private int medicineIndex = -1; // Position inside Inventory list
    private Inventory inventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_edit);

        // Initialize views
        etMedicineName = findViewById(R.id.etMedicineName);
        etPurchaseDate = findViewById(R.id.etPurchaseDate);
        etExpiryDate = findViewById(R.id.etExpiryDate);
        etRemainingAmount = findViewById(R.id.etRemainingAmount);
        etCapacity = findViewById(R.id.etCapacity);

        etLabel = findViewById(R.id.etMedicineLabel);

        btnSave = findViewById(R.id.btnSaveMedicine);
        btnDelete = findViewById(R.id.btnDeleteMedicine);
        btnBack = findViewById(R.id.btnBack);

        // Setup dropdown for labels
        ArrayAdapter<String> labelAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                new String[]{"CONTROLLER", "RESCUE"}
        );
        etLabel.setAdapter(labelAdapter);

        // Check if editing existing item
        if (getIntent().hasExtra("medicineIndex")) {
            isEditMode = true;
            medicineIndex = getIntent().getIntExtra("medicineIndex", -1);

            InventoryItem item = inventory.getInventory().get(medicineIndex);

            etMedicineName.setText(item.getName());
            etPurchaseDate.setText(item.getPurchaseDate().toLocalDate().toString());
            etExpiryDate.setText(item.getExpiryDate().toLocalDate().toString());
            etRemainingAmount.setText(String.valueOf(item.getAmount()));
            etCapacity.setText(String.valueOf(item.getCapacity()));
            etLabel.setText(item.getLabel().name());

            btnDelete.setVisibility(View.VISIBLE);
        }

        // Date pickers
        etPurchaseDate.setOnClickListener(v -> showDatePicker(etPurchaseDate));
        etExpiryDate.setOnClickListener(v -> showDatePicker(etExpiryDate));

        btnSave.setOnClickListener(v -> saveMedicine());
        btnDelete.setOnClickListener(v -> deleteMedicine());
        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePicker(TextInputEditText targetField) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                    targetField.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.show();
    }

    private void saveMedicine() {
        String name = etMedicineName.getText().toString().trim();
        String purchase = etPurchaseDate.getText().toString().trim();
        String expiry = etExpiryDate.getText().toString().trim();
        String amountStr = etRemainingAmount.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();
        String labelStr = etLabel.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter medicine name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (purchase.isEmpty() || expiry.isEmpty()) {
            Toast.makeText(this, "Please select dates", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountStr.isEmpty() || capacityStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount and capacity", Toast.LENGTH_SHORT).show();
            return;
        }
        if (labelStr.isEmpty()) {
            Toast.makeText(this, "Please select a label", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        double capacity = Double.parseDouble(capacityStr);
        MedicineLabel label = MedicineLabel.valueOf(labelStr);

        LocalDateTime purchaseDate = LocalDate.parse(purchase).atStartOfDay();
        LocalDateTime expiryDate = LocalDate.parse(expiry).atStartOfDay();

        InventoryItem updatedItem =
                new InventoryItem(name, amount, capacity, purchaseDate, expiryDate, label);

        if (isEditMode) {
            // Replace existing item
            inventory.getInventory().set(medicineIndex, updatedItem);
        } else {
            // Add new
            inventory.addItem(updatedItem);
        }

        Toast.makeText(this, "Medicine saved successfully", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void deleteMedicine() {
        if (!isEditMode) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Medicine")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    inventory.getInventory().remove(medicineIndex);
                    Toast.makeText(this, "Medicine deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}

