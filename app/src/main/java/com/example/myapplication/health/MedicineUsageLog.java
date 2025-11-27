package com.example.myapplication.health;

import com.example.myapplication.models.TechniqueQuality;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicineUsageLog {
    private InventoryItem medicine;
    private double dosageAmount;
    private LocalDateTime timestamp;
    private TechniqueQuality techniqueQuality;

    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, LocalDateTime timestamp, TechniqueQuality techniqueQuality) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.techniqueQuality = techniqueQuality;
    }

    public InventoryItem getMedicineName() {
        return medicine;
    }

    public double getDosageAmount() {
        return dosageAmount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LocalDate getDate() {
        return timestamp.toLocalDate();
    }

    public TechniqueQuality getControllerQuality() {
        return techniqueQuality;
    }

    @Override
    public String toString() {
        return "Medicine: " + medicine.toString() + ", Dosage: " + dosageAmount + ", Timestamp: " + timestamp;
    }
}
