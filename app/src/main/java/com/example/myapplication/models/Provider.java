package com.example.myapplication.models;
import android.util.Log;

import java.util.ArrayList;
import com.example.myapplication.health.HealthInfo;
import com.example.myapplication.health.SharedAccessInvite;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class Provider extends User{
    private final ArrayList<String> patients;

    public Provider(String id, String name, String email, String role) {
        super(id, name, role);
        this.emailUsername = email;
        patients = new ArrayList<>(); // Using diamond operator
    }

    /** This method adds an existing patient into the list of the provider's patients.
     * @param id of the patient
     */
    public void addPatient(String id) {
        patients.add(id); // Java autoboxing handles the conversion from int to Integer
    }

    // Public Getters and Setters
    public ArrayList<String> getPatients() {
        return patients;
    }

    // Setter for patients is omitted; the addPatient method controls modification.

    // ----- Sharing invitation -----

    // Helper method
    private void findChildById(String childId, OnSuccessListener<Child> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")  // or "children" if stored differently
                .document(childId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Child child = documentSnapshot.toObject(Child.class);
                        listener.onSuccess(child);
                    } else {
                        listener.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Failed to fetch child", e);
                    listener.onSuccess(null);
                });
    }

    public void redeemInvite(
            Parent parent,
            String code,
            OnSuccessListener<HealthProfile> listener
    ) {
        SharedAccessInvite invite = parent.getInviteByCode(code);

        if (invite == null || !invite.isValid()) {
            listener.onSuccess(null);
            return;
        }

        invite.markAsUsed();

        findChildById(invite.getChildID(), child -> {
            if (child == null) {
                listener.onSuccess(null);
                return;
            }

            HealthProfile filtered = filterProfileByInvite(child.getHealthProfile(), invite);
            listener.onSuccess(filtered);
        });
    }

    private HealthProfile filterProfileByInvite(HealthProfile full, SharedAccessInvite invite) {
        HealthProfile shared = new HealthProfile();
        for (HealthInfo field : invite.getSharedFields()) {
            switch (field) {
                case RESCUE_LOGS:
                    shared.setRescueLogs(full.getRescueLogs());
                    break;
                case CONTROLLER_ADHERENCE:
                    shared.setControllerAdherence(full.getControllerAdherence());
                    break;
                case SYMPTOMS:
                    shared.setSymptoms(full.getSymptoms());
                    break;
                case TRIGGERS:
                    shared.setTriggers(full.getTriggers());
                    break;
                case PEF_LOG:
                    shared.setPEF_PB(full.getPEF_PB());
                    break;
                case TRIAGE_INCIDENTS:
                    shared.setTriageIncidents(full.getTriageIncidents());
                    break;
                case CHARTS:
                    shared.setCharts(full.getCharts());
                    break;
            }
        }
        return shared;
    }
}