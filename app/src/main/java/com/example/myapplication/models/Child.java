package com.example.myapplication.models;

import android.util.Log;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.myapplication.health.Inventory;
import com.example.myapplication.health.MedicineLabel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.myapplication.auth.AuthMan.addToDatabase;

public class Child extends User{

    final String parentID;
    private LocalDate dateOfBirth;
    private String notes;
    private HealthProfile healthProfile;
    private Inventory inventory;
    private StreakCount streakCount;
    private Badges badges;

    // Constructor is not updated to initialize the new fields yet
    // If update, remember to link inventory to streakCount and streakCount to badges

    public Child(String id, String parentID, String name, String parentEmail, String role) {
        super(id, name, role);
        this.parentID = parentID;
        this.emailUsername = parentEmail;
        // Add to firebase firestore
        addToDatabase(this);
    }

    // Public Setters
    public void setNotes(String notes) {
        this.notes = notes;
    }
    public void setDOB(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setHealthProfile(HealthProfile profile){
        this.healthProfile = profile;
    }

    public void setInventory(Inventory inventory){ this.inventory = inventory; }

    public void setStreakCount(StreakCount streakCount) { this.streakCount = streakCount; }

    public void setBadges(Badges badges) { this.badges = badges; }

    public HealthProfile getHealthProfile(){
        return healthProfile;
    }

    // Public Getters
    public String getParentID() {
        return parentID;
    }

    public void getParentEmail(String parentID, OnSuccessListener<String> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(parentID);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("emailUsername");
                        listener.onSuccess(email);
                    } else {
                        listener.onSuccess(null); // No user found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("Firestore", "Error retrieving user document", e);
                    listener.onSuccess(null); // return null if error
                });
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public Inventory getInventory() { return inventory; }

    public StreakCount getStreakCount() { return streakCount; }

    public Badges getBadges() { return badges; }

    // Setter is omitted as parentID shouldn't change after creation.
    // For reference, LocalDate.of(int year, int month, int day) may be used for changing if needed

    // When use medicine, automatically add to streak
    public void useMedicine(MedicineLabel label, double amount, LocalDateTime timestamp) {
        inventory.useMedicine(label, amount, timestamp);
        streakCount.countStreaks();
        badges.updateControllerBadge();
        badges.updateTechniqueBadge();
        badges.updateRescueBadge();
    }
}
