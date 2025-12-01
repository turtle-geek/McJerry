package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class HomeStepsRecovery extends AppCompatActivity {

    CheckBox step1, step2, step3, step4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_steps_recovery);

        initializeBoxes();
        setUpVideos();
        findViewById(R.id.doneButton).setOnClickListener(v -> {
                    while (!checkBoxes()) {
                        continue;
                    } if (checkBoxes()){
                        finish();
                    }
                });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    void initializeBoxes(){
        step1 = findViewById(R.id.step1);
        step2 = findViewById(R.id.step2);
        step3 = findViewById(R.id.step3);
        step4 = findViewById(R.id.step4);

        step1.setChecked(false);
        step2.setChecked(false);
        step3.setChecked(false);
        step4.setChecked(false);
    }
    void setUpVideos(){
        // Initialize Rescue Inhaler YouTube Player
        YouTubePlayerView rescuePlayer = findViewById(R.id.rescueYoutubePlayer);
        getLifecycle().addObserver(rescuePlayer);

        rescuePlayer.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                String videoId = "LU-pRbN7AD4";
                youTubePlayer.cueVideo(videoId, 60);
            }
        });

        // Initialize Breathing Exercises YouTube Player
        YouTubePlayerView breathingPlayer = findViewById(R.id.breathingPlayer);
        getLifecycle().addObserver(breathingPlayer);

        breathingPlayer.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                String videoId = "FyjZLPmZ534";
                youTubePlayer.cueVideo(videoId, 0);
            }
        });
    }

    boolean checkBoxes(){
        if (step1.isChecked() && step2.isChecked() && step3.isChecked() && step4.isChecked()){
            Toast toast = Toast.makeText(this, "Please complete all steps", Toast.LENGTH_SHORT);
            toast.show();
            return false;
        } else {
            return true;
        }
    }
}