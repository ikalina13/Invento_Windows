package com.ict.lending.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique transaction IDs: TXN-yyyyMMdd-####
 */
public final class IdGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);
    private static String lastDate = "";

    private IdGenerator() {
    }

    public static synchronized String nextTransactionId() {
        String today = LocalDate.now().format(DATE_FMT);
        if (!today.equals(lastDate)) {
            lastDate = today;
            SEQUENCE.set(0);
        }
        int seq = SEQUENCE.incrementAndGet();
        return String.format("TXN-%s-%04d", today, seq);
    }

    public static void syncSequence(int maxSeqForToday) {
        String today = LocalDate.now().format(DATE_FMT);
        lastDate = today;
        SEQUENCE.set(Math.max(SEQUENCE.get(), maxSeqForToday));
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "—";
        }
        return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    public static String formatTime(LocalTime time) {
        if (time == null) {
            return "—";
        }
        return time.format(DateTimeFormatter.ofPattern("hh:mm a"));
    }

    public static String formatDateTime(LocalDateTime dt) {
        if (dt == null) {
            return "—";
        }
        return dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
    }
}
