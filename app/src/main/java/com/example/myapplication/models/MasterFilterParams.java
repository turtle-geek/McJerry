// MasterFilterParams.java
package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;

public class MasterFilterParams {
    // Child Username
    public String username;
    // Date Range
    public Long startTimestamp = null;
    public Long endTimestamp = null;

    // Trigger Filter (for multiple triggers)
    public List<String> selectedTriggers = new ArrayList<>();

    // Symptom Filters (true if filter is active)
    public Boolean nightWaking = null;
    public Boolean activityLimits = null;
    public Integer minCoughWheezeScore = null;
    public Integer maxCoughWheezeScore = null;
}