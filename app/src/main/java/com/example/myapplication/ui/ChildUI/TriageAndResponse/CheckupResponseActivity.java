package com.example.myapplication.ui.ChildUI.TriageAndResponse;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.ui.ChildUI.TriageAndResponse.TriageActivity;
import com.example.myapplication.ui.ChildUI.TriageAndResponse.FullScreenEscalationNotification;


import jp.wasabeef.blurry.Blurry;

public class CheckupResponseActivity extends AppCompatActivity {

    private static final String TAG = "CheckupResponseActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkup_response);

        String userId = getIntent().getStringExtra("id");

        boolean fromNotification = getIntent().getBooleanExtra("fromNotification", false);
        Log.d(TAG, "Activity created. From Notification: " + fromNotification);

        View mainContent = findViewById(R.id.homePage);
        if (mainContent != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Blurry.with(this).radius(25).sampling(2).onto((ViewGroup) mainContent);
        }

        if (fromNotification) {
            getSharedPreferences("checkup", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("responded", true)
                    .apply();

            cancelNoResponseAlarm();
        }

        setListeners(userId);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setListeners(String userId) {
        ImageButton better, same, worse;
        better = findViewById(R.id.better);
        same = findViewById(R.id.same);
        worse = findViewById(R.id.Worse);

        better.setOnClickListener(v -> {
            Toast.makeText(
                            this,
                            "Awesome! Sending you back to home page",
                            Toast.LENGTH_LONG)
                    .show();
            finish();
        });

        same.setOnClickListener(v -> {
            Intent intent = new Intent(this, TriageActivity.class);
            if (userId != null) {
                intent.putExtra("id", userId);
            }
            startActivity(intent);
            finish();
        });

        worse.setOnClickListener(v -> {
            Intent intent = new Intent(this, TriageActivity.class);
            if (userId != null) {
                intent.putExtra("id", userId);
            }
            startActivity(intent);
            finish();
        });
    }

    private void cancelNoResponseAlarm(){
        Intent intent = new Intent(this, FullScreenEscalationNotification.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "No response alarm cancelled.");
        } else {
            Log.e(TAG, "AlarmManager service is null.");
        }
    }
}