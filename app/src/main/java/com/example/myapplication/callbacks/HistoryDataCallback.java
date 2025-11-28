package com.example.myapplication.callbacks;

import com.example.myapplication.models.DailyCheckIn;

import java.util.List;

public interface HistoryDataCallback {
    void onDataReceived(List<DailyCheckIn> results);
    void onFailure(Exception e);
}