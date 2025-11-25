package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;
import com.example.myapplication.models.Child;
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

        // Initialize children list
        childrenList = new ArrayList<>();

        // Set up filter switch
        doctorFilterSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            doctorChipGroup.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Add child button click
        btnAddChild.setOnClickListener(v -> {
            Intent intent = new Intent(ParentManagement.this, ParentChildEdit.class);
            startActivityForResult(intent, 100);
        });

        // Load children from database (Firebase, etc.)
        loadChildren();
    }

    private void loadChildren() {
        // TODO: Load children from Firebase or local database
        // For now, using empty list

        updateUI();
    }

    private void updateUI() {
        if (childrenList.isEmpty()) {
            tvEmptyState.setVisibility(View.VISIBLE);
            childrenCardsContainer.setVisibility(View.GONE);
        } else {
            tvEmptyState.setVisibility(View.GONE);
            childrenCardsContainer.setVisibility(View.VISIBLE);
            displayChildren();
        }
    }

    private void displayChildren() {
        childrenCardsContainer.removeAllViews();

        for (Child child : childrenList) {
            View cardView = LayoutInflater.from(this).inflate(R.layout.activity_parent_childcard, childrenCardsContainer, false);

            TextView tvChildName = cardView.findViewById(R.id.tvChildName);
            TextView tvBirthday = cardView.findViewById(R.id.tvBirthday);
            TextView tvSpecialNote = cardView.findViewById(R.id.tvSpecialNote);
            CardView childCard = cardView.findViewById(R.id.childCard);

            tvChildName.setText(child.getName());
            tvBirthday.setText("Birthday: " + child.getDateOfBirth().toString());
            tvSpecialNote.setText("Special Note: " + (child.getDateOfBirth().toString().isEmpty() ? "None" : child.getHealthProfile()));

            // Card click listener
            childCard.setOnClickListener(v -> {
                Intent intent = new Intent(ParentManagement.this, ParentChildEdit.class);
                intent.putExtra("childId", child.getId());
                intent.putExtra("childName", child.getName());
                intent.putExtra("childBirthday", child.getDateOfBirth());
                intent.putExtra("childNote", child.getDateOfBirth());
                startActivityForResult(intent, 101);
            });

            childrenCardsContainer.addView(cardView);
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