package com.ict.lending.controller;

import com.ict.lending.model.Transaction;
import com.ict.lending.service.HistoryService;

import java.util.List;

public class HistoryController {

    private final HistoryService historyService = new HistoryService();

    public List<Transaction> list(String search, String status) {
        return historyService.list(search, status);
    }
}
