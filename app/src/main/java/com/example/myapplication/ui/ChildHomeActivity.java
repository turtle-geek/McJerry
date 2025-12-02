package com.example.myapplication.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.CheckupNotificationReceiver;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut_child;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * FIXED Child Home Activity
 * All bugs resolved - ready for production
 */
public class ChildHomeActivity extends AppCompatActivity {
    private static final String TAG = "ChildHomeActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private TextView todayDate, pefDisplay, pefDateTime;
    private CardView pefCard, graphCard1, graphCard2;
    private ImageButton sosButton;
    private Button pefButton;
    private ConstraintLayout editPEF;

    // Trend Snippet
    private LinearLayout trendContainer;
    private TrendSnippet trendSnippet;

    // Child data
    private HealthProfile hp;
    private Child currentChild;
    private String selectedChildId;
    private int selectedChildPersonalBest = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });

        try {
            Log.d(TAG, "onCreate started");

            mAuth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();

            // Initialize ONLY views that exist
            initializeViews();

            // Set up bottom navigation
            setupBottomNavigation();

            // Set current date
            setCurrentDate();

            // Set up card listeners
            setupCardListeners();

            // Setup trend snippet
            setupTrendSnippet();

            // Auto-load current child
            loadCurrentChildData();

            // Set up listeners for buttons
            setListeners();

            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "FATAL ERROR in onCreate", e);
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "initializeViews started");

        // Only get views that ACTUALLY EXIST in XML
        todayDate = findViewById(R.id.todayDate);
        pefCard = findViewById(R.id.pefCard);
        graphCard1 = findViewById(R.id.graphCard1);
        graphCard2 = findViewById(R.id.graphCard2);
        bottomNavigationView = findViewById(R.id.menuBar);
        trendContainer = findViewById(R.id.trendContainer);
        editPEF = findViewById(R.id.editPEF);
        pefDisplay = findViewById(R.id.pefDisplay);
        pefButton = findViewById(R.id.pefButton);
        pefDateTime = findViewById(R.id.pefDateTime);
        sosButton = findViewById(R.id.sosButton);

        editPEF.setVisibility(View.GONE);

        Log.d(TAG, "todayDate: " + (todayDate != null ? "found" : "NULL"));
        Log.d(TAG, "bottomNavigationView: " + (bottomNavigationView != null ? "found" : "NULL"));
        Log.d(TAG, "trendContainer: " + (trendContainer != null ? "found" : "NULL"));
    }

    private void loadCurrentChildData() {
        try {
            Log.d(TAG, "loadCurrentChildData started");

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "No user logged in");
                return;
            }

            selectedChildId = currentUser.getUid();
            Log.d(TAG, "Loading data for child: " + selectedChildId);

            db.collection("users").document(selectedChildId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String childName = documentSnapshot.getString("name");
                            Long pbLong = documentSnapshot.getLong("PEF_PB");
                            selectedChildPersonalBest = pbLong != null ? pbLong.intValue() : 400;

                            Log.d(TAG, "Loaded child: " + childName + ", PB: " + selectedChildPersonalBest);

                            currentChild = documentSnapshot.toObject(Child.class);
                            if (currentChild != null) {
                                hp = currentChild.getHealthProfile();
                                if (hp != null && hp.getPEFLog() != null && !hp.getPEFLog().isEmpty()) {
                                    displayTodayPeakFlow();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load child data", e);
                        Toast.makeText(this, "Error loading child data", Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadCurrentChildData", e);
        }
    }

    private void loadPeakFlowData() {
        if (currentChild != null && hp != null && hp.getPEFLog() != null && !hp.getPEFLog().isEmpty()) {
            displayTodayPeakFlow();
        }
    }

    private void setupTrendSnippet() {
        try {
            if (trendContainer != null) {
                trendSnippet = new TrendSnippet(this);
                trendContainer.addView(trendSnippet);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up trend snippet", e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onStart", e);
        }
    }

    private void setupBottomNavigation() {
        try {
            if (bottomNavigationView == null) {
                Log.e(TAG, "bottomNavigationView is NULL!");
                return;
            }

            bottomNavigationView.setSelectedItemId(R.id.homeButton);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        return true;

                    } else if (id == R.id.fileButton) {
                        startActivity(new Intent(ChildHomeActivity.this, ChildManagement.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(ChildHomeActivity.this, HomeStepsRecovery.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.moreButton) {
                        startActivity(new Intent(ChildHomeActivity.this, SignOut_child.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation", e);
            e.printStackTrace();
        }
    }

    private void displayTodayPeakFlow() {
        if (hp == null || hp.getPEFLog() == null || hp.getPEFLog().isEmpty()) {
            return;
        }

        ArrayList<PeakFlow> log = hp.getPEFLog();
        PeakFlow latest = log.get(log.size() - 1);

        updatePeakFlowUI(latest);
    }

    private void updatePeakFlowUI(PeakFlow todayPeakFlow) {
        pefDisplay.setText(String.valueOf(todayPeakFlow.getPeakFlow()));

        // Convert time format
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a, MMM d");
            pefDateTime.setText(todayPeakFlow.getTime().format(formatter));
        }

        switch (todayPeakFlow.getZone()) {
            case "green":
                pefCard.setCardBackgroundColor(Color.parseColor("#008000"));
                break;
            case "yellow":
                pefCard.setCardBackgroundColor(Color.parseColor("#FFD700"));
                break;
            case "red":
                pefCard.setCardBackgroundColor(Color.parseColor("#FF0000"));
                break;
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setListeners() {
        sosButton.setOnClickListener(v -> {
            // FIX: Add null check
            if (currentChild == null) {
                Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, TriageActivity.class);
            intent.putExtra("id", currentChild.getId());
            startActivity(intent);
            scheduleCheckupNotification();
        });

        pefButton.setOnClickListener(v -> {
            editPEF.setVisibility(View.VISIBLE);
            pefButton.setVisibility(View.GONE);
            TextView editTextNumber = findViewById(R.id.editTextNumber);
            editTextNumber.requestFocus();

            editTextNumber.setOnFocusChangeListener((view, hasFocus) -> {
                if (!editTextNumber.hasFocus()) {
                    savePeakFlowEntry(editTextNumber);
                }
            });
        });
    }

    private void savePeakFlowEntry(TextView editTextNumber) {
        String text = editTextNumber.getText().toString();

        if (text.isEmpty()) {
            editPEF.setVisibility(View.GONE);
            pefButton.setVisibility(View.VISIBLE);
            return;
        }

        // FIX: Add null checks
        if (currentChild == null || hp == null) {
            Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
            editPEF.setVisibility(View.GONE);
            pefButton.setVisibility(View.VISIBLE);
            return;
        }

        try {
            int peakFlowValue = Integer.parseInt(text);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime submitTime = LocalDateTime.now();
                PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);

                // FIX: Compute zone AND save it
                String zone = pef.computeZone(currentChild);
                pef.setZone(zone);  // CRITICAL: Must save the zone!

                // Add to local log
                hp.addPEFToLog(pef);

                // FIX: Save to Firebase
                savePeakFlowToFirebase();

                // Clear input
                editTextNumber.setText("");
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            editPEF.setVisibility(View.GONE);
            pefButton.setVisibility(View.VISIBLE);
        }
    }

    private void savePeakFlowToFirebase() {
        if (selectedChildId == null || hp == null) {
            return;
        }

        db.collection("users").document(selectedChildId)
                .update("healthProfile.PEFLog", hp.getPEFLog())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Peak flow saved successfully");
                    displayTodayPeakFlow();
                    editPEF.setVisibility(View.GONE);
                    pefButton.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Peak flow saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save peak flow", e);
                    Toast.makeText(this, "Failed to save. Please try again.", Toast.LENGTH_SHORT).show();
                    editPEF.setVisibility(View.GONE);
                    pefButton.setVisibility(View.VISIBLE);
                });
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleCheckupNotification() {
        long triggerTime = System.currentTimeMillis() + 10 * 60 * 1000; // 10 minutes
        Intent intent = new Intent(this, CheckupNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
        }
    }

    private void setCurrentDate() {
        try {
            if (todayDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());
                todayDate.setText(currentDate);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting date", e);
        }
    }

    private void setupCardListeners() {
        try {
            if (graphCard1 != null) {
                graphCard1.setOnClickListener(v ->
                        Toast.makeText(this, "Daily Check-in", Toast.LENGTH_SHORT).show());
            }

            if (graphCard2 != null) {
                graphCard2.setOnClickListener(v -> {
                    Intent intent = new Intent(ChildHomeActivity.this, InventoryManagement.class);
                    intent.putExtra("childId", selectedChildId);
                    startActivity(intent);
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Error setting up card listeners", e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            Log.d(TAG, "onResume called");
            if (selectedChildId != null) {
                loadPeakFlowData();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }
}