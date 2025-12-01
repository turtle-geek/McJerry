package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ParentManagement extends AppCompatActivity {

    private static final String TAG = "ParentManagement";

    private ImageButton btnAddChild;
    private TextView tvEmptyState;
    private LinearLayout childrenCardsContainer;
    private List<Child> childrenList;
    private SwitchMaterial doctorFilterSwitch;
    private ChipGroup doctorChipGroup;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentParentId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_management);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        btnAddChild = findViewById(R.id.btnAddChild);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        childrenCardsContainer = findViewById(R.id.childrenCardsContainer);
        doctorFilterSwitch = findViewById(R.id.doctorFilterSwitch);
        doctorChipGroup = findViewById(R.id.doctorChipGroup);
        bottomNavigationView = findViewById(R.id.menuBar);

        // Initialize children list
        // TODO what does this do?
        childrenList = new ArrayList<>();

        // Set up bottom navigation - ONLY if it exists
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }

        // Set up filter switch - ONLY if it exists
        if (doctorFilterSwitch != null && doctorChipGroup != null) {
            doctorFilterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                doctorChipGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            });
        }

        // Add child button click - Navigate to registration
        if (btnAddChild != null) {
            btnAddChild.setOnClickListener(v -> {
                Intent intent = new Intent(ParentManagement.this, ParentRegisterChild.class);
                intent.putExtra("parentID", currentParentId);
                startActivity(intent);
            });
        }

        // Check if returning from successful child addition
        if (getIntent().getBooleanExtra("childAdded", false)) {
            Toast.makeText(this, "Child added successfully!", Toast.LENGTH_SHORT).show();
        }

        // Load children from Firebase
        loadChildren();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload children when returning to this activity
        loadChildren();
    }

    private void setupBottomNavigation() {
        try {
            // Set the current item as selected
            bottomNavigationView.setSelectedItemId(R.id.fileButton);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        // Navigate to Parent Home
                        startActivity(new Intent(ParentManagement.this, ParentHomeActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.fileButton) {
                        // Already on Parent Management - do nothing
                        return true;

                    } else if (id == R.id.nav_profile) {
                        // Navigate to Parent Tutorial
                        startActivity(new Intent(ParentManagement.this, ParentTutorial.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.moreButton) {
                        // Navigate to Sign Out Page
                        startActivity(new Intent(ParentManagement.this, SignOut.class));
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

    // TODO What does this do?
    private void loadChildren() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        currentParentId = currentUser.getUid();

        // Query children where parentID matches current user
        db.collection("users")
                .whereEqualTo("role", "child")
                .whereEqualTo("parentID", currentParentId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        // No children found
                        updateUI();
                    } else {
                        // Process each child document
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String childId = document.getId();
                            String childName = document.getString("name");
                            String id = document.getString("id");
                            String username = document.getString("emailUsername");
                            String dob = document.getString("dateOfBirth");
                            String notes = document.getString("notes");

                            Child child = new Child(childId, currentParentId, childName, username, "child");
                            child.setDOB(dob);
                            child.setNotes(notes);

                            childrenList.add(child);
                        }

                        // Display children cards
                        updateUI();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children", e);
                    Toast.makeText(this, "Failed to load children: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUI() {
        if (tvEmptyState != null && childrenCardsContainer != null) {
            if (childrenList.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                childrenCardsContainer.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                childrenCardsContainer.setVisibility(View.VISIBLE);
                displayChildren();
            }
        }
    }

    private void displayChildren() {
        if (childrenCardsContainer == null) return;

        childrenCardsContainer.removeAllViews();

        for (Child child : childrenList) {
            try {
                View cardView = LayoutInflater.from(this).inflate(
                        R.layout.activity_parent_childcard,
                        childrenCardsContainer,
                        false);

                TextView tvChildName = cardView.findViewById(R.id.tvChildName);
                TextView tvBirthday = cardView.findViewById(R.id.tvBirthday);
                TextView tvSpecialNote = cardView.findViewById(R.id.tvSpecialNote);
                CardView childCard = cardView.findViewById(R.id.childCard);

                if (tvChildName != null) {
                    tvChildName.setText(child.getName());
                }

                if (tvBirthday != null) {
                    if (child.getDateOfBirth() != null) {
                        tvBirthday.setText("Birthday: " + child.getDateOfBirth());
                    } else {
                        tvBirthday.setText("Birthday: Not provided");
                    }
                }

                if (tvSpecialNote != null) {
                    if (child.getNotes() != null && !child.getNotes().isEmpty()) {
                        tvSpecialNote.setText("Special Note: " + child.getNotes());
                    } else {
                        tvSpecialNote.setText("Special Note: None");
                    }
                }

                // Card click listener - Navigate to child details
                if (childCard != null) {
                    childCard.setOnClickListener(v -> {
                        Intent intent = new Intent(ParentManagement.this, ParentChildDetails.class);
                        intent.putExtra("childId", child.getId());
                        intent.putExtra("childName", child.getName());
                        intent.putExtra("childUserId", child.getId());
                        // TODO remove
                        // intent.putExtra("childPassword", child.password);  // ✅ ADDED - Pass password
                        intent.putExtra("childBirthday", child.getDateOfBirth());
                        intent.putExtra("childNote", child.getNotes());
                        startActivity(intent);
                    });
                }

                childrenCardsContainer.addView(cardView);
            } catch (Exception e) {
                Log.e(TAG, "Error displaying child card", e);
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}