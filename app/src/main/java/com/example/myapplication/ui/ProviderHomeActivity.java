package com.example.myapplication.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut;
import com.example.myapplication.health.HealthInfo;
import com.example.myapplication.health.SharedAccessInvite;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ProviderHomeActivity extends AppCompatActivity {

    private static final String TAG = "ProviderHomeActivity";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Components
    private BottomNavigationView bottomNavigationView;
    private ImageButton bellButton;
    private TextView selectedChildName;
    private LinearLayout childSelectorLayout;

    // Dashboard Cards
    private CardView cardRescueLogs, cardControllerAdherence, cardSymptoms, cardTriggers, cardPeakFlow, cardTriage;

    // Trend Snippet
    private LinearLayout trendContainer;
    private TrendSnippet trendSnippet;

    // Data
    private List<ChildInfo> patientsList;
    private String selectedChildId = null;
    private String selectedChildNameStr = "Select a child";
    private int selectedChildPersonalBest = 400; // Default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        patientsList = new ArrayList<>();

        initializeViews();
        setupBottomNavigation();
        setupButtonListeners();
        setupChildSelector();
        setupTrendSnippet();

        // Initial State: Hide all cards until a child is selected and permissions checked
        hideAllCards();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
        loadPatientsList();
    }

    private void initializeViews() {
        bellButton = findViewById(R.id.bell);
        selectedChildName = findViewById(R.id.selectedChildName);
        childSelectorLayout = findViewById(R.id.childSelectorLayout);
        bottomNavigationView = findViewById(R.id.menuBar);
        trendContainer = findViewById(R.id.trendContainer);

        // Cards
        cardRescueLogs = findViewById(R.id.cardRescueLogs);
        cardControllerAdherence = findViewById(R.id.cardControllerAdherence);
        cardSymptoms = findViewById(R.id.cardSymptoms);
        cardTriggers = findViewById(R.id.cardTriggers);
        cardPeakFlow = findViewById(R.id.cardPeakFlow);
        cardTriage = findViewById(R.id.cardTriage);
    }

    private void hideAllCards() {
        cardRescueLogs.setVisibility(View.GONE);
        cardControllerAdherence.setVisibility(View.GONE);
        cardSymptoms.setVisibility(View.GONE);
        cardTriggers.setVisibility(View.GONE);
        cardPeakFlow.setVisibility(View.GONE);
        cardTriage.setVisibility(View.GONE);
        if (trendContainer != null) trendContainer.setVisibility(View.GONE);
    }

    private void setupChildSelector() {
        if (childSelectorLayout != null) {
            childSelectorLayout.setOnClickListener(v -> showChildSelectionDialog());
        }
    }

    private void loadPatientsList() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String providerId = currentUser.getUid();

        db.collection("users").document(providerId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Assuming 'patients' field exists as ArrayList<String>
                        List<String> patientIds = (List<String>) documentSnapshot.get("patients");

                        if (patientIds != null && !patientIds.isEmpty()) {
                            fetchPatientDetails(patientIds);
                        } else {
                            selectedChildName.setText("No patients assigned");
                        }
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading provider data", e));
    }

    private void fetchPatientDetails(List<String> patientIds) {
        patientsList.clear();

        db.collection("users")
                .whereIn("id", patientIds) // Note: Firestore 'whereIn' limit is 10
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        Integer pb = doc.getLong("PEF_PB") != null ?
                                doc.getLong("PEF_PB").intValue() : 400;
                        patientsList.add(new ChildInfo(id, name, pb));
                    }

                    if (!patientsList.isEmpty()) {
                        // Auto-select first patient
                        selectChild(patientsList.get(0));
                    }
                });
    }

    private void showChildSelectionDialog() {
        if (patientsList.isEmpty()) {
            Toast.makeText(this, "No patients available", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> names = new ArrayList<>();
        for (ChildInfo child : patientsList) {
            names.add(child.name);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Patient");
        builder.setItems(names.toArray(new String[0]), (dialog, which) -> {
            selectChild(patientsList.get(which));
        });
        builder.show();
    }

    private void selectChild(ChildInfo child) {
        selectedChildId = child.id;
        selectedChildNameStr = child.name;
        selectedChildPersonalBest = child.personalBest;

        selectedChildName.setText(selectedChildNameStr);

        // Load permissions for this child
        checkPermissionsAndLoadUI(selectedChildId);
    }

    private void checkPermissionsAndLoadUI(String childId) {
        hideAllCards();
        if (trendSnippet != null) trendSnippet.showLoading();

        String providerId = mAuth.getCurrentUser().getUid();

        // Find the specific invite/permission link
        db.collection("shared_access_invites")
                .whereEqualTo("providerID", providerId)
                .whereEqualTo("childID", childId)
                .whereEqualTo("isUsed", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Use the first valid invite found
                        SharedAccessInvite invite = queryDocumentSnapshots.getDocuments().get(0).toObject(SharedAccessInvite.class);
                        if (invite != null) {
                            updateDashboardVisibility(invite);
                        }
                    } else {
                        Toast.makeText(this, "No active shared access found for this patient.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching permissions", e);
                    Toast.makeText(this, "Failed to load permissions", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateDashboardVisibility(SharedAccessInvite invite) {
        ArrayList<HealthInfo> sharedFields = invite.getSharedFields();
        if (sharedFields == null) return;

        // 1. Rescue Logs
        if (sharedFields.contains(HealthInfo.RESCUE_LOGS)) {
            cardRescueLogs.setVisibility(View.VISIBLE);
        }

        // 2. Controller Adherence
        if (sharedFields.contains(HealthInfo.CONTROLLER_ADHERENCE)) {
            cardControllerAdherence.setVisibility(View.VISIBLE);
        }

        // 3. Symptoms
        if (sharedFields.contains(HealthInfo.SYMPTOMS)) {
            cardSymptoms.setVisibility(View.VISIBLE);
        }

        // 4. Triggers
        if (sharedFields.contains(HealthInfo.TRIGGERS)) {
            cardTriggers.setVisibility(View.VISIBLE);
        }

        // 5. Peak Flow (Logs)
        if (sharedFields.contains(HealthInfo.PEF_LOG)) {
            cardPeakFlow.setVisibility(View.VISIBLE);
        }

        // 6. Triage Incidents
        if (sharedFields.contains(HealthInfo.TRIAGE_INCIDENTS)) {
            cardTriage.setVisibility(View.VISIBLE);
        }

        // 7. Charts (Trend Snippet)
        if (sharedFields.contains(HealthInfo.CHARTS) || sharedFields.contains(HealthInfo.PEF_LOG)) {
            trendContainer.setVisibility(View.VISIBLE);
            loadPeakFlowData(selectedChildId);
        } else {
            trendContainer.setVisibility(View.GONE);
        }
    }

    private void setupButtonListeners() {
        // Bell Button
        bellButton.setOnClickListener(v -> Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show());

        // Card Listeners

        cardRescueLogs.setOnClickListener(v -> {
            if (selectedChildId != null) {
                Intent intent = new Intent(ProviderHomeActivity.this, InventoryRescueLog.class);
                intent.putExtra("childId", selectedChildId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please select a patient first", Toast.LENGTH_SHORT).show();
            }
        });

        // Other Placeholders
        cardControllerAdherence.setOnClickListener(v -> Toast.makeText(this, "Adherence Details", Toast.LENGTH_SHORT).show());
        cardSymptoms.setOnClickListener(v -> Toast.makeText(this, "Symptom History", Toast.LENGTH_SHORT).show());
        cardTriggers.setOnClickListener(v -> Toast.makeText(this, "Trigger History", Toast.LENGTH_SHORT).show());
        cardPeakFlow.setOnClickListener(v -> Toast.makeText(this, "Peak Flow Logs)", Toast.LENGTH_SHORT).show());
        cardTriage.setOnClickListener(v -> Toast.makeText(this, "Triage Incidents", Toast.LENGTH_SHORT).show());
    }

    // --- Chart Logic ---

    private void setupTrendSnippet() {
        trendSnippet = new TrendSnippet(this);
        if (trendContainer != null) {
            trendContainer.removeAllViews();
            trendContainer.addView(trendSnippet);
        }
    }

    private void loadPeakFlowData(String childId) {
        if (childId == null) return;

        List<PeakFlow> peakFlows = new ArrayList<>();

        db.collection("users").document(childId)
                .collection("peakFlowLogs")
                .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Integer val = doc.getLong("peakFlow") != null ? doc.getLong("peakFlow").intValue() : 0;
                            com.google.firebase.Timestamp timestamp = doc.getTimestamp("time");

                            if (timestamp != null && val > 0) {
                                Date date = timestamp.toDate();
                                LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
                                PeakFlow pf = new PeakFlow(val, localDateTime);

                                // Determine Zone
                                if (val >= 0.8 * selectedChildPersonalBest) pf.setZone("green");
                                else if (val >= 0.5 * selectedChildPersonalBest) pf.setZone("yellow");
                                else pf.setZone("red");

                                peakFlows.add(pf);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing peak flow", e);
                        }
                    }
                    if (trendSnippet != null) {
                        trendSnippet.setData(peakFlows);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error loading peak flow", e));
    }

    // --- Navigation ---

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.homeButton);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.homeButton) return true;

                // Navigation to other Provider activities
                if (id == R.id.fileButton) {
                    startActivity(new Intent(ProviderHomeActivity.this, ProviderManagement.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(ProviderHomeActivity.this, HomeStepsRecovery.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                } else if (id == R.id.moreButton) {
                    startActivity(new Intent(ProviderHomeActivity.this, SignOutProvider.class));
                    overridePendingTransition(0, 0);
                    finish();
                    return true;
                }
                return false;
            }
        });
    }

    // Helper Class
    private static class ChildInfo {
        String id;
        String name;
        int personalBest;

        ChildInfo(String id, String name, int personalBest) {
            this.id = id;
            this.name = name;
            this.personalBest = personalBest;
        }
    }
}