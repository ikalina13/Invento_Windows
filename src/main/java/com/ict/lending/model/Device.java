package com.ict.lending.model;

import java.time.LocalDate;

/**
 * Represents an ICT laboratory device in inventory.
 */
public class Device {
    private int deviceId;
    private String deviceName;
    private String category;
    private String brand;
    private String serialNumber;
    private int quantity;
    private int availableQuantity;
    private String status;
    private LocalDate dateAdded;
    /** Relative path under data/ (e.g. device-images/uuid.jpg), or null. */
    private String imagePath;

    public Device() {
    }

    public Device(int deviceId, String deviceName, String category, String brand,
                  String serialNumber, int quantity, int availableQuantity,
                  String status, LocalDate dateAdded) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.category = category;
        this.brand = brand;
        this.serialNumber = serialNumber;
        this.quantity = quantity;
        this.availableQuantity = availableQuantity;
        this.status = status;
        this.dateAdded = dateAdded;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDate dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /** Computes inventory status from available quantity. */
    public static String computeStatus(int available, int total) {
        if (available <= 0) {
            return "Out of Stock";
        }
        if (available <= Math.max(1, total / 4)) {
            return "Low Stock";
        }
        return "Available";
    }

    @Override
    public String toString() {
        return deviceName + " (" + availableQuantity + " available)";
    }
}
