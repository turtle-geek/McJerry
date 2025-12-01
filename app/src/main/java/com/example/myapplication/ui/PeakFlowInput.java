package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Build;
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

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PeakFlowInput extends BaseChildActivity {
    ImageButton sosButton;
    TextView pbDisplay;
    EditText peakFlowInput;
    Button submitButton;
    TextView pefDisplay;
    TextView zoneDisplay;
    TextView pefDateTime;
    Button newPEFButton;

    View inputLayout, zoneLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // make layout extend under system bars
        EdgeToEdge.enable(this);
        // load XML
        setContentView(R.layout.activity_peak_flow_tracker);
        // Ensure content is not covered by the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkUserType();
        bindViews();
        setListeners();

        int pb = hp.getPEF_PB();
        String displayText = "Personal Best :" + pb;
        pbDisplay.setText(displayText);

    }

    void bindViews(){
        sosButton = findViewById(R.id.sosButton);
        pbDisplay = findViewById(R.id.pbDisplay);
        peakFlowInput = findViewById(R.id.peakFlowInput);
        submitButton = findViewById(R.id.submitButton);
        newPEFButton = findViewById(R.id.newPEFButton);
        pefDisplay = findViewById(R.id.pefDisplay);
        zoneDisplay = findViewById(R.id.zoneDisplay);
        pefDateTime = findViewById(R.id.pefDateTime);
        inputLayout = findViewById(R.id.inputLayout);
        zoneLayout = findViewById(R.id.zoneLayout);
    }

    void setListeners(){
        sosButton.setOnClickListener(v ->
            startActivity(new Intent(this, TriageActivity.class)));

        submitButton.setOnClickListener(v -> submitPEF());

        newPEFButton.setOnClickListener(v -> resetToInput());
    }

    void submitPEF(){
        final String text = peakFlowInput.getText().toString();
        if (!text.isEmpty()) {
            peakFlowValue = Integer.parseInt(text);
            LocalDateTime submitTime = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                submitTime = LocalDateTime.now();
            }

            if (submitTime != null){
                PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);
                hp.addPEFToLog(pef);
                String zone = pef.computeZone(currentChild);
                setZoneScreen(zone, submitTime);
            }
            else throw new RuntimeException("submitTime is null");
        } else throw new RuntimeException("text is empty"); // should I throw here?
    }

    void setZoneScreen(String zone, LocalDateTime submitTime){
        // Hide input layout
        inputLayout.setVisibility(View.GONE);
        zoneLayout.setVisibility(View.VISIBLE);

        // Show zone layout
        pefDisplay.setText(String.valueOf(peakFlowValue));
        zoneDisplay.setText(zone);
        pefDateTime.setText(formatDateTime(submitTime));

    }

    void resetToInput(){
        // Hide zone layout
        findViewById(R.id.zoneLayout).setVisibility(View.GONE);
        // Show input layout
        findViewById(R.id.inputLayout).setVisibility(View.VISIBLE);
    }

    String formatDateTime(LocalDateTime time) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("h:mm a, MMM d");
            return time.format(formatter);
        } else {
            // fallback for older devices
            java.util.Date date = new java.util.Date();
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a, MMM d",
                    Locale.getDefault());
            return sdf.format(date);
        }
    }
}