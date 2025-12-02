package com.example.myapplication.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.health.MedicineLabel;
import com.example.myapplication.health.MedicineUsageLog;
import com.example.myapplication.models.Child;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class InventoryRescueLog extends AppCompatActivity {

    private LinearLayout containerLogs;
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
        setContentView(R.layout.activity_inventory_rescue_log_history);

        containerLogs = findViewById(R.id.containerLogs);

        // Load child from firebase
        db = FirebaseFirestore.getInstance();
        childId = getIntent().getStringExtra("childId");
        loadChild();

        // Back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateLogs() {
        containerLogs.removeAllViews();
        ArrayList<MedicineUsageLog> logs = child.getInventory().getRescueLog();

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