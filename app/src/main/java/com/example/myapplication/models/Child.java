package com.example.myapplication.models;

public class Child extends User {
    private final int parentID;

    public Child(int id, int parentID, String name, String role) {
        super(id, name, role);
        this.parentID = parentID;
    }

    // Public Getters
    public int getParentID() {
        return parentID;
    }

    // Setter is omitted as parentID shouldn't change after creation.
}