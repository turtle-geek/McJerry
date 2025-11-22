package com.example.myapplication.models;

import com.example.myapplication.health.Chart;

import java.util.ArrayList;

public class HealthProfile {
    private final ArrayList<String> rescueLogs;
    private final ArrayList<String> controllerAdherence;
    private final ArrayList<String> symptoms;
    private final ArrayList<String> triggers;
    private final ArrayList<PeakFlow> PEF_LOG;
    private int PEF_PB;
    private final ArrayList<String> triageIncidents;
    private final ArrayList<Chart> charts;

    // Default constructor
    public HealthProfile() {
        rescueLogs = new ArrayList<String>();
        controllerAdherence = new ArrayList<String>();
        symptoms = new ArrayList<String>();
        triggers = new ArrayList<String>();
        PEF_LOG = new ArrayList<PeakFlow>();
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

    public ArrayList<PeakFlow> getPEFLog() {
        return PEF_LOG;
    }

    public void addPEFToLog(PeakFlow pef) {
        PEF_LOG.add(pef);
    }

    public int getPEF_PB() {
        return PEF_PB;
    }

    public void setPEF_PB(int pb) {
        PEF_PB = pb;
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
