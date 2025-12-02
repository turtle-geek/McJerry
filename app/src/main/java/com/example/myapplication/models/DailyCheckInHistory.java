package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DailyCheckInHistory {
    private List<DailyCheckIn> historyLogs;

    public DailyCheckInHistory() {
        this.historyLogs = new ArrayList<>();
    }

    public List<DailyCheckIn> getHistoryLogs() {
        return this.historyLogs;
    }

    /**
     * Adds a new daily check-in entry to the history.
     */
    public void addEntry(DailyCheckIn entry) {
        this.historyLogs.add(entry);
    }

    /**
     * Returns the full list of check-in history (a copy).
     */
    public List<DailyCheckIn> getAllEntries() {
        return new ArrayList<>(historyLogs);
    }

    /**
     * Filters the entire history log based on the provided MasterFilterParams.
     * Applies filters sequentially (AND logic).
     */
    public List<DailyCheckIn> masterFilter(MasterFilterParams params) {
        List<DailyCheckIn> filteredEntries = getAllEntries();

        // Apply USERNAME Filter (Mandatory, uses childId from params)
        if (params.username != null && !params.username.isEmpty()) {
            filteredEntries = filterByUsername(filteredEntries, params.username);
        } else {
            // If the username is missing, return an empty list immediately
            return new ArrayList<>();
        }

        // Apply Date Range Filter
        if (params.startTimestamp != null && params.endTimestamp != null) {
            filteredEntries = filterByDateRange(filteredEntries, params.startTimestamp, params.endTimestamp);
        }

        // Apply Multi-Trigger Filter (OR Logic: Entry must contain AT LEAST ONE selected trigger)
        if (params.selectedTriggers != null && !params.selectedTriggers.isEmpty()) {
            filteredEntries = filterByMultipleTriggers(filteredEntries, params.selectedTriggers);
        }

        // Apply Night Waking Filter
        if (params.nightWaking != null && params.nightWaking) {
            filteredEntries = filterByNightWaking(filteredEntries);
        }

        // Apply Activity Limits Filter
        if (params.activityLimits != null && params.activityLimits) {
            filteredEntries = filterByActivityLimits(filteredEntries);
        }

        // Apply Cough Score Filter (Range Check for 0-4 scale)
        if (params.minCoughWheezeScore != null || params.maxCoughWheezeScore != null) {
            int minScore = 0;
            if (params.minCoughWheezeScore != null) {
                minScore = params.minCoughWheezeScore.intValue();
            }

            int maxScore = 4;
            if (params.maxCoughWheezeScore != null) {
                maxScore = params.maxCoughWheezeScore.intValue();
            }

            filteredEntries = filterByCoughScoreRange(filteredEntries, minScore, maxScore);
        }

        return filteredEntries;
    }

    /**
     * Filters an existing list by the provided username (childId).
     */
    public List<DailyCheckIn> filterByUsername(List<DailyCheckIn> entriesToFilter, String username) {
        return entriesToFilter.stream()
                .filter(entry -> username.equals(entry.getUsername()))
                .collect(Collectors.toList());
    }

    /**
     * Filters an existing list by date range.
     */
    public List<DailyCheckIn> filterByDateRange(
            List<DailyCheckIn> entriesToFilter, long startTimestamp, long endTimestamp){
        List<DailyCheckIn> result = new ArrayList<>();
        for(DailyCheckIn entry : entriesToFilter){
            if(entry.getCheckInTimestamp() >= startTimestamp &&
                    entry.getCheckInTimestamp() <= endTimestamp){
                result.add(entry);
            }
        }
        return result;
    }

    /**
     * Filters an existing list by AT LEAST ONE of the selected triggers (OR Logic).
     */
    public List<DailyCheckIn> filterByMultipleTriggers(List<DailyCheckIn> entriesToFilter, List<String> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            return entriesToFilter;
        }

        return entriesToFilter.stream()
                .filter(entry -> entry.getSelectedTriggers() != null)
                .filter(entry -> entry.getSelectedTriggers().stream()
                        .anyMatch(triggers::contains))
                .collect(Collectors.toList());
    }

    /**
     * Filters an existing list by Night Waking presence (True only).
     */
    public List<DailyCheckIn> filterByNightWaking(List<DailyCheckIn> entriesToFilter) {
        return entriesToFilter.stream()
                .filter(DailyCheckIn::getNightWaking)
                .collect(Collectors.toList());
    }

    /**
     * Filters an existing list by Activity Limits presence (Limits > 0).
     */
    public List<DailyCheckIn> filterByActivityLimits(List<DailyCheckIn> entriesToFilter) {
        return entriesToFilter.stream()
                .filter(entry -> entry.getActivityLimits() > 0)
                .collect(Collectors.toList());
    }

    /**
     * Filters an existing list by Cough Score within a range (inclusive).
     */
    public List<DailyCheckIn> filterByCoughScoreRange(List<DailyCheckIn> entriesToFilter, int minScore, int maxScore) {
        return entriesToFilter.stream()
                .filter(entry -> entry.getCough() >= minScore && entry.getCough() <= maxScore)
                .collect(Collectors.toList());
    }
}