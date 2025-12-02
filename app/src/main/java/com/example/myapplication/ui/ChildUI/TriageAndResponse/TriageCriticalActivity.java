package com.example.myapplication.ui.ChildUI.TriageAndResponse;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

public class TriageCriticalActivity extends AppCompatActivity {

    private static final int CALL_PERMISSION_REQUEST_CODE = 101;
    private static final long TOTAL_COUNTDOWN_MS = 10000;
    private static final long COUNTDOWN_INTERVAL_MS = 100;

    TextView countdownTextView;
    Button cancelButton;
    ProgressBar progressBar;
    CardView SOSCard;
    ImageButton backButton;

    private CountDownTimer call911Timer;
    private boolean isTimerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage_critical);

        bindViews();
        setListeners();

        startCountdown();
    }

    private void bindViews() {
        countdownTextView = findViewById(R.id.textView2);
        cancelButton = findViewById(R.id.cancel_button);
        progressBar = findViewById(R.id.progressBar);
        SOSCard = findViewById(R.id.SOS);
        backButton = findViewById(R.id.btnBack);

        SOSCard.setVisibility(View.VISIBLE);

        View nonSOS = findViewById(R.id.NonSOS);
        if (nonSOS != null) {
            nonSOS.setVisibility(View.GONE);
        }
    }

    private void setListeners() {
        if (cancelButton != null) {
            cancelButton.setOnClickListener(v -> cancelCall());
        }
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void startCountdown() {
        if (isTimerRunning) return;

        progressBar.setMax((int) TOTAL_COUNTDOWN_MS);

        call911Timer = new CountDownTimer(TOTAL_COUNTDOWN_MS, COUNTDOWN_INTERVAL_MS) {
            @Override
            public void onFinish() {
                isTimerRunning = false;
                progressBar.setProgress(0);
                if (cancelButton != null) {
                    cancelButton.setVisibility(View.GONE);
                }
                countdownTextView.setText("CALLING 911 NOW...");
                makeEmergencyCall();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                int secondsLeft = (int) Math.ceil(millisUntilFinished / 1000.0);

                progressBar.setProgress((int) millisUntilFinished);

                String text = "Calling 911 in " + secondsLeft + " seconds";
                countdownTextView.setText(text);
            }
        }.start();
        isTimerRunning = true;
    }

    private void cancelCall() {
        if (call911Timer != null) {
            call911Timer.cancel();
            isTimerRunning = false;

            Toast.makeText(this, "Emergency call countdown canceled.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void makeEmergencyCall() {
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
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
            startActivity(callIntent);

            finish();
        } catch (SecurityException e) {
            Toast.makeText(this, "Call initiation failed: Check CALL_PHONE permission in Manifest.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initiateCall();
            } else {
                Toast.makeText(this, "Call permission denied. Call failed.", Toast.LENGTH_LONG).show();
                countdownTextView.setText("Call Failed: Permission Denied.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (call911Timer != null) {
            call911Timer.cancel();
        }
        super.onDestroy();
    }
}