package com.example.myapplication.health;

import com.example.myapplication.models.TechniqueQuality;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MedicineUsageLog {
    private InventoryItem medicine;
    private double dosageAmount;
    private String timestamp;
    private TechniqueQuality techniqueQuality;
    private String rating;

    public MedicineUsageLog() {}

    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, String timestamp, TechniqueQuality techniqueQuality) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.techniqueQuality = techniqueQuality;
        this.rating = null;
    }

    // NEW: Constructor with rating
    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, String timestamp, TechniqueQuality techniqueQuality, String rating) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.techniqueQuality = techniqueQuality;
        this.rating = rating;
    }

    public InventoryItem getMedicine() {
        return medicine;
    }

    public void setMedicine(InventoryItem medicine) {
        this.medicine = medicine;
    }

    public double getDosageAmount() {
        return dosageAmount;
    }

    public void setDosageAmount(double dosageAmount) {
        this.dosageAmount = dosageAmount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime parseTimestamp() {
        return LocalDateTime.parse(timestamp);
    }

    public LocalDate parseDate() {
        return LocalDateTime.parse(timestamp).toLocalDate();
    }

    public TechniqueQuality getTechniqueQuality() {
        return techniqueQuality;
    }

    public void setTechniqueQuality(TechniqueQuality techniqueQuality) {
        this.techniqueQuality = techniqueQuality;
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