package com.example.myapplication.models;

import java.time.LocalDate;

public class StreakDay {
    private LocalDate date;
    private boolean controllerUsage;
    private boolean rescueUsage;
    private TechniqueQuality techniqueQuality;

    public StreakDay(LocalDate date, boolean controllerUsage, boolean rescueUsage, TechniqueQuality techniqueQuality) {
        this.date = date;
        this.controllerUsage = controllerUsage;
        this.rescueUsage = rescueUsage;
        this.techniqueQuality = techniqueQuality;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isControllerUsage() {
        return controllerUsage;
    }

    public void setControllerUsage(boolean controllerUsage) {
        this.controllerUsage = controllerUsage;
    }

    public boolean isRescueUsage() {
        return rescueUsage;
    }

    public void setRescueUsage(boolean rescueUsage) {
        this.rescueUsage = rescueUsage;
    }

    public TechniqueQuality getTechniqueQuality() {
        return techniqueQuality;
    }

    public void setTechniqueQuality(TechniqueQuality techniqueQuality) {
        this.techniqueQuality = techniqueQuality;
    }
}
