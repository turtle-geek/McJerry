package com.example.myapplication.auth;

import com.example.myapplication.models.Child;
import com.example.myapplication.models.Parent;
import com.example.myapplication.models.Provider;
import com.example.myapplication.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

import androidx.annotation.NonNull;

public class AuthMan {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void signIn(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // User signed in successfully
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // what is this
                            Log.d("AuthMan", "User signed in with ID: " + user.getUid());
                        }
                    } else {
                        // Error signing in
                        Log.w("AuthMan", "Sign in failed", task.getException());

                    }
                });



    }

    public static void signUp(String email, String password, String name, String role) {
        if (!validateInput(email, password)) {
            //TO-DO add an explicit error message?
            return;
        }

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // User created successfully
                    FirebaseUser fbUser = auth.getCurrentUser();
                    if (fbUser != null) {
                        Log.d("AuthMan", "User created with ID: " + fbUser.getUid());
                        String id = fbUser.getUid();
                        // Create a user profile to add to Firebase Firestore
                        User user = createUserProfile(id, name, email, role);
                        // Add to firebase firestore
                        addToDatabase(user);
                        Log.d("AuthMan", "User profile created successfully");
                    } else {
                        Log.w("AuthMan", "fbUser created unsuccessfully.");
                    }
                } else {
                    // Error creating user
                    Log.w("AuthMan", "createUserWithEmail:failure", task.getException());
                    // some extra handling of the errors? work it out
                }
        });

    }

    /**
     * Adds a user to the database (Firestore, subject to change)
     * @param user too add to the database
     */
    public static void addToDatabase(User user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getId()).set(user);
        // For later development: Check whether user existed before adding new entry
    }

    public static boolean validateInput(@NonNull String email, String password) {
        // Check for empty fields
        if (email.isEmpty() || password.isEmpty()) {
            throw new IllegalArgumentException("Email and password cannot be empty");
        }
        // Check for proper email format
        if (!email.contains("@.")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        // Check for password length
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        // Check for password complexity
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpperCase = true;
            } else if (Character.isLowerCase(c)) {
                hasLowerCase = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasUpperCase) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!hasLowerCase) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one digit");
        }

        return true;
    }

    // Method to create a Parent or Provider profile
    public static User createUserProfile(String id, String name, String email, String role) {
        // Create user with email and password
        switch (role) {
            case "Parent":
                return new Parent(id, name, email); // does this affect the compiling
            case "Provider":
                return new Provider(id, name, email);
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }

    }

    // Overloaded method for creating an independent child profile
    public static User createUserProfile(String id, String name, String email, String parentID, String role){
        if (!role.equals("Child")) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        return new Child(id, parentID, name, email); // front end might have to pull ID from database using a different field, like email
    }

    public static void signOut() {
        auth.signOut();
        Log.d("AuthMan", "User signed out");
    }

    // Listens for changes in authentication state
    public static void attachAuthStateListener(AuthStateListener listener) {
        auth.addAuthStateListener(listener);
    }

    public static void detachAuthStateListener(AuthStateListener listener) {
        auth.removeAuthStateListener(listener);
    }

}
