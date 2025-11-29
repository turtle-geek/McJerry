package com.example.myapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.PorterDuff;
import com.example.myapplication.R;

import com.example.myapplication.health.*;
import com.example.myapplication.models.*;

public class StreakManagement extends AppCompatActivity {

    private TextView tvControllerStreak, tvTechniqueStreak, tvRescueMonthly;
    private ImageButton btnBack;

    private ImageView badgeController, badgeTechnique, badgeRescue;

    private Child child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_streak_badges);

        btnBack = findViewById(R.id.btnBack);
        tvControllerStreak = findViewById(R.id.tvControllerStreak);
        tvTechniqueStreak = findViewById(R.id.tvTechniqueStreak);
        tvRescueMonthly = findViewById(R.id.tvRescueMonthly);

        badgeController = findViewById(R.id.badgeController);
        badgeTechnique = findViewById(R.id.badgeTechnique);
        badgeRescue = findViewById(R.id.badgeRescue);

        // TODO: Load child from Firebase here
        child.getStreakCount().setInventory(child.getInventory());
        child.getBadges().setStreakCount(child.getStreakCount());

        child.getStreakCount().countStreaks();
        child.getBadges().updateControllerBadge();
        child.getBadges().updateTechniqueBadge();
        child.getBadges().updateRescueBadge();

        updateUI();

        btnBack.setOnClickListener(v -> finish());
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