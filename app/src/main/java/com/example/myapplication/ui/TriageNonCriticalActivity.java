package com.example.myapplication.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

public class TriageNonCriticalActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST_CODE = 102; // Use a distinct request code

    // Views from the NonSOS card in the XML
    private Button btnCall911NonSOS;
    private Button btnStartRecovery;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Assuming the layout file for the non-critical screen is named activity_triage_non_critical.xml
        setContentView(R.layout.activity_triage_non_critical);

        bindViews();
        setListeners();
    }

    private void bindViews() {
        // Buttons from the yellow card layout
        btnCall911NonSOS = findViewById(R.id.btn_call_911_non_sos);
        btnStartRecovery = findViewById(R.id.btn_start_recovery);

        // Back button from the common top bar
        btnBack = findViewById(R.id.btnBack);
    }

    private void setListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // 1. Call 911 button (Manual call, no countdown)
        if (btnCall911NonSOS != null) {
            btnCall911NonSOS.setOnClickListener(v -> makeEmergencyCall());
        }

        // 2. Start Recovery button (Navigate to the next guidance/steps screen)
        if (btnStartRecovery != null) {
            btnStartRecovery.setOnClickListener(v -> startRecoverySteps());
        }
    }

    private void makeEmergencyCall() {
        // Standard check for CALL_PHONE permission before placing the call
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PERMISSION_REQUEST_CODE);
        } else {
            initiateCall();
        }
    }

    private void initiateCall() {
        try {
            String phoneNumber = "911";
            // Use ACTION_DIAL to prompt the user before calling
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);
            // Don't finish the activity; allow the user to return to the recovery options
        } catch (SecurityException e) {
            Toast.makeText(this, "Call failed: Permission or security issue.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void startRecoverySteps() {
        // TODO: Replace HomeStepsRecovery.class with your actual recovery activity class
        // Example: Intent intent = new Intent(this, HomeStepsRecovery.class);
        // startActivity(intent);
        Toast.makeText(this, "Navigating to Home Recovery Steps...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateCall();
            } else {
                Toast.makeText(this, "Permission denied. Manual dialing required.", Toast.LENGTH_LONG).show();
            }
        }
    }
}