package com.example.myapplication.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresPermission;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.cardview.widget.CardView;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.CheckupNotificationReceiver;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;
import com.example.myapplication.auth.SignOut_child;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MINIMAL CRASH-PROOF Child Home Activity
 * Only includes views that actually exist in XML
 */
public class ChildHomeActivity extends AppCompatActivity {
    private static final String TAG = "ChildHomeActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private TextView todayDate, pefDisplay, pefDateTime;
    private CardView pefCard, statusCard2, statusCard3, graphCard;
    ImageButton sosButton;
    Button pefButton;
    ConstraintLayout editPEF;

    // Trend Snippet
    private LinearLayout trendContainer;
    private TrendSnippet trendSnippet;

    // Child data
    HealthProfile hp;
    Child currentChild;
    private String selectedChildId;
    private int selectedChildPersonalBest = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_child_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            return insets;
        });

        editPEF = findViewById(R.id.editPEF);
        pefCard = findViewById(R.id.pefCard);
        pefDisplay = findViewById(R.id.pefDisplay);
        pefButton = findViewById(R.id.pefButton);
        pefDateTime = findViewById(R.id.pefDateTime);

        editPEF.setVisibility(View.GONE);

        String id = getIntent().getStringExtra("id");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (id == null) {
            Log.e("ChildHomeActivity", "No child ID provided");
            return;
        } else {
            db.collection("users").document(id).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) return;

                        currentChild = documentSnapshot.toObject(Child.class);
                        if (currentChild == null) return;

                        hp = currentChild.getHealthProfile();
                        if (hp == null || hp.getPEFLog() == null || hp.getPEFLog().isEmpty())
                            return;
                        displayTodayPeakFlow();
                    });
        }

        try {
            setContentView(R.layout.activity_child_home);

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

            displayTodayPeakFlow();

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
        statusCard2 = findViewById(R.id.statusCard2);
        statusCard3 = findViewById(R.id.statusCard3);
        graphCard1 = findViewById(R.id.graphCard1);
        graphCard2 = findViewById(R.id.graphCard2);
        bottomNavigationView = findViewById(R.id.menuBar);
        trendContainer = findViewById(R.id.trendContainer);
        editPEF = findViewById(R.id.editPEF);
        pefDisplay = findViewById(R.id.pefDisplay);
        pefButton = findViewById(R.id.pefButton);
        pefDateTime = findViewById(R.id.pefDateTime);

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

                            loadPeakFlowData();
                        } else {
                            Log.e(TAG, "Child document does not exist");
                            loadTestData();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading child data", e);
                        loadTestData();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadCurrentChildData", e);
            e.printStackTrace();
        }
    }

    private void displayTodayPeakFlow() {
        ArrayList<PeakFlow> log = hp.getPEFLog();
        PeakFlow latest = log.get(log.size() - 1);

        if (selectedChildId == null) {
            Log.e("ChildHomeActivity", "No child ID provided");
            return;
        } else {
            db.collection("users").document(selectedChildId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (!documentSnapshot.exists()) return;

                        currentChild = documentSnapshot.toObject(Child.class);
                        if (currentChild == null) return;

                        hp = currentChild.getHealthProfile();
                        if (hp == null || hp.getPEFLog() == null || hp.getPEFLog().isEmpty())
                            return;
                    });
        }
        updatePeakFlowUI(latest);
    }

    private void updatePeakFlowUI(PeakFlow todayPeakFlow) {
        pefDisplay.setText(String.valueOf(todayPeakFlow.getPeakFlow()));

        // Convert time format
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("h:mm a, MMM d");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

    private void savePeakFlowToFirebase(int peakFlowValue, String zone) {
        if (selectedChildId == null) {
            Log.e(TAG, "Cannot save: selectedChildId is null");
            return;
        }

        try {
            // Create a map to store the peak flow data
            java.util.HashMap<String, Object> peakFlowData = new java.util.HashMap<>();
            peakFlowData.put("peakFlow", peakFlowValue);
            peakFlowData.put("zone", zone);
            peakFlowData.put("time", com.google.firebase.firestore.FieldValue.serverTimestamp());

            // Save to Firebase
            db.collection("users")
                    .document(selectedChildId)
                    .collection("peakFlowLogs")
                    .add(peakFlowData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Peak flow saved successfully: " + documentReference.getId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error saving peak flow", e);
                        Toast.makeText(this, "Error saving data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Exception in savePeakFlowToFirebase", e);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void setListeners() {
        sosButton = findViewById(R.id.sosButton);
        sosButton.setOnClickListener(v -> {
            // Use selectedChildId instead of currentChild to avoid null pointer
            if (selectedChildId != null) {
                Intent intent = new Intent(this, TriageActivity.class);
                intent.putExtra("id", selectedChildId);
                startActivity(intent);
                scheduleCheckupNotification();
            } else {
                Toast.makeText(this, "Unable to load child data. Please try again.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "SOS button clicked but selectedChildId is null");
            }
        });

        pefButton.setOnClickListener(v -> {
            // Hide button and show edit field
            pefButton.setVisibility(View.GONE);
            editPEF.setVisibility(View.VISIBLE);

            EditText editTextNumber = findViewById(R.id.editTextNumber);

            // Request focus so keyboard appears
            editTextNumber.requestFocus();

            editTextNumber.setOnFocusChangeListener((view, hasFocus) -> {
                if (!hasFocus) {
                    // User finished entering data
                    String text = editTextNumber.getText().toString();

                    if (!text.isEmpty()) {
                        try {
                            int peakFlowValue = Integer.parseInt(text);

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                LocalDateTime submitTime = LocalDateTime.now();
                                PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);

                                // Calculate zone based on personal best
                                if (peakFlowValue >= 0.8 * selectedChildPersonalBest) {
                                    pef.setZone("green");
                                } else if (peakFlowValue >= 0.5 * selectedChildPersonalBest) {
                                    pef.setZone("yellow");
                                } else {
                                    pef.setZone("red");
                                }

                                // Update UI immediately
                                updatePeakFlowUI(pef);

                                // Save to Firebase
                                if (selectedChildId != null) {
                                    savePeakFlowToFirebase(peakFlowValue, pef.getZone());
                                }

                                // Update local data if available
                                if (currentChild != null && hp != null) {
                                    hp.addPEFToLog(pef);
                                }

                                Toast.makeText(ChildHomeActivity.this,
                                        "Peak flow recorded: " + peakFlowValue,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(ChildHomeActivity.this,
                                    "Please enter a valid number",
                                    Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Invalid number format", e);
                        }
                    }

                    // Hide edit field and show button again
                    editPEF.setVisibility(View.GONE);
                    pefButton.setVisibility(View.VISIBLE);
                    editTextNumber.setText(""); // Clear the field for next time
                }
            });
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
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }

    private void setupTrendSnippet() {
        try {
            Log.d(TAG, "setupTrendSnippet started");

            if (trendContainer != null) {
                trendSnippet = new TrendSnippet(this);
                trendContainer.removeAllViews();
                trendContainer.addView(trendSnippet);
                Log.d(TAG, "TrendSnippet added successfully");
            } else {
                Log.w(TAG, "trendContainer is NULL - cannot add TrendSnippet");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up trend snippet", e);
            e.printStackTrace();
        }
    }

    private void loadPeakFlowData() {
        try {
            if (selectedChildId == null) {
                Log.w(TAG, "No child ID");
                return;
            }

            if (trendSnippet != null) {
                trendSnippet.showLoading();
            }

            List<PeakFlow> peakFlows = new ArrayList<>();

            db.collection("users").document(selectedChildId)
                    .collection("peakFlowLogs")
                    .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(30)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            try {
                                int peakFlowValue = doc.getLong("peakFlow") != null ?
                                        doc.getLong("peakFlow").intValue() : 0;
                                com.google.firebase.Timestamp timestamp = doc.getTimestamp("time");

                                if (timestamp != null && peakFlowValue > 0) {
                                    Date date = timestamp.toDate();
                                    LocalDateTime localDateTime = LocalDateTime.ofInstant(
                                            date.toInstant(),
                                            java.time.ZoneId.systemDefault()
                                    );

                                    PeakFlow pf = new PeakFlow(peakFlowValue, localDateTime);

                                    if (peakFlowValue >= 0.8 * selectedChildPersonalBest) {
                                        pf.setZone("green");
                                    } else if (peakFlowValue >= 0.5 * selectedChildPersonalBest) {
                                        pf.setZone("yellow");
                                    } else {
                                        pf.setZone("red");
                                    }

                                    peakFlows.add(pf);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing peak flow data", e);
                            }
                        }

                        if (peakFlows.isEmpty()) {
                            loadTestData();
                        } else if (trendSnippet != null) {
                            trendSnippet.setData(peakFlows);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading peak flow data", e);
                        loadTestData();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadPeakFlowData", e);
        }
    }

    private void loadTestData() {
        try {
            List<PeakFlow> testData = new ArrayList<>();

            for (int i = 0; i < 30; i++) {
                LocalDateTime time = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    time = LocalDateTime.now().minusDays(i);
                }
                int value = 250 + (int)(Math.random() * 150);

                PeakFlow pf = new PeakFlow(value, time);

                if (value >= 360) {
                    pf.setZone("green");
                } else if (value >= 250) {
                    pf.setZone("yellow");
                } else {
                    pf.setZone("red");
                }

                testData.add(pf);
            }

            if (trendSnippet != null) {
                trendSnippet.setData(testData);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading test data", e);
        }
    }

    //This method is to block users' access to visit this app's Child Home Activities,
    // if they don't have an account
    @Override
    protected void onStart(){
        super.onStart();
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser == null){
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

    private void displayTodayPeakFlow() {
        ArrayList<PeakFlow> log = hp.getPEFLog();
        PeakFlow latest = log.get(log.size() - 1);

        updatePeakFlowUI(latest);
    }

    private void updatePeakFlowUI(PeakFlow todayPeakFlow) {
        pefDisplay.setText(String.valueOf(todayPeakFlow.getPeakFlow()));

        // Convert time format
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("h:mm a, MMM d");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        sosButton = findViewById(R.id.sosButton);
        sosButton.setOnClickListener(v ->{
                Intent intent = new Intent(this, TriageActivity.class);
                intent.putExtra("id", currentChild.getId());
                startActivity(intent);
                scheduleCheckupNotification();
        });

        pefButton.setOnClickListener(v -> {
            editPEF.setVisibility(View.VISIBLE);
            TextView editTextNumber = findViewById(R.id.editTextNumber);
            editTextNumber.setOnFocusChangeListener((view, hasFocus) -> {
                if (editTextNumber.hasFocus()) {
                    // Uh;
                } else {
                    String text = editTextNumber.getText().toString();
                    int peakFlowValue;
                    if (!text.isEmpty()) {
                        peakFlowValue = Integer.parseInt(text);
                        LocalDateTime submitTime = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            submitTime = LocalDateTime.now();
                        }

                        if (submitTime != null){
                            PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);
                            pef.computeZone(currentChild);
                            hp.addPEFToLog(pef);
                        }
                    }
                }
            });
        });
                    return false;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error setting up navigation", e);
            e.printStackTrace();
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleCheckupNotification() {
        long triggerTime = System.currentTimeMillis() + 10*60*1000; // 10 minutes
        Intent intent = new Intent(this, CheckupNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
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
            if (editPEF != null) {
                editPEF.setOnClickListener(v ->
                        Toast.makeText(this, "Today's Status", Toast.LENGTH_SHORT).show());
            }

            if (statusCard2 != null) {
                statusCard2.setOnClickListener(v ->
                        Toast.makeText(this, "Last Rescue Time", Toast.LENGTH_SHORT).show());
            }

            if (statusCard3 != null) {
                statusCard3.setOnClickListener(v ->
                        Toast.makeText(this, "Weekly Rescue Time", Toast.LENGTH_SHORT).show());
            }

            if (graphCard != null) {
                graphCard.setOnClickListener(v ->
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