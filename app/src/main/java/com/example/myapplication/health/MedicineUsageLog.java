package com.example.myapplication.health;

import com.example.myapplication.models.TechniqueQuality;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicineUsageLog {
    private InventoryItem medicine;
    private double dosageAmount;
    private LocalDateTime timestamp;
    private TechniqueQuality techniqueQuality;
    private String rating;

    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, LocalDateTime timestamp, TechniqueQuality techniqueQuality) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.techniqueQuality = techniqueQuality;
        this.rating = null;
    }

    // NEW: Constructor with rating
    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, LocalDateTime timestamp, TechniqueQuality techniqueQuality, String rating) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.techniqueQuality = techniqueQuality;
        this.rating = rating;
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

    // NEW: Rating getter and setter
    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        String ratingText = (rating != null) ? ", Rating: " + rating : "";

        if (techniqueQuality != TechniqueQuality.NA)
            return "Medicine: " + medicine.toString() + ", Dosage: " + dosageAmount + ratingText + ", Timestamp: " + timestamp;
        else
            return "Medicine: " + medicine.toString() + ", Dosage: " + dosageAmount + ratingText + ", Timestamp: " + timestamp + ", Controller Quality: " + techniqueQuality;
    }
}