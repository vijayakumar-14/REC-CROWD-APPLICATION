package com.vijayakumar.reccrowdmonitor.model;

import java.io.Serializable;

public class LocationStatus implements Serializable {
    public static final String STATUS_LOW = "LOW";
    public static final String STATUS_MODERATE = "MODERATE";
    public static final String STATUS_HIGH = "HIGH";

    private String id;
    private String name;
    private int occupancyPercent;
    private String statusLevel;
    private String lastUpdatedFormatted;

    // Default constructor for Firebase Realtime Database
    public LocationStatus() {
    }

    public LocationStatus(String id, String name, int occupancyPercent, String lastUpdatedFormatted) {
        this.id = id;
        this.name = name;
        this.occupancyPercent = occupancyPercent;
        this.statusLevel = calculateStatusLevel(occupancyPercent);
        this.lastUpdatedFormatted = lastUpdatedFormatted;
    }

    public static String calculateStatusLevel(int percent) {
        if (percent <= 40) {
            return STATUS_LOW;
        } else if (percent <= 75) {
            return STATUS_MODERATE;
        } else {
            return STATUS_HIGH;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOccupancyPercent() {
        return occupancyPercent;
    }

    public void setOccupancyPercent(int occupancyPercent) {
        this.occupancyPercent = occupancyPercent;
        this.statusLevel = calculateStatusLevel(occupancyPercent);
    }

    public String getStatusLevel() {
        if (statusLevel == null || statusLevel.isEmpty()) {
            return calculateStatusLevel(occupancyPercent);
        }
        return statusLevel;
    }

    public void setStatusLevel(String statusLevel) {
        this.statusLevel = statusLevel;
    }

    public String getLastUpdatedFormatted() {
        return lastUpdatedFormatted;
    }

    public void setLastUpdatedFormatted(String lastUpdatedFormatted) {
        this.lastUpdatedFormatted = lastUpdatedFormatted;
    }
}
