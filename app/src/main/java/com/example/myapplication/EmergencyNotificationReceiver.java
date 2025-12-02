package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.myapplication.ui.FullScreenEscalationNotification;

public class EmergencyNotificationReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        // Notification channel
        String channelId = "checkup_channel";
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    channelId,
                    "Escalation Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(context, FullScreenEscalationNotification.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification
        @SuppressLint("FullScreenIntentPolicy") NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Escalating Asthma Attack Response")
                        .setContentText("We are calling 911 because you did not respond to the check-up.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setFullScreenIntent(pendingIntent, true);

        notificationManager.notify(1002, builder.build());
    }
}
