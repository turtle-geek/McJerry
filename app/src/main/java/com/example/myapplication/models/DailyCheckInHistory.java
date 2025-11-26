// CheckInHistory.java
package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;

public class DailyCheckInHistory {
    // A dynamic array (ArrayList) to store all the check-in entries
    private List<DailyCheckIn> historyLogs;

    public DailyCheckInHistory() {
        // Initialize the list when the object is created
        this.historyLogs = new ArrayList<>();
    }

    /**
     * Adds a new daily check-in entry to the history.
     * @param entry The completed AsthmaCheckIn object.
     */
    public void addEntry(DailyCheckIn entry) {
        this.historyLogs.add(entry);
    }

    /**
     * Returns the full list of check-in history.
     * This will be used to implement the history browser, filtering, and export.
     */
    public List<DailyCheckIn> getAllEntries() {
        return new ArrayList<>(historyLogs); // Return a copy to prevent external modification
    }

    // You will add methods here later for:
    // - getEntriesByDateRange(long startTimestamp, long endTimestamp)
    // - filterByTrigger(String trigger)
    // - filterBySymptom(int minCoughWheeze)
    // - exportToCSV(List<AsthmaCheckIn> entries)
}