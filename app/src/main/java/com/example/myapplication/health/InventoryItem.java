package com.example.myapplication.health;

import java.time.LocalDateTime;

public class InventoryItem {
    private final String name;
    private double amount;
    private final double capacity;
    private final LocalDateTime purchaseDate;
    private final LocalDateTime expiryDate;
    private final MedicineLabel label;

    public InventoryItem(String name, double amount, double capacity, LocalDateTime purchaseDate, LocalDateTime expiryDate, MedicineLabel label) {
        this.name = name;
        this.amount = amount;
        this.capacity = capacity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
        this.label = label;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getCapacity() {
        return capacity;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public MedicineLabel getLabel() {
        return label;
    }

    public boolean lowVolumeAlert() {
        return amount / capacity <= 0.2;
    }

    public boolean expiryAlert() {
        return LocalDateTime.now().isAfter(expiryDate.minusWeeks(4)); // 4 weeks before expiry
    }

    @Override
    public String toString() {
        return name;
    }
}
