package com.example.myapplication.health;

import java.time.LocalDate;

public class InventoryItem {
    private double amount;
    private double capacity;
    private String purchaseDate;
    private String expiryDate;

    public InventoryItem() {
        this.amount = 0;
        this.capacity = 0;
        this.purchaseDate = LocalDate.now().toString();
        this.expiryDate = LocalDate.now().toString();
    }

    public InventoryItem(double amount, double capacity, String purchaseDate, String expiryDate) {
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

    public String getExpiryDate() {
        return expiryDate;
    }

    public LocalDate parseExpiryDate() {
        return LocalDate.parse(expiryDate);
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public LocalDate parsePurchaseDate() {
        return LocalDate.parse(purchaseDate);
    }

    public boolean lowVolumeAlert() {
        return amount / (capacity + 1e-10) <= 0.2;
    }

    public boolean expiryAlert() {
        return LocalDate.now().isAfter(this.parseExpiryDate());
    }
}