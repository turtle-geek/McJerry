package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ProviderManagement extends AppCompatActivity {

    private static final String TAG = "ProviderManagement";

    // UI Components
    private TextInputEditText searchEditText;
    private TextView searchResultText;
    private TextView emptyStateText;
    private LinearLayout childrenCardsContainer;
    private ScrollView scrollView;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Data
    private List<ChildData> childrenList;
    private String currentProviderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_management);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup search functionality
        setupSearch();

        // Load children data
        loadChildrenData();
    }

    private void initializeViews() {
        searchEditText = findViewById(R.id.searchEditText);
        searchResultText = findViewById(R.id.searchResultText);
        emptyStateText = findViewById(R.id.emptyStateText);
        childrenCardsContainer = findViewById(R.id.childrenCardsContainer);
        scrollView = findViewById(R.id.scrollView);

        childrenList = new ArrayList<>();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim().toLowerCase();

                if (query.isEmpty()) {
                    // Show all children when search is empty
                    searchResultText.setVisibility(View.GONE);
                    showAllChildren();
                } else {
                    // Search for matching child
                    searchChild(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchChild(String query) {
        boolean found = false;
        View firstMatch = null;

        for (int i = 0; i < childrenCardsContainer.getChildCount(); i++) {
            View childCard = childrenCardsContainer.getChildAt(i);

            // Get child data from tag
            ChildData childData = (ChildData) childCard.getTag();

            if (childData != null) {
                String childId = childData.id.toLowerCase();
                String childName = childData.name.toLowerCase();

                // Check if query matches ID or name
                if (childId.contains(query) || childName.contains(query)) {
                    childCard.setVisibility(View.VISIBLE);

                    if (!found) {
                        firstMatch = childCard;
                        found = true;
                    }
                } else {
                    childCard.setVisibility(View.GONE);
                }
            }
        }

        // Show appropriate message and scroll to first match
        if (!found) {
            searchResultText.setText("The user does not exist");
            searchResultText.setVisibility(View.VISIBLE);
        } else {
            searchResultText.setVisibility(View.GONE);
            if (firstMatch != null) {
                scrollToView(firstMatch);
            }
        }
    }

    private void showAllChildren() {
        for (int i = 0; i < childrenCardsContainer.getChildCount(); i++) {
            childrenCardsContainer.getChildAt(i).setVisibility(View.VISIBLE);
        }
    }

    private void scrollToView(final View view) {
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                int scrollY = view.getTop() - scrollView.getPaddingTop();
                scrollView.smoothScrollTo(0, scrollY);
            }
        });
    }

    private void loadChildrenData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        currentProviderId = currentUser.getUid();

        // Query children who are sharing data with this provider
        db.collection("users")
                .whereEqualTo("role", "child")
                .whereArrayContains("sharedWithProviders", currentProviderId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    childrenList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        // No children sharing data
                        showEmptyState();
                    } else {
                        // Process each child document
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String childId = document.getId();
                            String childName = document.getString("name");
                            String parentId = document.getString("parentID");
                            String email = document.getString("email");
                            String dob = document.getString("dateOfBirth");

                            // Fixed: Now passing parentId parameter
                            ChildData childData = new ChildData(childId, childName, parentId, email, dob);
                            childrenList.add(childData);
                        }

                        // Display children cards
                        displayChildrenCards();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading children data", e);
                    Toast.makeText(this, "Failed to load children data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        emptyStateText.setVisibility(View.VISIBLE);
        childrenCardsContainer.setVisibility(View.GONE);
    }

    private void displayChildrenCards() {
        emptyStateText.setVisibility(View.GONE);
        childrenCardsContainer.setVisibility(View.VISIBLE);
        childrenCardsContainer.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this);

        for (ChildData child : childrenList) {
            // Inflate the card layout
            View childCard = inflater.inflate(R.layout.activity_parent_childcard, childrenCardsContainer, false);

            // Populate the card with data
            populateChildCard(childCard, child);

            // Store child data as tag for search
            childCard.setTag(child);

            // Set click listener
            childCard.setOnClickListener(v -> showChildDetails(child));

            // Add to container
            childrenCardsContainer.addView(childCard);
        }
    }

    private void populateChildCard(View cardView, ChildData child) {
        // Get all three TextViews from the card
        TextView tvChildName = cardView.findViewById(R.id.tvChildName);
        TextView tvBirthday = cardView.findViewById(R.id.tvBirthday);
        TextView tvSpecialNote = cardView.findViewById(R.id.tvSpecialNote);

        // Set child's name
        tvChildName.setText(child.name);

        // Set birthday (date of birth)
        if (child.dob != null && !child.dob.isEmpty()) {
            tvBirthday.setText("Birthday: " + child.dob);
        } else {
            tvBirthday.setText("Birthday: Not provided");
        }

        // Set special note (showing child ID)
        tvSpecialNote.setText("ID: " + child.id);
    }

    private void showChildDetails(ChildData child) {
        // Navigate to detail activity
        Intent intent = new Intent(this, ProviderChildDetails.class);
        intent.putExtra("childId", child.id);
        intent.putExtra("childName", child.name);
        intent.putExtra("parentId", child.parentId);
        intent.putExtra("email", child.email);
        intent.putExtra("dob", child.dob);
        startActivity(intent);
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

    // Inner class to hold child data
    private static class ChildData {
        String id;
        String name;
        String parentId;  // Added parentId field
        String email;
        String dob;

        // Fixed: Added parentId parameter to constructor
        ChildData(String id, String name, String parentId, String email, String dob) {
            this.id = id;
            this.name = name;
            this.parentId = parentId;  // Store parentId
            this.email = email;
            this.dob = dob;
        }
    }
}