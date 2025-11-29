package com.example.myapplication.health;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.UUID;

public class SharedAccessInvite {
    private final String providerID;
    private final String childID;
    private final EnumSet<HealthInfo> sharedFields;
    private final String inviteCode;
    private final LocalDateTime expiryDate;
    private boolean isUsed;

    public SharedAccessInvite(String providerID, String childID, EnumSet<HealthInfo> sharedFields, int daysValid) {
        this.providerID = providerID;
        this.childID = childID;
        this.sharedFields = EnumSet.copyOf(sharedFields);
        this.inviteCode = UUID.randomUUID().toString();
        this.expiryDate = LocalDateTime.now().plusDays(daysValid);
        this.isUsed = false;
    }

    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiryDate);
    }

    public void markAsUsed() { this.isUsed = true; }

    // Public getters
    public String getProviderID() { return providerID; }
    public String getChildID() { return childID; }
    public EnumSet<HealthInfo> getSharedFields() { return sharedFields; }
    public String getInviteCode() { return inviteCode; }
    public LocalDateTime getExpiryDate() { return expiryDate; }
    public boolean getIsUsed() { return isUsed; }
}