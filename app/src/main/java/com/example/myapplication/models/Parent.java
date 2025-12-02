package com.example.myapplication.models;

import com.example.myapplication.health.HealthInfo;
import java.util.ArrayList;
import java.util.EnumSet;
import com.example.myapplication.health.SharedAccessInvite;
import com.example.myapplication.health.Inventory;

public class Parent extends User{
    private ArrayList<Child> children;
    private String providerID;
    private ArrayList<SharedAccessInvite> invites;
    static int idChildModifier;

    public Parent() {
        super();
        this.children = new ArrayList<>();
        this.invites = new ArrayList<>();
    }

    public Parent(String id, String name, String emailUsername, String role) {
        super(id, name, role);
        this.emailUsername = emailUsername;
        this.children = new ArrayList<>(); // Using diamond operator for cleaner code
        this.invites = new ArrayList<>();
    }

    public void createChild(String idParent, String childName) {
        String idChild = id + idChildModifier;
        idChildModifier++;
        Child child = new Child(idChild, idParent, name, emailUsername, role);
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public void addProvider(String providerID) {
        this.providerID = providerID; // setter logic
    }

    // Public Getters and Setters
    public String getEmailUsername() {
        return emailUsername;
    }

    public ArrayList<Child> getChildren() {
        // Return a copy or an unmodifiable list for better encapsulation if needed,
        // but for now, returning the reference allows access to the list contents.
        return children;
    }

    public String getProviderID() {
        return providerID;
    }
    // Note: addProvider(int) serves as the setter for providerID

    public ArrayList<SharedAccessInvite> getInvites() {
        return invites;
    }

    public void setInvites(ArrayList<SharedAccessInvite> invites) {
        this.invites = invites;
    }

    // ----- Sharing invitation -----

    public SharedAccessInvite generateInvite(String childID, ArrayList<HealthInfo> sharedFields) {
        SharedAccessInvite invite = new SharedAccessInvite(childID, sharedFields, 7);
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

    public Inventory getInventory(Child child) {
        return child.getInventory();
    }

    // Might be redundant
//    public ArrayList<String> viewControllerUsage(Child child) {
//        return child.getInventory().getControllerLog();
//    }
//
//    public ArrayList<String> viewRescueUsage(Child child) {
//        return child.getInventory().getRescueLog();
//    }

    public StreakCount getStreakCount(Child child) {
        return child.getStreakCount();
    }

    public Badges getBadges(Child child) {
        return child.getBadges();
    }
}