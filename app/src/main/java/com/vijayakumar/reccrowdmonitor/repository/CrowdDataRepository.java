package com.vijayakumar.reccrowdmonitor.repository;

import android.os.Handler;
import android.os.Looper;
import com.vijayakumar.reccrowdmonitor.model.LocationStatus;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class CrowdDataRepository {
    public interface DataCallback {
        void onSuccess(List<LocationStatus> locations);
        void onError(String errorMessage);
    }

    private static CrowdDataRepository instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable autoRefreshRunnable;
    private boolean isAutoRefreshing = false;
    private final Random random = new Random();
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());

    // In-memory cache for offline mode & instant updates
    private final Map<String, LocationStatus> cachedData = new HashMap<>();

    private CrowdDataRepository() {
        initializeInitialMockData();
    }

    public static synchronized CrowdDataRepository getInstance() {
        if (instance == null) {
            instance = new CrowdDataRepository();
        }
        return instance;
    }

    private void initializeInitialMockData() {
        String now = getCurrentTimeString();
        cachedData.put("rec_cafe", new LocationStatus("rec_cafe", "REC Cafe", 62, now));
        cachedData.put("hut_cafe", new LocationStatus("hut_cafe", "Hut Cafe", 28, now));
        cachedData.put("rec_mart", new LocationStatus("rec_mart", "REC Mart", 45, now));
    }

    public void fetchLocations(DataCallback callback) {
        // Simulating a quick asynchronous data fetch
        handler.postDelayed(() -> {
            if (callback != null) {
                callback.onSuccess(getLocationsList());
            }
        }, 300);
    }

    public void refreshLocationsData(DataCallback callback) {
        // Randomize percentages slightly within realistic bounds (e.g. ±8%) to simulate live campus crowd dynamics
        String timestamp = getCurrentTimeString();
        
        LocationStatus recCafe = cachedData.get("rec_cafe");
        if (recCafe != null) {
            int newCafePct = Math.max(15, Math.min(95, recCafe.getOccupancyPercent() + (random.nextInt(17) - 8)));
            recCafe.setOccupancyPercent(newCafePct);
            recCafe.setLastUpdatedFormatted(timestamp);
        }

        LocationStatus hutCafe = cachedData.get("hut_cafe");
        if (hutCafe != null) {
            int newHutPct = Math.max(10, Math.min(88, hutCafe.getOccupancyPercent() + (random.nextInt(15) - 7)));
            hutCafe.setOccupancyPercent(newHutPct);
            hutCafe.setLastUpdatedFormatted(timestamp);
        }

        LocationStatus recMart = cachedData.get("rec_mart");
        if (recMart != null) {
            int newMartPct = Math.max(20, Math.min(90, recMart.getOccupancyPercent() + (random.nextInt(13) - 6)));
            recMart.setOccupancyPercent(newMartPct);
            recMart.setLastUpdatedFormatted(timestamp);
        }

        if (callback != null) {
            callback.onSuccess(getLocationsList());
        }
    }

    public void startAutoRefresh(final DataCallback callback) {
        if (isAutoRefreshing) return;
        isAutoRefreshing = true;

        autoRefreshRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAutoRefreshing) {
                    refreshLocationsData(callback);
                    // Refresh every 15 seconds
                    handler.postDelayed(this, 15000);
                }
            }
        };

        handler.postDelayed(autoRefreshRunnable, 15000);
    }

    public void stopAutoRefresh() {
        isAutoRefreshing = false;
        if (autoRefreshRunnable != null) {
            handler.removeCallbacks(autoRefreshRunnable);
        }
    }

    public List<LocationStatus> getLocationsList() {
        List<LocationStatus> list = new ArrayList<>();
        if (cachedData.containsKey("rec_cafe")) list.add(cachedData.get("rec_cafe"));
        if (cachedData.containsKey("hut_cafe")) list.add(cachedData.get("hut_cafe"));
        if (cachedData.containsKey("rec_mart")) list.add(cachedData.get("rec_mart"));
        return list;
    }

    private String getCurrentTimeString() {
        return timeFormat.format(new Date());
    }
}
