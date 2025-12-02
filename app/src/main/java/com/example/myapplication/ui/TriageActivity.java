package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.example.myapplication.R;
import com.example.myapplication.models.PeakFlow;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TriageActivity extends BaseChildActivity {

    ChipGroup chipGroup;
    Button nextButton;

    RadioGroup rescueAttemptGroup;
    RadioButton radioRescueYes;
    RadioButton radioRescueNo;

    Slider triagePEFSlider;
    TextView tvPeakFlowValue;
    SwitchMaterial peakFlowToggleSwitch;
    LinearLayout peakFlowInputContainer;

    int peakFlowValue = 74;

    private static final int COLOR_CHIP_RED = 0xFFF44336;
    private static final int COLOR_CHIP_WHITE = 0xFFFFFFFF;
    private static final int COLOR_CHIP_GREEN = 0xFF4CAF50;
    private static final int COLOR_CHIP_DEFAULT_BACKGROUND = 0xFFE0E0E0;

    private static final int COLOR_SLIDER_GREEN = 0xFF064200;
    private static final int COLOR_SLIDER_LIGHT_GRAY = 0xFFE0E0E0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_triage);

        bindViews();
        setupSlider();
        setupSliderColors();
        setupChipListeners();
        setupRescueButtonListeners();

        nextButton.setOnClickListener(v -> redFlagCheck());
    }

    void bindViews(){
        chipGroup = findViewById(R.id.chipGroup);
        nextButton = findViewById(R.id.nextButton);

        rescueAttemptGroup = findViewById(R.id.rescueAttemptGroup);
        radioRescueYes = findViewById(R.id.radioRescueYes);
        radioRescueNo = findViewById(R.id.radioRescueNo);

        triagePEFSlider = findViewById(R.id.TriagePEFSlider);
        tvPeakFlowValue = findViewById(R.id.tvPeakFlowValue);
        peakFlowToggleSwitch = findViewById(R.id.peakFlowToggleSwitch);
        peakFlowInputContainer = findViewById(R.id.peakFlowInputContainer);
    }

    void setupSliderColors() {
        int[][] states = new int[][] {
                { android.R.attr.state_enabled},
                {-android.R.attr.state_enabled}
        };

        int[] activeColors = new int[] {
                COLOR_SLIDER_GREEN,
                COLOR_SLIDER_GREEN
        };

        int[] inactiveColors = new int[] {
                COLOR_SLIDER_LIGHT_GRAY,
                COLOR_SLIDER_LIGHT_GRAY
        };

        ColorStateList activeTintList = new ColorStateList(states, activeColors);
        ColorStateList inactiveTintList = new ColorStateList(states, inactiveColors);

        triagePEFSlider.setThumbTintList(ColorStateList.valueOf(COLOR_SLIDER_GREEN));
        triagePEFSlider.setTrackActiveTintList(activeTintList);
        triagePEFSlider.setTrackInactiveTintList(inactiveTintList);
        triagePEFSlider.setTickActiveTintList(activeTintList);
        triagePEFSlider.setTickInactiveTintList(inactiveTintList);
    }

    void setupSlider() {
        triagePEFSlider.addOnChangeListener((slider, value, fromUser) -> {
            peakFlowValue = (int) value;
            tvPeakFlowValue.setText("Value: " + peakFlowValue + " L/min");
        });

        triagePEFSlider.setValue(peakFlowValue);
        tvPeakFlowValue.setText("Value: " + peakFlowValue + " L/min");

        peakFlowToggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                peakFlowInputContainer.setVisibility(View.VISIBLE);
            } else {
                peakFlowInputContainer.setVisibility(View.GONE);
            }
        });
    }

    void setupChipListeners() {
        ColorStateList defaultSelector = ColorStateList.valueOf(COLOR_CHIP_DEFAULT_BACKGROUND);

        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;

                chip.setChipBackgroundColor(defaultSelector);
                chip.setChipStrokeColor(null);

                chip.setOnClickListener(v -> {
                    if (chip.isChecked()) {
                        // Chips turn RED when selected
                        chip.setChipBackgroundColor(ColorStateList.valueOf(COLOR_CHIP_RED));
                        chip.setChipStrokeColor(ColorStateList.valueOf(COLOR_CHIP_RED));
                    } else {
                        chip.setChipBackgroundColor(defaultSelector);
                        chip.setChipStrokeColor(null);
                    }
                });
            }
        }
    }

    private ColorStateList createGreenRadioTintList() {
        int states[][] = new int[][] {
                { android.R.attr.state_checked },
                {}
        };
        int colors[] = new int[] {
                COLOR_SLIDER_GREEN,
                getResources().getColor(android.R.color.darker_gray, getTheme())
        };
        return new ColorStateList(states, colors);
    }

    void setupRescueButtonListeners() {
        ColorStateList greenTintList = createGreenRadioTintList();

        radioRescueYes.setButtonTintList(greenTintList);
        radioRescueNo.setButtonTintList(greenTintList);

        rescueAttemptGroup.setOnCheckedChangeListener((group, checkedId) -> {

        });
    }


    void redFlagCheck() {
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            View child = chipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                if (chip.isChecked()) {
                    selected.add(chip.getId());
                }
            }
        }

        boolean rescueAttemptSelected = rescueAttemptGroup.getCheckedRadioButtonId() != -1;
        boolean anyRedFlagSelected = !selected.isEmpty();

        if (!anyRedFlagSelected) {
            Toast.makeText(this, "Please select at least one physical red flag symptom.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!rescueAttemptSelected) {
            Toast.makeText(this, "Please indicate if there was any recent rescue attempt.", Toast.LENGTH_LONG).show();
            return;
        }


        String pefZone = processPEF();

        if (selected.contains(R.id.chip11) || selected.contains(R.id.chip12) ||
                selected.contains(R.id.chip5) || selected.contains(R.id.chip4) ||
                pefZone.equals("red")) {

            Intent intent = new Intent(this, TriageCriticalActivity.class);
            intent.putExtra("DECISION", "SOS");
            startActivity(intent);
            return;
        }

        Intent intent = new Intent(this, TriageNonCriticalActivity.class);
        intent.putExtra("DECISION", "NOT SOS");
        startActivity(intent);
    }

    String processPEF(){
        if (!peakFlowToggleSwitch.isChecked()) {
            return "normal";
        }

        LocalDateTime submitTime = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            submitTime = LocalDateTime.now();
        }

        if (submitTime != null) {
            PeakFlow pef = new PeakFlow(peakFlowValue, submitTime);
            return "normal";
        }

        return "normal";
    }
}