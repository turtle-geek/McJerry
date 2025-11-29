package com.example.myapplication.models;

import java.time.LocalDateTime;

public class PeakFlow {
    int peakFlow;
    LocalDateTime time;  // Keep as package-private or private
    String zone;

    public PeakFlow(int peakFlow, LocalDateTime time) {
        this.peakFlow = peakFlow;
        this.time = time;
    }

    public int getPeakFlow() {
        return peakFlow;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String computeZone(Child child){
        int personalBest = child.getHealthProfile().getPEF_PB();
        if (peakFlow >= 0.8*personalBest){
            zone = "green";
        } else if (peakFlow >= 0.5*personalBest){
            zone = "yellow";
        } else {
            zone = "red";
        }
        return zone;
    }
}