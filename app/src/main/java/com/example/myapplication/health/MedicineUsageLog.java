package com.example.myapplication.health;

import android.os.Build;

import com.example.myapplication.models.TechniqueQuality;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MedicineUsageLog {
    private InventoryItem medicine;
    private double dosageAmount;
    private String timestamp; // Kept as String for Firebase compatibility
    private LocalDateTime localTimestamp; // Internal use for modern Java Time API
    private TechniqueQuality techniqueQuality;
    private String rating;

    // No-arg constructor for Firebase
    public MedicineUsageLog() {
        // Firebase needs this
    }

    // Helper to format LocalDateTime for storage (using ISO format for consistency)
    private String formatLocalDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return localDateTime.toString(); 
        }
        return localDateTime.toString();
    }

    // ========== BACKWARD COMPATIBILITY CONSTRUCTORS (Accept String) ==========

    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, String timestamp, TechniqueQuality techniqueQuality) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.localTimestamp = parseStringToLocalDateTime(timestamp);
        this.techniqueQuality = techniqueQuality;
        this.rating = null;
    }

    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, String timestamp, TechniqueQuality techniqueQuality, String rating) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.timestamp = timestamp;
        this.localTimestamp = parseStringToLocalDateTime(timestamp);
        this.techniqueQuality = techniqueQuality;
        this.rating = rating;
    }

    // ========== MODERN CONSTRUCTORS (Accept LocalDateTime) ==========

    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, LocalDateTime localTimestamp, TechniqueQuality techniqueQuality) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.localTimestamp = localTimestamp;
        this.timestamp = formatLocalDateTimeToString(localTimestamp); 
        this.techniqueQuality = techniqueQuality;
        this.rating = null;
    }

    public MedicineUsageLog(InventoryItem medicine, double dosageAmount, LocalDateTime localTimestamp, TechniqueQuality techniqueQuality, String rating) {
        this.medicine = medicine;
        this.dosageAmount = dosageAmount;
        this.localTimestamp = localTimestamp;
        this.timestamp = formatLocalDateTimeToString(localTimestamp); 
        this.techniqueQuality = techniqueQuality;
        this.rating = rating;
    }

    // ========== HELPER METHOD ==========

    private LocalDateTime parseStringToLocalDateTime(String timestampStr) {
        if (timestampStr == null || timestampStr.isEmpty()) {
            return LocalDateTime.now();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                return LocalDateTime.parse(timestampStr);
            } catch (Exception e1) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");
                    return LocalDateTime.parse(timestampStr, formatter);
                } catch (Exception e2) {
                    return LocalDateTime.now();
                }
            }
        } else {
            return LocalDateTime.now();
        }
    }

    // ========== GETTERS ==========

    public InventoryItem getMedicine() {
        return medicine;
    }

    public double getDosageAmount() {
        return dosageAmount;
    }
    
    // Getter for Firebase (String field)
    public String getTimestamp() {
        return timestamp;
    }

    // Getter for internal app logic (LocalDateTime field)
    public LocalDateTime getLocalTimestamp() {
        return localTimestamp;
    }

    public String getTimestampAsString() {
        if (localTimestamp == null) {
            return "";
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a");
            return localTimestamp.format(formatter);
        } else {
            return localTimestamp.toString();
        }
    }

    public String getFormattedTimestamp() {
        return getTimestampAsString();
    }

    public LocalDate getDate() {
        if (localTimestamp == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return localTimestamp.toLocalDate();
        }
        return null;
    }

    public TechniqueQuality getTechniqueQuality() {
        return techniqueQuality;
    }

    public String getRating() {
        return rating;
    }

    // ========== SETTERS ==========

    public void setMedicine(InventoryItem medicine) {
        this.medicine = medicine;
    }

    public void setDosageAmount(double dosageAmount) {
        this.dosageAmount = dosageAmount;
    }

    // Setter for Firebase (String field)
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        this.localTimestamp = parseStringToLocalDateTime(timestamp);
    }

    public void setLocalTimestamp(LocalDateTime localTimestamp) {
        this.localTimestamp = localTimestamp;
        this.timestamp = formatLocalDateTimeToString(localTimestamp); 
    }

    public void setTechniqueQuality(TechniqueQuality techniqueQuality) {
        this.techniqueQuality = techniqueQuality;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    // ========== DEPRECATED METHODS ==========

    @Deprecated
    public LocalDateTime parseTimestamp() {
        return localTimestamp;
    }

    @Deprecated
    public LocalDate parseDate() {
        return getDate();
    }

    // ========== toString ==========

    @Override
    public String toString() {
        String ratingText = (rating != null) ? ", Rating: " + rating : "";
        String timestampText = getFormattedTimestamp();

        if (techniqueQuality == TechniqueQuality.NA) {
            return "Dosage: " + dosageAmount +
                    ratingText +
                    ", Timestamp: " + timestampText;
        } else {
            return "Dosage: " + dosageAmount +
                    ratingText +
                    ", Timestamp: " + timestampText +
                    ", Controller Quality: " + techniqueQuality;
        }
    }
}