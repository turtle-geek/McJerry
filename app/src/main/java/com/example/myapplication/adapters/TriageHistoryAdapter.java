package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.models.IncidentLogEntry;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TriageHistoryAdapter extends RecyclerView.Adapter<TriageHistoryAdapter.ViewHolder> {

    private final List<IncidentLogEntry> incidentList;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());


    public TriageHistoryAdapter(List<IncidentLogEntry> incidentList) {
        this.incidentList = incidentList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_triage_log, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        IncidentLogEntry entry = incidentList.get(position);

        holder.tvDecision.setText("DECISION: " + entry.getFinalDecision());
        if ("SOS".equals(entry.getFinalDecision())) {
            holder.tvDecision.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red_critical, holder.itemView.getContext().getTheme()));
        } else {
            holder.tvDecision.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green_primary, holder.itemView.getContext().getTheme()));
        }

        if (entry.getTimestamp() != null) {
            holder.tvTimestamp.setText(entry.getTimestamp().format(FORMATTER));
        } else {
            holder.tvTimestamp.setText("Timestamp: N/A");
        }

        holder.tvSymptoms.setText("Symptoms selected: " + entry.getSelectedSymptomIds().size());

        String pefText = entry.isPeakFlowEntered() ?
                entry.getPeakFlowValue() + " L/min" :
                "N/A";
        holder.tvPeakFlow.setText("Peak Flow: " + pefText);

        String rescueText = entry.isRescueAttemptMade() ? "Yes" : "No";
        holder.tvRescue.setText("Rescue Attempted: " + rescueText);
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDecision;
        final TextView tvTimestamp;
        final TextView tvSymptoms;
        final TextView tvPeakFlow;
        final TextView tvRescue;

        public ViewHolder(View view) {
            super(view);
            tvDecision = view.findViewById(R.id.tvLogDecision);
            tvTimestamp = view.findViewById(R.id.tvLogTimestamp);
            tvSymptoms = view.findViewById(R.id.tvLogSymptoms);
            tvPeakFlow = view.findViewById(R.id.tvLogPeakFlow);
            tvRescue = view.findViewById(R.id.tvLogRescue);
        }
    }
}