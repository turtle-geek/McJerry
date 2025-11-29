package com.example.myapplication.health;

import java.util.ArrayList;

public class TechniqueSessionCount {
    private ArrayList<TechniqueSession> techniqueSessions;

    public TechniqueSessionCount() {
        techniqueSessions = new ArrayList<>();
    }

    public void addTechniqueSession(TechniqueSession techniqueSession) {
        techniqueSessions.add(techniqueSession);
    }

    public ArrayList<TechniqueSession> getTechniqueSessions() {
        return techniqueSessions;
    }
}