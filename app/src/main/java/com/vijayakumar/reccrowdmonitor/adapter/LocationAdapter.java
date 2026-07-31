package com.vijayakumar.reccrowdmonitor.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.vijayakumar.reccrowdmonitor.R;
import com.vijayakumar.reccrowdmonitor.model.LocationStatus;
import java.util.ArrayList;
import java.util.List;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private final List<LocationStatus> locations = new ArrayList<>();
    private Context context;

    public LocationAdapter() {
    }

    public void updateLocations(List<LocationStatus> newLocations) {
        this.locations.clear();
        if (newLocations != null) {
            this.locations.addAll(newLocations);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_location_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocationStatus item = locations.get(position);
        holder.tvName.setText(item.getName());

        int pct = item.getOccupancyPercent();
        holder.tvOccupancy.setText(context.getString(R.string.occupancy_format, pct));
        holder.progressBar.setProgress(pct);

        String updatedText = context.getString(R.string.last_updated_format, item.getLastUpdatedFormatted());
        holder.tvLastUpdated.setText(updatedText);

        String statusLevel = item.getStatusLevel();
        if (LocationStatus.STATUS_LOW.equals(statusLevel)) {
            holder.tvBadge.setText(R.string.status_low);
            holder.tvBadge.setBackgroundResource(R.drawable.badge_green_bg);
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_green));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_green)));
        } else if (LocationStatus.STATUS_MODERATE.equals(statusLevel)) {
            holder.tvBadge.setText(R.string.status_moderate);
            holder.tvBadge.setBackgroundResource(R.drawable.badge_amber_bg);
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_amber));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_amber)));
        } else {
            holder.tvBadge.setText(R.string.status_high);
            holder.tvBadge.setBackgroundResource(R.drawable.badge_red_bg);
            holder.tvBadge.setTextColor(ContextCompat.getColor(context, R.color.status_red));
            holder.progressBar.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_red)));
        }
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBadge, tvOccupancy, tvLastUpdated;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_location_name);
            tvBadge = itemView.findViewById(R.id.tv_status_badge);
            tvOccupancy = itemView.findViewById(R.id.tv_occupancy_percent);
            tvLastUpdated = itemView.findViewById(R.id.tv_last_updated);
            progressBar = itemView.findViewById(R.id.progress_occupancy);
        }
    }
}
