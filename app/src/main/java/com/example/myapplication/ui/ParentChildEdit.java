package com.example.myapplication.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Calendar;

public class ParentChildEdit extends AppCompatActivity {

    private TextInputEditText etChildName, etBirthday, etSpecialNote;
    private Button btnSave, btnDelete;
    private ImageButton btnBack;
    private String childId;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_editchild);

        // Initialize views
        etChildName = findViewById(R.id.etChildName);
        etBirthday = findViewById(R.id.etBirthday);
        etSpecialNote = findViewById(R.id.etSpecialNote);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        // Check if editing existing child
        if (getIntent().hasExtra("childId")) {
            isEditMode = true;
            childId = getIntent().getStringExtra("childId");
            etChildName.setText(getIntent().getStringExtra("childName"));
            etBirthday.setText(getIntent().getStringExtra("childBirthday"));
            etSpecialNote.setText(getIntent().getStringExtra("childNote"));
            btnDelete.setVisibility(View.VISIBLE);
        }

        // Birthday picker
        etBirthday.setOnClickListener(v -> showDatePicker());

        // Save button
        btnSave.setOnClickListener(v -> saveChild());

        // Delete button
        btnDelete.setOnClickListener(v -> deleteChild());

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year);
                    etBirthday.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveChild() {
        String name = etChildName.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();
        String note = etSpecialNote.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter child's name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (birthday.isEmpty()) {
            Toast.makeText(this, "Please select birthday", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Save to Firebase or local database
        // Child child = new Child(childId, name, birthday, note);
        // Save child to database

        Toast.makeText(this, "Child saved successfully", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void deleteChild() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Child")
                .setMessage("Are you sure you want to delete this child's information?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // TODO: Delete from Firebase or local database
                    Toast.makeText(this, "Child deleted", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}