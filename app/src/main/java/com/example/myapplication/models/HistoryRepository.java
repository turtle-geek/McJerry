package com.example.myapplication.models;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.example.myapplication.callbacks.HistoryDataCallback;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DailyCheckInHistory historyManager;

    public HistoryRepository(DailyCheckInHistory historyManager) {
        this.historyManager = historyManager;
    }

    public void fetchAndFilterDataAsync(MasterFilterParams params, HistoryDataCallback callback) {
        db.collection("daily_checkins")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DailyCheckIn> rawEntries = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            try {
                                // Assume DailyCheckIn has a public no-argument constructor for Firebase to use
                                DailyCheckIn entry = doc.toObject(DailyCheckIn.class);
                                if (entry != null) {
                                    rawEntries.add(entry);
                                }
                            } catch (Exception e) {
                                Log.e("HistoryRepo", "Error converting document: " + e.getMessage());
                            }
                        }

                        // Update local data safely using the getter
                        historyManager.getHistoryLogs().clear();
                        historyManager.getHistoryLogs().addAll(rawEntries);

                        // Apply master filter (synchronous)
                        List<DailyCheckIn> filteredResults = historyManager.masterFilter(params);

                        callback.onDataReceived(filteredResults);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}