package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.service.dto.*;

import java.util.List;

/**
 * Service interface for Excel export operations.
 * Provides methods to export report data to Excel format.
 */
public interface ExcelExportService {

    /**
     * Exports order report data to Excel.
     * @param orders List of order report data
     * @param tenantName Tenant name for header
     * @return Excel file as byte array
     */
    byte[] exportOrdersToExcel(List<OrderReportDTO> orders, String tenantName);

    /**
     * Exports payment report data to Excel.
     * @param payments List of payment report data
     * @param tenantName Tenant name for header
     * @return Excel file as byte array
     */
    byte[] exportPaymentsToExcel(List<PaymentReportDTO> payments, String tenantName);

    /**
     * Exports stock report data to Excel.
     * @param stocks List of stock report data
     * @param tenantName Tenant name for header
     * @return Excel file as byte array
     */
    byte[] exportStockToExcel(List<StockReportDTO> stocks, String tenantName);

    /**
     * Exports customer report data to Excel.
     * @param customers List of customer report data
     * @param tenantName Tenant name for header
     * @return Excel file as byte array
     */
    byte[] exportCustomersToExcel(List<CustomerReportDTO> customers, String tenantName);
}
