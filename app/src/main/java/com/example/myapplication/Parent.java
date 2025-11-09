package com.example.myapplication;
import java.util.ArrayList;

public class Parent extends User{
    private String email;
    private String password;
    private final ArrayList<Child> children;
    private int providerID;

    public Parent(int id, String name, String email, String password, String role) {
        super(id, name, role);
        this.email = email;
        this.password = password;
        this.children = new ArrayList<>(); // Using diamond operator for cleaner code
    }

    public void createChild(int idChild, int idParent, String name) {
        Child child = new Child(idChild, idParent, name, "child");
        children.add(child);
    }

    public void addProvider(int providerID) {
        this.providerID = providerID; // setter logic
    }

    // Public Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<Child> getChildren() {
        // Return a copy or an unmodifiable list for better encapsulation if needed,
        // but for now, returning the reference allows access to the list contents.
        return children;
    }

    public int getProviderID() {
        return providerID;
    }
    // Note: addProvider(int) serves as the setter for providerID
}