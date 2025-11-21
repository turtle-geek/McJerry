package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;

public class AuthManager {
    private FirebaseAuth mAuth;

    public AuthManager() {
        mAuth = FirebaseAuth.getInstance();
    }

    public void signOut() {
        mAuth.signOut();
    }
}