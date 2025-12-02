package com.example.myapplication.ui;

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
import com.example.myapplication.models.User;

import java.time.LocalDateTime;

public class PeakFlowInput extends AppCompatActivity {
    ImageButton sosButton;
    TextView pbDisplay;
    EditText peakFlowInput;
    Button submitButton;
    TextView pefDisplay;
    TextView zoneDisplay;
    TextView pefDateTime;
    Button newPEFButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Make sure only a child can see this screen
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (!(currentUser instanceof Child)){
            // tk: redirect to a different page
            // Option 1: finish this activity and go to a generic home
            //  finish();
            // Optional 2: start another activity
            //  startActivity(new Intent(this, GenericHomeActivity.class));
            //  return;
        }
        Child child = (Child)currentUser;
        HealthProfile hp = child.getHealthProfile();

        // make layout extend under system bars
        EdgeToEdge.enable(this);
        // load XML
        setContentView(R.layout.activity_peak_flow_tracker);

        sosButton = findViewById(R.id.sosButton);
        sosButton.setOnClickListener(v -> {
            // tk
            // take user to triage screen
        });

        pbDisplay = findViewById(R.id.pbDisplay);
        pbDisplay.setText("Personal Best: " + hp.getPEF_PB());

        peakFlowInput = findViewById(R.id.peakFlowInput);
        submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> {
            final String text = peakFlowInput.getText().toString();
            if (!text.isEmpty()) {
                int peakFlowValue = Integer.parseInt(text);
                LocalDateTime submitTime = LocalDateTime.now();

                PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);
                hp.addPEFToLog(pef);
                String zone = pef.computeZone(child);

                // switch screen based on zone
                // Hide input layout
                findViewById(R.id.inputLayout).setVisibility(View.GONE);
                // Show zone layout
                pefDisplay = findViewById(R.id.pefDisplay);
                pefDisplay.setText(peakFlowValue);
                zoneDisplay = findViewById(R.id.zoneDisplay);
                zoneDisplay.setText(zone);
                pefDateTime = findViewById(R.id.pefDateTime);
                pefDateTime.setText(submitTime.toString()); // format might be off

                findViewById(R.id.zoneLayout).setVisibility(View.VISIBLE);
            }
        });
        newPEFButton = findViewById(R.id.newPEFButton);
        newPEFButton.setOnClickListener(v -> {
            // Show input layout
            findViewById(R.id.inputLayout).setVisibility(View.VISIBLE);
            // Hide zone layout
            findViewById(R.id.zoneLayout).setVisibility(View.GONE);
        });

        // Ensure content is not covered by the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }


}