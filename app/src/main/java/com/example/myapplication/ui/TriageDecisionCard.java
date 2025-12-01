package com.example.myapplication.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.Intent;
import android.os.CountDownTimer;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class TriageDecisionCard extends AppCompatActivity {

    TextView liabilityWarning, liabilityWarning2, textView, textView2, textView3, textView4;
    ImageView yellow_card, red_card;
    Button cancel_button, yes_button, sos_button;
    ProgressBar progressBar;
    ConstraintLayout NonSos, SOS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_triage_decision_card);

        bindViews();

        String decision = getIntent().getStringExtra("DECISION");

        if ("SOS".equals(decision)) {
            SOSDecision();
        } else if ("NOT SOS".equals(decision)) {
            nonSOSDecision();
        } else{
            throw new RuntimeException("No decision made");
        }

        setListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


    void bindViews(){
        liabilityWarning = findViewById(R.id.liabilityWarning);
        liabilityWarning2 = findViewById(R.id.liabilityWarning2);

        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);

        red_card = findViewById(R.id.red_card);
        yellow_card = findViewById(R.id.yellow_card);

        cancel_button = findViewById(R.id.cancel_button);
        yes_button = findViewById(R.id.yes_button);
        sos_button = findViewById(R.id.sos_button);

        progressBar = findViewById(R.id.progressBar);

        NonSos = findViewById(R.id.NonSOS);
        SOS = findViewById(R.id.SOS);
    }

    void setListeners(){
        sos_button.setOnClickListener(v -> {
            // TODO: CALL 911
        });
        cancel_button.setOnClickListener(v -> {
            // 911 IS NOT CALLED
            nonSOSDecision();
        });
        yes_button.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeStepsRecovery.class)); // code redundancy
        });

    }

    void SOSDecision(){
        SOS.setVisibility(View.VISIBLE);
        NonSos.setVisibility(View.GONE);
        progressBar.setMax(100);
        new CountDownTimer(10000, 100) {
            /**
             * Callback fired when the time is up.
             */
            @Override
            public void onFinish() {
                progressBar.setProgress(100);
                progressBar.setVisibility(View.GONE);
                cancel_button.setVisibility(View.GONE);
                // TODO: CALL 911
            }

            @Override
            public void onTick(long millisUntilFinished) {
                int progressPercentage = (int) ((10000 - millisUntilFinished) / 100);
                int secondsLeft = (int) (millisUntilFinished / 1000);
                progressBar.setProgress(progressPercentage);
                String text = "CALLING 911 IN " + secondsLeft + " SECONDS";
                textView2.setText(text);
            }
        }.start();
    }

    void nonSOSDecision(){
        SOS.setVisibility(View.GONE);
        NonSos.setVisibility(View.VISIBLE);
        startActivity(new Intent(this, HomeStepsRecovery.class));
    }

}