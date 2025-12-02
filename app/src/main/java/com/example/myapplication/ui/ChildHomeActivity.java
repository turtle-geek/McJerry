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
 * - ADDED: Show prepost_check dialog after medicine usage
 */
public class ChildHomeActivity extends AppCompatActivity {
    private static final String TAG = "ChildHomeActivity";
    private static final int LOADING_TIMEOUT_MS = 10000; // 10 seconds

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private TextView todayDate, pefDisplay, pefDateTime;
    private CardView pefCard, graphCard1, graphCard2;
    private ImageButton sosButton, logoButton;
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

    // FIX: Flag to track if we should show prepost check
    private boolean shouldShowPrepostCheck = false;

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

        // FIX: Show prepost check dialog after loading completes
        if (shouldShowPrepostCheck) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                showPrePostCheckPopup();
                shouldShowPrepostCheck = false; // Reset flag
            }, 500); // Small delay to ensure UI is ready
        }
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
                        "Loading is taking longer than expected...",
                        Toast.LENGTH_LONG).show();
            }
        }, LOADING_TIMEOUT_MS);
    }

    // ==================== INITIALIZATION METHODS ====================

    /**
     * FIX: Retrieve medicine data from intent (from ParentTutorial)
     */
    private void retrieveIntentData() {
        try {
            // Check if we should show prepost check dialog
            shouldShowPrepostCheck = getIntent().getBooleanExtra("SHOW_PREPOST_CHECK", false);

            if (shouldShowPrepostCheck) {
                medicineLabel = getIntent().getStringExtra("medicineLabel");
                medicineName = getIntent().getStringExtra("medicineName");
                dosage = getIntent().getDoubleExtra("dosage", 0);

                Log.d(TAG, "=== PREPOST CHECK DATA ===");
                Log.d(TAG, "Should show: " + shouldShowPrepostCheck);
                Log.d(TAG, "Medicine Label: " + medicineLabel);
                Log.d(TAG, "Medicine Name: " + medicineName);
                Log.d(TAG, "Dosage: " + dosage);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving intent data", e);
        }
    }

    private void initializeViews() {
        try {
            Log.d(TAG, "Initializing views...");

            // FIX: Initialize prepostCheckPopup from layout
            prepostCheckPopup = findViewById(R.id.prepostCheckPopup);
            if (prepostCheckPopup != null) {
                prepostCheckPopup.setVisibility(View.GONE); // Hidden by default
                Log.d(TAG, "PrepostCheckPopup initialized successfully");
            } else {
                Log.e(TAG, "WARNING: prepostCheckPopup not found in layout!");
            }

            todayDate = findViewById(R.id.todayDate);
            pefDisplay = findViewById(R.id.pefDisplay);
            pefDateTime = findViewById(R.id.pefDateTime);
            pefCard = findViewById(R.id.pefCard);
            graphCard1 = findViewById(R.id.graphCard1);
            graphCard2 = findViewById(R.id.graphCard2);
            pefButton = findViewById(R.id.pefButton);
            editPEF = findViewById(R.id.editPEF);
            trendContainer = findViewById(R.id.trendContainer);

            // Initialize buttons
            sosButton = findViewById(R.id.sosButton);
            logoButton = findViewById(R.id.logo);

            // FIX: Initialize bottom navigation with null check
            bottomNavigationView = findViewById(R.id.menuBar);

            // Initially hide edit PEF
            if (editPEF != null) {
                editPEF.setVisibility(View.GONE);
            }

            Log.d(TAG, "Views initialized successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }

    private void setupBottomNavigation() {
        if (bottomNavigationView == null) {
            Log.e(TAG, "Bottom navigation view is null!");
            return;
        }

        try {
            bottomNavigationView.setSelectedItemId(R.id.homeButton);
            bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.homeButton) {
                    return true;
                } else if (id == R.id.fileButton) {
                    // FIX: Check if data is loaded
                    if (!isDataLoaded || selectedChildId == null) {
                        Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Intent intent = new Intent(ChildHomeActivity.this, InventoryManagement.class);
                    intent.putExtra("childId", selectedChildId);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.nav_profile) {
                    Intent profileIntent = new Intent(ChildHomeActivity.this, SignOut_child.class);
                    startActivity(profileIntent);
                    return true;
                } else if (id == R.id.moreButton) {
                    // FIX: Check if data is loaded
                    if (!isDataLoaded || selectedChildId == null) {
                        Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    Intent intent = new Intent(ChildHomeActivity.this, InventoryLog.class);
                    intent.putExtra("childId", selectedChildId);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
            Log.d(TAG, "Bottom navigation setup complete");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up bottom navigation", e);
        }
    }

    private void setupTrendSnippet() {
        try {
            if (trendContainer != null) {
                // Create TrendSnippet instance
                trendSnippet = new TrendSnippet(this);

                // Add to container
                trendContainer.addView(trendSnippet);

                Log.d(TAG, "Trend snippet initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up trend snippet", e);
        }
    }

    private void setListeners() {
        try {
            // FIX: SOS button
            if (sosButton != null) {
                sosButton.setOnClickListener(v -> {
                    if (!isDataLoaded) {
                        Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent sosIntent = new Intent(ChildHomeActivity.this, TriageActivity.class);
                    startActivity(sosIntent);
                });
            }

            // Logo button for sign out
            if (logoButton != null) {
                logoButton.setOnClickListener(v -> {
                    Intent profileIntent = new Intent(ChildHomeActivity.this, SignOut_child.class);
                    startActivity(profileIntent);
                });
            }

            // FIX: PEF button - shows EditText when clicked
            if (pefButton != null) {
                pefButton.setOnClickListener(v -> {
                    if (!isDataLoaded) {
                        Toast.makeText(this, "Please wait while data loads", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (editPEF != null && pefButton != null) {
                        editPEF.setVisibility(View.VISIBLE);
                        pefButton.setVisibility(View.GONE);

                        // Focus on the EditText
                        EditText etPEF = findViewById(R.id.editTextNumber);
                        if (etPEF != null) {
                            etPEF.requestFocus();
                        }
                    }
                });
            }

            // FIX: Handle EditText done action to submit PEF
            EditText etPEF = findViewById(R.id.editTextNumber);
            if (etPEF != null) {
                etPEF.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                        submitPeakFlow();
                        return true;
                    }
                    return false;
                });
            }

            Log.d(TAG, "Listeners set up successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting up listeners", e);
        }
    }

    // ==================== DATA LOADING METHODS ====================

    /**
     * FIX: Load current child data with proper error handling
     */
    private void loadCurrentChildData() {
        try {
            Log.d(TAG, "Loading current child data...");

            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Log.e(TAG, "No user logged in!");
                hideLoading();
                Toast.makeText(this, "Please log in again", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }

            selectedChildId = currentUser.getUid();
            Log.d(TAG, "Loading child with ID: " + selectedChildId);

            // FIX: Set timeout
            setLoadingTimeout();

            db.collection("users")
                    .document(selectedChildId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        try {
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "Child document found");

                                // FIX: Use consistent variable name - currentChild only
                                currentChild = documentSnapshot.toObject(Child.class);

                                if (currentChild != null) {
                                    hp = currentChild.getHealthProfile();
                                    if (hp != null && hp.getPEF_PB() != 0) {
                                        selectedChildPersonalBest = hp.getPEF_PB();
                                    }

                                    // Update UI with child data
                                    displayTodayPeakFlow();
                                    if (trendSnippet != null && hp != null) {
                                        trendSnippet.updateWithData(hp.getPEFLog(), selectedChildPersonalBest);
                                    }

                                    Log.d(TAG, "Child data loaded successfully");
                                } else {
                                    Log.e(TAG, "Failed to parse child data");
                                    Toast.makeText(this, "Error loading child data", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.e(TAG, "Child document doesn't exist");
                                Toast.makeText(this, "Child profile not found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing child data", e);
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            // FIX: Always hide loading and re-enable UI
                            hideLoading();
                            disableInteractiveElements(false);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to load child data", e);
                        Toast.makeText(this, "Failed to load data: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                        hideLoading();
                        disableInteractiveElements(false);
                    });

        } catch (Exception e) {
            Log.e(TAG, "Error in loadCurrentChildData", e);
            hideLoading();
            disableInteractiveElements(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadPeakFlowData() {
        try {
            if (selectedChildId == null) {
                Log.e(TAG, "No selected child ID");
                return;
            }

            db.collection("users")
                    .document(selectedChildId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Child child = documentSnapshot.toObject(Child.class);
                            if (child != null) {
                                currentChild = child; // FIX: Update currentChild reference
                                hp = child.getHealthProfile();
                                displayTodayPeakFlow();

                                if (trendSnippet != null && hp != null) {
                                    trendSnippet.updateWithData(hp.getPEFLog(), selectedChildPersonalBest);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to reload PEF data", e));
        } catch (Exception e) {
            Log.e(TAG, "Error in loadPeakFlowData", e);
        }
    }

    // ==================== UI UPDATE METHODS ====================

    private void displayTodayPeakFlow() {
        try {
            if (hp == null || hp.getPEFLog() == null || hp.getPEFLog().isEmpty()) {
                if (pefDisplay != null) {
                    pefDisplay.setText("No Data");
                }
                if (pefDateTime != null) {
                    pefDateTime.setText("");
                }
                return;
            }

            ArrayList<PeakFlow> pefLog = hp.getPEFLog();
            PeakFlow latestPEF = pefLog.get(pefLog.size() - 1);

            if (pefDisplay != null) {
                pefDisplay.setText(String.valueOf(latestPEF.getPeakFlow()));
            }

            if (pefDateTime != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
                String dateTimeStr = latestPEF.getTime().format(formatter);
                pefDateTime.setText(dateTimeStr);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying peak flow", e);
            if (pefDisplay != null) {
                pefDisplay.setText("Error");
            }
        }
    }

    private void submitPeakFlow() {
        try {
            EditText etPEF = findViewById(R.id.editTextNumber);
            if (etPEF == null || hp == null) {
                return;
            }

            String pefText = etPEF.getText().toString().trim();
            if (pefText.isEmpty()) {
                Toast.makeText(this, "Please enter a peak flow value", Toast.LENGTH_SHORT).show();
                return;
            }

            int pefValue = Integer.parseInt(pefText);
            LocalDateTime now = LocalDateTime.now();
            PeakFlow newPEF = new PeakFlow(pefValue, now);

            hp.getPEFLog().add(newPEF);
            savePeakFlowToFirebase();

            // Clear the input
            etPEF.setText("");

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            if (editPEF != null && pefButton != null) {
                editPEF.setVisibility(View.GONE);
                pefButton.setVisibility(View.VISIBLE);
            }
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
            Log.d(TAG, "Showing prepost check popup");
            prepostCheckPopup.setVisibility(View.VISIBLE);

            // Optional: Blur background
            View mainContent = findViewById(R.id.homePage);
            if (mainContent != null) {
                try {
                    Blurry.with(this).radius(25).sampling(2).onto((ViewGroup) mainContent);
                } catch (Exception e) {
                    Log.e(TAG, "Error blurring background", e);
                }
            }
        } else {
            Log.e(TAG, "Cannot show popup - prepostCheckPopup is null!");
        }
    }

    // FIX: New method to hide popup
    private void hidePrePostCheckPopup() {
        if (prepostCheckPopup != null) {
            Log.d(TAG, "Hiding prepost check popup");
            prepostCheckPopup.setVisibility(View.GONE);

            // Optional: Remove blur
            View mainContent = findViewById(R.id.homePage);
            if (mainContent != null) {
                try {
                    Blurry.delete((ViewGroup) mainContent);
                } catch (Exception e) {
                    Log.e(TAG, "Error removing blur", e);
                }
            }
        }
    }

    // FIX: Single, properly structured saveNewLogWithRating method
    private void saveNewLogWithRating(String rating) {
        Log.d(TAG, "=== SAVING LOG WITH RATING ===");
        Log.d(TAG, "Rating: " + rating);
        Log.d(TAG, "Medicine Label: " + medicineLabel);
        Log.d(TAG, "Medicine Name: " + medicineName);
        Log.d(TAG, "Dosage: " + dosage);

        if (currentChild == null) {
            Log.e(TAG, "Error: Child data not loaded");
            Toast.makeText(this, "Error: Child data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (medicineLabel == null) {
            Log.e(TAG, "Error: Medicine label not set");
            Toast.makeText(this, "Error: Medicine label not set", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            MedicineLabel label = MedicineLabel.valueOf(medicineLabel);

            // Get inventory item
            InventoryItem medicineItem = currentChild.getInventory().getMedicine(label);

            if (medicineItem == null) {
                Log.e(TAG, "Error: Medicine not found in inventory");
                Toast.makeText(this, "Error: Medicine not found in inventory", Toast.LENGTH_SHORT).show();
                return;
            }

            LocalDateTime timestamp = LocalDateTime.now();
            TechniqueQuality quality = (label == MedicineLabel.CONTROLLER) ? TechniqueQuality.HIGH : TechniqueQuality.NA;

            // Create new log with rating
            MedicineUsageLog newLog = new MedicineUsageLog(
                    medicineItem,
                    dosage,
                    timestamp,
                    quality,
                    rating
            );

            Log.d(TAG, "Created new log: " + newLog.toString());

            // Add to appropriate log list
            if (label == MedicineLabel.CONTROLLER) {
                currentChild.getInventory().getControllerLog().add(newLog);
                Log.d(TAG, "Added to controller log");
            } else {
                currentChild.getInventory().getRescueLog().add(newLog);
                Log.d(TAG, "Added to rescue log");
            }

            // Save to Firebase
            saveChildToFirebase(rating);

        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Error: Invalid medicine label", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Invalid medicine label: " + medicineLabel, e);
        } catch (Exception e) {
            Toast.makeText(this, "Error saving log: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error in saveNewLogWithRating", e);
        }
    }

    // FIX: New method to save child data to Firebase
    private void saveChildToFirebase(String rating) {
        if (selectedChildId == null || currentChild == null) {
            Log.e(TAG, "Cannot save - selectedChildId or currentChild is null");
            return;
        }

        Log.d(TAG, "Saving to Firebase...");
        db.collection("users").document(selectedChildId)
                .set(currentChild)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Rating saved: " + rating, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "✓ Child data saved successfully with rating: " + rating);

                    // Clear the medicine data after successful save
                    medicineLabel = null;
                    medicineName = null;
                    dosage = 0;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save rating", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "✗ Failed to save child data", e);
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