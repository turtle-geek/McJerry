package com.example.myapplication.health;

import com.example.myapplication.models.TechniqueQuality;

import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Inventory {
    private InventoryItem controller;
    private InventoryItem rescue;
    private ArrayList<MedicineUsageLog> controllerLog;
    private ArrayList<MedicineUsageLog> rescueLog;

    public Inventory() {
        controller = new InventoryItem();
        rescue = new InventoryItem();
        controllerLog = new ArrayList<>();
        rescueLog = new ArrayList<>();
    }

    public InventoryItem getController() {
        return controller;
    }

    public void setController(InventoryItem controller) {
        this.controller = controller;
    }

    public InventoryItem getRescue() {
        return rescue;
    }

    public void setRescue(InventoryItem rescue) {
        this.rescue = rescue;
    }

    public InventoryItem getMedicine(MedicineLabel label) {
        if (label == MedicineLabel.CONTROLLER)
            return controller;
        else if (label == MedicineLabel.RESCUE)
            return rescue;
        else
            return null;
    }

    public void setMedicine(MedicineLabel label, InventoryItem medicine) {
        if (label == MedicineLabel.CONTROLLER)
            controller = medicine;
        else if (label == MedicineLabel.RESCUE)
            rescue = medicine;
    }

    public ArrayList<MedicineUsageLog> getControllerLog() {
        return controllerLog;
    }

    public ArrayList<MedicineUsageLog> getRescueLog() {
        return rescueLog;
    }

    public boolean useMedicine(MedicineLabel label, double amount, String timestamp) {
        InventoryItem medicine = getMedicine(label);
        if (amount > medicine.getAmount())
            return false;
        else {
            medicine.setAmount(medicine.getAmount() - amount);
            if (label == MedicineLabel.CONTROLLER) {
                // TODO: move to technique session
                TechniqueQuality techniqueQuality = TechniqueQuality.HIGH; // Placeholder
                controllerLog.add(new MedicineUsageLog(medicine, amount, timestamp, techniqueQuality));
            } else {
                rescueLog.add(new MedicineUsageLog(medicine, amount, timestamp, TechniqueQuality.NA));
            }
            return true;
        }
    }
}