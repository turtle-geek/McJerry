package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.example.myapplication.R;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.chip.ChipGroup;

import java.time.LocalDateTime;
import java.util.List;

public class TriageActivity extends BaseChildActivity {

    ChipGroup chipGroup;
    static int chipCount = 0;
    Button nextButton;
    EditText peakFlowInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_triage);

        bindViews();
        nextButton.setOnClickListener(v -> redFlagCheck());

    }

    void bindViews(){
        chipGroup = findViewById(R.id.chipGroup);
        nextButton = findViewById(R.id.nextButton);
        peakFlowInput = findViewById(R.id.TriagePEF);
    }

    void redFlagCheck() {
        List<Integer> selected = chipGroup.getCheckedChipIds();

        Intent intent = new Intent(this, TriageDecisionCard.class);
        if (selected.contains(R.id.chip10) || selected.contains(R.id.chip9) ||
                selected.contains(R.id.chip8) || selected.contains(R.id.chip6) ||
                selected.contains(R.id.chip7) || processPEF().equals("red")) {
            intent.putExtra("DECISION", "SOS");
            startActivity(intent);
        } else {
            intent.putExtra("DECISION", "NOT SOS");
            startActivity(intent);
        }
    }

    String processPEF(){ // refactor this, code redundancy with PEFInput
        final String text = peakFlowInput.getText().toString();
        if (!text.isEmpty()) {
            peakFlowValue = Integer.parseInt(text);
            LocalDateTime submitTime = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                submitTime = LocalDateTime.now();
            }

            if (submitTime != null) {
                PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);
                hp.addPEFToLog(pef);
                return pef.computeZone(currentChild);
            }
            else throw new RuntimeException("submitTime is null");
        } else throw new RuntimeException("text is empty");
    }
}