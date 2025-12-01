package com.example.myapplication.models;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.myapplication.callbacks.HistoryDataCallback;

import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DailyCheckInHistory historyManager;

    public HistoryRepository(DailyCheckInHistory historyManager) {
        this.historyManager = historyManager;
    }

    /**
     * Builds and executes a Firestore Query based on MasterFilterParams,
     * applying filters server-side before retrieving data.
     */
    public void fetchAndFilterDataAsync(MasterFilterParams params, HistoryDataCallback callback) {

        // 1. Start with the base collection reference
        Query queryRef = db.collection("daily_checkins");

        // --- Apply Server-Side Filters based on MasterFilterParams ---

        // 2. Filter by selectedTriggers using arrayContainsAny (The Fix)
        // This implements OR logic: match any document that contains at least one of the selected triggers.
        if (params.selectedTriggers != null && !params.selectedTriggers.isEmpty()) {
            // Firestore field name is 'selectedTriggers' (verified from your image)
            queryRef = queryRef.whereArrayContainsAny("selectedTriggers", params.selectedTriggers);
        }

        // 3. Filter by Night Waking (Assuming 'true' means filter is ON)
        // If the main filter checkbox is checked, we assume the user only wants entries where nightWaking is true.
        if (params.nightWaking != null && params.nightWaking) {
            queryRef = queryRef.whereEqualTo("nightWaking", true);
        }

        // 4. Filter by Activity Limits (Assuming 'true' means filter is ON and we need to check children)
        // NOTE: Your current gatherFilterParams only uses a boolean for activityLimits,
        // suggesting further client-side filtering might be needed if you rely on child checkboxes.
        // For a simple server-side filter: only show entries where activityLimits > 0 (some limitation)
        if (params.activityLimits != null && params.activityLimits) {
            queryRef = queryRef.whereGreaterThan("activityLimits", 0);
        }

        // 5. Filter by Cough/Wheeze Score Range
        // Field name assumed to be 'cough' (verified from your image)
        if (params.minCoughWheezeScore != null && params.maxCoughWheezeScore != null) {
            queryRef = queryRef.whereGreaterThanOrEqualTo("cough", params.minCoughWheezeScore);
            queryRef = queryRef.whereLessThanOrEqualTo("cough", params.maxCoughWheezeScore);
        }

        // 6. Apply Date Range Filter (Placeholder - assuming fields are checkInTimestamp)
        // You would need to add start/end date fields (as timestamps) to MasterFilterParams
        // if (params.startDateTimestamp != null) {
        //     queryRef = queryRef.whereGreaterThanOrEqualTo("checkInTimestamp", params.startDateTimestamp);
        // }
        // if (params.endDateTimestamp != null) {
        //     queryRef = queryRef.whereLessThanOrEqualTo("checkInTimestamp", params.endDateTimestamp);
        // }

        // 7. Execute the combined query
        queryRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DailyCheckIn> filteredResults = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            try {
                                DailyCheckIn entry = doc.toObject(DailyCheckIn.class);
                                if (entry != null) {
                                    filteredResults.add(entry);
                                }
                            } catch (Exception e) {
                                Log.e("HistoryRepo", "Error converting document: " + e.getMessage());
                            }
                        }

                        // NOTE: If you still need other client-side filters (e.g., complex logic
                        // that can't be done on Firestore), run them here:
                        // filteredResults = historyManager.masterFilter(params, filteredResults);

                        callback.onDataReceived(filteredResults);
                    } else {
                        Log.e("HistoryRepo", "Firestore Query Failed", task.getException());
                        callback.onFailure(task.getException());
                    }
                });
    }
}