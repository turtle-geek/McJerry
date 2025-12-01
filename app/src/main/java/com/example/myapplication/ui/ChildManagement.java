package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut_child;
import com.example.myapplication.models.Child;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Child Management Activity
 * - Shows ONLY child's own profile
 * - Card IS CLICKABLE to view details
 * - NO edit permission (read-only)
 */
public class ChildManagement extends AppCompatActivity {

    private static final String TAG = "ChildManagement";

    private TextView tvEmptyState;
    private LinearLayout childrenCardsContainer;
    private Child currentChild;
    private BottomNavigationView bottomNavigationView;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentChildId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_child_management);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Set up bottom navigation
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }

        // Load current child's profile
        loadCurrentChildProfile();
    }

    private void initializeViews() {
        tvEmptyState = findViewById(R.id.tvEmptyState);
        childrenCardsContainer = findViewById(R.id.childrenCardsContainer);
        bottomNavigationView = findViewById(R.id.menuBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload profile when returning to this activity
        loadCurrentChildProfile();
    }

    private void setupBottomNavigation() {
        try {
            bottomNavigationView.setSelectedItemId(R.id.fileButton);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        startActivity(new Intent(ChildManagement.this, ChildHomeActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.fileButton) {
                        return true;

                    } else if (id == R.id.nav_profile) {
                        startActivity(new Intent(ChildManagement.this, HomeStepsRecovery.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.moreButton) {
                        // ✅ Navigate to SignOut_child (not SignOut!)
                        startActivity(new Intent(ChildManagement.this, SignOut_child.class));
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

    /**
     * Load current child's own profile
     */
    private void loadCurrentChildProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        currentChildId = currentUser.getUid();

        // Query the current child's document
        db.collection("users")
                .document(currentChildId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String childId = documentSnapshot.getId();
                        String childName = documentSnapshot.getString("name");
                        String parentId = documentSnapshot.getString("parentID");
                        String username = documentSnapshot.getString("emailUsername");
                        String email = documentSnapshot.getString("email");
                        String dob = documentSnapshot.getString("dateOfBirth");
                        String notes = documentSnapshot.getString("notes");
                        String role = documentSnapshot.getString("role");

                        // Create Child object
                        currentChild = new Child(childId, parentId, childName, email, role);
                        currentChild.setDOB(dob);
                        currentChild.setNotes(notes);

                        // Display the profile
                        updateUI(true);
                        displayChildProfile();

                        Log.d(TAG, "Loaded child profile: " + childName);
                    } else {
                        Log.e(TAG, "Child profile not found in database");
                        updateUI(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading child profile", e);
                    Toast.makeText(this, "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    updateUI(false);
                });
    }

    private void updateUI(boolean hasProfile) {
        if (tvEmptyState != null && childrenCardsContainer != null) {
            if (!hasProfile) {
                tvEmptyState.setText("Profile not found. Please contact your parent.");
                tvEmptyState.setVisibility(View.VISIBLE);
                childrenCardsContainer.setVisibility(View.GONE);
            } else {
                tvEmptyState.setVisibility(View.GONE);
                childrenCardsContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Display child's profile card - CLICKABLE to view details
     */
    private void displayChildProfile() {
        if (childrenCardsContainer == null || currentChild == null) return;

        childrenCardsContainer.removeAllViews();

        try {
            // Use the same card layout as parent
            View cardView = LayoutInflater.from(this).inflate(
                    R.layout.activity_parent_childcard,
                    childrenCardsContainer,
                    false);

            TextView tvChildName = cardView.findViewById(R.id.tvChildName);
            TextView tvBirthday = cardView.findViewById(R.id.tvBirthday);
            TextView tvSpecialNote = cardView.findViewById(R.id.tvSpecialNote);
            CardView childCard = cardView.findViewById(R.id.childCard);

            // Set child's name
            if (tvChildName != null) {
                tvChildName.setText(currentChild.getName());
            }

            // Set birthday
            if (tvBirthday != null) {
                if (currentChild.getDateOfBirth() != null && !currentChild.getDateOfBirth().isEmpty()) {
                    tvBirthday.setText("Birthday: " + currentChild.getDateOfBirth());
                } else {
                    tvBirthday.setText("Birthday: Not provided");
                }
            }

            // Set special note
            if (tvSpecialNote != null) {
                if (currentChild.getNotes() != null && !currentChild.getNotes().isEmpty()) {
                    tvSpecialNote.setText("Special Note: " + currentChild.getNotes());
                } else {
                    tvSpecialNote.setText("Special Note: None");
                }
            }

            // ✅ Child CAN click to VIEW details (but not edit)
            if (childCard != null) {
                childCard.setOnClickListener(v -> {
                    // Navigate to ChildChildDetails to view profile
                    Intent intent = new Intent(ChildManagement.this, ChildChildDetails.class);

                    // Pass child data
                    intent.putExtra("childId", currentChild.getId());
                    intent.putExtra("childName", currentChild.getName());
                    intent.putExtra("childUserId", currentChild.getId());
                    intent.putExtra("childEmail", currentChild.getEmailUsername());
                    intent.putExtra("childBirthday", currentChild.getDateOfBirth());
                    intent.putExtra("childNote", currentChild.getNotes());
                    // Don't pass password for security

                    startActivity(intent);
                });
            }

            // Add the card to container
            childrenCardsContainer.addView(cardView);

        } catch (Exception e) {
            Log.e(TAG, "Error displaying child profile card", e);
            Toast.makeText(this, "Error displaying profile", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}