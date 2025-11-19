package com.example.myapplication.peakflow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.auth.SessionManager;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.HealthProfile;
import com.example.myapplication.models.PeakFlow;

import java.time.LocalDateTime;

public class PeakFlowTracker extends AppCompatActivity {
    ImageButton sosButton;
    TextView pbDisplay;
    EditText peakFlowInput;
    int peakFlowValue;
    Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // make layout extend under system bars
        EdgeToEdge.enable(this);
        // load XML
        setContentView(R.layout.activity_peak_flow_tracker);

        sosButton = (ImageButton) findViewById(R.id.sosButton);
        sosButton.setOnClickListener(new View.onClickListener() {
                                          // tk
                                          // take user to triage screen

                                      });

        pbDisplay = (TextView) findViewById(R.id.pbDisplay);
        pbDisplay.setText("Personal Best: " + PeakFlow.getPersonalBest());

        peakFlowInput = (EditText) findViewById(R.id.peakFlowInput);
        submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String text = peakFlowInput.getText().toString();
                if (!text.isEmpty()) {
                    int peakFlowValue = Integer.parseInt(text);
                    LocalDateTime submitTime = LocalDateTime.now();

                    Child child = (Child) SessionManager.getInstance().getCurrentUser();

                    PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);
                    child.getHealthProfile().addPEFToLog(pef); // call with a child profile! get user somewhere
                    String zone = pef.computeZone();

                    // switch screen based on zone
                }
            }});

        // Ensure content is not covered by the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


}