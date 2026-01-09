package com.smtech.SM_Caterer.service;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for Report operations.
 * Provides filtered report data for various entities.
 */
public interface ReportService {

    // ===== Order Reports =====

    /**
     * Gets paginated order report with filters.
     */
    Page<OrderReportDTO> getOrderReport(Long tenantId, OrderStatus status, Long customerId,
                                         Long eventTypeId, LocalDate fromDate, LocalDate toDate,
                                         Pageable pageable);

    /**
     * Gets all order data for export (no pagination).
     */
    List<OrderReportDTO> getOrderReportForExport(Long tenantId, OrderStatus status, Long customerId,
                                                  Long eventTypeId, LocalDate fromDate, LocalDate toDate);

    // ===== Payment Reports =====

    /**
     * Gets paginated payment report with filters.
     */
    Page<PaymentReportDTO> getPaymentReport(Long tenantId, PaymentStatus status, PaymentMethod method,
                                             Long customerId, LocalDate fromDate, LocalDate toDate,
                                             Pageable pageable);

    /**
     * Gets all payment data for export (no pagination).
     */
    List<PaymentReportDTO> getPaymentReportForExport(Long tenantId, PaymentStatus status, PaymentMethod method,
                                                      Long customerId, LocalDate fromDate, LocalDate toDate);

    // ===== Stock Reports =====

    /**
     * Gets paginated stock report with filters.
     */
    Page<StockReportDTO> getStockReport(Long tenantId, Long materialGroupId, Status status,
                                         String stockStatus, Pageable pageable);

    /**
     * Gets all stock data for export (no pagination).
     */
    List<StockReportDTO> getStockReportForExport(Long tenantId, Long materialGroupId, Status status,
                                                  String stockStatus);

    // ===== Customer Reports =====

    /**
     * Gets paginated customer report with filters.
     */
    Page<CustomerReportDTO> getCustomerReport(Long tenantId, Status status, Pageable pageable);

    /**
     * Gets all customer data for export (no pagination).
     */
    List<CustomerReportDTO> getCustomerReportForExport(Long tenantId, Status status);

    /**
     * Gets customers with pending balances.
     */
    List<CustomerReportDTO> getCustomersWithPendingBalance(Long tenantId);
}
