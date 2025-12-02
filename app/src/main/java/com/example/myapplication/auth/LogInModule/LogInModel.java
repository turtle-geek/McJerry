package com.example.myapplication.auth.LogInModule;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LogInModel {
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";
    private FirebaseAuth fAuth;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;

    public LogInModel(Context context) {
        // Initialize Firebase Auth and Firestore
        fAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }


    public void logIn(String input, String password, LogInPresenter presenter) {
        fAuth.signInWithEmailAndPassword(input, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        presenter.outputMessage("Login successful!");
                        presenter.onLoginSuccess(true);

                    } else {
                        presenter.outputMessage(
                                "Login failed: " + task.getException().getMessage());
                    }
                });
    }

    public void saveCredentials(String input, String password) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, input);
        editor.putString(KEY_PASSWORD, password);
        editor.putBoolean(KEY_REMEMBER, true);
        editor.apply();
    }

    public void clearCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PASSWORD);
        editor.putBoolean(KEY_REMEMBER, false);
        editor.apply();
    }

    public void fetchSavedCredentials(LogInPresenter presenter) {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER, false);
        String input = null, password = null;

        if (rememberMe) {
            input = sharedPreferences.getString(KEY_EMAIL, "");
            password = sharedPreferences.getString(KEY_PASSWORD, "");
        }
        presenter.returnSavedCredentials(input, password);
    }
}