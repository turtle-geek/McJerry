package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut_child;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private TextView todayDate;
    private CardView statusCard1, statusCard2, statusCard3, graphCard;

    // Trend Snippet
    private LinearLayout trendContainer;
    private TrendSnippet trendSnippet;

    // Child data
    private String selectedChildId = null;
    private int selectedChildPersonalBest = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        statusCard1 = findViewById(R.id.statusCard1);
        statusCard2 = findViewById(R.id.statusCard2);
        statusCard3 = findViewById(R.id.statusCard3);
        graphCard = findViewById(R.id.graphCard1);
        bottomNavigationView = findViewById(R.id.menuBar);
        trendContainer = findViewById(R.id.trendContainer);

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
                                Integer peakFlowValue = doc.getLong("peakFlow") != null ?
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
                LocalDateTime time = LocalDateTime.now().minusDays(i);
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
                        // ✅ FIXED: Added curly braces
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
            if (statusCard1 != null) {
                statusCard1.setOnClickListener(v ->
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
        } catch (Exception e) {
            Log.e(TAG, "Error setting up card listeners", e);
        }
    }

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