package com.example.myapplication.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.models.DailyCheckIn;
import com.example.myapplication.models.MasterFilterParams;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date; // Added for drawPageContent date formatting

public class HistoryFilterActivity extends AppCompatActivity {

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    private List<DailyCheckIn> resultsToExport;
    private ActivityResultLauncher<Intent> createPdfFileLauncher;

    private final int margin = 72;
    private final int cellHeight = 30;
    private final int columns = 5;
    private final String[] headers = {"Date", "Waking", "Activity", "Cough", "Triggers"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_filter);

        setupPdfLauncher();

        setupSymptomFilter("nw", R.id.filterNightWaking, R.id.nwFilterHeader, R.id.nwFilterDropdownOptions, R.id.nwBtnToggleDropdown, R.id.nwCbFilterMain);
        setupSymptomFilter("al", R.id.filterActivityLimits, R.id.alFilterHeader, R.id.alFilterDropdownOptions, R.id.alBtnToggleDropdown, R.id.alCbFilterMain);
        setupSymptomFilter("cw", R.id.filterCoughWheeze, R.id.cwFilterHeader, R.id.cwFilterDropdownOptions, R.id.cwBtnToggleDropdown, R.id.cwCbFilterMain);
        setupDateRangeToggle();

        findViewById(R.id.btnApplyFilter).setOnClickListener(v -> applyFiltersAndSave());
    }

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
        LinearLayout dropdownLayout = findViewById(dropdownId);
        ImageButton toggleButton = findViewById(toggleBtnId);
        CheckBox mainCheckbox = findViewById(mainCbId);

        LinearLayout header = findViewById(headerId);

        View.OnClickListener toggleListener = v -> {
            toggleVisibility(dropdownLayout);
        };

        header.setOnClickListener(toggleListener);
        toggleButton.setOnClickListener(toggleListener);

        mainCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            toggleChildCheckboxes(dropdownLayout, isChecked);
        });

        mainCheckbox.setOnClickListener(toggleListener);
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
     * Assumes Cough/Wheeze is a LinearLayout containing CheckBoxes (based on XML fix).
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
            // If the structure retains RadioGroup (e.g., if XML fix was incomplete for cw), handle that.
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

    private void setupDateRangeToggle() {
        ((RadioGroup)findViewById(R.id.radioGroupDateRange)).setOnCheckedChangeListener((group, checkedId) -> {
            LinearLayout customRangeLayout = findViewById(R.id.customRangeLayout);
            if (checkedId == R.id.rbCustomRange) {
                customRangeLayout.setVisibility(View.VISIBLE);
            } else {
                customRangeLayout.setVisibility(View.GONE);
            }
        });

        findViewById(R.id.tvStartDate).setOnClickListener(v -> showDatePicker(R.id.tvStartDate));
        findViewById(R.id.tvEndDate).setOnClickListener(v -> showDatePicker(R.id.tvEndDate));
    }

    private void showDatePicker(int textViewId) {
        Toast.makeText(this, "Opening Date Picker...", Toast.LENGTH_SHORT).show();
    }


    private void applyFiltersAndSave() {
        MasterFilterParams params = gatherFilterParams();

        // 1. EXECUTE MASTER FILTER (Placeholder)
        // Note: Replace this placeholder with your actual Firestore retrieval and filtering logic.
        List<DailyCheckIn> finalResults = createSampleData();

        if (finalResults.isEmpty()) {
            Toast.makeText(this, "No records match your selected filters.", Toast.LENGTH_LONG).show();
            return;
        }

        resultsToExport = finalResults;
        startSaveIntent();
    }

    /**
     * MODIFIED: Rewritten Cough/Wheeze gathering logic for multi-select checkboxes (0-4 scale).
     */
    private MasterFilterParams gatherFilterParams() {
        MasterFilterParams params = new MasterFilterParams();

        // 1. Gather Trigger Filters (Multi-select CheckBoxes)
        gatherTriggerFilters(params, R.id.triggerFilterOptions);

        // 2. Gather Symptom Filters (ON/OFF)
        params.nightWaking = ((CheckBox) findViewById(R.id.nwCbFilterMain)).isChecked();
        params.activityLimits = ((CheckBox) findViewById(R.id.alCbFilterMain)).isChecked();

        // 3. Gather Cough/Wheeze Score Filter
        gatherCoughWheezeFilters(params, R.id.cwFilterDropdownOptions);

        // 4. Gather Date Range (Implementation needed)
        // ...

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
     * NEW HELPER: Gathers minimum and maximum selected Cough/Wheeze scores from checkboxes (0-4 scale).
     */
    private void gatherCoughWheezeFilters(MasterFilterParams params, int dropdownLayoutId) {
        LinearLayout dropdownLayout = findViewById(dropdownLayoutId);
        int minScore = Integer.MAX_VALUE;
        int maxScore = Integer.MIN_VALUE;
        boolean anyChecked = false;

        // Find the inner container holding the checkboxes
        // Based on the XML fix, this is the cwFilterDropdownOptions LinearLayout

        // Iterate through all children of the dropdownLayout
        for (int i = 0; i < dropdownLayout.getChildCount(); i++) {
            View child = dropdownLayout.getChildAt(i);

            // Handle RadioGroup container if the previous fix wasn't applied correctly
            if (child instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) child;
                for (int j = 0; j < radioGroup.getChildCount(); j++) {
                    View radioChild = radioGroup.getChildAt(j);
                    if (radioChild instanceof RadioButton) { // Check for RadioButtons
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
                    } else if (radioChild instanceof CheckBox) { // Check for CheckBoxes inside RadioGroup
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
            // Handle direct CheckBoxes (preferred structure)
            else if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
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

    private List<DailyCheckIn> createSampleData() {
        List<DailyCheckIn> list = new ArrayList<>();
        list.add(new DailyCheckIn(System.currentTimeMillis() - 86400000L * 2, "Parent", true, 2, 4, List.of("Cold Air")));
        list.add(new DailyCheckIn(System.currentTimeMillis() - 86400000L * 5, "Child", false, 0, 1, List.of("None")));
        list.add(new DailyCheckIn(System.currentTimeMillis(), "Parent", true, 1, 3, List.of("Exercise", "Dust/Pets")));
        return list;
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
            canvas.drawText(dateFormat.format(new Date(entry.getCheckInTimestamp())), x + (columnWidth * 0) + 5, rowY + cellHeight - 5, paint); // Use new Date(timestamp)

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