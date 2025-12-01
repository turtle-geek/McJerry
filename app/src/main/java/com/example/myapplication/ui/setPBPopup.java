package com.example.myapplication.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class setPBPopup extends AppCompatActivity {

    EditText editPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_set_pb_popup);

        editPB = findViewById(R.id.editPB);

        editPB.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String text = editPB.getText().toString();
                if (text.isEmpty()) {
                    // TODO: do nothing
                    Toast.makeText(this, "No Personal Best entered",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // TODO save to HealthProfile
                    // of which child???
                    // which consequently saves to database
                    // somehow must make sure this is properly updated
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}