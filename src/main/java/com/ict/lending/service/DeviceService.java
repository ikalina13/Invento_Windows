package com.ict.lending.service;

import com.ict.lending.database.DeviceDao;
import com.ict.lending.model.Device;
import com.ict.lending.utils.DeviceImages;
import com.ict.lending.utils.Validators;

import java.nio.file.Path;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DeviceService {

    private final DeviceDao deviceDao = new DeviceDao();
    private final AuditService auditService = new AuditService();

    public List<Device> list(String search, String category) {
        try {
            return deviceDao.findFiltered(search, category);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load devices: " + e.getMessage(), e);
        }
    }

    public List<String> categories() {
        try {
            return deviceDao.findCategories();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load categories: " + e.getMessage(), e);
        }
    }

    public Device get(int id) {
        try {
            return deviceDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found."));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load device: " + e.getMessage(), e);
        }
    }

    public void add(String name, String category, String brand, String serial,
                    int quantity, Path imageSource) {
        Validators.requireNonBlank(name, "Device name");
        Validators.requireNonBlank(category, "Category");
        Validators.requireNonBlank(brand, "Brand");
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        Device device = new Device();
        device.setDeviceName(name.trim());
        device.setCategory(category.trim());
        device.setBrand(brand.trim());
        device.setSerialNumber(serial == null || serial.isBlank() ? null : serial.trim());
        device.setQuantity(quantity);
        device.setAvailableQuantity(quantity);
        device.setStatus(Device.computeStatus(quantity, quantity));
        device.setDateAdded(LocalDate.now());
        if (imageSource != null) {
            try {
                device.setImagePath(DeviceImages.storeCopy(imageSource));
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
        try {
            int id = deviceDao.insert(device);
            auditService.log("DEVICE_ADD", "Added device #" + id + " " + name);
        } catch (SQLException e) {
            DeviceImages.deleteQuietly(device.getImagePath());
            throw new RuntimeException("Failed to add device: " + e.getMessage(), e);
        }
    }

    public void update(int id, String name, String category, String brand, String serial,
                       int newTotalQuantity, Path imageSource, boolean clearImage) {
        Validators.requireNonBlank(name, "Device name");
        Validators.requireNonBlank(category, "Category");
        Validators.requireNonBlank(brand, "Brand");
        if (newTotalQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        try {
            Device existing = deviceDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found."));
            int borrowed = existing.getQuantity() - existing.getAvailableQuantity();
            if (newTotalQuantity < borrowed) {
                throw new IllegalArgumentException(
                        "Total quantity cannot be less than currently borrowed (" + borrowed + ").");
            }
            int available = newTotalQuantity - borrowed;
            existing.setDeviceName(name.trim());
            existing.setCategory(category.trim());
            existing.setBrand(brand.trim());
            existing.setSerialNumber(serial == null || serial.isBlank() ? null : serial.trim());
            existing.setQuantity(newTotalQuantity);
            existing.setAvailableQuantity(available);
            existing.setStatus(Device.computeStatus(available, newTotalQuantity));

            String previousImage = existing.getImagePath();
            if (clearImage) {
                existing.setImagePath(null);
            } else if (imageSource != null) {
                existing.setImagePath(DeviceImages.storeCopy(imageSource));
            }

            deviceDao.update(existing);

            if (clearImage || imageSource != null) {
                DeviceImages.deleteQuietly(previousImage);
            }
            auditService.log("DEVICE_UPDATE", "Updated device #" + id + " " + name);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update device: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void delete(int id) {
        try {
            if (deviceDao.hasActiveBorrows(id)) {
                throw new IllegalArgumentException(
                        "Cannot delete a device that still has active borrows.");
            }
            Device existing = deviceDao.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Device not found."));
            deviceDao.delete(id);
            DeviceImages.deleteQuietly(existing.getImagePath());
            auditService.log("DEVICE_DELETE", "Deleted device #" + id + " " + existing.getDeviceName());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete device: " + e.getMessage(), e);
        }
    }
}
