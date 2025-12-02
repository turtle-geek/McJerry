package com.example.myapplication.health;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.UUID;

public class SharedAccessInvite {
    private String providerID;
    private String childID;
    private ArrayList<HealthInfo> sharedFields;
    private String inviteCode;
    private String expiryDate;
    private boolean isUsed;

    public SharedAccessInvite() {}

    public SharedAccessInvite(String childID, ArrayList<HealthInfo> sharedFields, int daysValid) {
        this.providerID = null;
        this.childID = childID;
        this.sharedFields = new ArrayList<>(sharedFields);
        this.inviteCode = UUID.randomUUID().toString();
        this.expiryDate = LocalDate.now().plusDays(daysValid).toString();
        this.isUsed = false;
    }

    public boolean isValid() {
        return !isUsed && LocalDate.now().isBefore(LocalDate.parse(expiryDate));
    }

    public void markAsUsed(String claimingProviderID) {
        this.isUsed = true;
        this.providerID = claimingProviderID;
    }

    // Public getters
    public String getProviderID() { return providerID; }
    public String getChildID() { return childID; }
    public ArrayList<HealthInfo> getSharedFields() { return sharedFields; }
    public String getInviteCode() { return inviteCode; }
    public String getExpiryDate() { return expiryDate; }
    public LocalDate parseExpiryDate() { return LocalDate.parse(expiryDate); }
    public boolean getIsUsed() { return isUsed; }
}