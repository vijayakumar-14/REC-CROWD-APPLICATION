package com.vijayakumar.reccrowdmonitor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.card.MaterialCardView;
import com.vijayakumar.reccrowdmonitor.R;
import com.vijayakumar.reccrowdmonitor.adapter.LocationAdapter;
import com.vijayakumar.reccrowdmonitor.model.LocationStatus;
import com.vijayakumar.reccrowdmonitor.repository.CrowdDataRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LiveStatusFragment extends Fragment {

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private LocationAdapter adapter;
    private MaterialCardView cardBanner;
    private TextView tvBannerText;
    private View layoutError;
    private View btnRetry;

    private CrowdDataRepository repository;
    private final List<LocationStatus> currentLocations = new ArrayList<>();
    private boolean isSortLeastCrowdedFirst = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_status, container, false);

        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        recyclerView = view.findViewById(R.id.recycler_view_locations);
        cardBanner = view.findViewById(R.id.card_suggestion_banner);
        tvBannerText = view.findViewById(R.id.tv_banner_text);
        layoutError = view.findViewById(R.id.layout_error);
        btnRetry = view.findViewById(R.id.btn_retry);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LocationAdapter();
        recyclerView.setAdapter(adapter);

        repository = CrowdDataRepository.getInstance();

        swipeRefresh.setOnRefreshListener(this::refreshData);

        btnRetry.setOnClickListener(v -> {
            layoutError.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(true);
            refreshData();
        });

        loadInitialData();

        return view;
    }

    private void loadInitialData() {
        swipeRefresh.setRefreshing(true);
        repository.fetchLocations(new CrowdDataRepository.DataCallback() {
            @Override
            public void onSuccess(List<LocationStatus> locations) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    updateUI(locations);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    showErrorState();
                }
            }
        });
    }

    private void refreshData() {
        repository.refreshLocationsData(new CrowdDataRepository.DataCallback() {
            @Override
            public void onSuccess(List<LocationStatus> locations) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    updateUI(locations);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (isAdded()) {
                    swipeRefresh.setRefreshing(false);
                    Toast.makeText(getContext(), R.string.error_loading, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateUI(List<LocationStatus> locations) {
        layoutError.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);

        currentLocations.clear();
        if (locations != null) {
            currentLocations.addAll(locations);
        }

        List<LocationStatus> displayList = new ArrayList<>(currentLocations);
        if (isSortLeastCrowdedFirst) {
            Collections.sort(displayList, Comparator.comparingInt(LocationStatus::getOccupancyPercent));
        }

        adapter.updateLocations(displayList);
        checkAndShowRecommendationBanner(currentLocations);
    }

    private void checkAndShowRecommendationBanner(List<LocationStatus> locations) {
        if (locations == null || locations.isEmpty()) {
            cardBanner.setVisibility(View.GONE);
            return;
        }

        LocationStatus crowdedLoc = null;
        LocationStatus leastCrowdedLoc = null;

        for (LocationStatus loc : locations) {
            if (LocationStatus.STATUS_HIGH.equals(loc.getStatusLevel()) && crowdedLoc == null) {
                crowdedLoc = loc;
            }
            if (leastCrowdedLoc == null || loc.getOccupancyPercent() < leastCrowdedLoc.getOccupancyPercent()) {
                leastCrowdedLoc = loc;
            }
        }

        if (crowdedLoc != null && leastCrowdedLoc != null && !crowdedLoc.getId().equals(leastCrowdedLoc.getId())) {
            String bannerMessage = getString(R.string.banner_suggestion,
                    crowdedLoc.getName(),
                    crowdedLoc.getOccupancyPercent(),
                    leastCrowdedLoc.getName(),
                    getReadableStatus(leastCrowdedLoc.getStatusLevel()),
                    leastCrowdedLoc.getOccupancyPercent());

            tvBannerText.setText(bannerMessage);
            cardBanner.setVisibility(View.VISIBLE);
        } else {
            cardBanner.setVisibility(View.GONE);
        }
    }

    private String getReadableStatus(String level) {
        if (LocationStatus.STATUS_LOW.equals(level)) return getString(R.string.status_low);
        if (LocationStatus.STATUS_MODERATE.equals(level)) return getString(R.string.status_moderate);
        return getString(R.string.status_high);
    }

    private void showErrorState() {
        recyclerView.setVisibility(View.GONE);
        cardBanner.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
    }

    public void toggleSortOrder() {
        isSortLeastCrowdedFirst = !isSortLeastCrowdedFirst;
        int toastRes = isSortLeastCrowdedFirst ? R.string.sort_toast_least_crowded : R.string.sort_toast_default;
        Toast.makeText(getContext(), toastRes, Toast.LENGTH_SHORT).show();
        updateUI(currentLocations);
    }

    public void shareCrowdStatus() {
        if (currentLocations.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.share_header));

        for (LocationStatus loc : currentLocations) {
            sb.append(getString(R.string.share_item_format,
                    loc.getName(),
                    loc.getOccupancyPercent(),
                    getReadableStatus(loc.getStatusLevel())));
        }
        sb.append(getString(R.string.share_footer));

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_title)));
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start live auto-refresh timer every 15 seconds
        repository.startAutoRefresh(new CrowdDataRepository.DataCallback() {
            @Override
            public void onSuccess(List<LocationStatus> locations) {
                if (isAdded()) {
                    updateUI(locations);
                }
            }

            @Override
            public void onError(String errorMessage) {
                // Keep showing cached data smoothly
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        repository.stopAutoRefresh();
    }
}
