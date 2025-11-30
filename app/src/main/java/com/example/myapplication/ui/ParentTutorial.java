package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

// Extend BaseParentActivity for auto-logout functionality
public class ParentTutorial extends BaseParentActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_technique_help);

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.menuBar);

        // Set up bottom navigation - ONLY if it exists
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }

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

    private void setupBottomNavigation() {
        try {
            // Set the current item as selected
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        // Navigate to Parent Home
                        startActivity(new Intent(ParentTutorial.this, ParentHomeActivity.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.fileButton) {
                        // Navigate to Parent Management
                        startActivity(new Intent(ParentTutorial.this, ParentManagement.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.nav_profile) {
                        // Already on Parent Tutorial - do nothing
                        return true;

                    } else if (id == R.id.moreButton) {
                        // Navigate to Sign Out Page
                        startActivity(new Intent(ParentTutorial.this, SignOut.class));
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
}