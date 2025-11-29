package com.example.myapplication.models;

public class Badges {
    private int controllerStreakThreshold;
    private int techniqueStreakThreshold;
    private int rescueCountThreshold;
    private boolean controllerBadge;
    private boolean techniqueBadge;
    private boolean rescueBadge;
    private StreakCount streakCount;

    // Default constructor
    // Thresholds are configurable in settings
    public Badges() {
        this.controllerStreakThreshold = 7;
        this.techniqueStreakThreshold = 10;
        this.rescueCountThreshold = 4;
        this.controllerBadge = false;
        this.techniqueBadge = false;
        this.rescueBadge = false;
        this.streakCount = new StreakCount();
    }

    public void setControllerStreakThreshold(int threshold) {
        this.controllerStreakThreshold = threshold;
    }

    public void setTechniqueStreakThreshold(int threshold) {
        this.techniqueStreakThreshold = threshold;
    }

    public void setRescueCountThreshold(int threshold) {
        this.rescueCountThreshold = threshold;
    }

    public void setStreakCount(StreakCount streakCount) {
        this.streakCount = streakCount;
    }

    public boolean isControllerBadge() {
        return controllerBadge;
    }

    public boolean isTechniqueBadge() {
        return techniqueBadge;
    }

    public boolean isRescueBadge() {
        return rescueBadge;
    }

    public void updateControllerBadge() {
        controllerBadge = streakCount.getControllerStreak() >= controllerStreakThreshold;
    }

    public void updateTechniqueBadge() {
        techniqueBadge = streakCount.getTechniqueStreak() >= techniqueStreakThreshold;
    }

    public void updateRescueBadge() {
        rescueBadge = streakCount.getRescueCount() <= rescueCountThreshold;
    }
}
