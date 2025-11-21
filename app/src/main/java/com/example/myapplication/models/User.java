package com.example.myapplication.models;

public abstract class User {
    final String id;
    String name; // name should be modifiable
    String email; // email should be modifiable

    public User(){//Firestore apparently needs no-arg constructor for reading data
        id = "";
        name = "";
    }

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Public Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail(){
        return email;
    }

    // Setters are omitted to prevent outside modification of ID/Role/Name after creation
    // If modification is needed, you would add public setters here.

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name){
        this.name = name;
    }
}