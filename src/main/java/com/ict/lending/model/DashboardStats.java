package com.ict.lending.model;

/**
 * Aggregated dashboard statistics.
 */
public class DashboardStats {
    private int totalDevices;
    private int availableDevices;
    private int borrowedDevices;
    private int returnedToday;

    public DashboardStats() {
    }

    public DashboardStats(int totalDevices, int availableDevices,
                          int borrowedDevices, int returnedToday) {
        this.totalDevices = totalDevices;
        this.availableDevices = availableDevices;
        this.borrowedDevices = borrowedDevices;
        this.returnedToday = returnedToday;
    }

    public int getTotalDevices() {
        return totalDevices;
    }

    public void setTotalDevices(int totalDevices) {
        this.totalDevices = totalDevices;
    }

    public int getAvailableDevices() {
        return availableDevices;
    }

    public void setAvailableDevices(int availableDevices) {
        this.availableDevices = availableDevices;
    }

    public int getBorrowedDevices() {
        return borrowedDevices;
    }

    public void setBorrowedDevices(int borrowedDevices) {
        this.borrowedDevices = borrowedDevices;
    }

    public int getReturnedToday() {
        return returnedToday;
    }

    public void setReturnedToday(int returnedToday) {
        this.returnedToday = returnedToday;
    }
}
