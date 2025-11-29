package com.example.myapplication.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.models.PeakFlow;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Reusable Trend Snippet Component
 * Displays a chart with 7-day or 30-day view of peak flow data
 */
public class TrendSnippet extends FrameLayout {

    private LineChart trendChart;
    private Button btn7Days, btn30Days;
    private TextView tvGreenCount, tvYellowCount, tvRedCount;
    private TextView emptyStateText;
    private ProgressBar loadingIndicator;

    private int currentDays = 7; // Default to 7 days
    private List<PeakFlow> allPeakFlowData;

    public TrendSnippet(Context context) {
        super(context);
        init(context);
    }

    public TrendSnippet(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TrendSnippet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.trend_snippet, this, true);

        // Initialize views
        trendChart = findViewById(R.id.trendChart);
        btn7Days = findViewById(R.id.btn7Days);
        btn30Days = findViewById(R.id.btn30Days);
        tvGreenCount = findViewById(R.id.tvGreenCount);
        tvYellowCount = findViewById(R.id.tvYellowCount);
        tvRedCount = findViewById(R.id.tvRedCount);
        emptyStateText = findViewById(R.id.emptyStateText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        // Setup chart
        setupChart();

        // Setup button listeners
        setupButtonListeners();
    }

    private void setupChart() {
        // Configure chart appearance
        trendChart.getDescription().setEnabled(false);
        trendChart.setTouchEnabled(true);
        trendChart.setDragEnabled(true);
        trendChart.setScaleEnabled(false);
        trendChart.setPinchZoom(false);
        trendChart.setDrawGridBackground(false);

        // Configure X-axis
        XAxis xAxis = trendChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#555555"));

        // Configure Y-axis
        YAxis leftAxis = trendChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.parseColor("#E0E0E0"));
        leftAxis.setTextColor(Color.parseColor("#555555"));

        YAxis rightAxis = trendChart.getAxisRight();
        rightAxis.setEnabled(false);

        // Configure legend
        trendChart.getLegend().setEnabled(false);
    }

    private void setupButtonListeners() {
        btn7Days.setOnClickListener(v -> {
            if (currentDays != 7) {
                currentDays = 7;
                updateButtonStyles();
                updateChart();
            }
        });

        btn30Days.setOnClickListener(v -> {
            if (currentDays != 30) {
                currentDays = 30;
                updateButtonStyles();
                updateChart();
            }
        });

    }

    private void updateButtonStyles() {
        if (currentDays == 7) {
            // 7 days selected
            btn7Days.setBackgroundTintList(getContext().getColorStateList(android.R.color.transparent));
            btn7Days.setBackgroundColor(Color.parseColor("#064200"));
            btn7Days.setTextColor(Color.WHITE);

            btn30Days.setBackgroundColor(Color.parseColor("#E8F5E9"));
            btn30Days.setTextColor(Color.parseColor("#064200"));
        } else {
            // 30 days selected
            btn7Days.setBackgroundColor(Color.parseColor("#E8F5E9"));
            btn7Days.setTextColor(Color.parseColor("#064200"));

            btn30Days.setBackgroundColor(Color.parseColor("#064200"));
            btn30Days.setTextColor(Color.WHITE);
        }
    }

    /**
     * Set peak flow data and update the chart
     * @param peakFlowData List of PeakFlow objects
     */
    public void setData(List<PeakFlow> peakFlowData) {
        this.allPeakFlowData = peakFlowData;
        updateChart();
    }

    private void updateChart() {
        if (allPeakFlowData == null || allPeakFlowData.isEmpty()) {
            showEmptyState();
            return;
        }

        showChart();

        // Filter data based on current days selection
        List<PeakFlow> filteredData = filterDataByDays(allPeakFlowData, currentDays);

        if (filteredData.isEmpty()) {
            showEmptyState();
            return;
        }

        // Create chart entries
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();

        for (int i = 0; i < filteredData.size(); i++) {
            PeakFlow pf = filteredData.get(i);
            entries.add(new Entry(i, pf.getPeakFlow()));

            // Format date for x-axis
            Date date = Date.from(pf.getTime().atZone(ZoneId.systemDefault()).toInstant());
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd", Locale.US);
            dates.add(sdf.format(date));
        }

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Peak Flow");
        dataSet.setColor(Color.parseColor("#064200"));
        dataSet.setCircleColor(Color.parseColor("#064200"));
        dataSet.setCircleRadius(4f);
        dataSet.setLineWidth(2f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(Color.parseColor("#E8F5E9"));

        // Create line data
        LineData lineData = new LineData(dataSet);
        trendChart.setData(lineData);

        // Set custom x-axis labels
        XAxis xAxis = trendChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dates.size()) {
                    return dates.get(index);
                }
                return "";
            }
        });

        // Update zone counts
        updateZoneCounts(filteredData);

        // Refresh chart
        trendChart.invalidate();
    }

    private List<PeakFlow> filterDataByDays(List<PeakFlow> data, int days) {
        List<PeakFlow> filtered = new ArrayList<>();
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);

        for (PeakFlow pf : data) {
            if (pf.getTime().isAfter(cutoffDate)) {
                filtered.add(pf);
            }
        }

        return filtered;
    }

    private void updateZoneCounts(List<PeakFlow> data) {
        int greenCount = 0;
        int yellowCount = 0;
        int redCount = 0;

        for (PeakFlow pf : data) {
            // Assuming zone is already computed
            String zone = pf.getZone();
            if (zone != null) {
                switch (zone) {
                    case "green":
                        greenCount++;
                        break;
                    case "yellow":
                        yellowCount++;
                        break;
                    case "red":
                        redCount++;
                        break;
                }
            }
        }

        tvGreenCount.setText("Green: " + greenCount);
        tvYellowCount.setText("Yellow: " + yellowCount);
        tvRedCount.setText("Red: " + redCount);
    }

    private void showEmptyState() {
        trendChart.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void showChart() {
        trendChart.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
    }

    public void showLoading() {
        trendChart.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }



    /**
     * Get current days selection (7 or 30)
     * @return current days
     */
    public int getCurrentDays() {
        return currentDays;
    }

    /**
     * Set days programmatically
     * @param days 7 or 30
     */
    public void setDays(int days) {
        if (days == 7 || days == 30) {
            this.currentDays = days;
            updateButtonStyles();
            updateChart();
        }
    }
}