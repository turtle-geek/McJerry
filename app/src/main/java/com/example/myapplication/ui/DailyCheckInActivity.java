package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyCheckInActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Load the UI (R.layout.activity_daily_check_in must exist)
        setContentView(R.layout.activity_daily_check_in);

        // 2. Call the function to set the date
        setDynamicDate();
    }

    /**
     * Finds the header TextView and updates it with the current date.
     */
    private void setDynamicDate() {
        try {
            // Find the TextView element using its ID from the XML
            TextView dateTextView = findViewById(R.id.headerDateText);

            // Get the current date and format it (e.g., "November 24, 2025")
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String formattedDate = sdf.format(new Date());

            // Set the dynamic text
            dateTextView.setText("Please complete this entry for " + formattedDate + ".");

        } catch (Exception e) {
            // This will catch the error if R.id.headerDateText is missing,
            // preventing the app from crashing.
            e.printStackTrace();
        }
    }
}
