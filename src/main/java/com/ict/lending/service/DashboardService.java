package com.ict.lending.service;

import com.ict.lending.database.DeviceDao;
import com.ict.lending.database.TransactionDao;
import com.ict.lending.model.DashboardStats;
import com.ict.lending.model.Device;
import com.ict.lending.model.Transaction;

import java.sql.SQLException;
import java.util.List;

public class DashboardService {

    private final DeviceDao deviceDao = new DeviceDao();
    private final TransactionDao transactionDao = new TransactionDao();

    public DashboardStats loadStats() {
        try {
            List<Device> devices = deviceDao.findAll();
            // Total Devices = number of device records in inventory (updates when add/delete)
            int totalDevices = devices.size();
            int availableUnits = 0;
            for (Device d : devices) {
                availableUnits += d.getAvailableQuantity();
            }
            int borrowed = transactionDao.countBorrowedUnits();
            int returnedToday = transactionDao.countReturnedToday();
            return new DashboardStats(totalDevices, availableUnits, borrowed, returnedToday);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load dashboard: " + e.getMessage(), e);
        }
    }

    public List<Transaction> recentTransactions(int limit) {
        try {
            return transactionDao.findRecent(limit);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load recent transactions: " + e.getMessage(), e);
        }
    }
}
