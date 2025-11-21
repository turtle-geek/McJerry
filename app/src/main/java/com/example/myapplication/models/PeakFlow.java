package com.example.myapplication.models;

import java.time.LocalDateTime;
public class PeakFlow {
    int peakFlow;
    LocalDateTime time;
    String zone;

    /**
     * Constructor for PeakFlow
     * @param peakFlow, a measure in integer of how fast a child can blow air out
     * @param time of logging the peak flow value
     */
    public PeakFlow(int peakFlow, LocalDateTime time) {
        this.peakFlow = peakFlow;
        this.time = time;
    }

    /**
     * Getter for peakFlow
     * @return peakFlow
     */
    public int getPeakFlow() {
        return peakFlow;
    }

    /**
     * Setter for zone
     * @return zone of green, yellow, or red
     */
//    public String computeZone(Child child){
//        int personalBest = child.getHealthProfile().getPEF_PB();
//        if (peakFlow >= 0.8*personalBest){
//            zone = "green";
//        } else if (peakFlow >= 0.5*personalBest){
//            zone = "yellow";
//        } else {
//            zone = "red";
//        }
//        return zone;
//    }
}
