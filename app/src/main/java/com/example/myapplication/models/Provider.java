package com.example.myapplication.models;
import java.util.ArrayList;

public class Provider extends User{
    String email;
    String password;
    ArrayList<Integer> patients;

    public Provider(int id, String name, String email, String password, String role) {
        super(id, name, role);
        this.email = email;
        this.password = password;
        patients = new ArrayList<Integer>();
    }
    /** This method adds an existing patient into the list of the provider's patients.
     * Verification must have passed before this method is called
     * @param id of the patient
     */
    public void addPatient(int id) { //not to be called directly by actual user - AuthMan should call this
        Integer idPatient = (Integer)id;
        patients.add(idPatient);
    }
}
