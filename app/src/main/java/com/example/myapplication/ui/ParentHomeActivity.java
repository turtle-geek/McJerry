package com.example.myapplication.ui;

import android.app.AlertDialog;
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
    private ImageButton bellButton;
    private TextView todayDate, selectedChildName;
    private CardView statusCard1, statusCard2, statusCard3, graphCard;
    private LinearLayout childSelectorLayout;

    // Trend Snippet
    private LinearLayout trendContainer;
    private TrendSnippet trendSnippet;

    // Child data
    private List<ChildInfo> childrenList;
    private String selectedChildId = null;
    private String selectedChildNameStr = "Select a child";
    private int selectedChildPersonalBest = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        childrenList = new ArrayList<>();

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

        // Setup child selector
        setupChildSelector();

        // Setup trend snippet
        setupTrendSnippet();

        // Load children list
        loadChildrenList();
    }

    private void initializeViews() {
        bellButton = findViewById(R.id.bell);
        todayDate = findViewById(R.id.todayDate);
        selectedChildName = findViewById(R.id.selectedChildName);
        childSelectorLayout = findViewById(R.id.childSelectorLayout);
        statusCard1 = findViewById(R.id.statusCard1);
        statusCard2 = findViewById(R.id.statusCard2);
        statusCard3 = findViewById(R.id.statusCard3);
        graphCard = findViewById(R.id.graphCard);
        bottomNavigationView = findViewById(R.id.menuBar);
        trendContainer = findViewById(R.id.trendContainer);
    }

    private void setupChildSelector() {
        if (childSelectorLayout != null) {
            childSelectorLayout.setOnClickListener(v -> showChildSelectionDialog());
        }
    }

    private void loadChildrenList() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "No user logged in");
            return;
        }

        String parentId = currentUser.getUid();

        // Query all children of this parent
        db.collection("users")
                .whereEqualTo("role", "child")
                .whereEqualTo("parentID", parentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String childId = doc.getId();
                        String childName = doc.getString("name");
                        Integer personalBest = doc.getLong("PEF_PB") != null ?
                                doc.getLong("PEF_PB").intValue() : 400;

                        childrenList.add(new ChildInfo(childId, childName, personalBest));
                    }

                    Log.d(TAG, "Loaded " + childrenList.size() + " children");

                    // If only one child, auto-select them
                    if (childrenList.size() == 1) {
                        ChildInfo child = childrenList.get(0);
                        selectedChildId = child.id;
                        selectedChildNameStr = child.name;
                        selectedChildPersonalBest = child.personalBest;

                        if (selectedChildName != null) {
                            selectedChildName.setText(selectedChildNameStr);
                        }

                        loadPeakFlowData();
                    } else if (childrenList.isEmpty()) {
                        // No children - show empty state
                        if (selectedChildName != null) {
                            selectedChildName.setText("No children registered");
                        }
                        loadTestData();
                    } else {
                        // Multiple children - show prompt to select
                        if (selectedChildName != null) {
                            selectedChildName.setText("Select a child");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children list", e);
                    Toast.makeText(this, "Failed to load children", Toast.LENGTH_SHORT).show();
                    loadTestData();
                });
    }

    private void showChildSelectionDialog() {
        if (childrenList.isEmpty()) {
            Toast.makeText(this, "No children registered yet", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create array of child names
        List<String> names = new ArrayList<>();
        for (ChildInfo child : childrenList) {
            names.add(child.name);
        }

        String[] nameArray = names.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Child");
        builder.setItems(nameArray, (dialog, which) -> {
            // Specific child selected
            ChildInfo selectedChild = childrenList.get(which);
            selectedChildId = selectedChild.id;
            selectedChildNameStr = selectedChild.name;
            selectedChildPersonalBest = selectedChild.personalBest;

            // Update UI
            if (selectedChildName != null) {
                selectedChildName.setText(selectedChildNameStr);
            }

            // Reload data for selected child
            loadPeakFlowData();

            Toast.makeText(this, "Showing data for: " + selectedChildNameStr, Toast.LENGTH_SHORT).show();
        });

        builder.show();
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
        if (selectedChildId == null) {
            Log.w(TAG, "No child selected");
            if (trendSnippet != null) {
                trendSnippet.showLoading();
            }
            return;
        }

        // Show loading
        if (trendSnippet != null) {
            trendSnippet.showLoading();
        }

        // Load data for the selected child
        List<PeakFlow> peakFlows = new ArrayList<>();
        loadChildPeakFlowData(selectedChildId, selectedChildPersonalBest, peakFlows, () -> {
            updateTrendSnippet(peakFlows);
        });
    }

    private void loadChildPeakFlowData(String childId, int personalBest, List<PeakFlow> allPeakFlows, Runnable onComplete) {
        db.collection("users").document(childId)
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

                                // Compute zone based on personal best
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
                // Show test data if no real data
                loadTestData();
            } else {
                trendSnippet.setData(peakFlows);
            }
        }
    }

    private void loadTestData() {
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
            Log.e(TAG, "Error setting up navigation", e);
        }
    }

    private void setCurrentDate() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            todayDate.setText(currentDate);
        } catch (Exception e) {
            Log.e(TAG, "Error setting date", e);
        }
    }

    private void setupButtonListeners() {
        if (bellButton != null) {
            bellButton.setOnClickListener(v -> {
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
            Log.e(TAG, "Error in onStart", e);
        }
    }

    // Inner class to hold child information
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