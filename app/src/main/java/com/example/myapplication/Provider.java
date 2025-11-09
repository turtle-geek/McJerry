package com.example.myapplication;
import java.util.ArrayList;

public class Provider extends User{
    private String email;
    private String password;
    private final ArrayList<Integer> patients;

    public Provider(int id, String name, String email, String password, String role) {
        super(id, name, role);
        this.email = email;
        this.password = password;
        patients = new ArrayList<>(); // Using diamond operator
    }

    /** This method adds an existing patient into the list of the provider's patients.
     * @param id of the patient
     */
    public void addPatient(int id) {
        patients.add(id); // Java autoboxing handles the conversion from int to Integer
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

    public ArrayList<Integer> getPatients() {
        return patients;
    }

    // Setter for patients is omitted; the addPatient method controls modification.
}