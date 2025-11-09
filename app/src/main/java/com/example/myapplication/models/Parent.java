package com.example.myapplication.models;
import java.util.ArrayList;

public class Parent extends User{
    ArrayList<Child> children;
    int providerID;

    public Parent(int id, String name, String email, String password, String role) {
        super(id, name, role);
        // Post-verification, email and password are valid,
        // this constructor is then called by AuthManager based on specified role
        this.email = email;
        this.password = password;
        this.children = new ArrayList<Child>();
    }

    public void createChildProfile(int idChild, int idParent, String name) {
        Child child = new Child(idChild, idParent, name, "child"); // Authentification logic can take care of the rest?
        children.add(child);
    }

    public void addProvider(int providerID) {
        this.providerID = providerID; // assuming we have one provider
    }
}
