package com.example.myapplication.models;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import com.example.myapplication.Chart;
import com.example.myapplication.HealthInfo;
import com.example.myapplication.HealthProfile;
import com.example.myapplication.SharedAccessInvite;

public class Parent extends User{
    private final ArrayList<Child> children;
    private int providerID;
    private ArrayList<SharedAccessInvite> invites;
    static int idChildModifier;

    public Parent(String id, String name, String email) {
        super(id, name);
        this.email = email;
        this.children = new ArrayList<>(); // Using diamond operator for cleaner code
    }

    public void createChild(String idParent, String childName) {
        String idChild = id + idChildModifier;
        idChildModifier++;
        Child child = new Child(idChild, idParent, name, email,"nested");
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

    // ----- Sharing invitation -----

    public SharedAccessInvite generateInvite(int providerID, int childID, EnumSet<HealthInfo> sharedFields) {
        SharedAccessInvite invite = new SharedAccessInvite(providerID, childID, sharedFields, 7);
        invites.add(invite);
        return invite;
    }

    public SharedAccessInvite getInviteByCode(String code) {
        for (SharedAccessInvite invite : invites) {
            if (invite.getInviteCode().equals(code))
                return invite;
        }
        return null;
    }
}