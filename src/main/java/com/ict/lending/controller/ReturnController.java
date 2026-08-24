package com.ict.lending.controller;

import com.ict.lending.model.Transaction;
import com.ict.lending.service.ReturnService;

import java.util.List;

public class ReturnController {

    private final ReturnService returnService = new ReturnService();

    public List<Transaction> listActive(String search) {
        return returnService.listActive(search);
    }

    public Transaction returnDevice(String transactionId, String fullName, String position,
                                    String gradeLevel, String section) {
        return returnService.returnDevice(transactionId, fullName, position, gradeLevel, section);
    }
}
