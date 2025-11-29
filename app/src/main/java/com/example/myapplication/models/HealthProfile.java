package com.example.myapplication.models;

import com.example.myapplication.health.Chart;

import java.util.ArrayList;

public class HealthProfile {
    private ArrayList<String> rescueLogs;
    private ArrayList<String> controllerAdherence;
    private ArrayList<String> symptoms;
    private ArrayList<String> triggers;
    private ArrayList<PeakFlow> PEF_LOG;
    private int PEF_PB;
    private ArrayList<String> triageIncidents;
    private ArrayList<Chart> charts;

    // Default constructor
    public HealthProfile() {
        rescueLogs = new ArrayList<>();
        controllerAdherence = new ArrayList<>();
        symptoms = new ArrayList<>();
        triggers = new ArrayList<>();
        PEF_LOG = new ArrayList<>();
        triageIncidents = new ArrayList<>();
        charts = new ArrayList<>();
    }

    // Public Getters and Setters

    public void setRescueLogs(ArrayList<String> rescueLogs) {
        this.rescueLogs = rescueLogs;
    }

    public ArrayList<String> getRescueLogs() {
        return rescueLogs;
    }

    public void addRescueLog(String log) {
        rescueLogs.add(log);
    }

    public void setControllerAdherence(ArrayList<String> adherence) {
        this.controllerAdherence = adherence;
    }

    public ArrayList<String> getControllerAdherence() {
        return controllerAdherence;
    }

    public void addControllerAdherence(String adherence) {
        controllerAdherence.add(adherence);
    }

    public void setSymptoms(ArrayList<String> symptoms) {
        this.symptoms = symptoms;
    }

    public ArrayList<String> getSymptoms() {
        return symptoms;
    }

    public void addSymptom(String symptom) {
        symptoms.add(symptom);
    }

    public void setTriggers(ArrayList<String> triggers) {
        this.triggers = triggers;
    }

    public ArrayList<String> getTriggers() {
        return triggers;
    }

    public void addTrigger(String trigger) {
        triggers.add(trigger);
    }

    public void setPEFLOG(ArrayList<PeakFlow> log) {
        this.PEF_LOG = log;
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

    public void setTriageIncidents(ArrayList<String> incidents) {
        this.triageIncidents = incidents;
    }

    public void addTriageIncident(String incident) {
        triageIncidents.add(incident);
    }

    public void setCharts(ArrayList<Chart> charts) {
        this.charts = charts;
    }

    public ArrayList<Chart> getCharts() {
        return charts;
    }

    public void addChart(Chart chart) {
        charts.add(chart);
    }
}
