package com.example.myapplication.ui.ChildUI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.PorterDuff;
import com.example.myapplication.R;

import com.example.myapplication.models.*;
import com.example.myapplication.sosButtonResponse;
import com.example.myapplication.ui.StreakThresholdConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class StreakManagement extends AppCompatActivity {

    private TextView tvControllerStreak, tvTechniqueStreak, tvRescueMonthly;
    private ImageButton btnBack, sosbButton;
    private Button btnThresholdConfig;

    private ImageView badgeController, badgeTechnique, badgeRescue;

    private Child child;

    private FirebaseFirestore db;
    private String childId;

    private void loadChild() {
        db.collection("users").document(childId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        child = snapshot.toObject(Child.class);
                        initializeData();
                    }
                });
    }

    private void initializeData() {
        if (child == null) return;

        child.getStreakCount().setInventory(child.getInventory());
        child.getBadges().setStreakCount(child.getStreakCount());

        child.getStreakCount().countStreaks();
        child.getBadges().updateControllerBadge();
        child.getBadges().updateTechniqueBadge();
        child.getBadges().updateRescueBadge();

        updateUI();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_badges);

        btnBack = findViewById(R.id.btnBack);
        sosbButton = findViewById(R.id.sosButton);

        tvControllerStreak = findViewById(R.id.tvControllerStreak);
        tvTechniqueStreak = findViewById(R.id.tvTechniqueStreak);
        tvRescueMonthly = findViewById(R.id.tvRescueMonthly);

        badgeController = findViewById(R.id.badgeController);
        badgeTechnique = findViewById(R.id.badgeTechnique);
        badgeRescue = findViewById(R.id.badgeRescue);
        btnThresholdConfig = findViewById(R.id.btnThresholdConfig);

        // Load child from Firebase here
        db = FirebaseFirestore.getInstance();
        childId = getIntent().getStringExtra("childId");
        loadChild();

        btnBack.setOnClickListener(v -> finish());
        btnThresholdConfig.setOnClickListener(v -> {
            Intent intent = new Intent(this, StreakThresholdConfig.class);
            intent.putExtra("childId", childId);
            startActivityForResult(intent, 1);
        });
        sosbButton.setOnClickListener(v -> {
            sosButtonResponse action = new sosButtonResponse();
            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            action.response(id, this);
        });
    }

    private void updateUI() {
        StreakCount streaks = child.getStreakCount();
        Badges badges = child.getBadges();

        // Numbers
        tvControllerStreak.setText(String.valueOf(streaks.getControllerStreak()));
        tvTechniqueStreak.setText(String.valueOf(streaks.getTechniqueStreak()));
        tvRescueMonthly.setText(String.valueOf(streaks.getRescueCount()));

        // Badges: active = green, inactive = gray
        tintBadge(badgeController, badges.isControllerBadge());
        tintBadge(badgeTechnique, badges.isTechniqueBadge());
        tintBadge(badgeRescue, badges.isRescueBadge());
    }

    private void tintBadge(ImageView imageView, boolean active) {
        int color = active ? Color.parseColor("#064200") : Color.parseColor("#9E9E9E"); // green or grey
        imageView.setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }
}