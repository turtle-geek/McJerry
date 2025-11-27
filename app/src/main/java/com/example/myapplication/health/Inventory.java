package com.example.myapplication.health;

import com.example.myapplication.models.TechniqueQuality;

import java.util.ArrayList;
import java.time.LocalDateTime;

public class Inventory {
    private ArrayList<InventoryItem> inventory;
    private ArrayList<MedicineUsageLog> controllerLog;
    private ArrayList<MedicineUsageLog> rescueLog;

    public Inventory() {
        inventory = new ArrayList<>();
        controllerLog = new ArrayList<>();
        rescueLog = new ArrayList<>();
    }

    public ArrayList<InventoryItem> getInventory() {
        return inventory;
    }

    public ArrayList<MedicineUsageLog> getControllerLog() {
        return controllerLog;
    }

    public ArrayList<MedicineUsageLog> getRescueLog() {
        return rescueLog;
    }

    public void addItem(InventoryItem medicine) {
        inventory.add(medicine);
    }

    public boolean useMedicine(int index, double amount, LocalDateTime timestamp, TechniqueQuality techniqueQuality) {
        if (index < 0 || index >= inventory.size())
            return false;
        InventoryItem medicine = inventory.get(index);
        if (amount > medicine.getAmount())
            return false;
        else {
            medicine.setAmount(medicine.getAmount() - amount);
            if (medicine.getAmount() == 0) {
                inventory.remove(medicine);
            }
            if (medicine.getLabel() == MedicineLabel.CONTROLLER)
                controllerLog.add(new MedicineUsageLog(medicine, amount, timestamp, techniqueQuality));
            else
                rescueLog.add(new MedicineUsageLog(medicine, amount, timestamp, techniqueQuality));
            return true;
        }
    }
}