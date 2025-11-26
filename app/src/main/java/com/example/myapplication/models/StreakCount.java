package com.example.myapplication.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

public class StreakCount {
    private ArrayList<StreakDay> streakDays;
    private PlannedControllerSchedule schedule;
    private int controllerStreak;
    private int techniqueStreak;
    private int rescueCount;

    public StreakCount() {
        streakDays = new ArrayList<>();
        schedule = new PlannedControllerSchedule();
        controllerStreak = 0;
        techniqueStreak = 0;
        rescueCount = 0;
    }

    public void addStreakDay(StreakDay day) {
        streakDays.add(day);
    }

    public void setSchedule(PlannedControllerSchedule schedule) { this.schedule = schedule; }
    
    public int getControllerStreak() {
        return controllerStreak;
    }
    
    public int getTechniqueStreak() {
        return techniqueStreak;
    }

    public int getRescueCount() {
        return rescueCount;
    }

    private int getMonthlyControllerUsage() {
        // Calculate rescueCount: number of controllerUsage=true in the current month.
        // "Current month" is the month of the last entry in streakDays.
        LocalDate lastDayDate = streakDays.get(streakDays.size() - 1).getDate();
        int currentMonth = lastDayDate.getMonthValue();
        int currentYear = lastDayDate.getYear();

        int monthlyControllerUsage = 0;
        for (StreakDay day : streakDays) {
            if (day.getDate().getYear() == currentYear && day.getDate().getMonthValue() == currentMonth) {
                if (day.isControllerUsage()) {
                    monthlyControllerUsage++;
                }
            }
        }
        return monthlyControllerUsage;
    }

    private int getCurrentControllerStreak() {

        // Sort scheduled dates to find the most recent ones in order
        ArrayList<LocalDate> scheduledDates = new ArrayList<>(schedule.getScheduledDates());
        scheduledDates.sort(Comparator.naturalOrder());

        if (scheduledDates.isEmpty() || streakDays.isEmpty()) {
            return 0;
        }

        // Start from the last scheduled date that appears in streakDays
        LocalDate lastLoggedDate = streakDays.get(streakDays.size() - 1).getDate();
        int lastIndex = scheduledDates.lastIndexOf(lastLoggedDate);
        if (lastIndex == -1) {
            return 0;
        }

        int streak = 0;

        // Walk backwards through scheduled dates
        for (int i = lastIndex; i >= 0; i--) {
            LocalDate scheduledDate = scheduledDates.get(i);

            // Find matching StreakDay
            StreakDay day = streakDays.stream()
                    .filter(d -> d.getDate().equals(scheduledDate))
                    .findFirst()
                    .orElse(null);

            if (day == null || !day.isControllerUsage()) {
                break;
            }

            streak++;
        }

        return streak;
    }

    private int getCurrentTechniqueStreak() {
        // Calculate the current streak of days where techniqueQuality is HIGH
        int currentTechniqueStreak = 0;
        LocalDate expectedTechniqueDate = streakDays.get(streakDays.size() - 1).getDate();

        for (int i = streakDays.size() - 1; i >= 0; i--) {
            StreakDay day = streakDays.get(i);

            if (!day.getDate().equals(expectedTechniqueDate)) {
                break;
            }

            if (day.getTechniqueQuality() == TechniqueQuality.HIGH) {
                currentTechniqueStreak++;
                expectedTechniqueDate = expectedTechniqueDate.minusDays(1);
            } else {
                break;
            }
        }
        return currentTechniqueStreak;
    }
    
    public void countStreaks() {
        // Reset counters
        controllerStreak = 0;
        techniqueStreak = 0;
        rescueCount = 0;

        if (streakDays.isEmpty()) {
            return;
        }

        // Sort by date to ensure correct streak calculation
        streakDays.sort(Comparator.comparing(StreakDay::getDate));

        this.controllerStreak = getCurrentControllerStreak();
        this.techniqueStreak = getCurrentTechniqueStreak();
        this.rescueCount = getMonthlyControllerUsage();
    }
}
