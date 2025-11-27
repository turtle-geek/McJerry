package com.example.myapplication.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.example.myapplication.auth.SignOut;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import jp.wasabeef.blurry.Blurry;

public class ParentHomeActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNavigationView;
    private ImageButton profileButton;
    private ImageButton bellButton;
    private TextView todayDate;
    private CardView statusCard1, statusCard2, statusCard3, graphCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_parent_home);

        try {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.homePage), (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                return insets;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        profileButton = findViewById(R.id.pfp_logo);
        bellButton = findViewById(R.id.bell);
        todayDate = findViewById(R.id.todayDate);
        statusCard1 = findViewById(R.id.statusCard1);
        statusCard2 = findViewById(R.id.statusCard2);
        statusCard3 = findViewById(R.id.statusCard3);
        graphCard = findViewById(R.id.graphCard);
        bottomNavigationView = findViewById(R.id.menuBar);

        // Set up bottom navigation - ONLY if it exists
        if (bottomNavigationView != null) {
            setupBottomNavigation();
        }

        // Set current date - ONLY if view exists
        if (todayDate != null) {
            setCurrentDate();
        }

        // Set up button listeners
        setupButtonListeners();

        // Set up card listeners
        setupCardListeners();
    }

    private void setupBottomNavigation() {
        try {
            // Set the current item as selected
            bottomNavigationView.setSelectedItemId(R.id.homeButton);

            bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    if (id == R.id.homeButton) {
                        // Already on Parent Home - do nothing
                        return true;

                    } else if (id == R.id.fileButton) {
                        // Navigate to Parent Management
                        startActivity(new Intent(ParentHomeActivity.this, ParentManagement.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.nav_profile) {
                        // Navigate to Parent Tutorial
                        startActivity(new Intent(ParentHomeActivity.this, ParentTutorial.class));
                        overridePendingTransition(0, 0);
                        finish();
                        return true;

                    } else if (id == R.id.moreButton) {
                        // Navigate to Sign Out Page
                        startActivity(new Intent(ParentHomeActivity.this, SignOut.class));
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

    private void setCurrentDate() {
        try {
            // Format current date as "MMM dd, yyyy"
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String currentDate = dateFormat.format(new Date());
            todayDate.setText(currentDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupButtonListeners() {
        // Profile button click
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                // TODO: Navigate to profile page
            });
        }

        // Bell button click (notifications)
        if (bellButton != null) {
            bellButton.setOnClickListener(v -> {
                // TODO: Navigate to notifications page
            });
        }
    }

    private void setupCardListeners() {
        // Status Card 1 - Today's Status
        if (statusCard1 != null) {
            statusCard1.setOnClickListener(v -> {
                // TODO: Navigate to detailed status view
            });
        }

        // Status Card 2 - Last Rescue Time
        if (statusCard2 != null) {
            statusCard2.setOnClickListener(v -> {
                // TODO: Navigate to rescue time details
            });
        }

        // Status Card 3 - Weekly Rescue Time
        if (statusCard3 != null) {
            statusCard3.setOnClickListener(v -> {
                // TODO: Navigate to weekly rescue time view
            });
        }

        // Graph Card - Daily Check-in
        if (graphCard != null) {
            graphCard.setOnClickListener(v -> {
                // TODO: Navigate to daily check-in
            });
        }
    }

    //This method is to block users' access to visit this app's Parent Home Activities,
    // if they don't have an account
    @Override
    protected void onStart(){
        super.onStart();
        try {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if(currentUser == null){
                //people cannot log in
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}