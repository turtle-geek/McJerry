package com.example.myapplication.models;

import java.time.LocalDate;
import java.util.*;
import com.example.myapplication.health.Inventory;
import com.example.myapplication.health.MedicineUsageLog;
import com.example.myapplication.health.TechniqueSessionCount;
import com.example.myapplication.health.TechniqueSession;

public class StreakCount {
    private Inventory inventory;
    private TechniqueSessionCount techniqueSessionCount;
    private PlannedControllerSchedule schedule;
    private int controllerStreak;
    private int techniqueStreak;
    private int rescueCount;

    public StreakCount() {
        inventory = new Inventory();
        techniqueSessionCount = new TechniqueSessionCount();
        schedule = new PlannedControllerSchedule();
        controllerStreak = 0;
        techniqueStreak = 0;
        rescueCount = 0;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public void setSchedule(PlannedControllerSchedule schedule) {
        this.schedule = schedule;
    }

    public int getControllerStreak() {
        return controllerStreak;
    }

    public int getTechniqueStreak() {
        return techniqueStreak;
    }

    public int getRescueCount() {
        return rescueCount;
    }

    public void countStreaks() {
        // Reset counters
        controllerStreak = 0;
        techniqueStreak = 0;
        rescueCount = 0;

        if (inventory == null || inventory.getControllerLog().isEmpty()) {
            return;
        }

        ArrayList<MedicineUsageLog> controllerLogs = inventory.getControllerLog();
        ArrayList<MedicineUsageLog> rescueLogs = inventory.getRescueLog();

        // Find lastControllerDate: the latest timestamp's date
        MedicineUsageLog latestControllerLog = controllerLogs.get(0);
        for (MedicineUsageLog log : controllerLogs) {
            if (log.getTimestamp().isAfter(latestControllerLog.getTimestamp())) {
                latestControllerLog = log;
            }
        }
        LocalDate lastControllerDate = latestControllerLog.getDate();

        MedicineUsageLog latestRescueLog = rescueLogs.get(0);
        for (MedicineUsageLog log : rescueLogs) {
            if (log.getTimestamp().isAfter(latestRescueLog.getTimestamp())) {
                latestRescueLog = log;
            }
        }
        LocalDate lastRescueDate = latestRescueLog.getDate();

        // Calculate rescueCount: number of distinct days with rescue usage in the current month
        int currentMonth = lastRescueDate.getMonthValue();
        int currentYear = lastRescueDate.getYear();
        Set<LocalDate> uniqueDates = new HashSet<>();
        for (MedicineUsageLog log : rescueLogs) {
            LocalDate date = log.getDate();
            if (date.getMonthValue() == currentMonth && date.getYear() == currentYear) {
                uniqueDates.add(date);
            }
        }
        rescueCount = uniqueDates.size();

        // Calculate controllerStreak
        ArrayList<LocalDate> scheduledDates = new ArrayList<>(schedule.getScheduledDates());
        scheduledDates.sort(Comparator.naturalOrder());
        int lastIndex = Collections.binarySearch(scheduledDates, lastControllerDate);
        if (lastIndex >= 0) {
            int streak = 0;
            for (int i = lastIndex; i >= 0; i--) {
                LocalDate scheduledDate = scheduledDates.get(i);
                boolean hasUsage = false;
                for (MedicineUsageLog log : controllerLogs) {
                    if (log.getDate().equals(scheduledDate)) {
                        hasUsage = true;
                        break;
                    }
                }
                if (hasUsage) {
                    streak++;
                } else {
                    break;
                }
            }
            controllerStreak = streak;
        }

        // Calculate techniqueStreak
        if (techniqueSessionCount != null && !techniqueSessionCount.getTechniqueSessions().isEmpty()) {
            ArrayList<TechniqueSession> sessions = new ArrayList<>(techniqueSessionCount.getTechniqueSessions());

            // Sort descending by date
            sessions.sort((s1, s2) -> s2.getDate().compareTo(s1.getDate()));

            int streak = 0;
            LocalDate lastCheckedDay = null;
            boolean currentDayHasHigh = false;

            for (TechniqueSession session : sessions) {
                LocalDate day = session.getDate();

                if (lastCheckedDay == null || !day.isEqual(lastCheckedDay)) {
                    if (lastCheckedDay != null && !currentDayHasHigh) {
                        break;
                    }

                    lastCheckedDay = day;
                    currentDayHasHigh = (session.getTechniqueQuality() == TechniqueQuality.HIGH);
                } else {
                    if (session.getTechniqueQuality() == TechniqueQuality.HIGH) {
                        currentDayHasHigh = true;
                    }
                }

                if (streak == 0 && currentDayHasHigh) {
                    streak = 1;
                }
            }

            techniqueStreak = streak;
        }
    }
}