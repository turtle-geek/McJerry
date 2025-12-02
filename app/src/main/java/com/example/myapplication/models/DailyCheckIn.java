package com.example.myapplication.models;

import java.util.List;

public class DailyCheckIn {
    private String username;
    private long checkInTimestamp;
    private String entryAuthor;
    private boolean nightWaking;
    private int activityLimits;
    private int cough;
    private List<String> selectedTriggers;

    public DailyCheckIn() {
    }

    public DailyCheckIn(String username, long checkInTimestamp, String entryAuthor, boolean nightWaking,
                        int activityLimits, int cough, List<String> selectedTriggers) {
        this.username = username;
        this.checkInTimestamp = checkInTimestamp;
        this.entryAuthor = entryAuthor;
        this.nightWaking = nightWaking;
        this.activityLimits = activityLimits;
        this.cough = cough;
        this.selectedTriggers = selectedTriggers;
    }

    public String getUsername() {
        return username;
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

    public boolean getNightWaking() {
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

    public int getCough() {
        return cough;
    }

    public void setCough(int cough) {
        this.cough = cough;
    }

    public List<String> getSelectedTriggers() {
        return selectedTriggers;
    }

    public void setSelectedTriggers(List<String> selectedTriggers) {
        this.selectedTriggers = selectedTriggers;
    }
}