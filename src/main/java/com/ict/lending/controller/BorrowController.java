package com.ict.lending.controller;

import com.ict.lending.model.Borrower;
import com.ict.lending.model.Device;
import com.ict.lending.model.Transaction;
import com.ict.lending.service.BorrowService;
import com.ict.lending.service.DeviceService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BorrowController {

    private final DeviceService deviceService = new DeviceService();
    private final BorrowService borrowService = new BorrowService();

    public List<Device> availableDevices(String search, String category) {
        return deviceService.list(search, category).stream()
                .filter(d -> d.getAvailableQuantity() > 0)
                .collect(Collectors.toList());
    }

    /** All devices for the booking counter (includes out-of-stock for display). */
    public List<Device> catalogDevices(String search, String category) {
        return deviceService.list(search, category);
    }

    public List<String> categoriesWithAll() {
        List<String> cats = new ArrayList<>();
        cats.add("All");
        cats.addAll(deviceService.categories());
        return cats;
    }

    public Transaction borrow(int deviceId, int qty, Borrower borrower) {
        return borrowService.borrow(deviceId, qty, borrower);
    }

    public List<Transaction> borrowBasket(List<BorrowService.BasketLine> lines, Borrower borrower) {
        return borrowService.borrowMany(lines, borrower);
    }
}
