package com.example.myapplication.models;

public abstract class User {
    private final int id;
    private final String name;
    private final String role;

    public User(int id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    // Public Getters
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    // Setters are omitted to prevent outside modification of ID/Role/Name after creation
    // If modification is needed, you would add public setters here.
}