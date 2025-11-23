package com.example.myapplication.health;

import java.time.LocalDateTime;

public class InventoryItem {
    private double amount;
    private final LocalDateTime purchaseDate;
    private final LocalDateTime expiryDate;

    public InventoryItem(double amount, LocalDateTime purchaseDate, LocalDateTime expiryDate) {
        this.amount = amount;
        this.purchaseDate = purchaseDate;
        this.expiryDate = expiryDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }
}
