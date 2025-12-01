// DailyCheckInHistory.java
package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DailyCheckInHistory {
    // A dynamic array (ArrayList) to store all the check-in entries
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

    // -------------------------------------------------------------------------
    // MASTER FILTER METHOD (The Pipeline)
    // -------------------------------------------------------------------------

    /**
     * Filters the entire history log based on the provided MasterFilterParams.
     * Applies filters sequentially (AND logic).
     */
    public List<DailyCheckIn> masterFilter(MasterFilterParams params) {
        // 1. Start with the full list (using a copy)
        List<DailyCheckIn> filteredEntries = getAllEntries();

        // 2. Apply Date Range Filter
        if (params.startTimestamp != null && params.endTimestamp != null) {
            filteredEntries = filterByDateRange(filteredEntries, params.startTimestamp, params.endTimestamp);
        }

        // 3. Apply Multi-Trigger Filter (OR Logic: Entry must contain AT LEAST ONE selected trigger)
        if (params.selectedTriggers != null && !params.selectedTriggers.isEmpty()) {
            filteredEntries = filterByMultipleTriggers(filteredEntries, params.selectedTriggers);
        }

        // 4. Apply Night Waking Filter
        if (params.nightWaking != null && params.nightWaking) {
            filteredEntries = filterByNightWaking(filteredEntries);
        }

        // 5. Apply Activity Limits Filter
        if (params.activityLimits != null && params.activityLimits) {
            filteredEntries = filterByActivityLimits(filteredEntries);
        }

        // 6. Apply Cough Score Filter (Range Check for 0-4 scale)
        if (params.minCoughWheezeScore != null || params.maxCoughWheezeScore != null) {
            // Set defaults if only one boundary is specified. Max is 4 based on your UI.

            // Replaced ternary operator with standard if blocks
            int minScore = 0;
            if (params.minCoughWheezeScore != null) {
                minScore = params.minCoughWheezeScore.intValue();
            }

            // Replaced ternary operator with standard if blocks
            int maxScore = 4;
            if (params.maxCoughWheezeScore != null) {
                maxScore = params.maxCoughWheezeScore.intValue();
            }

            filteredEntries = filterByCoughScoreRange(filteredEntries, minScore, maxScore);
        }

        return filteredEntries;
    }

    // -------------------------------------------------------------------------
    // PIPELINE-FRIENDLY FILTER METHODS
    // -------------------------------------------------------------------------

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