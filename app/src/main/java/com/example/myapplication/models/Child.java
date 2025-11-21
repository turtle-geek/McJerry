package com.example.myapplication.models;

import android.util.Log;
import java.time.LocalDate;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import static com.example.myapplication.auth.AuthMan.addToDatabase;

public class Child extends User{

    final String parentID;
    private LocalDate dateOfBirth;
    private String notes;
    private HealthProfile healthProfile;

    // Constructor to create child profile under a parent
    // Package private

    /**
     * Constructor to create child profile under a parent. This constructor also calls
     * addToDataBase in auth.AuthMan to add the user into the database, as a nested child profile
     * cannot be logged into the app directly with email and password.
     * @param id of the child user
     * @param parentID
     * @param name
     * @param parentEmail
     * @param nestedStatus
     */
    Child(String id, String parentID, String name, String parentEmail, String nestedStatus) {
        super(id, name);
        if (!nestedStatus.equals("nested")) {
            // is this ok
            throw new IllegalArgumentException("nestedStatus must be 'nested'");
        }
        this.parentID = parentID;
        this.email = parentEmail;
        // Add to firebase firestore
        addToDatabase(this);
    }

    // Overloaded constructor for child to create their own profile
    public Child(String id, String parentID, String name, String email){
        super(id, name);
        this.parentID = parentID;
        if (email != null)
            this.email = email;


    }
    // Public Setters
    public void setDOB(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setHealthProfile(HealthProfile profile){
        this.healthProfile = profile;
    }

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
                        String email = documentSnapshot.getString("email");
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

    // Setter is omitted as parentID shouldn't change after creation.
    // For reference, LocalDate.of(int year, int month, int day) may be used for changing if needed

}
