package com.ict.lending.controller;

import com.ict.lending.model.DashboardStats;
import com.ict.lending.model.Transaction;
import com.ict.lending.service.DashboardService;

import java.util.List;

public class DashboardController {

    private final DashboardService service = new DashboardService();

    public DashboardStats loadStats() {
        return service.loadStats();
    }

    public List<Transaction> recent() {
        return service.recentTransactions(12);
    }
}
