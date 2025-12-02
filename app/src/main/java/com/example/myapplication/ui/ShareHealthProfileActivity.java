package com.example.myapplication.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.myapplication.R;
import com.example.myapplication.auth.SessionManager;
import com.example.myapplication.health.HealthInfo;
import com.example.myapplication.health.SharedAccessInvite;
import com.example.myapplication.models.Parent;
import com.example.myapplication.ui.ParentUI.BaseParentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.EnumSet;

public class ShareHealthProfileActivity extends BaseParentActivity {

    private Switch toggleRescueLogs;
    private Switch toggleControllerAdherence;
    private Switch toggleSymptoms;
    private Switch toggleTriggers;
    private Switch togglePefLog;
    private Switch togglePefPb;
    private Switch toggleTriageIncidents;
    private Switch toggleCharts;

    private String currentChildId;
    private Parent currentParent;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_health_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Retrieve the child ID passed from the previous activity
        if (getIntent().hasExtra("childId")) {
            currentChildId = getIntent().getStringExtra("childId");
        } else {
            Toast.makeText(this, "Error: No child selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadParentProfile();
    }

    private void loadParentProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: User not logged in.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use the authenticated UID to fetch the Parent document
        DocumentReference parentDocRef = db.collection("users").document(currentUser.getUid());

        parentDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("role");
                if ("parent".equals(role)) {
                    // Convert the document directly to a Parent object
                    Parent parent = documentSnapshot.toObject(Parent.class);
                    if (parent != null) {
                        currentParent = parent;
                        onParentProfileLoaded();
                    } else {
                        Toast.makeText(this, "Error: Failed to parse parent data.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    Toast.makeText(this, "Error: User is not a parent", Toast.LENGTH_SHORT).show();
                    finish();
                }
            } else {
                Toast.makeText(this, "Error: User data document not found.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void onParentProfileLoaded() {
        // Set Button Listeners
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        Button btnSave = findViewById(R.id.btnSaveSharing);
        btnSave.setOnClickListener(v -> generateAndSaveInvite());
    }

    private void initializeViews() {
        toggleRescueLogs = findViewById(R.id.toggle_rescue_logs);
        toggleControllerAdherence = findViewById(R.id.toggle_controller_adherence);
        toggleSymptoms = findViewById(R.id.toggle_symptoms);
        toggleTriggers = findViewById(R.id.toggle_triggers);
        togglePefLog = findViewById(R.id.toggle_pef_log);
        togglePefPb = findViewById(R.id.toggle_pef_pb);
        toggleTriageIncidents = findViewById(R.id.toggle_triage_incidents);
        toggleCharts = findViewById(R.id.toggle_charts);
    }

    private void generateAndSaveInvite() {
        ArrayList<HealthInfo> sharedFields = new ArrayList<>();

        if (toggleRescueLogs.isChecked()) sharedFields.add(HealthInfo.RESCUE_LOGS);
        if (toggleControllerAdherence.isChecked()) sharedFields.add(HealthInfo.CONTROLLER_ADHERENCE);
        if (toggleSymptoms.isChecked()) sharedFields.add(HealthInfo.SYMPTOMS);
        if (toggleTriggers.isChecked()) sharedFields.add(HealthInfo.TRIGGERS);
        if (togglePefLog.isChecked()) sharedFields.add(HealthInfo.PEF_LOG);
        if (togglePefPb.isChecked()) sharedFields.add(HealthInfo.PEF_PB);
        if (toggleTriageIncidents.isChecked()) sharedFields.add(HealthInfo.TRIAGE_INCIDENTS);
        if (toggleCharts.isChecked()) sharedFields.add(HealthInfo.CHARTS);

        if (sharedFields.isEmpty()) {
            Toast.makeText(this, "Please select at least one field to share.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedAccessInvite newInvite = currentParent.generateInvite(currentChildId, sharedFields);

        saveParentToFirestore(newInvite);
    }

    private void saveParentToFirestore(SharedAccessInvite invite) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId)
                .set(currentParent)
                .addOnSuccessListener(aVoid -> showInviteCodeDialog(invite.getInviteCode()))
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to create invite: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void showInviteCodeDialog(String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share Code Generated");
        builder.setMessage("Provide this code to your doctor to grant them access:\n\n" + code + "\n\nThis code expires in 7 days.");

        builder.setPositiveButton("Copy Code", (dialog, which) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Invite Code", code);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Code copied to clipboard", Toast.LENGTH_SHORT).show();
            finish(); // Close activity after successful generation
        });

        builder.setNegativeButton("Close", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}