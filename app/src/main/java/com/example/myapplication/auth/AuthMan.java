package com.example.myapplication.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;

import android.util.Log;

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

    public static void signUp(String email, String password) {
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

        // Create user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // User created successfully
                    FirebaseUser user = auth.getCurrentUser();
                    Log.d("AuthMan", "User created with ID: " + user.getUid());
                } else {
                    // Error creating user
                    Log.w("AuthMan", "createUserWithEmail:failure", task.getException());
                    // some extra handling of the error?
                }
        });
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
