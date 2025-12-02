package com.example.myapplication.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

/** This class is the check-up screen that will show up when the user interacts with the
 *  10-minute check-up notification.
 */
public class CheckupResponseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checkup_response);

        boolean fromNotification = getIntent().getBooleanExtra("fromNotification", false);

        // TODO Layout: Rachel's drawings!!

        if (fromNotification) {
            getSharedPreferences("checkup", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("responded", true)
                    .apply();

            cancelNoResponseAlarm();
            // TODO: Launch Rachel's check up view
            // TODO: If yes, show toast and then return to home
            // TODO: If no, show red card
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cancelNoResponseAlarm(){
        // Match intent of the alarm we want to cancel
        Intent intent = new Intent(this, FullScreenEscalationNotification.class);

        // Wrap in a matching PendingIntent as alarms run on PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        // Get reference to AlarmManager and cancel said alarm
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }
}