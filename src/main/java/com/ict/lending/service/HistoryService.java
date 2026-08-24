package com.ict.lending.service;

import com.ict.lending.database.TransactionDao;
import com.ict.lending.model.Transaction;

import java.sql.SQLException;
import java.util.List;

public class HistoryService {

    private final TransactionDao transactionDao = new TransactionDao();

    public List<Transaction> list(String search, String status) {
        try {
            return transactionDao.findFiltered(search, status);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions: " + e.getMessage(), e);
        }
    }

    public Transaction get(String transactionId) {
        try {
            return transactionDao.findById(transactionId)
                    .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transaction: " + e.getMessage(), e);
        }
    }
}
