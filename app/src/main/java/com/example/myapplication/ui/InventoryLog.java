package com.example.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.example.myapplication.R;
import android.graphics.Color;
import java.util.ArrayList;

import com.example.myapplication.health.*;
import com.example.myapplication.models.*;
import com.google.firebase.firestore.FirebaseFirestore;

public class InventoryLog extends AppCompatActivity {

    private LinearLayout containerLogs;
    private MaterialSwitch switchLogFilter;
    private MedicineLabel currentLabel = MedicineLabel.CONTROLLER;
    private Child child;
    private FirebaseFirestore db;
    private String childId;

    private void loadChild() {
        db.collection("users").document(childId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        child = snapshot.toObject(Child.class);
                        updateLogs();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_log_history);

        containerLogs = findViewById(R.id.containerLogs);
        switchLogFilter = findViewById(R.id.switchLogFilter);

        // Load child from firebase
        db = FirebaseFirestore.getInstance();
        childId = getIntent().getStringExtra("childId");
        loadChild();

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Switch listener
        switchLogFilter.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentLabel = isChecked ? MedicineLabel.RESCUE : MedicineLabel.CONTROLLER;
            updateLogs();
        });
    }

    private void updateLogs() {
        containerLogs.removeAllViews();
        ArrayList<MedicineUsageLog> logs = (currentLabel == MedicineLabel.CONTROLLER)
                ? child.getInventory().getControllerLog()
                : child.getInventory().getRescueLog();

        for (MedicineUsageLog log : logs) {
            TextView tvLog = new TextView(this);
            tvLog.setText(log.toString());
            tvLog.setTextSize(16);
            tvLog.setTextColor(Color.parseColor("#064200"));

            LinearLayout logLayout = new LinearLayout(this);
            logLayout.setOrientation(LinearLayout.VERTICAL);
            logLayout.setPadding(16, 16, 16, 16);
            logLayout.setBackgroundColor(Color.WHITE);
            logLayout.setElevation(4);
            logLayout.addView(tvLog);

            containerLogs.addView(logLayout);
        }
    }
}