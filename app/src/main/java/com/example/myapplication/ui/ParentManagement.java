package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut;
import com.example.myapplication.models.Child;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.chip.ChipGroup;
import java.util.ArrayList;
import java.util.List;


public class ParentManagement extends AppCompatActivity {

    private ImageButton btnAddChild;
    private TextView tvEmptyState;
    private LinearLayout childrenCardsContainer;
    private List<Child> childrenList;
    private SwitchMaterial doctorFilterSwitch;
    private ChipGroup doctorChipGroup;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_management);

        // Initialize views
        btnAddChild = findViewById(R.id.btnAddChild);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        childrenCardsContainer = findViewById(R.id.childrenCardsContainer);
        doctorFilterSwitch = findViewById(R.id.doctorFilterSwitch);
        doctorChipGroup = findViewById(R.id.doctorChipGroup);
        bottomNavigationView = findViewById(R.id.menuBar);

        // Initialize children list
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

        // Add child button click - ONLY if it exists
        if (btnAddChild != null) {
            btnAddChild.setOnClickListener(v -> {
                Intent intent = new Intent(ParentManagement.this, ParentChildEdit.class);
                startActivityForResult(intent, 100);
            });
        }

        // Load children from database (Firebase, etc.)
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

    private void loadChildren() {
        // TODO: Load children from Firebase or local database
        // For now, using empty list

        updateUI();
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
                View cardView = LayoutInflater.from(this).inflate(R.layout.activity_parent_childcard, childrenCardsContainer, false);

                TextView tvChildName = cardView.findViewById(R.id.tvChildName);
                TextView tvBirthday = cardView.findViewById(R.id.tvBirthday);
                TextView tvSpecialNote = cardView.findViewById(R.id.tvSpecialNote);
                CardView childCard = cardView.findViewById(R.id.childCard);

                if (tvChildName != null) {
                    tvChildName.setText(child.getName());
                }
                if (tvBirthday != null) {
                    tvBirthday.setText("Birthday: " + child.getDateOfBirth().toString());
                }
                if (tvSpecialNote != null) {
                    tvSpecialNote.setText("Special Note: " + (child.getHealthProfile().toString().isEmpty() ? "None" : child.getHealthProfile()));
                }

                // Card click listener
                if (childCard != null) {
                    childCard.setOnClickListener(v -> {
                        Intent intent = new Intent(ParentManagement.this, ParentChildEdit.class);
                        intent.putExtra("childId", child.getId());
                        intent.putExtra("childName", child.getName());
                        intent.putExtra("childBirthday", child.getDateOfBirth());
                        intent.putExtra("childNote", child.getHealthProfile().toString());
                        startActivityForResult(intent, 101);
                    });
                }

                childrenCardsContainer.addView(cardView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // Reload children after add/edit
            loadChildren();
        }
    }
}