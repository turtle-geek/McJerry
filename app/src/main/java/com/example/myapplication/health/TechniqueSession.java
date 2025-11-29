package com.example.myapplication.health;

import com.example.myapplication.models.TechniqueQuality;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TechniqueSession {
    private TechniqueQuality techniqueQuality;
    private LocalDateTime timeStamp;

    // To be implemented
    // May include seal lips, slow breath, hold, etc.
    // Refer to https://piazza.com/class/mf00kk8dxik1mj/post/196#

    public TechniqueSession(TechniqueQuality techniqueQuality, LocalDateTime timeStamp) {
        this.techniqueQuality = techniqueQuality;
        this.timeStamp = timeStamp;
    }

    public TechniqueQuality getTechniqueQuality() {
        return techniqueQuality;
    }

    public LocalDateTime getTimeStamp() {
        return timeStamp;
    }

    public LocalDate getDate() {
        return timeStamp.toLocalDate();
    }
}