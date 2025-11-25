package com.example.myapplication.health;

import java.util.ArrayList;
import java.time.LocalDateTime;

public class Inventory {
    private ArrayList<InventoryItem> inventory;
    private ArrayList<String> controllerLog;
    private ArrayList<String> rescueLog;

    public Inventory() {
        inventory = new ArrayList<>();
        controllerLog = new ArrayList<>();
        rescueLog = new ArrayList<>();
    }

    public ArrayList<InventoryItem> getInventory() {
        return inventory;
    }

    public void addItem(InventoryItem medicine) {
        inventory.add(medicine);
    }

    public boolean useMedicine(int index, double amount) {
        if (index < 0 || index >= inventory.size())
            return false;
        InventoryItem medicine = inventory.get(index);
        if (amount > medicine.getAmount())
            return false;
        else {
            medicine.setAmount(medicine.getAmount() - amount);
            if (medicine.getAmount() == 0) {
                inventory.remove(medicine);
                if (medicine.getLabel() == MedicineLabel.CONTROLLER)
                    controllerLog.add("Medicine removed from inventory: " + medicine.getName());
                else
                    rescueLog.add("Medicine removed from inventory: " + medicine.getName());
            }
            if (medicine.getLabel() == MedicineLabel.CONTROLLER)
                controllerLog.add("Medicine used: " + medicine.getName() + ", Amount: " + amount+ "Time: " + LocalDateTime.now());
            else
                rescueLog.add("Medicine used: " + medicine.getName() + ", Amount: " + amount+ "Time: " + LocalDateTime.now());
            return true;
        }
    }


}