package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class ParentTutorial extends BaseParentActivity {

    private Button btnFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_technique_help);

        // Get medicine details from intent
        String medicineLabel = getIntent().getStringExtra("medicineLabel");
        String medicineName = getIntent().getStringExtra("medicineName");
        double dosage = getIntent().getDoubleExtra("dosage", 0);

        // Initialize Finish button
        btnFinish = findViewById(R.id.btnSaveMedicine);

        // Set up Finish button click listener
        btnFinish.setOnClickListener(v -> {
            // Go back to ChildHomeActivity and show prepost_check with medicine details
            Intent intent = new Intent(ParentTutorial.this, ChildHomeActivity.class);
            intent.putExtra("SHOW_PREPOST_CHECK", true);
            intent.putExtra("medicineLabel", medicineLabel);
            intent.putExtra("medicineName", medicineName);
            intent.putExtra("dosage", dosage);
            startActivity(intent);
            finish();
        });

        // Initialize Controller Inhaler YouTube Player
        try {
            YouTubePlayerView controllerPlayer = findViewById(R.id.breathingPlayer);
            if (controllerPlayer != null) {
                getLifecycle().addObserver(controllerPlayer);

                controllerPlayer.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                        String videoId = "BbONuRXJdr0";
                        youTubePlayer.cueVideo(videoId, 0);
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}