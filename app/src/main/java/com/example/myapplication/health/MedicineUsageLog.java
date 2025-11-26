package com.example.myapplication.health;

import com.example.myapplication.models.TechniqueQuality;

import java.time.LocalDateTime;

public class MedicineUsageLog {
    private String medicineName;
    private double dosageAmount;
    private LocalDateTime timestamp;
    private TechniqueQuality techniqueQuality;

    public MedicineUsageLog(String medicineName, double dosageAmount, LocalDateTime timestamp, TechniqueQuality techniqueQuality) {
        this.medicineName = medicineName;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.techniqueQuality = techniqueQuality;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public double getDosageAmount() {
        return dosageAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Medicine: " + medicineName + ", Dosage: " + dosageAmount + ", Timestamp: " + timestamp;
    }
}
