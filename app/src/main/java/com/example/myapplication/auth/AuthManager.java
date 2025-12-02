package com.example.myapplication.auth;

import com.example.myapplication.callbacks.UserCallback;
import com.example.myapplication.models.Child;
import com.example.myapplication.models.Parent;
import com.example.myapplication.models.Provider;
import com.example.myapplication.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import android.util.Log;

public class AuthManager {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final String TAG = "AuthManager";

    /**
     * Attempts to sign in the user and, if successful, fetches the user's profile
     * and updates the SessionManager.
     */
    public static void signIn(String emailUsername, String password) {
        auth.signInWithEmailAndPassword(emailUsername, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fbUser = auth.getCurrentUser();
                        if (fbUser != null) {
                            fetchUser(fbUser, user -> {
                                if (user != null) {
                                    SessionManager.getInstance().setCurrentUser(user);
                                    Log.d(TAG, "User signed in and session updated successfully: " + user.getId());
                                } else {
                                    signOut();
                                    Log.w(TAG, "Sign in succeeded but user profile was not found in Firestore. Logging out.");
                                }
                            });
                        } else {
                            Log.w(TAG, "FirebaseUser is null after successful sign-in.");
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                    }
                });
    }

    /**
     * Fetches user profile from Firestore and executes the callback.
     */
    private static void fetchUser(FirebaseUser fbUser, UserCallback callback) {
        String id = fbUser.getUid();
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
                                    user = new Child(id, documentSnapshot.getString("parentID"), name, emailUsername, role);
                                    break;
                                default:
                                    Log.w(TAG, "Invalid role: " + role);
                                    break;
                            }
                        }
                        callback.onUserFetched(user);

                    } else {
                        Log.w(TAG, "User profile not found for ID: " + id);
                        callback.onUserFetched(null);
                    }
                }).addOnFailureListener(e -> {
                    Log.w(TAG, "Error fetching user profile", e);
                    callback.onUserFetched(null);
                });
    }

    /**
     * Creates a new user in Firebase Auth and, on success, creates and persists
     * the user profile to Firestore and updates the SessionManager.
     * @param listener The OnCompleteListener to handle UI feedback in the calling Activity.
     */
    public static void signUp(String email, String password, String name, String role, OnCompleteListener<AuthResult> listener) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser fbUser = auth.getCurrentUser();
                        if (fbUser != null) {
                            Log.d(TAG, "User created with ID: " + fbUser.getUid());
                            String id = fbUser.getUid();

                            User user = createUserProfile(id, name, email, role);

                            addToDatabase(user);

                            SessionManager.getInstance().setCurrentUser(user);

                            Log.d(TAG, "User profile and session created successfully");
                        } else {
                            Log.w(TAG, "fbUser created unsuccessfully or is null.");
                        }
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    }
                    listener.onComplete(task);
                });
    }

    /**
     * Adds a user profile to the database (Firestore).
     * @param user to add to the database
     */
    public static void addToDatabase(User user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getId()).set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User " + user.getId() + " successfully written to Firestore.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error writing user " + user.getId() + " to Firestore.", e);
                });
    }

    /**
     * Creates a local Parent or Provider profile object.
     */
    public static User createUserProfile(String id, String name, String email, String role) {
        switch (role) {
            case "Parent":
                return new Parent(id, name, email, role);
            case "Provider":
                return new Provider(id, name, email, role);
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
    }

    /**
     * Performs Firebase sign-out and clears the local session.
     */
    public static void signOut() {
        auth.signOut();
        SessionManager.getInstance().setCurrentUser(null);
        Log.d(TAG, "User signed out and session cleared.");
    }

    /**
     * Listens for changes in authentication state.
     */
    public static void attachAuthStateListener(FirebaseAuth.AuthStateListener listener) {
        auth.addAuthStateListener(listener);
    }

    /**
     * Detaches the listener for authentication state changes.
     */
    public static void detachAuthStateListener(FirebaseAuth.AuthStateListener listener) {
        auth.removeAuthStateListener(listener);
    }
}