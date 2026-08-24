package com.ict.lending;

import com.ict.lending.database.DatabaseConnection;
import com.ict.lending.model.Borrower;
import com.ict.lending.model.DashboardStats;
import com.ict.lending.model.Device;
import com.ict.lending.model.Transaction;
import com.ict.lending.service.AuthService;
import com.ict.lending.service.BackupService;
import com.ict.lending.service.BorrowService;
import com.ict.lending.service.DashboardService;
import com.ict.lending.service.DeviceService;
import com.ict.lending.service.ExportService;
import com.ict.lending.service.HistoryService;
import com.ict.lending.service.ReturnService;
import com.ict.lending.utils.AppPaths;

import java.nio.file.Files;
import java.util.List;

/**
 * Headless smoke test for services (no JavaFX).
 * Run: mvnw exec:java -Dexec.mainClass=com.ict.lending.SmokeTest
 */
public final class SmokeTest {

    public static void main(String[] args) throws Exception {
        // Use isolated test data folder
        System.setProperty("user.dir", AppPaths.dataDir().getParent().toString());

        DatabaseConnection.getInstance();
        AuthService auth = new AuthService();
        assertTrue(auth.login("admin", "admin123"), "admin login");

        DeviceService devices = new DeviceService();
        List<Device> list = devices.list("", "All");
        assertTrue(!list.isEmpty(), "seed devices exist");
        Device first = list.get(0);

        Borrower borrower = new Borrower();
        borrower.setFullName("Juan Dela Cruz");
        borrower.setPosition("Student");
        borrower.setGradeLevel("Grade 12");
        borrower.setSection("ICT-A");
        borrower.setPurpose("Capstone demo");

        BorrowService borrowService = new BorrowService();
        Transaction txn = borrowService.borrow(first.getDeviceId(), 1, borrower);
        assertTrue(txn.getTransactionId().startsWith("TXN-"), "transaction id");

        DashboardStats stats = new DashboardService().loadStats();
        assertTrue(stats.getBorrowedDevices() >= 1, "borrowed count");

        ReturnService returnService = new ReturnService();
        Transaction returned = returnService.returnDevice(
                txn.getTransactionId(), "Juan Dela Cruz", "Student", "Grade 12", "ICT-A");
        assertTrue("Returned".equals(returned.getStatus()), "return status");

        List<Transaction> history = new HistoryService().list("", "All");
        assertTrue(!history.isEmpty(), "history has rows");

        ExportService export = new ExportService();
        var excel = export.exportExcel(history);
        var pdf = export.exportPdfReport(history);
        var receipt = export.printReceipt(returned);
        assertTrue(Files.exists(excel), "excel exists");
        assertTrue(Files.exists(pdf), "pdf exists");
        assertTrue(Files.exists(receipt), "receipt exists");

        var backup = new BackupService().backupNow();
        assertTrue(Files.exists(backup), "backup exists");

        System.out.println("SMOKE TEST PASSED");
        System.out.println("DB: " + AppPaths.databaseFile());
        System.out.println("Txn: " + txn.getTransactionId());
        System.out.println("Exports: " + AppPaths.exportDir());
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("FAILED: " + message);
        }
        System.out.println("OK: " + message);
    }
}
