package com.example.myapplication.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ParentChildDetails extends AppCompatActivity {

    private TextView tvDetailName, tvDetailBirthday, tvDetailSpecialNote, tvAge;
    private ImageButton btnBack, btnEdit;
    private Button btnViewMedicalRecords;

    private String childId;
    private String childName;
    private String childBirthday;
    private String childNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_carddetails);

        // Initialize views
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailBirthday = findViewById(R.id.tvDetailBirthday);
        tvDetailSpecialNote = findViewById(R.id.tvDetailSpecialNote);
        tvAge = findViewById(R.id.tvAge);
        btnBack = findViewById(R.id.btnBack);
        btnEdit = findViewById(R.id.btnEdit);
        btnViewMedicalRecords = findViewById(R.id.btnViewMedicalRecords);

        // Get data from intent
        childId = getIntent().getStringExtra("childId");
        childName = getIntent().getStringExtra("childName");
        childBirthday = getIntent().getStringExtra("childBirthday");
        childNote = getIntent().getStringExtra("childNote");

        // Display data
        displayChildDetails();

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Edit button - goes to edit page
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(ParentChildDetails.this, ParentChildEdit.class);
            intent.putExtra("childId", childId);
            intent.putExtra("childName", childName);
            intent.putExtra("childBirthday", childBirthday);
            intent.putExtra("childNote", childNote);
            startActivityForResult(intent, 101);
        });

        // Medical records button (for future expansion)
        btnViewMedicalRecords.setOnClickListener(v -> {
            // TODO: Navigate to medical records page
        });
    }

    private void displayChildDetails() {
        tvDetailName.setText(childName);
        tvDetailBirthday.setText(childBirthday);

        if (childNote == null || childNote.isEmpty()) {
            tvDetailSpecialNote.setText("None");
        } else {
            tvDetailSpecialNote.setText(childNote);
        }

        // Calculate and display age
        calculateAge(childBirthday);
    }

    private void calculateAge(String birthday) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
            Date birthDate = sdf.parse(birthday);

            if (birthDate != null) {
                Calendar birthCalendar = Calendar.getInstance();
                birthCalendar.setTime(birthDate);

                Calendar today = Calendar.getInstance();

                int age = today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR);

                // Check if birthday hasn't occurred yet this year
                if (today.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--;
                }

                // Calculate months for children under 2 years
                if (age < 2) {
                    int months = (today.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)) * 12
                            + today.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH);

                    if (months < 12) {
                        tvAge.setText("Age: " + months + " months old");
                    } else {
                        tvAge.setText("Age: " + age + " year" + (age == 1 ? "" : "s") + " old");
                    }
                } else {
                    tvAge.setText("Age: " + age + " years old");
                }
            }
        } catch (ParseException e) {
            tvAge.setText("Age: Unknown");
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK) {
            // Child was edited, refresh the details
            // You might want to reload data from database here
            // For now, just finish this activity to go back to list
            setResult(RESULT_OK);
            finish();
        }
    }
}