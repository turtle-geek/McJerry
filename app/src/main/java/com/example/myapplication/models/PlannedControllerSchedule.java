package com.example.myapplication.models;

import java.time.LocalDate;
import java.util.ArrayList;

public class PlannedControllerSchedule {
    private ArrayList<LocalDate> scheduledDates;

    public PlannedControllerSchedule() {
        scheduledDates = new ArrayList<>();
    }

    public void addDate(LocalDate date) {
        scheduledDates.add(date);
    }

    public ArrayList<LocalDate> getScheduledDates() {
        return scheduledDates;
    }
}
