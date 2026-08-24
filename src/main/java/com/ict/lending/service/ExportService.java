package com.ict.lending.service;

import com.ict.lending.model.Transaction;
import com.ict.lending.utils.AppPaths;
import com.ict.lending.utils.IdGenerator;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExportService {

    private final AuditService auditService = new AuditService();

    public Path exportExcel(List<Transaction> transactions) {
        try {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path file = AppPaths.exportDir().resolve("transactions_" + stamp + ".xlsx");
            try (Workbook wb = new XSSFWorkbook()) {
                Sheet sheet = wb.createSheet("Transactions");
                String[] headers = {
                        "Transaction ID", "Borrower", "Device", "Qty",
                        "Borrow Date", "Borrow Time", "Return Date", "Return Time", "Status"
                };
                Row header = sheet.createRow(0);
                for (int i = 0; i < headers.length; i++) {
                    header.createCell(i).setCellValue(headers[i]);
                }
                int rowIdx = 1;
                for (Transaction t : transactions) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(t.getTransactionId());
                    row.createCell(1).setCellValue(nullSafe(t.getBorrowerName()));
                    row.createCell(2).setCellValue(nullSafe(t.getDeviceName()));
                    row.createCell(3).setCellValue(t.getQuantity());
                    row.createCell(4).setCellValue(IdGenerator.formatDate(t.getBorrowDate()));
                    row.createCell(5).setCellValue(IdGenerator.formatTime(t.getBorrowTime()));
                    row.createCell(6).setCellValue(IdGenerator.formatDate(t.getReturnDate()));
                    row.createCell(7).setCellValue(IdGenerator.formatTime(t.getReturnTime()));
                    row.createCell(8).setCellValue(nullSafe(t.getStatus()));
                }
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                try (OutputStream out = Files.newOutputStream(file)) {
                    wb.write(out);
                }
            }
            auditService.log("EXPORT_EXCEL", "Exported " + transactions.size() + " transactions");
            return file;
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed: " + e.getMessage(), e);
        }
    }

    public Path exportPdfReport(List<Transaction> transactions) {
        try {
            String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            Path file = AppPaths.exportDir().resolve("transactions_" + stamp + ".pdf");
            StringBuilder rows = new StringBuilder();
            for (Transaction t : transactions) {
                rows.append("<tr>")
                        .append("<td>").append(esc(t.getTransactionId())).append("</td>")
                        .append("<td>").append(esc(t.getBorrowerName())).append("</td>")
                        .append("<td>").append(esc(t.getDeviceName())).append("</td>")
                        .append("<td>").append(t.getQuantity()).append("</td>")
                        .append("<td>").append(esc(IdGenerator.formatDate(t.getBorrowDate()))).append("</td>")
                        .append("<td>").append(esc(IdGenerator.formatTime(t.getBorrowTime()))).append("</td>")
                        .append("<td>").append(esc(IdGenerator.formatDate(t.getReturnDate()))).append("</td>")
                        .append("<td>").append(esc(IdGenerator.formatTime(t.getReturnTime()))).append("</td>")
                        .append("<td>").append(esc(t.getStatus())).append("</td>")
                        .append("</tr>");
            }
            String html = """
                    <html><head><style>
                    body{font-family:Segoe UI,Arial,sans-serif;font-size:10px;color:#1a1a1a}
                    h1{font-size:16px;margin:0 0 8px}
                    p{margin:0 0 12px;color:#555}
                    table{width:100%%;border-collapse:collapse}
                    th,td{border:1px solid #ccc;padding:4px 6px;text-align:left}
                    th{background:#1e293b;color:#fff}
                    </style></head><body>
                    <h1>Device Lending — Transaction Report</h1>
                    <p>Generated %s · %d record(s)</p>
                    <table>
                    <thead><tr>
                    <th>ID</th><th>Borrower</th><th>Device</th><th>Qty</th>
                    <th>Borrow Date</th><th>Borrow Time</th>
                    <th>Return Date</th><th>Return Time</th><th>Status</th>
                    </tr></thead>
                    <tbody>%s</tbody>
                    </table></body></html>
                    """.formatted(
                    IdGenerator.formatDateTime(LocalDateTime.now()),
                    transactions.size(),
                    rows);
            renderPdf(html, file);
            auditService.log("EXPORT_PDF", "Exported " + transactions.size() + " transactions to PDF");
            return file;
        } catch (Exception e) {
            throw new RuntimeException("PDF export failed: " + e.getMessage(), e);
        }
    }

    public Path printReceipt(Transaction txn) {
        try {
            Path file = AppPaths.exportDir().resolve("receipt_" + txn.getTransactionId() + ".pdf");
            String template;
            try (InputStream in = getClass().getResourceAsStream("/templates/receipt.html")) {
                if (in == null) {
                    throw new IllegalStateException("Receipt template missing.");
                }
                template = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
            String html = template
                    .replace("{{transactionId}}", esc(txn.getTransactionId()))
                    .replace("{{borrowerName}}", esc(txn.getBorrowerName()))
                    .replace("{{position}}", esc(txn.getPosition()))
                    .replace("{{gradeLevel}}", esc(txn.getGradeLevel()))
                    .replace("{{section}}", esc(txn.getSection()))
                    .replace("{{purpose}}", esc(nullSafe(txn.getPurpose())))
                    .replace("{{deviceName}}", esc(txn.getDeviceName()))
                    .replace("{{quantity}}", String.valueOf(txn.getQuantity()))
                    .replace("{{borrowDate}}", esc(IdGenerator.formatDate(txn.getBorrowDate())))
                    .replace("{{borrowTime}}", esc(IdGenerator.formatTime(txn.getBorrowTime())))
                    .replace("{{returnDate}}", esc(IdGenerator.formatDate(txn.getReturnDate())))
                    .replace("{{returnTime}}", esc(IdGenerator.formatTime(txn.getReturnTime())))
                    .replace("{{status}}", esc(txn.getStatus()));
            renderPdf(html, file);
            auditService.log("PRINT_RECEIPT", "Receipt for " + txn.getTransactionId());
            return file;
        } catch (Exception e) {
            throw new RuntimeException("Receipt generation failed: " + e.getMessage(), e);
        }
    }

    private void renderPdf(String html, Path file) throws Exception {
        try (OutputStream out = Files.newOutputStream(file)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
        }
    }

    private static String nullSafe(String v) {
        return v == null ? "" : v;
    }

    private static String esc(String v) {
        if (v == null) {
            return "";
        }
        return v.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
