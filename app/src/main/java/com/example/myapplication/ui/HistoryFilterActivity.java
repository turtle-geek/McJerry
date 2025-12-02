package com.example.myapplication.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;

import com.example.myapplication.R;
import com.example.myapplication.callbacks.HistoryDataCallback;
import com.example.myapplication.models.DailyCheckIn;
import com.example.myapplication.models.HistoryRepository;
import com.example.myapplication.models.MasterFilterParams;
import com.example.myapplication.models.DailyCheckInHistory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Date;
import java.util.concurrent.TimeUnit;


public class HistoryFilterActivity extends AppCompatActivity {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()); // For TextViews

    private List<DailyCheckIn> resultsToExport;
    private ActivityResultLauncher<Intent> createPdfFileLauncher;

    // View references for Date Range
    private TextView tvStartDate;
    private TextView tvEndDate;
    private LinearLayout customRangeLayout;

    // Date storage
    private Calendar startDateCalendar;
    private Calendar endDateCalendar;

    // CONSTANTS FOR DATE RANGE VALIDATION (Approximate values)
    private static final long MIN_RANGE_MILLIS = 3 * 30L * 24 * 60 * 60 * 1000; // Approx 3 months
    private static final long MAX_RANGE_MILLIS = 6 * 30L * 24 * 60 * 60 * 1000; // Approx 6 months

    // INSTANTIATE NECESSARY SERVICES
    private final DailyCheckInHistory historyManager = new DailyCheckInHistory();
    private final HistoryRepository historyRepository = new HistoryRepository(historyManager);


    private final int margin = 72;
    private final int cellHeight = 30;
    private final int columns = 5;
    private final String[] headers = {"Date", "Waking", "Activity", "Cough", "Triggers"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_filter);

        // Initialize Date objects
        startDateCalendar = Calendar.getInstance();
        endDateCalendar = Calendar.getInstance();

        // Initialize Date Range Views
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        customRangeLayout = findViewById(R.id.customRangeLayout);

        // Set Default Date Range (Today back 3 months)
        setDefaultDateRange();

        setupPdfLauncher();

        // Setup the back button listener
        setupBackButton();

        setupSymptomFilter("nw", R.id.filterNightWaking, R.id.nwFilterHeader, R.id.nwFilterDropdownOptions, R.id.nwBtnToggleDropdown, R.id.nwCbFilterMain);
        setupSymptomFilter("al", R.id.filterActivityLimits, R.id.alFilterHeader, R.id.alFilterDropdownOptions, R.id.alBtnToggleDropdown, R.id.alCbFilterMain);
        setupSymptomFilter("cw", R.id.filterCoughWheeze, R.id.cwFilterHeader, R.id.cwFilterDropdownOptions, R.id.cwBtnToggleDropdown, R.id.cwCbFilterMain);

        // Setup the date click listeners
        tvStartDate.setOnClickListener(v -> showDatePicker(tvStartDate, startDateCalendar, true));
        tvEndDate.setOnClickListener(v -> showDatePicker(tvEndDate, endDateCalendar, false));

        findViewById(R.id.btnApplyFilter).setOnClickListener(v -> applyFiltersAndSave());
    }

    /**
     * Finds the back button and sets an OnClickListener to close the activity.
     */
    private void setupBackButton() {
        ImageButton backButton = findViewById(R.id.btnBack);
        if (backButton != null) {
            backButton.setOnClickListener(v -> {
                // Closes the current activity and returns to the previous activity
                finish();
            });
        }
    }

    // --- Date Picker and Range Logic ---

    /**
     * Sets the default date range: End Date is today, Start Date is 3 months prior.
     */
    private void setDefaultDateRange() {
        // Set end date to the very end of today
        endDateCalendar.setTimeInMillis(System.currentTimeMillis());
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);

        // Set start date 3 months back, setting time to midnight
        startDateCalendar.setTimeInMillis(System.currentTimeMillis());
        startDateCalendar.add(Calendar.MONTH, -3);
        startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startDateCalendar.set(Calendar.MINUTE, 0);
        startDateCalendar.set(Calendar.SECOND, 0);

        tvStartDate.setText(displayDateFormat.format(startDateCalendar.getTime()));
        tvEndDate.setText(displayDateFormat.format(endDateCalendar.getTime()));
    }

    /**
     * Shows a DatePickerDialog for the selected TextView.
     * @param textView The TextView to update.
     * @param calendar The Calendar object to store the selection.
     * @param isStartDate True if setting the start date, false for end date.
     */
    private void showDatePicker(TextView textView, Calendar calendar, boolean isStartDate) {
        // Use the date currently in the calendar for the dialog's default selection
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                R.style.DatePickerTheme, // Keep the theme for green header/calendar
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Update the Calendar object with the selected date
                    calendar.set(selectedYear, selectedMonth, selectedDay);

                    if (isStartDate) {
                        // Set Start Date to the beginning of the day (00:00:00)
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                    } else {
                        // Set End Date to the end of the day (23:59:59)
                        calendar.set(Calendar.HOUR_OF_DAY, 23);
                        calendar.set(Calendar.MINUTE, 59);
                        calendar.set(Calendar.SECOND, 59);
                    }

                    // Update the TextView
                    textView.setText(displayDateFormat.format(calendar.getTime()));
                },
                year, month, day);

        // Restrict end date selection to today or earlier
        if (!isStartDate) {
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        // CRITICAL FIX: Show the dialog first, then manually set the button text color.
        datePickerDialog.show();

        // --- MANUAL BUTTON COLOR OVERRIDE ---
        try {
            // Find the Positive Button (OK) and set its text color to green
            Button positiveButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE);
            if (positiveButton != null) {
                // Get the color resource for green_primary
                int greenColor = getResources().getColor(R.color.green_primary, getTheme());
                positiveButton.setTextColor(greenColor);
            }

            // Find the Negative Button (Cancel) and set its text color to green
            Button negativeButton = datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE);
            if (negativeButton != null) {
                int greenColor = getResources().getColor(R.color.green_primary, getTheme());
                negativeButton.setTextColor(greenColor);
            }
        } catch (Exception e) {
            Log.e("DatePickerFix", "Failed to manually set button color: " + e.getMessage());
        }
        // ------------------------------------
    }


    // --- PDF Launcher, Data Fetch, and Filtering ---

    private void setupPdfLauncher() {
        createPdfFileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            writePdfDocument(uri, resultsToExport);
                        }
                    } else {
                        Toast.makeText(this, "PDF save cancelled.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupSymptomFilter(String prefix, int containerId, int headerId, int dropdownId, int toggleBtnId, int mainCbId) {
        // ... (existing code for setupSymptomFilter remains the same)
        LinearLayout dropdownLayout = findViewById(dropdownId);
        ImageButton toggleButton = findViewById(toggleBtnId);
        CheckBox mainCheckbox = findViewById(mainCbId);

        LinearLayout header = findViewById(headerId);

        // Define the listener to ONLY toggle visibility (open/close dropdown)
        View.OnClickListener toggleListener = v -> {
            toggleVisibility(dropdownLayout);
        };

        // Attach the toggle listener to the header and button
        header.setOnClickListener(toggleListener);
        toggleButton.setOnClickListener(toggleListener);

        // The main checkbox should ONLY toggle the child checkboxes (select all/none)
        mainCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleChildCheckboxes(dropdownLayout, isChecked);
        });
    }

    private void toggleVisibility(LinearLayout layout) {
        if (layout.getVisibility() == View.GONE) {
            layout.setVisibility(View.VISIBLE);
        } else {
            layout.setVisibility(View.GONE);
        }
    }

    /**
     * Handles setting children checked status when the main filter checkbox is toggled.
     */
    private void toggleChildCheckboxes(LinearLayout parentLayout, boolean isChecked) {
        // Iterate through all children of the parent layout
        for (int i = 0; i < parentLayout.getChildCount(); i++) {
            View child = parentLayout.getChildAt(i);

            // Handle CheckBoxes (used for Cough, Night Waking, Activity Limits)
            if (child instanceof CheckBox) {
                ((CheckBox) child).setChecked(isChecked);
            }
            // Handle RadioButtons (used for Date Range in the parent LinearLayout)
            else if (child instanceof RadioButton) {
                ((RadioButton) child).setChecked(isChecked);
            }
            // If the structure retains RadioGroup, handle that case explicitly.
            else if (child instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) child;
                for (int j = 0; j < radioGroup.getChildCount(); j++) {
                    View radioChild = radioGroup.getChildAt(j);
                    if (radioChild instanceof RadioButton) {
                        ((RadioButton) radioChild).setChecked(isChecked);
                    } else if (radioChild instanceof CheckBox) {
                        ((CheckBox) radioChild).setChecked(isChecked);
                    }
                }
            }
        }
    }

    private void applyFiltersAndSave() {
        // 1. Gather parameters first (to get trigger list and timestamps)
        MasterFilterParams params = gatherFilterParams();

        // 2. Check for FILTER selection (Must be first)
        boolean isSymptomFilterOn =
                ((CheckBox) findViewById(R.id.nwCbFilterMain)).isChecked() ||
                        ((CheckBox) findViewById(R.id.alCbFilterMain)).isChecked() ||
                        ((CheckBox) findViewById(R.id.cwCbFilterMain)).isChecked();

        boolean isTriggerFilterOn = !params.selectedTriggers.isEmpty();

        if (!isSymptomFilterOn && !isTriggerFilterOn) {
            Toast.makeText(this, "Please select at least one Symptom or Trigger filter.", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Basic Date Order Validity Check: Start must be before End
        if (params.startTimestamp > params.endTimestamp) {
            Toast.makeText(this, "Error: Start Date cannot be after End Date.", Toast.LENGTH_LONG).show();
            return;
        }

        // 4. Enforce Minimum and Maximum Range
        long rangeMillis = params.endTimestamp - params.startTimestamp;

        // Shorter message for minimum range
        if (rangeMillis < MIN_RANGE_MILLIS) {
            Toast.makeText(this, "Date range must be at least 3 months.", Toast.LENGTH_LONG).show();
            return;
        }

        // Shorter message for maximum range
        if (rangeMillis > MAX_RANGE_MILLIS) {
            Toast.makeText(this, "Date range cannot exceed 6 months.", Toast.LENGTH_LONG).show();
            return;
        }


        // ASYNCHRONOUS FETCH CALL
        historyRepository.fetchAndFilterDataAsync(params, new HistoryDataCallback() {
            @Override
            public void onDataReceived(List<DailyCheckIn> results) {
                onDataReady(results);
            }

            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("HistoryFilter", "Data fetch failed: " + e.getMessage(), e);
                Toast.makeText(HistoryFilterActivity.this, "Error fetching history data.", Toast.LENGTH_LONG).show();
                onDataReady(new ArrayList<>()); // Proceed with empty results on failure
            }
        });
    }

    /**
     * Handles the successful completion of the asynchronous data fetch and proceeds to printing.
     */
    private void onDataReady(List<DailyCheckIn> finalResults) {
        if (finalResults.isEmpty()) {
            Toast.makeText(this, "No records match your selected filters.", Toast.LENGTH_LONG).show();
            return; // Exit if no results
        }

        resultsToExport = finalResults;
        startSaveIntent();
    }


    private MasterFilterParams gatherFilterParams() {
        MasterFilterParams params = new MasterFilterParams();

        // 1. Gather Trigger Filters
        gatherTriggerFilters(params, R.id.triggerFilterOptions);

        // 2. Gather Symptom Filters (ON/OFF)
        params.nightWaking = ((CheckBox) findViewById(R.id.nwCbFilterMain)).isChecked();
        params.activityLimits = ((CheckBox) findViewById(R.id.alCbFilterMain)).isChecked();

        // 3. Gather Cough/Wheeze Score Filter
        gatherCoughWheezeFilters(params, R.id.cwFilterDropdownOptions);

        // 4. Gather Date Range
        // The dates are already set in startDateCalendar and endDateCalendar by the selection logic.

        // Ensure the end date is never in the future and handles the full day.
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);

        params.startTimestamp = startDateCalendar.getTimeInMillis();
        params.endTimestamp = endDateCalendar.getTimeInMillis();

        return params;
    }

    private void gatherTriggerFilters(MasterFilterParams params, int gridLayoutId) {
        GridLayout gridLayout = findViewById(gridLayoutId);
        List<String> selectedTriggers = new ArrayList<>();

        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    selectedTriggers.add(cb.getText().toString());
                }
            }
        }
        params.selectedTriggers = selectedTriggers;
    }

    /**
     * Gathers minimum and maximum selected Cough/Wheeze scores from checkboxes (0-4 scale).
     */
    private void gatherCoughWheezeFilters(MasterFilterParams params, int dropdownLayoutId) {
        LinearLayout dropdownLayout = findViewById(dropdownLayoutId);
        int minScore = Integer.MAX_VALUE;
        int maxScore = Integer.MIN_VALUE;
        boolean anyChecked = false;

        // Iterate through all children of the dropdownLayout
        for (int i = 0; i < dropdownLayout.getChildCount(); i++) {
            View child = dropdownLayout.getChildAt(i);

            // Handle CheckBox children (preferred structure)
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    try {
                        String resourceName = getResources().getResourceEntryName(cb.getId());
                        // Assuming the ID ends in the score (e.g., 'cwCbOption0')
                        int score = Integer.parseInt(resourceName.substring(resourceName.length() - 1));

                        minScore = Math.min(minScore, score);
                        maxScore = Math.max(maxScore, score);
                        anyChecked = true;
                    } catch (Exception e) {
                        Log.e("HistoryFilter", "Error parsing score from CheckBox ID.", e);
                    }
                }
            }
            // Handle RadioGroup/RadioButton children (in case XML is not flat)
            else if (child instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) child;
                for (int j = 0; j < radioGroup.getChildCount(); j++) {
                    View radioChild = radioGroup.getChildAt(j);
                    if (radioChild instanceof RadioButton) {
                        RadioButton rb = (RadioButton) radioChild;
                        if (rb.isChecked()) {
                            try {
                                String resourceName = getResources().getResourceEntryName(rb.getId());
                                int score = Integer.parseInt(resourceName.substring(resourceName.length() - 1));
                                minScore = Math.min(minScore, score);
                                maxScore = Math.max(maxScore, score);
                                anyChecked = true;
                            } catch (Exception e) { /* Ignore parsing errors */ }
                        }
                    } else if (radioChild instanceof CheckBox) {
                        CheckBox cb = (CheckBox) radioChild;
                        if (cb.isChecked()) {
                            try {
                                String resourceName = getResources().getResourceEntryName(cb.getId());
                                int score = Integer.parseInt(resourceName.substring(resourceName.length() - 1));
                                minScore = Math.min(minScore, score);
                                maxScore = Math.max(maxScore, score);
                                anyChecked = true;
                            } catch (Exception e) { /* Ignore parsing errors */ }
                        }
                    }
                }
            }
        }

        // --- Final Filter Parameter Assignment ---
        if (anyChecked) {
            params.minCoughWheezeScore = minScore;
            params.maxCoughWheezeScore = maxScore;
        } else {
            // If the main checkbox was checked, but no children were selected, filter the full 0-4 range
            if (((CheckBox) findViewById(R.id.cwCbFilterMain)).isChecked()) {
                params.minCoughWheezeScore = 0;
                params.maxCoughWheezeScore = 4; // Max score is 4
            }
            // Otherwise, leave params null to not filter by score
        }
    }


    private void startSaveIntent() {
        String defaultFileName = new SimpleDateFormat("'AsthmaReport_'yyyyMMdd'.pdf'", Locale.getDefault()).format(new java.util.Date());

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_TITLE, defaultFileName);

        Toast.makeText(this, "Select save location for PDF...", Toast.LENGTH_SHORT).show();
        createPdfFileLauncher.launch(intent);
    }

    private void writePdfDocument(Uri uri, List<DailyCheckIn> entries) {
        PdfDocument document = new PdfDocument();

        int pageWidth = 595;
        int pageHeight = 842;

        int numItemsPerPage = (pageHeight - (margin * 2) - cellHeight) / cellHeight;
        int pageCount = (int) Math.ceil((double) entries.size() / numItemsPerPage);
        if (pageCount == 0) pageCount = 1;

        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "w")) {
            if (pfd != null) {
                FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor());

                for (int i = 0; i < pageCount; i++) {
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i + 1).create();
                    PdfDocument.Page page = document.startPage(pageInfo);

                    drawPageContent(page.getCanvas(), i, numItemsPerPage, entries, pageWidth, pageHeight);

                    document.finishPage(page);
                }

                document.writeTo(fos);
                Toast.makeText(this, "Report saved successfully to the selected location!", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(this, "Error: Could not open save file descriptor.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e("HistoryFilter", "Error writing PDF: " + e.getMessage(), e);
            Toast.makeText(this, "Error writing PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            document.close();
        }
    }

    private void drawPageContent(Canvas canvas, int pageIndex, int itemsPerPage, List<DailyCheckIn> data, int pageWidth, int pageHeight) {
        int x = margin;
        int y = margin;
        int tableWidth = pageWidth - (margin * 2);
        int columnWidth = tableWidth / columns;

        Paint paint = new Paint();
        paint.setTextSize(12);
        paint.setStyle(Paint.Style.FILL);

        // Draw Headers
        for (int i = 0; i < columns; i++) {
            canvas.drawText(headers[i], x + (columnWidth * i) + 5, y + cellHeight - 5, paint);
        }
        canvas.drawLine(x, y + cellHeight, x + tableWidth, y + cellHeight, paint);
        y += cellHeight + 5;

        // Draw Data Rows
        paint.setTextSize(10);
        int startItem = pageIndex * itemsPerPage;
        int endItem = Math.min(startItem + itemsPerPage, data.size());

        for (int i = startItem; i < endItem; i++) {
            DailyCheckIn entry = data.get(i);
            int rowY = y + (i - startItem) * cellHeight;

            // 1. Date
            canvas.drawText(dateFormat.format(new Date(entry.getCheckInTimestamp())), x + (columnWidth * 0) + 5, rowY + cellHeight - 5, paint);

            // 2. Waking
            canvas.drawText(entry.getNightWaking() ? "Yes" : "No", x + (columnWidth * 1) + 5, rowY + cellHeight - 5, paint);

            // 3. Activity Limits
            canvas.drawText(String.valueOf(entry.getActivityLimits()), x + (columnWidth * 2) + 5, rowY + cellHeight - 5, paint);

            // 4. Cough Wheeze
            canvas.drawText(String.valueOf(entry.getCough()), x + (columnWidth * 3) + 5, rowY + cellHeight - 5, paint);

            // 5. Triggers
            String triggersText = entry.getSelectedTriggers().size() > 2 ?
                    entry.getSelectedTriggers().get(0) + ", ..." :
                    String.join(", ", entry.getSelectedTriggers());
            canvas.drawText(triggersText, x + (columnWidth * 4) + 5, rowY + cellHeight - 5, paint);

            canvas.drawLine(x, rowY + cellHeight, x + tableWidth, rowY + cellHeight, paint);
        }
    }
}