package com.example.myapplication.auth;

import com.example.myapplication.models.User;

public class SessionManager {
    private static SessionManager instance; // static - one session (logged in user) at a time
    private User currentUser;

    private SessionManager() {
        // private constructor to enforce singleton
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean hasUser() {
        return currentUser != null;
    }
}
