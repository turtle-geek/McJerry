package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Onboarding extends AppCompatActivity {
    private int currentScreen = 0;
    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;

    // Array of onboarding layout resources
    private final int[] onboardingLayouts = {
            R.layout.onboarding1,
            R.layout.onboarding2,
            R.layout.onboarding3,
            R.layout.onboarding4,
            R.layout.onboarding5,
            R.layout.onboarding6,
            R.layout.onboarding7
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fStore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        // Show first onboarding screen
        showScreen(currentScreen);
    }

    private void showScreen(int screenIndex) {
        // Set the content view to the current onboarding screen
        setContentView(onboardingLayouts[screenIndex]);

        // Find the next button in the current layout (assuming each layout has a button)
        // You may need to adjust the button ID based on your actual layouts
        Button nextButton = findViewById(R.id.nextButton);
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                if (currentScreen < onboardingLayouts.length - 1) {
                    // Move to next screen
                    currentScreen++;
                    showScreen(currentScreen);
                } else {
                    // Last screen, finish onboarding
                    finishOnboarding();
                }
            });
        }

        // Optional: Add skip button functionality if your layouts have one
        Button skipButton = findViewById(R.id.nextButton);
        if (skipButton != null) {
            skipButton.setOnClickListener(v -> finishOnboarding());
        }
    }

    private void finishOnboarding() {
        // Mark onboarding as completed in Firestore
        String userId = fAuth.getCurrentUser().getUid();
        fStore.collection("users").document(userId)
                .update("onboardingCompleted", true)
                .addOnSuccessListener(aVoid -> {
                    // Navigate to MainActivity (which will redirect to appropriate home)
                    Intent intent = new Intent(Onboarding.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Even if update fails, proceed to MainActivity
                    Intent intent = new Intent(Onboarding.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                });
    }

    @Override
    public void onBackPressed() {
        if (currentScreen > 0) {
            // Go back to previous screen
            currentScreen--;
            showScreen(currentScreen);
        } else {
            // On first screen, allow default back behavior
            super.onBackPressed();
        }
    }
}