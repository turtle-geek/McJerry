package com.example.myapplication.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.example.myapplication.health.InventoryItem;
import com.example.myapplication.health.MedicineLabel;
import com.example.myapplication.health.MedicineUsageLog;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;
import com.example.myapplication.models.TechniqueQuality;
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

import jp.wasabeef.blurry.Blurry;

/**
 * FIXED Child Home Activity
 * - PrePostCheck rating system bugs resolved
 * - ANR prevention with loading indicators
 * - Proper error handling and timeouts
 */
public class ChildHomeActivity extends AppCompatActivity {
    private static final String TAG = "ChildHomeActivity";
    private static final int LOADING_TIMEOUT_MS = 10000; // 10 seconds

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private TextView todayDate, pefDisplay, pefDateTime;
    private CardView pefCard, graphCard1, graphCard2;
    private ImageButton sosButton;
    private Button pefButton;
    private ConstraintLayout editPEF;

    // FIX: Loading indicator
    private ProgressDialog progressDialog;
    private boolean isDataLoaded = false;

    // FIX: Properly initialize prepostCheckPopup
    private View prepostCheckPopup;

    // FIX: Use consistent variable name - only currentChild
    private Child currentChild;

    // FIX: Medicine data from intent
    private String medicineLabel;
    private String medicineName;
    private double dosage;

    // Trend Snippet
    private LinearLayout trendContainer;
    private TrendSnippet trendSnippet;

    // Child data
    private HealthProfile hp;
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

            // FIX: Get intent data for medicine rating
            retrieveIntentData();

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

            // FIX: Show loading indicator BEFORE loading data
            showLoading("Loading your data...");

            // FIX: Disable UI during loading
            disableInteractiveElements(true);

            // Auto-load current child
            loadCurrentChildData();

            // Set up listeners for buttons
            setListeners();

            // FIX: Set up prepost check buttons
            setupPrePostCheckButtons();

            Log.d(TAG, "onCreate completed successfully");

        } catch (Exception e) {
            Log.e(TAG, "FATAL ERROR in onCreate", e);
            e.printStackTrace();
            hideLoading();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ==================== ANR PREVENTION METHODS ====================

    /**
     * FIX: Show loading indicator to prevent ANR
     */
    private void showLoading(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        isDataLoaded = false;
    }

    /**
     * FIX: Hide loading indicator
     */
    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        isDataLoaded = true;
    }

    /**
     * FIX: Disable/enable interactive elements during loading
     */
    private void disableInteractiveElements(boolean disable) {
        float alpha = disable ? 0.5f : 1.0f;

        if (pefButton != null) {
            pefButton.setEnabled(!disable);
            pefButton.setAlpha(alpha);
        }
        if (sosButton != null) {
            sosButton.setEnabled(!disable);
            sosButton.setAlpha(alpha);
        }
        if (graphCard1 != null) {
            graphCard1.setEnabled(!disable);
            graphCard1.setAlpha(alpha);
        }
        if (graphCard2 != null) {
            graphCard2.setEnabled(!disable);
            graphCard2.setAlpha(alpha);
        }
        if (bottomNavigationView != null) {
            bottomNavigationView.setEnabled(!disable);
        }
    }

    /**
     * FIX: Set timeout for loading operations
     */
    private void setLoadingTimeout() {
        Handler timeoutHandler = new Handler(Looper.getMainLooper());
        timeoutHandler.postDelayed(() -> {
            if (!isDataLoaded && progressDialog != null && progressDialog.isShowing()) {
                hideLoading();
                disableInteractiveElements(false);
                Toast.makeText(this,
                        "Loading timed out. Please check your connection and try again.",
                        Toast.LENGTH_LONG).show();
                Log.w(TAG, "Loading timeout reached");
            }
        }, LOADING_TIMEOUT_MS);
    }

    // ==================== ORIGINAL METHODS ====================

    // FIX: New method to retrieve intent data
    private void retrieveIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            medicineLabel = intent.getStringExtra("medicineLabel");
            medicineName = intent.getStringExtra("medicineName");
            dosage = intent.getDoubleExtra("dosage", 0.0);

            Log.d(TAG, "Intent data - Label: " + medicineLabel + ", Name: " + medicineName + ", Dosage: " + dosage);
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

        // FIX: Initialize prepostCheckPopup
        prepostCheckPopup = findViewById(R.id.prepostCheckPopup);

        editPEF.setVisibility(View.GONE);
        if (prepostCheckPopup != null) {
            prepostCheckPopup.setVisibility(View.GONE);
        }

        Log.d(TAG, "todayDate: " + (todayDate != null ? "found" : "NULL"));
        Log.d(TAG, "bottomNavigationView: " + (bottomNavigationView != null ? "found" : "NULL"));
        Log.d(TAG, "trendContainer: " + (trendContainer != null ? "found" : "NULL"));
        Log.d(TAG, "prepostCheckPopup: " + (prepostCheckPopup != null ? "found" : "NULL"));
    }

    /**
     * FIX: Load child data with proper loading indicators and timeout
     */
    private void loadCurrentChildData() {
        try {
            Log.d(TAG, "loadCurrentChildData started");
            long startTime = System.currentTimeMillis();

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.w(TAG, "No user logged in");
                hideLoading();
                disableInteractiveElements(false);
                return;
            }

            selectedChildId = currentUser.getUid();
            Log.d(TAG, "Loading data for child: " + selectedChildId);

            // FIX: Set timeout
            setLoadingTimeout();

            db.collection("users").document(selectedChildId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        long loadTime = System.currentTimeMillis() - startTime;
                        Log.d(TAG, "Firebase query completed in " + loadTime + "ms");

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

                            // FIX: Hide loading and enable UI
                            hideLoading();
                            disableInteractiveElements(false);

                            // FIX: Show popup if we came from medicine activity
                            if (medicineLabel != null) {
                                showPrePostCheckPopup();
                            }

                            Log.d(TAG, "Data load completed successfully in " + (System.currentTimeMillis() - startTime) + "ms");
                        } else {
                            Log.w(TAG, "Child document does not exist");
                            hideLoading();
                            disableInteractiveElements(false);
                            Toast.makeText(this, "Child data not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load child data", e);
                        hideLoading();
                        disableInteractiveElements(false);
                        Toast.makeText(this, "Error loading data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadCurrentChildData", e);
            hideLoading();
            disableInteractiveElements(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            // FIX: Check if data is loaded
            if (!isDataLoaded || currentChild == null) {
                Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, TriageActivity.class);
            intent.putExtra("childId", selectedChildId);
            startActivity(intent);
        });

        pefButton.setOnClickListener(v -> {
            // FIX: Check if data is loaded
            if (!isDataLoaded || currentChild == null) {
                Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                return;
            }
            editPEF.setVisibility(View.VISIBLE);
            pefButton.setVisibility(View.GONE);
        });

        // Handle PEF input - automatically save when user presses Done/Enter
        EditText peakFlowInput = findViewById(R.id.editTextNumber);
        if (peakFlowInput != null) {
            peakFlowInput.setOnEditorActionListener((textView, actionId, keyEvent) -> {
                savePeakFlowEntry();
                return true;
            });
        }
    }

    private void savePeakFlowEntry() {
        EditText editTextNumber = findViewById(R.id.editTextNumber);
        String text = editTextNumber.getText().toString().trim();

        // FIX: Check if data is loaded
        if (!isDataLoaded || currentChild == null || hp == null) {
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

                String zone = pef.computeZone(currentChild);
                pef.setZone(zone);

                hp.addPEFToLog(pef);

                savePeakFlowToFirebase();

                editTextNumber.setText("");
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            editPEF.setVisibility(View.GONE);
            pefButton.setVisibility(View.VISIBLE);
        }
    }

    // FIX: Properly structured setupPrePostCheckButtons method
    private void setupPrePostCheckButtons() {
        ImageButton betterBtn = findViewById(R.id.better);
        ImageButton sameBtn = findViewById(R.id.same);
        ImageButton worseBtn = findViewById(R.id.Worse);

        if (betterBtn != null) {
            betterBtn.setOnClickListener(v -> {
                saveNewLogWithRating("Better");
                hidePrePostCheckPopup();
            });
        }
        if (sameBtn != null) {
            sameBtn.setOnClickListener(v -> {
                saveNewLogWithRating("Same");
                hidePrePostCheckPopup();
            });
        }
        if (worseBtn != null) {
            worseBtn.setOnClickListener(v -> {
                saveNewLogWithRating("Worse");
                hidePrePostCheckPopup();
            });
        }
    }

    // FIX: New method to show popup
    private void showPrePostCheckPopup() {
        if (prepostCheckPopup != null) {
            prepostCheckPopup.setVisibility(View.VISIBLE);

            // Optional: Blur background
            View mainContent = findViewById(R.id.homePage);
            if (mainContent != null) {
                Blurry.with(this).radius(25).sampling(2).onto((ViewGroup) mainContent);
            }
        }
    }

    // FIX: New method to hide popup
    private void hidePrePostCheckPopup() {
        if (prepostCheckPopup != null) {
            prepostCheckPopup.setVisibility(View.GONE);

            // Optional: Remove blur
            View mainContent = findViewById(R.id.homePage);
            if (mainContent != null) {
                Blurry.delete((ViewGroup) mainContent);
            }
        }
    }

    // FIX: Single, properly structured saveNewLogWithRating method
    private void saveNewLogWithRating(String rating) {
        if (currentChild == null) {
            Toast.makeText(this, "Error: Child data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (medicineLabel == null) {
            Toast.makeText(this, "Error: Medicine label not set", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MedicineLabel label = MedicineLabel.valueOf(medicineLabel);

            // Get inventory item
            InventoryItem medicineItem = currentChild.getInventory().getMedicine(label);

            if (medicineItem == null) {
                Toast.makeText(this, "Error: Medicine not found in inventory", Toast.LENGTH_SHORT).show();
                return;
            }

            LocalDateTime timestamp = LocalDateTime.now();
            TechniqueQuality quality = (label == MedicineLabel.CONTROLLER) ? TechniqueQuality.HIGH : TechniqueQuality.NA;

            MedicineUsageLog newLog = new MedicineUsageLog(
                    medicineItem,
                    dosage,
                    timestamp,
                    quality,
                    rating
            );

            // Add to appropriate log list
            if (label == MedicineLabel.CONTROLLER) {
                currentChild.getInventory().getControllerLog().add(newLog);
            } else {
                currentChild.getInventory().getRescueLog().add(newLog);
            }

            // Save to Firebase
            saveChildToFirebase(rating);

        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Error: Invalid medicine label", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid medicine label: " + medicineLabel, e);
        }
    }

    // FIX: New method to save child data to Firebase
    private void saveChildToFirebase(String rating) {
        if (selectedChildId == null || currentChild == null) {
            return;
        }

        db.collection("users").document(selectedChildId)
                .set(currentChild)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rating saved: " + rating, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Child data saved with rating: " + rating);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save rating", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to save child data", e);
                });
    }

    private void savePeakFlowToFirebase() {
        if (selectedChildId == null || hp == null) {
            return;
        }

        db.collection("users").document(selectedChildId)
                .update("healthProfile.PEF_LOG", hp.getPEFLog())
                .addOnSuccessListener(aVoid -> {
                    displayTodayPeakFlow();
                    editPEF.setVisibility(View.GONE);
                    pefButton.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Peak flow saved!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save peak flow", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to save peak flow", e);
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
                graphCard1.setOnClickListener(v -> {
                    // FIX: Check if data is loaded
                    if (!isDataLoaded) {
                        Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(this, "Daily Check-in", Toast.LENGTH_SHORT).show();
                });
            }

            if (graphCard2 != null) {
                graphCard2.setOnClickListener(v -> {
                    // FIX: Check if data is loaded
                    if (!isDataLoaded || selectedChildId == null) {
                        Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                        return;
                    }
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
            if (selectedChildId != null && isDataLoaded) {
                loadPeakFlowData();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // FIX: Clean up progress dialog
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}