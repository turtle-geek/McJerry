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

        // Safety check for inventory
        if (inventory == null) {
            return;
        }

        ArrayList<MedicineUsageLog> controllerLogs = inventory.getControllerLog();
        ArrayList<MedicineUsageLog> rescueLogs = inventory.getRescueLog();

        // --- CONTROLLER LOGIC ---
        // Only calculate controller streak if logs exist and schedule is set
        if (controllerLogs != null && !controllerLogs.isEmpty()) {
            // Find lastControllerDate
            MedicineUsageLog latestControllerLog = controllerLogs.get(0);
            for (MedicineUsageLog log : controllerLogs) {
                if (log.parseTimestamp().isAfter(latestControllerLog.parseTimestamp())) {
                    latestControllerLog = log;
                }
            }
            LocalDate lastControllerDate = latestControllerLog.parseDate();

            // Calculate controllerStreak
            // Added check for schedule != null to prevent NPE
            if (schedule != null) {
                ArrayList<LocalDate> scheduledDates = new ArrayList<>(schedule.getScheduledDates());
                scheduledDates.sort(Comparator.naturalOrder());
                int lastIndex = Collections.binarySearch(scheduledDates, lastControllerDate);

                if (lastIndex >= 0) {
                    int streak = 0;
                    for (int i = lastIndex; i >= 0; i--) {
                        LocalDate scheduledDate = scheduledDates.get(i);
                        boolean hasUsage = false;
                        for (MedicineUsageLog log : controllerLogs) {
                            if (log.parseDate().equals(scheduledDate)) {
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
            }
        }

        // --- RESCUE LOGIC (FIXED) ---
        // Only calculate rescue count if logs exist
        if (rescueLogs != null && !rescueLogs.isEmpty()) {
            MedicineUsageLog latestRescueLog = rescueLogs.get(0);
            for (MedicineUsageLog log : rescueLogs) {
                if (log.parseTimestamp().isAfter(latestRescueLog.parseTimestamp())) {
                    latestRescueLog = log;
                }
            }
            LocalDate lastRescueDate = latestRescueLog.parseDate();

            int currentMonth = lastRescueDate.getMonthValue();
            int currentYear = lastRescueDate.getYear();
            Set<LocalDate> uniqueDates = new HashSet<>();
            for (MedicineUsageLog log : rescueLogs) {
                LocalDate date = log.parseDate();
                if (date.getMonthValue() == currentMonth && date.getYear() == currentYear) {
                    uniqueDates.add(date);
                }
            }
            rescueCount = uniqueDates.size();
        } else {
            rescueCount = 0; // Default to 0 if no logs
        }

        // --- TECHNIQUE LOGIC ---
        if (techniqueSessionCount != null && !techniqueSessionCount.getTechniqueSessions().isEmpty()) {
            ArrayList<TechniqueSession> sessions = new ArrayList<>(techniqueSessionCount.getTechniqueSessions());

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