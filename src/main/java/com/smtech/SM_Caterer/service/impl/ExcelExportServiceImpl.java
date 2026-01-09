package com.smtech.SM_Caterer.service.impl;

import com.smtech.SM_Caterer.service.ExcelExportService;
import com.smtech.SM_Caterer.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service implementation for Excel export operations.
 * Uses Apache POI to generate Excel files (.xlsx format).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Override
    public byte[] exportOrdersToExcel(List<OrderReportDTO> orders, String tenantName) {
        log.debug("Exporting {} orders to Excel for tenant: {}", orders.size(), tenantName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Orders");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Order Report - " + tenantName);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 12));

            // Generated timestamp
            Row timestampRow = sheet.createRow(rowNum++);
            timestampRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMAT));

            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Order #", "Customer", "Phone", "Email", "Event Date", "Event Type",
                    "Guests", "Menu Total", "Utility Total", "Grand Total", "Paid", "Balance", "Status"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (OrderReportDTO order : orders) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                row.createCell(colNum++).setCellValue(nullSafe(order.getOrderNumber()));
                row.createCell(colNum++).setCellValue(nullSafe(order.getCustomerName()));
                row.createCell(colNum++).setCellValue(nullSafe(order.getCustomerPhone()));
                row.createCell(colNum++).setCellValue(nullSafe(order.getCustomerEmail()));

                Cell dateCell = row.createCell(colNum++);
                if (order.getEventDate() != null) {
                    dateCell.setCellValue(order.getEventDate().format(DATE_FORMAT));
                }
                dateCell.setCellStyle(dateStyle);

                row.createCell(colNum++).setCellValue(nullSafe(order.getEventTypeName()));
                row.createCell(colNum++).setCellValue(order.getGuestCount() != null ? order.getGuestCount() : 0);

                Cell menuCell = row.createCell(colNum++);
                menuCell.setCellValue(toDouble(order.getMenuSubtotal()));
                menuCell.setCellStyle(currencyStyle);

                Cell utilityCell = row.createCell(colNum++);
                utilityCell.setCellValue(toDouble(order.getUtilitySubtotal()));
                utilityCell.setCellStyle(currencyStyle);

                Cell grandTotalCell = row.createCell(colNum++);
                grandTotalCell.setCellValue(toDouble(order.getGrandTotal()));
                grandTotalCell.setCellStyle(currencyStyle);

                Cell paidCell = row.createCell(colNum++);
                paidCell.setCellValue(toDouble(order.getAdvanceAmount()));
                paidCell.setCellStyle(currencyStyle);

                Cell balanceCell = row.createCell(colNum++);
                balanceCell.setCellValue(toDouble(order.getBalanceAmount()));
                balanceCell.setCellStyle(currencyStyle);

                row.createCell(colNum++).setCellValue(nullSafe(order.getStatus()));
            }

            // Summary row
            rowNum++; // Empty row
            Row summaryRow = sheet.createRow(rowNum);
            summaryRow.createCell(0).setCellValue("Total Records: " + orders.size());

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("Failed to export orders to Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    @Override
    public byte[] exportPaymentsToExcel(List<PaymentReportDTO> payments, String tenantName) {
        log.debug("Exporting {} payments to Excel for tenant: {}", payments.size(), tenantName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Payments");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Payment Report - " + tenantName);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // Generated timestamp
            Row timestampRow = sheet.createRow(rowNum++);
            timestampRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMAT));

            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Payment #", "Order #", "Customer", "Amount", "Method",
                    "Reference", "UPI ID", "Date", "Status", "Notes"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (PaymentReportDTO payment : payments) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                row.createCell(colNum++).setCellValue(nullSafe(payment.getPaymentNumber()));
                row.createCell(colNum++).setCellValue(nullSafe(payment.getOrderNumber()));
                row.createCell(colNum++).setCellValue(nullSafe(payment.getCustomerName()));

                Cell amountCell = row.createCell(colNum++);
                amountCell.setCellValue(toDouble(payment.getAmount()));
                amountCell.setCellStyle(currencyStyle);

                if (payment.getAmount() != null) {
                    totalAmount = totalAmount.add(payment.getAmount());
                }

                row.createCell(colNum++).setCellValue(nullSafe(payment.getPaymentMethod()));
                row.createCell(colNum++).setCellValue(nullSafe(payment.getTransactionReference()));
                row.createCell(colNum++).setCellValue(nullSafe(payment.getUpiId()));

                Cell dateCell = row.createCell(colNum++);
                if (payment.getPaymentDate() != null) {
                    dateCell.setCellValue(payment.getPaymentDate().format(DATE_FORMAT));
                }
                dateCell.setCellStyle(dateStyle);

                row.createCell(colNum++).setCellValue(nullSafe(payment.getStatus()));
                row.createCell(colNum++).setCellValue(nullSafe(payment.getNotes()));
            }

            // Summary row
            rowNum++; // Empty row
            Row summaryRow = sheet.createRow(rowNum);
            summaryRow.createCell(0).setCellValue("Total Records: " + payments.size());
            summaryRow.createCell(3).setCellValue("Total Amount:");
            Cell totalCell = summaryRow.createCell(4);
            totalCell.setCellValue(totalAmount.doubleValue());
            totalCell.setCellStyle(currencyStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("Failed to export payments to Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    @Override
    public byte[] exportStockToExcel(List<StockReportDTO> stocks, String tenantName) {
        log.debug("Exporting {} stock items to Excel for tenant: {}", stocks.size(), tenantName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Stock");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Stock Report - " + tenantName);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 9));

            // Generated timestamp
            Row timestampRow = sheet.createRow(rowNum++);
            timestampRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMAT));

            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Code", "Name", "Group", "Current Stock", "Minimum", "Unit",
                    "Cost/Unit", "Total Value", "Status", "Active"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            BigDecimal totalValue = BigDecimal.ZERO;
            for (StockReportDTO stock : stocks) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                row.createCell(colNum++).setCellValue(nullSafe(stock.getMaterialCode()));
                row.createCell(colNum++).setCellValue(nullSafe(stock.getMaterialName()));
                row.createCell(colNum++).setCellValue(nullSafe(stock.getGroupName()));

                Cell stockCell = row.createCell(colNum++);
                stockCell.setCellValue(toDouble(stock.getCurrentStock()));
                stockCell.setCellStyle(numberStyle);

                Cell minCell = row.createCell(colNum++);
                minCell.setCellValue(toDouble(stock.getMinimumStock()));
                minCell.setCellStyle(numberStyle);

                row.createCell(colNum++).setCellValue(nullSafe(stock.getUnitSymbol()));

                Cell costCell = row.createCell(colNum++);
                costCell.setCellValue(toDouble(stock.getCostPerUnit()));
                costCell.setCellStyle(currencyStyle);

                Cell valueCell = row.createCell(colNum++);
                valueCell.setCellValue(toDouble(stock.getTotalValue()));
                valueCell.setCellStyle(currencyStyle);

                if (stock.getTotalValue() != null) {
                    totalValue = totalValue.add(stock.getTotalValue());
                }

                row.createCell(colNum++).setCellValue(nullSafe(stock.getStockStatus()));
                row.createCell(colNum++).setCellValue(stock.getIsActive() != null && stock.getIsActive() ? "Yes" : "No");
            }

            // Summary row
            rowNum++; // Empty row
            Row summaryRow = sheet.createRow(rowNum);
            summaryRow.createCell(0).setCellValue("Total Records: " + stocks.size());
            summaryRow.createCell(6).setCellValue("Total Value:");
            Cell totalCell = summaryRow.createCell(7);
            totalCell.setCellValue(totalValue.doubleValue());
            totalCell.setCellStyle(currencyStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("Failed to export stock to Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    @Override
    public byte[] exportCustomersToExcel(List<CustomerReportDTO> customers, String tenantName) {
        log.debug("Exporting {} customers to Excel for tenant: {}", customers.size(), tenantName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Customers");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Customer Report - " + tenantName);
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 8));

            // Generated timestamp
            Row timestampRow = sheet.createRow(rowNum++);
            timestampRow.createCell(0).setCellValue("Generated: " + LocalDateTime.now().format(DATETIME_FORMAT));

            rowNum++; // Empty row

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Name", "Phone", "Email", "Address", "Total Orders",
                    "Total Value", "Total Paid", "Balance", "Active"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            BigDecimal totalBalance = BigDecimal.ZERO;
            for (CustomerReportDTO customer : customers) {
                Row row = sheet.createRow(rowNum++);
                int colNum = 0;

                row.createCell(colNum++).setCellValue(nullSafe(customer.getName()));
                row.createCell(colNum++).setCellValue(nullSafe(customer.getPhone()));
                row.createCell(colNum++).setCellValue(nullSafe(customer.getEmail()));
                row.createCell(colNum++).setCellValue(nullSafe(customer.getAddress()));
                row.createCell(colNum++).setCellValue(customer.getTotalOrders() != null ? customer.getTotalOrders() : 0);

                Cell valueCell = row.createCell(colNum++);
                valueCell.setCellValue(toDouble(customer.getTotalValue()));
                valueCell.setCellStyle(currencyStyle);

                Cell paidCell = row.createCell(colNum++);
                paidCell.setCellValue(toDouble(customer.getTotalPaid()));
                paidCell.setCellStyle(currencyStyle);

                Cell balanceCell = row.createCell(colNum++);
                balanceCell.setCellValue(toDouble(customer.getTotalBalance()));
                balanceCell.setCellStyle(currencyStyle);

                if (customer.getTotalBalance() != null) {
                    totalBalance = totalBalance.add(customer.getTotalBalance());
                }

                row.createCell(colNum++).setCellValue(customer.getIsActive() != null && customer.getIsActive() ? "Yes" : "No");
            }

            // Summary row
            rowNum++; // Empty row
            Row summaryRow = sheet.createRow(rowNum);
            summaryRow.createCell(0).setCellValue("Total Records: " + customers.size());
            summaryRow.createCell(6).setCellValue("Total Balance:");
            Cell totalCell = summaryRow.createCell(7);
            totalCell.setCellValue(totalBalance.doubleValue());
            totalCell.setCellStyle(currencyStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            return writeWorkbookToBytes(workbook);
        } catch (IOException e) {
            log.error("Failed to export customers to Excel: {}", e.getMessage());
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    // ===== Private Helper Methods =====

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private byte[] writeWorkbookToBytes(Workbook workbook) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String nullSafe(String value) {
        return value != null ? value : "";
    }

    private double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : 0.0;
    }
}
