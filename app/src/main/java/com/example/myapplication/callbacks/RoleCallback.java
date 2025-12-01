package com.example.myapplication.callbacks;

public interface RoleCallback {
    void onRoleFetched(String role);
    void onFailure(String errorMessage);
}