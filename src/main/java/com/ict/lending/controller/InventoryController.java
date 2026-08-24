package com.ict.lending.controller;

import com.ict.lending.model.Device;
import com.ict.lending.service.DeviceService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class InventoryController {

    private final DeviceService deviceService = new DeviceService();

    public List<Device> list(String search, String category) {
        return deviceService.list(search, category);
    }

    public List<String> categoriesWithAll() {
        List<String> cats = new ArrayList<>();
        cats.add("All");
        cats.addAll(deviceService.categories());
        return cats;
    }

    public void add(String name, String category, String brand, String serial, int qty, Path imageSource) {
        deviceService.add(name, category, brand, serial, qty, imageSource);
    }

    public void update(int id, String name, String category, String brand, String serial, int qty,
                       Path imageSource, boolean clearImage) {
        deviceService.update(id, name, category, brand, serial, qty, imageSource, clearImage);
    }

    public void delete(int id) {
        deviceService.delete(id);
    }
}
