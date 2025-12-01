package com.example.myapplication.auth;

import com.example.myapplication.models.Child;
import com.example.myapplication.models.Parent;
import com.example.myapplication.models.Provider;
import com.example.myapplication.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth.AuthStateListener;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

import androidx.annotation.NonNull;

public class AuthMan {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void signIn(String emailUsername, String password) {
        auth.signInWithEmailAndPassword(emailUsername, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser FBUser = auth.getCurrentUser();
                        if (FBUser != null) {
                            fetchUser(FBUser, user -> {
                                if (user != null) {
                                    SessionManager.getInstance().setCurrentUser(user);
                                    Log.d("AuthMan", "User fetched successfully");
                                } else {
                                    Log.w("AuthMan", "Sign in succeeded but user unauthenticated.");
                                }
                            });
                        } else {
                            Log.w("AuthMan", "User fetched unsuccessfully.");
                        }
                    }else {
                        Log.w("AuthMan", "signInWithEmail:failure", task.getException());
                        // TODO: some extra handling of the errors?
                    }
                });
    }

    private static void fetchUser(FirebaseUser fbUser, UserCallback callback) {
        String id = fbUser.getUid();
        // Fetch user profile from Firebase Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(id).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String emailUsername = documentSnapshot.getString("emailUsername");
                    String role = documentSnapshot.getString("role");

                    User user = null;

                    if (role != null) {
                        switch (role) {
                            case "Parent":
                                user = new Parent(id, name, emailUsername, role);
                                break;
                            case "Provider":
                                user = new Provider(id, name, emailUsername, role);
                                break;
                            case "Child":
                                String parentID = documentSnapshot.getString("parentID");
                                String parentEmail = documentSnapshot.getString("parentEmail");
                                user = new Child(id, parentID, name, emailUsername, role);
                                break;
                            default:
                                Log.w("AuthMan", "Invalid role: " + role);
                                break;
                        }
                    }
                    callback.onUserFetched(user);

                } else {
                    Log.w("AuthMan", "User profile not found for ID: " + id);
                    callback.onUserFetched(null);
            }
        }).addOnFailureListener(e -> {
            Log.w("AuthMan", "Error fetching user profile", e);
            callback.onUserFetched(null);
        });
    }

    public static void signUp(String email, String password, String name, String role, OnCompleteListener<AuthResult> listener) {
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
                listener.onComplete(task);
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


    // Method to create a Parent or Provider profile
    public static User createUserProfile(String id, String name, String email, String role) {
        // Create user with email and password
        switch (role) {
            case "Parent":
                return new Parent(id, name, email, role); // does this affect the compiling
            case "Provider":
                return new Provider(id, name, email, role);
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }

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
