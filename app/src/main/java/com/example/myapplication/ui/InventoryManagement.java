package com.example.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.example.myapplication.R;
import android.content.Intent;
import java.time.format.DateTimeFormatter;

import com.example.myapplication.health.*;
import com.example.myapplication.models.*;


public class InventoryManagement extends AppCompatActivity {

    private TextView tvDetailMedicineName, tvDetailPurchaseDate, tvDetailExpiryDate, tvDetailAmount;
    private ImageView iconLowCapacity, iconExpired;
    private Button btnConsume, btnRefill;
    private ImageButton btnBack;
    private MaterialSwitch switchLogFilter;
    private MedicineLabel currentLabel = MedicineLabel.CONTROLLER;
    private Child child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize views
        tvDetailMedicineName = findViewById(R.id.tvDetailMedicineName);
        tvDetailPurchaseDate = findViewById(R.id.tvDetailPurchaseDate);
        tvDetailExpiryDate = findViewById(R.id.tvDetailExpiryDate);
        tvDetailAmount = findViewById(R.id.tvDetailAmount);
        iconLowCapacity = findViewById(R.id.iconLowCapacity);
        iconExpired = findViewById(R.id.iconExpired);
        btnConsume = findViewById(R.id.btnConsume);
        btnRefill = findViewById(R.id.btnRefill);
        btnBack = findViewById(R.id.btnBack);
        switchLogFilter = findViewById(R.id.switchLogFilter);

        // TODO: Load child from firebase

        // Switch listener to toggle between controller and rescue
        switchLogFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentLabel = isChecked ? MedicineLabel.RESCUE : MedicineLabel.CONTROLLER;
            updateUI();
        });

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Refill button launches InventoryRefill
        btnRefill.setOnClickListener(v -> {
            Intent intent = new Intent(this, InventoryRefill.class);
            intent.putExtra("label", currentLabel.name());
            startActivityForResult(intent, 1);
        });

        // Consume button launches InventoryUsage
        btnConsume.setOnClickListener(v -> {
            Intent intent = new Intent(this, InventoryUsage.class);
            intent.putExtra("label", currentLabel.name());
            startActivityForResult(intent, 2);
        });

        // Initial UI update
        updateUI();
    }

    private void updateUI() {
        InventoryItem item = child.getInventory().getMedicine(currentLabel);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        tvDetailMedicineName.setText(currentLabel.name());
        tvDetailPurchaseDate.setText(item.getPurchaseDate().format(formatter));
        tvDetailExpiryDate.setText(item.getExpiryDate().format(formatter));
        tvDetailAmount.setText(String.valueOf(item.getAmount()));

        // Update alert icons
        iconLowCapacity.setVisibility(item.lowVolumeAlert() ? View.VISIBLE : View.GONE);
        iconExpired.setVisibility(item.expiryAlert() ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateUI();
            child.getStreakCount().countStreaks();  // Update streaks after changes
        }
    }
}
