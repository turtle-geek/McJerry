package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut;
import com.example.myapplication.models.PeakFlow;
import com.example.myapplication.ui.TrendSnippet;
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

public class ParentHomeActivity extends AppCompatActivity {

    private static final String TAG = "ParentHomeActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private ImageButton profileButton;
    private ImageButton bellButton;
    private TextView todayDate;
    private CardView statusCard1, statusCard2, statusCard3, graphCard;

    // Trend Snippet
    private LinearLayout trendContainer;
    private TrendSnippet trendSnippet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Set up bottom navigation
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }

        // Set current date
        if (todayDate != null) {
            setCurrentDate();
        }

        // Set up button listeners
        setupButtonListeners();

        // Set up card listeners
        setupCardListeners();

        // Setup and load trend snippet
        setupTrendSnippet();
        loadPeakFlowData();
    }

    private void initializeViews() {
        profileButton = findViewById(R.id.pfp_logo);
        bellButton = findViewById(R.id.bell);
        todayDate = findViewById(R.id.todayDate);
        statusCard1 = findViewById(R.id.statusCard1);
        statusCard2 = findViewById(R.id.statusCard2);
        statusCard3 = findViewById(R.id.statusCard3);
        graphCard = findViewById(R.id.graphCard);
        bottomNavigationView = findViewById(R.id.menuBar);
        trendContainer = findViewById(R.id.trendContainer);
    }

    private void setupTrendSnippet() {
        // Create trend snippet
        trendSnippet = new TrendSnippet(this);

        // Add to container
        if (trendContainer != null) {
            trendContainer.removeAllViews();
            trendContainer.addView(trendSnippet);
        }

    }

    private void loadPeakFlowData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in");
            return;
        }

        String parentId = currentUser.getUid();

        // Show loading
        if (trendSnippet != null) {
            trendSnippet.showLoading();
        }

        // Query all children of this parent
        db.collection("users")
                .whereEqualTo("role", "child")
                .whereEqualTo("parentID", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d(TAG, "No children found for parent: " + parentId);
                        // Show empty state with test data for demo
                        loadTestData();
                        return;
                    }

                    List<PeakFlow> allPeakFlows = new ArrayList<>();
                    int[] childrenProcessed = {0}; // Counter for async operations
                    int totalChildren = queryDocumentSnapshots.size();

                    // For each child, get their peak flow data
                    for (QueryDocumentSnapshot childDoc : queryDocumentSnapshots) {
                        String childId = childDoc.getId();
                        Integer personalBest = childDoc.getLong("PEF_PB") != null ?
                                childDoc.getLong("PEF_PB").intValue() : 400;

                        loadChildPeakFlowData(childId, personalBest, allPeakFlows, () -> {
                            childrenProcessed[0]++;
                            if (childrenProcessed[0] == totalChildren) {
                                // All children processed
                                updateTrendSnippet(allPeakFlows);
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children", e);
                    Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    // Load test data on failure
                    loadTestData();
                });
    }

    private void loadChildPeakFlowData(String childId, int personalBest, List<PeakFlow> allPeakFlows, Runnable onComplete) {
        db.collection("users").document(childId)
                .collection("peakFlowLogs")
                .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(30) // Get last 30 entries
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        try {
                            Integer peakFlowValue = doc.getLong("peakFlow") != null ?
                                    doc.getLong("peakFlow").intValue() : 0;
                            com.google.firebase.Timestamp timestamp = doc.getTimestamp("time");

                            if (timestamp != null && peakFlowValue > 0) {
                                // Convert Firebase Timestamp to LocalDateTime
                                Date date = timestamp.toDate();
                                LocalDateTime localDateTime = LocalDateTime.ofInstant(
                                        date.toInstant(),
                                        java.time.ZoneId.systemDefault()
                                );

                                PeakFlow pf = new PeakFlow(peakFlowValue, localDateTime);

                                // Compute zone
                                if (peakFlowValue >= 0.8 * personalBest) {
                                    pf.setZone("green");
                                } else if (peakFlowValue >= 0.5 * personalBest) {
                                    pf.setZone("yellow");
                                } else {
                                    pf.setZone("red");
                                }

                                allPeakFlows.add(pf);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error parsing peak flow data", e);
                        }
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading peak flow data for child: " + childId, e);
                    onComplete.run();
                });
    }

    private void updateTrendSnippet(List<PeakFlow> peakFlows) {
        if (trendSnippet != null) {
            if (peakFlows.isEmpty()) {
                // Load test data if no real data
                loadTestData();
            } else {
                trendSnippet.setData(peakFlows);
            }
        }
    }

    private void loadTestData() {
        // Create test data for demonstration
        List<PeakFlow> testData = new ArrayList<>();

        for (int i = 0; i < 30; i++) {
            LocalDateTime time = LocalDateTime.now().minusDays(i);
            // Generate random peak flow between 250-400
            int value = 250 + (int)(Math.random() * 150);

            PeakFlow pf = new PeakFlow(value, time);

            // Compute zone based on value
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
    }

    private void setupBottomNavigation() {
        try {
            bottomNavigationView.setSelectedItemId(R.id.homeButton);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        return true;
                    } else if (id == R.id.fileButton) {
                        startActivity(new Intent(ParentHomeActivity.this, ParentManagement.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(ParentHomeActivity.this, ParentTutorial.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    } else if (id == R.id.moreButton) {
                        startActivity(new Intent(ParentHomeActivity.this, SignOut.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    }
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCurrentDate() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            todayDate.setText(currentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonListeners() {
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                // TODO: Navigate to profile page
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
            });
        }

        if (bellButton != null) {
            bellButton.setOnClickListener(v -> {
                // TODO: Navigate to notifications page
                Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void setupCardListeners() {
        if (statusCard1 != null) {
            statusCard1.setOnClickListener(v -> {
                Toast.makeText(this, "Today's Status", Toast.LENGTH_SHORT).show();
            });
        }

        if (statusCard2 != null) {
            statusCard2.setOnClickListener(v -> {
                Toast.makeText(this, "Last Rescue Time", Toast.LENGTH_SHORT).show();
            });
        }

        if (statusCard3 != null) {
            statusCard3.setOnClickListener(v -> {
                Toast.makeText(this, "Weekly Rescue Time", Toast.LENGTH_SHORT).show();
            });
        }

        if (graphCard != null) {
            graphCard.setOnClickListener(v -> {
                Toast.makeText(this, "Daily Check-in", Toast.LENGTH_SHORT).show();
            });
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
            e.printStackTrace();
        }
    }
}