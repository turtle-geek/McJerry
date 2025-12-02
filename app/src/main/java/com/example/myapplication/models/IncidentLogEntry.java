package com.example.myapplication.models;

import java.time.LocalDateTime;
import java.util.List;

public class IncidentLogEntry {

    private String username;
    private LocalDateTime timestamp;
    private List<Integer> selectedSymptomIds;
    private String finalDecision;
    private boolean rescueAttemptMade;
    private boolean peakFlowEntered;
    private int peakFlowValue;

    public IncidentLogEntry() {
    }

    public IncidentLogEntry(
            String username,
            LocalDateTime timestamp,
            List<Integer> selectedSymptomIds,
            String finalDecision,
            boolean rescueAttemptMade,
            boolean peakFlowEntered,
            int peakFlowValue) {

        this.username = username;
        this.timestamp = timestamp;
        this.selectedSymptomIds = selectedSymptomIds;
        this.finalDecision = finalDecision;
        this.rescueAttemptMade = rescueAttemptMade;
        this.peakFlowEntered = peakFlowEntered;
        this.peakFlowValue = peakFlowValue;
    }

    // --- Getters ---

    public String getUsername() {
        return username;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public List<Integer> getSelectedSymptomIds() {
        return selectedSymptomIds;
    }

    public String getFinalDecision() {
        return finalDecision;
    }

    public boolean isRescueAttemptMade() {
        return rescueAttemptMade;
    }

    public boolean isPeakFlowEntered() {
        return peakFlowEntered;
    }

    public int getPeakFlowValue() {
        return peakFlowValue;
    }

    // --- Setters (Matching the mutable pattern of DailyCheckIn) ---

    public void setUsername(String username) {
        this.username = username;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setSelectedSymptomIds(List<Integer> selectedSymptomIds) {
        this.selectedSymptomIds = selectedSymptomIds;
    }

    public void setFinalDecision(String finalDecision) {
        this.finalDecision = finalDecision;
    }

    public void setRescueAttemptMade(boolean rescueAttemptMade) {
        this.rescueAttemptMade = rescueAttemptMade;
    }

    public void setPeakFlowEntered(boolean peakFlowEntered) {
        this.peakFlowEntered = peakFlowEntered;
    }

    public void setPeakFlowValue(int peakFlowValue) {
        this.peakFlowValue = peakFlowValue;
    }
}