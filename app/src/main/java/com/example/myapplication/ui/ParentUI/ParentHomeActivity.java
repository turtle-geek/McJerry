package com.example.myapplication.ui.ParentUI;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
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
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;
import com.example.myapplication.ui.Onboarding;
import com.example.myapplication.ui.TrendSnippet;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParentHomeActivity extends AppCompatActivity {

    private static final String TAG = "ParentHomeActivity";

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private ImageButton bellButton;
    private TextView todayDate, selectedChildName, pefDisplay, pefDateTime;

    // NOTE: pefCard refers to the Status Card 1 (Today's Peak Flow)
    private CardView pefCard, statusCard2, statusCard3, graphCard2; // graphCard renamed to graphCard2 to match XML ID

    private LinearLayout childSelectorLayout;
    private LinearLayout trendContainer;

    // NOTE: TrendSnippet must exist in your project
    private TrendSnippet trendSnippet;

    // Child data
    private List<Child> childrenList;
    private String selectedChildId = null;
    private String selectedChildNameStr = "Select a child";
    private int selectedChildPersonalBest;

    private String parentUserId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        childrenList = new ArrayList<>();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User is not authenticated. Redirecting to router.");
            Toast.makeText(this, "Session expired, please sign in.", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        parentUserId = currentUser.getUid();

        initializeViews();

        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }

        if (todayDate != null) {
            setCurrentDate();
        }

        setupButtonListeners();
        setupCardListeners();
        setupChildSelector();
        setupTrendSnippet();

        loadChildrenList();
    }

    private void initializeViews() {
        bellButton = findViewById(R.id.bell);
        todayDate = findViewById(R.id.todayDate);
        selectedChildName = findViewById(R.id.selectedChildName);
        childSelectorLayout = findViewById(R.id.childSelectorLayout);

        // FIX: pefCard maps to the first status card in the XML which is R.id.pefCard
        pefCard = findViewById(R.id.pefCard);
//        statusCard2 = findViewById(R.id.statusCard2);
//        statusCard3 = findViewById(R.id.statusCard3);

        // FIX: graphCard refers to R.id.graphCard2 in the XML
        graphCard2 = findViewById(R.id.graphCard2);

        bottomNavigationView = findViewById(R.id.menuBar);
        trendContainer = findViewById(R.id.trendContainer);

        // FIX: pefDisplay and pefDateTime must be found within the pefCard layout, but the XML gives them unique IDs, so this is okay.
        pefDisplay = findViewById(R.id.pefDisplay);
        pefDateTime = findViewById(R.id.pefDateTime);
    }

    private void setupChildSelector() {
        if (childSelectorLayout != null) {
            childSelectorLayout.setOnClickListener(v -> showChildSelectionDialog());
        }
    }

    private void loadChildrenList() {
        if (parentUserId == null) {
            Log.w(TAG, "Cannot load children: parentUserId is null.");
            return;
        }

        db.collection("users")
                .whereEqualTo("role", "child")
                .whereEqualTo("parentID", parentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Child currentChild = doc.toObject(Child.class);
                        childrenList.add(currentChild);
                    }

                    Log.d(TAG, "Loaded " + childrenList.size() + " children");

                    // If only one child, auto-select them
                    if (childrenList.size() >= 1) {
                        // FIX: Automatically select the first child if none is selected
                        Child child = childrenList.get(0);
                        selectedChildId = child.getId();
                        selectedChildNameStr = child.getName();

                        HealthProfile hp = child.getHealthProfile();
                        if (hp!= null)
                            selectedChildPersonalBest = hp.getPEF_PB();
                        else
                            selectedChildPersonalBest = 400;

                        if (selectedChildName != null) {
                            selectedChildName.setText(selectedChildNameStr);
                        }


                        // Load data for the selected child
                        loadChildDataAndDisplayUI();
                    } else { // childrenList.isEmpty()
                        if (selectedChildName != null) {
                            selectedChildName.setText("No children registered");
                        }
                        // FIX: Use test data only if no children are registered AND no specific child is selected
                        loadTestData();
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

        List<String> names = new ArrayList<>();
        for (Child child : childrenList) {
            names.add(child.getName());
        }

        String[] nameArray = names.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Child");
        builder.setItems(nameArray, (dialog, which) -> {
            // Specific child selected
            Child selectedChild = childrenList.get(which);
            selectedChildNameStr = selectedChild.getName();

            if (selectedChildName != null) {
                selectedChildName.setText(selectedChildNameStr);
            }

            // Load data for the newly selected child
            loadChildDataAndDisplayUI();

            Toast.makeText(this, "Showing data for: " + selectedChildNameStr,
                    Toast.LENGTH_SHORT).show();
        });

        builder.show();
    }

    /**
     * FIX: Primary method to load all child data (PEF, Trends) from Firebase.
     */
    private void loadChildDataAndDisplayUI() {
        if (selectedChildId == null) {
            pefDisplay.setText("---");
            pefDateTime.setText("No child selected");
            if (trendSnippet != null) {
                trendSnippet.showLoading();
            }
            return;
        }

        db.collection("users").document(selectedChildId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        loadTestData();
                        return;
                    }

                    Child child = documentSnapshot.toObject(Child.class);
                    if (child == null) {
                        loadTestData();
                        return;
                    }

                    HealthProfile hp = child.getHealthProfile();
                    if (hp != null && hp.getPEFLog() != null) {
                        List<PeakFlow> peakFlows = hp.getPEFLog();

                        // Sort by time descending to find latest (for Today's PEF)
                        peakFlows.sort(Comparator.comparing(PeakFlow::getTime, Comparator.nullsLast(Comparator.reverseOrder())));

                        // Display latest PEF data
                        if (!peakFlows.isEmpty()) {
                            updatePeakFlowUI(peakFlows.get(0));
                        } else {
                            pefDisplay.setText("N/A");
                            pefDateTime.setText("No PEF data");
                        }

                        // Display trend data
                        updateTrendSnippet(peakFlows);

                    } else {
                        pefDisplay.setText("N/A");
                        pefDateTime.setText("No PEF data");
                        updateTrendSnippet(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading child data for " + selectedChildId, e);
                    loadTestData();
                });
    }


    private void updatePeakFlowUI(PeakFlow todayPeakFlow) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        pefDisplay.setText(String.valueOf(todayPeakFlow.getPeakFlow()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a, MMM d");
        pefDateTime.setText(todayPeakFlow.getTime().format(formatter));

        if (pefCard != null) {
            String zone = todayPeakFlow.getZone() != null ? todayPeakFlow.getZone().toLowerCase() : "";

            switch (zone) {
                case "green":
                    pefCard.setCardBackgroundColor(Color.parseColor("#008000")); // Green
                    break;
                case "yellow":
                    pefCard.setCardBackgroundColor(Color.parseColor("#FFD700")); // Yellow
                    break;
                case "red":
                    pefCard.setCardBackgroundColor(Color.parseColor("#FF0000")); // Red
                    break;
                default:
                    pefCard.setCardBackgroundColor(Color.parseColor("#ABFFFFFF")); // Default background
            }
        }
    }

    private void setupTrendSnippet() {
        // NOTE: Assuming TrendSnippet class is defined elsewhere and handles its own loading state.
        trendSnippet = new TrendSnippet(this);

        if (trendContainer != null) {
            trendContainer.removeAllViews();
            trendContainer.addView(trendSnippet);
        }
    }

    // FIX: Simplified loadPeakFlowData to act as a trigger, relying on loadChildDataAndDisplayUI
    private void loadPeakFlowData() {
        loadChildDataAndDisplayUI();
    }

    // FIX: Removed duplicated/incorrect loadChildPeakFlowData method and used the data from the HealthProfile log.

    private void updateTrendSnippet(List<PeakFlow> peakFlows) {
        if (trendSnippet != null) {
            if (peakFlows.isEmpty()) {
                // If data is empty, use test data logic
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
            pf.setZone("yellow"); // Default zone for test data

            testData.add(pf);
        }

        if (trendSnippet != null) {
            trendSnippet.setData(testData);
        }

        // Also update the static PEF card with latest test data
        if (!testData.isEmpty()) {
            updatePeakFlowUI(testData.get(0));
        }
    }

    private void setupBottomNavigation() {
        try {
            if (bottomNavigationView == null) return;

            bottomNavigationView.setSelectedItemId(R.id.homeButton);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    Intent intent;

                    if (id == R.id.homeButton) {
                        return true;
                    } else if (id == R.id.fileButton) {
                        intent = new Intent(ParentHomeActivity.this, ParentManagement.class);
                        intent.putExtra("id", parentUserId);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        return true;
                    } else if (id == R.id.nav_profile) {
                        // Assuming Onboarding is a placeholder for profile settings/dashboard
                        intent = new Intent(ParentHomeActivity.this, Onboarding.class);
                        startActivity(intent);
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
            if (todayDate == null) return;

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
        if (pefCard != null) {
            pefCard.setOnClickListener(v -> {
                Toast.makeText(this, "Today's Zone", Toast.LENGTH_SHORT).show();
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

        if (graphCard2 != null) {
            graphCard2.setOnClickListener(v -> {
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

            // FIX: Only reload the list if it's empty to prevent unnecessary Firebase reads
            if (childrenList.isEmpty()) {
                loadChildrenList();
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onStart", e);
        }
    }

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