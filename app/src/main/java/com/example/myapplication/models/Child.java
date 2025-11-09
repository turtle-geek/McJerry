package com.example.myapplication.models;

public class Child extends User{
    int parentID;

    public Child(int id, int parentID, String name, String role) {
        super(id, name, role);
        this.parentID = parentID;
        // Potentially adding parent's email and password here too?
    }

    // Overloaded method for child to create their own profile
    public Child(int id, int parentID, String name, String role, String email, String password){
        super(id, name, role);
        this.parentID = parentID;
        this.email = email;
        this.password = password;
    }

    public int getParentID() {
        return parentID;
    }
}
