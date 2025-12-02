package com.example.myapplication.ui.Inventory;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.sosButtonResponse;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.example.myapplication.R;
import android.content.Intent;
import java.time.format.DateTimeFormatter;

import com.example.myapplication.health.*;
import com.example.myapplication.models.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class InventoryManagement extends AppCompatActivity {

    private TextView tvDetailMedicineName, tvDetailPurchaseDate, tvDetailExpiryDate, tvDetailAmount, tvLowCapacity, tvExpired;
    private ImageView iconLowCapacity, iconExpired;
    private Button btnConsume, btnRefill;
    private ImageButton btnBack, sosButton;
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
                        updateUI();
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Initialize views
        tvDetailMedicineName = findViewById(R.id.tvDetailMedicineName);
        tvDetailPurchaseDate = findViewById(R.id.tvDetailPurchaseDate);
        tvDetailExpiryDate = findViewById(R.id.tvDetailExpiryDate);
        tvDetailAmount = findViewById(R.id.tvDetailAmount);
        tvLowCapacity = findViewById(R.id.tvLowCapacity);
        tvExpired = findViewById(R.id.tvExpired);
        iconLowCapacity = findViewById(R.id.iconLowCapacity);
        iconExpired = findViewById(R.id.iconExpired);
        btnConsume = findViewById(R.id.btnConsume);
        btnRefill = findViewById(R.id.btnRefill);
        btnBack = findViewById(R.id.btnBack);
        switchLogFilter = findViewById(R.id.switchLogFilter);
        sosButton = findViewById(R.id.sosButton);

        // Load child from firebase
        db = FirebaseFirestore.getInstance();
        childId = getIntent().getStringExtra("childId");
        loadChild();

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
            intent.putExtra("childId", childId);
            startActivityForResult(intent, 1);
        });

        // Consume button launches InventoryUsage
        btnConsume.setOnClickListener(v -> {
            Intent intent = new Intent(this, InventoryUsage.class);
            intent.putExtra("label", currentLabel.name());
            intent.putExtra("childId", childId);
            startActivityForResult(intent, 2);
        });

        // SOS Button viewable only in child view, and launches SOS response
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

        // Initial UI update
        updateUI();
    }

    private void updateUI() {
        InventoryItem item = child.getInventory().getMedicine(currentLabel);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        tvDetailMedicineName.setText(currentLabel.name());
        tvDetailPurchaseDate.setText(item.parsePurchaseDate().format(formatter));
        tvDetailExpiryDate.setText(item.parseExpiryDate().format(formatter));
        tvDetailAmount.setText(String.valueOf(item.getAmount()));

        // Update alert icons
        tvLowCapacity.setVisibility(item.lowVolumeAlert() ? View.VISIBLE : View.INVISIBLE);
        iconLowCapacity.setVisibility(item.lowVolumeAlert() ? View.VISIBLE : View.INVISIBLE);
        tvExpired.setVisibility(item.expiryAlert() ? View.VISIBLE : View.INVISIBLE);
        iconExpired.setVisibility(item.expiryAlert() ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadChild();
            updateUI();
            child.getStreakCount().countStreaks();  // Update streaks after changes
        }
    }
}
