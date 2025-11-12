package com.example.myapplication;

import java.util.ArrayList;

public class HealthProfile {
    private ArrayList<String> rescueLogs;
    private ArrayList<String> controllerAdherence;
    private ArrayList<String> symptoms;
    private ArrayList<String> triggers;
    private double PEF;
    private ArrayList<String> triageIncidents;
    private ArrayList<Chart> charts;

    // Default constructor
    public HealthProfile() {
        rescueLogs = new ArrayList<String>();
        controllerAdherence = new ArrayList<String>();
        symptoms = new ArrayList<String>();
        triggers = new ArrayList<String>();
        PEF = 0;
        triageIncidents = new ArrayList<String>();
        charts = new ArrayList<Chart>();
    }

    // Public Getters and Setters

    public ArrayList<String> getRescueLogs() {
        return rescueLogs;
    }

    public void addRescueLog(String log) {
        rescueLogs.add(log);
    }

    public ArrayList<String> getControllerAdherence() {
        return controllerAdherence;
    }

    public void addControllerAdherence(String adherence) {
        controllerAdherence.add(adherence);
    }

    public ArrayList<String> getSymptoms() {
        return symptoms;
    }

    public void addSymptom(String symptom) {
        symptoms.add(symptom);
    }

    public ArrayList<String> getTriggers() {
        return triggers;
    }

    public void addTrigger(String trigger) {
        triggers.add(trigger);
    }

    public double getPEF() {
        return PEF;
    }

    public void setPEF(double PEF) {
        this.PEF = PEF;
    }

    public ArrayList<String> getTriageIncidents() {
        return triageIncidents;
    }

    public void addTriageIncident(String incident) {
        triageIncidents.add(incident);
    }

    public ArrayList<Chart> getCharts() {
        return charts;
    }

    public void addChart(Chart chart) {
        charts.add(chart);
    }
}
