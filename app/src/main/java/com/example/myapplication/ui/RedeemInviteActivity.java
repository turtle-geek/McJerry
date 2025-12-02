package com.example.myapplication.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;
import com.example.myapplication.health.SharedAccessInvite;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class RedeemInviteActivity extends AppCompatActivity {

    private static final String TAG = "RedeemInviteActivity";

    private TextInputEditText inputCode;
    private Button btnRedeem;
    private ImageButton btnBack;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redeem_invite);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        inputCode = findViewById(R.id.inputCode);
        btnRedeem = findViewById(R.id.btnRedeem);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnRedeem.setOnClickListener(v -> handleRedemption());
    }

    private void handleRedemption() {
        String code = inputCode.getText().toString().trim();

        if (code.isEmpty()) {
            inputCode.setError("Please enter a code");
            return;
        }

        btnRedeem.setEnabled(false);
        btnRedeem.setText("Verifying...");

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String providerId = user.getUid();

        // Query for the Invite Code
        db.collection("shared_access_invites")
                .whereEqualTo("inviteCode", code)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        resetButton();
                        inputCode.setError("Invalid invitation code");
                        return;
                    }

                    // Loop through results (should be unique)
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        SharedAccessInvite invite = doc.toObject(SharedAccessInvite.class);
                        processInvite(doc.getId(), invite, providerId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error finding invite", e);
                    resetButton();
                    Toast.makeText(this, "Network error. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void processInvite(String docId, SharedAccessInvite invite, String providerId) {
        if (invite.getIsUsed()) {
            resetButton();
            Toast.makeText(this, "This code has already been redeemed.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!invite.isValid()) {
            resetButton();
            Toast.makeText(this, "This code has expired.", Toast.LENGTH_LONG).show();
            return;
        }

        // Execute Transaction
        db.runTransaction(transaction -> {
            // Update Invite Status
            transaction.update(db.collection("shared_access_invites").document(docId),
                    "isUsed", true,
                    "providerID", providerId);

            // Add Child to Provider's Patient List
            transaction.update(db.collection("users").document(providerId),
                    "patients", FieldValue.arrayUnion(invite.getChildID()));

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Invitation Redeemed Successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Close activity and return to More page
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Transaction failed", e);
            resetButton();
            Toast.makeText(this, "Failed to redeem code. Please try again.", Toast.LENGTH_SHORT).show();
        });
    }

    private void resetButton() {
        btnRedeem.setEnabled(true);
        btnRedeem.setText("Redeem Code");
    }
}