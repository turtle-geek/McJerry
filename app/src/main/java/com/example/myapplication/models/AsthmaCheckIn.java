package com.example.myapplication.models;

import java.util.List;

public class AsthmaCheckIn {
    private long checkInTimestamp;
    private String entryAuthor;
    private boolean nightWaking;
    private int activityLimits; // scale from 0-3
    private int coughWheeze; // scale from 1-5
    private List<String> selectedTriggers;

    public AsthmaCheckIn(long checkInTimestamp, String entryAuthor, boolean nightWaking,
                         int activityLimits, int coughWheeze, List<String> selectedTriggers) {
        this.checkInTimestamp = checkInTimestamp;
        this.entryAuthor = entryAuthor;
        this.nightWaking = nightWaking;
        this.activityLimits = activityLimits;
        this.coughWheeze = coughWheeze;
        this.selectedTriggers = selectedTriggers;
    }

    public long getCheckInTimestamp() {
        return checkInTimestamp;
    }

    public void setCheckInTimestamp(long checkInTimestamp) {
        this.checkInTimestamp = checkInTimestamp;
    }

    public String getEntryAuthor() {
        return entryAuthor;
    }

    public void setEntryAuthor(String entryAuthor) {
        this.entryAuthor = entryAuthor;
    }

    public boolean isNightWaking() {
        return nightWaking;
    }

    public void setNightWaking(boolean nightWaking) {
        this.nightWaking = nightWaking;
    }

    public int getActivityLimits() {
        return activityLimits;
    }

    public void setActivityLimits(int activityLimits) {
        this.activityLimits = activityLimits;
    }

    public int getCoughWheeze() {
        return coughWheeze;
    }

    public void setCoughWheeze(int coughWheeze) {
        this.coughWheeze = coughWheeze;
    }

    public List<String> getSelectedTriggers() {
        return selectedTriggers;
    }

    public void setSelectedTriggers(List<String> selectedTriggers) {
        this.selectedTriggers = selectedTriggers;
    }
}
