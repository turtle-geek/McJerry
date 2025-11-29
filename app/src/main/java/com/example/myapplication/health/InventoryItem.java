package com.example.myapplication.health;

import java.time.LocalDate;

public class InventoryItem {
    private double amount;
    private final double capacity;
    private final LocalDate purchaseDate;
    private final LocalDate expiryDate;

    public InventoryItem(double amount, double capacity, LocalDate purchaseDate, LocalDate expiryDate) {
        this.amount = amount;
        this.capacity = capacity;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
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

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDate getPurchaseDate() {
        return purchaseDate;
    }

    public boolean lowVolumeAlert() {
        return amount / (capacity + 1e-10) <= 0.2;
    }

    public boolean expiryAlert() {
        return LocalDate.now().isAfter(expiryDate);
    }
}