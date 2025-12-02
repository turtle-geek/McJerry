package com.example.myapplication.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.IncidentLogEntry;
import com.example.myapplication.adapters.TriageHistoryAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TriageHistoryActivity extends AppCompatActivity {

    private String username;
    private RecyclerView recyclerView;
    private TextView tvLoading;
    private TriageHistoryAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_triage_history);

        db = FirebaseFirestore.getInstance();
        tvLoading = findViewById(R.id.tvLoading);
        recyclerView = findViewById(R.id.recyclerViewTriageHistory);
        setupBackButton();

        checkAndSetupUser();
    }

    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void checkAndSetupUser() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Error: User must be logged in.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String uid = user.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullEmailUsername = documentSnapshot.getString("emailUsername");

                        if (fullEmailUsername != null && !fullEmailUsername.isEmpty()) {
                            int atIndex = fullEmailUsername.indexOf('@');
                            String cleanUsername = (atIndex > 0) ? fullEmailUsername.substring(0, atIndex) : fullEmailUsername;

                            if (!cleanUsername.isEmpty()) {
                                this.username = cleanUsername;
                                fetchTriageHistory();
                            } else {
                                Toast.makeText(this, "Error: Username not found.", Toast.LENGTH_LONG).show();
                                finishSetup(false);
                            }
                        } else {
                            finishSetup(false);
                        }
                    } else {
                        finishSetup(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("TriageHistoryActivity", "Failed to fetch user data: ", e);
                    finishSetup(false);
                });
    }

    private void fetchTriageHistory() {
        if (username == null) {
            finishSetup(false);
            return;
        }

        db.collection("triage_incidents")
                .whereEqualTo("username", this.username)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<IncidentLogEntry> entries = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                IncidentLogEntry entry = document.toObject(IncidentLogEntry.class);
                                entries.add(entry);

                            } catch (Exception e) {
                                Log.e("TriageHistoryActivity", "Error deserializing entry: " + e.getMessage());
                            }
                        }
                        setupRecyclerView(entries);
                        finishSetup(!entries.isEmpty());
                    } else {
                        Log.e("TriageHistoryActivity", "Error getting documents: ", task.getException());
                        finishSetup(false);
                    }
                });
    }

    private void setupRecyclerView(List<IncidentLogEntry> entries) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TriageHistoryAdapter(entries);
        recyclerView.setAdapter(adapter);
    }

    private void finishSetup(boolean dataFound) {
        tvLoading.setVisibility(View.GONE);
        if (dataFound) {
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            tvLoading.setText("No triage incidents found for this user.");
            tvLoading.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }
}