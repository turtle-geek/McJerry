package com.example.myapplication.models;

public abstract class User {
    final String id;
    String name; // name should be modifiable
    String emailUsername; // emailUsername should be modifiable
    String role;

    public User(){//Firestore apparently needs no-arg constructor for reading data
        id = "";
    }

    public User(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    // Public Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmailUsername(){
        return emailUsername;
    }

    public String getRole() {
        return role;
    }

    // Setters are omitted to prevent outside modification of ID/Role/Name after creation
    // If modification is needed, you would add public setters here.

    public void setEmailUsername(String emailUsername) {
        this.emailUsername = emailUsername;
    }

    public void setName(String name){
        this.name = name;
    }
}