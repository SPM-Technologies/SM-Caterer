package com.smtech.SM_Caterer.web.controller;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import com.smtech.SM_Caterer.domain.enums.Status;
import com.smtech.SM_Caterer.domain.repository.CustomerRepository;
import com.smtech.SM_Caterer.domain.repository.EventTypeRepository;
import com.smtech.SM_Caterer.domain.repository.MaterialGroupRepository;
import com.smtech.SM_Caterer.security.CustomUserDetails;
import com.smtech.SM_Caterer.service.ExcelExportService;
import com.smtech.SM_Caterer.service.ReportService;
import com.smtech.SM_Caterer.service.TenantService;
import com.smtech.SM_Caterer.service.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Web Controller for Report pages.
 * Handles report viewing with filters and pagination.
 */
@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")
@Slf4j
public class ReportWebController {

    private static final String EXCEL_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ReportService reportService;
    private final ExcelExportService excelExportService;
    private final TenantService tenantService;
    private final CustomerRepository customerRepository;
    private final EventTypeRepository eventTypeRepository;
    private final MaterialGroupRepository materialGroupRepository;

    // ===== Reports Index =====

    @GetMapping
    public String reportsIndex() {
        return "reports/index";
    }

    // ===== Order Reports =====

    @GetMapping("/orders")
    public String orderReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(required = false) OrderStatus status,
                               @RequestParam(required = false) Long customerId,
                               @RequestParam(required = false) Long eventTypeId,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "eventDate") String sortBy,
                               @RequestParam(defaultValue = "desc") String sortDir,
                               Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderReportDTO> reportPage = reportService.getOrderReport(
                tenantId, status, customerId, eventTypeId, fromDate, toDate, pageable);

        model.addAttribute("orders", reportPage.getContent());
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("customers", customerRepository.findByTenantId(tenantId));
        model.addAttribute("eventTypes", eventTypeRepository.findByTenantId(tenantId));

        addPaginationAttributes(model, page, size, sortBy, sortDir, reportPage);
        addFilterAttributes(model, status, customerId, eventTypeId, null, null, fromDate, toDate, null);

        log.debug("Order report: {} records for tenant {}", reportPage.getTotalElements(), tenantId);

        return "reports/orders";
    }

    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrders(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                @RequestParam(required = false) OrderStatus status,
                                                @RequestParam(required = false) Long customerId,
                                                @RequestParam(required = false) Long eventTypeId,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Long tenantId = userDetails.getTenantId();
        String tenantName = getTenantName(tenantId);

        List<OrderReportDTO> orders = reportService.getOrderReportForExport(
                tenantId, status, customerId, eventTypeId, fromDate, toDate);

        byte[] excelBytes = excelExportService.exportOrdersToExcel(orders, tenantName);
        String filename = "OrderReport_" + LocalDate.now().format(FILE_DATE_FORMAT) + ".xlsx";

        log.info("Exported {} orders to Excel for tenant {}", orders.size(), tenantId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(excelBytes);
    }

    // ===== Payment Reports =====

    @GetMapping("/payments")
    public String paymentReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestParam(required = false) PaymentStatus status,
                                 @RequestParam(required = false) PaymentMethod method,
                                 @RequestParam(required = false) Long customerId,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "20") int size,
                                 @RequestParam(defaultValue = "paymentDate") String sortBy,
                                 @RequestParam(defaultValue = "desc") String sortDir,
                                 Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PaymentReportDTO> reportPage = reportService.getPaymentReport(
                tenantId, status, method, customerId, fromDate, toDate, pageable);

        model.addAttribute("payments", reportPage.getContent());
        model.addAttribute("statuses", PaymentStatus.values());
        model.addAttribute("methods", PaymentMethod.values());
        model.addAttribute("customers", customerRepository.findByTenantId(tenantId));

        addPaginationAttributes(model, page, size, sortBy, sortDir, reportPage);
        addFilterAttributes(model, null, customerId, null, status, method, fromDate, toDate, null);

        log.debug("Payment report: {} records for tenant {}", reportPage.getTotalElements(), tenantId);

        return "reports/payments";
    }

    @GetMapping("/payments/export")
    public ResponseEntity<byte[]> exportPayments(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                  @RequestParam(required = false) PaymentStatus status,
                                                  @RequestParam(required = false) PaymentMethod method,
                                                  @RequestParam(required = false) Long customerId,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        Long tenantId = userDetails.getTenantId();
        String tenantName = getTenantName(tenantId);

        List<PaymentReportDTO> payments = reportService.getPaymentReportForExport(
                tenantId, status, method, customerId, fromDate, toDate);

        byte[] excelBytes = excelExportService.exportPaymentsToExcel(payments, tenantName);
        String filename = "PaymentReport_" + LocalDate.now().format(FILE_DATE_FORMAT) + ".xlsx";

        log.info("Exported {} payments to Excel for tenant {}", payments.size(), tenantId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(excelBytes);
    }

    // ===== Stock Reports =====

    @GetMapping("/stock")
    public String stockReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestParam(required = false) Long materialGroupId,
                               @RequestParam(required = false) Status status,
                               @RequestParam(required = false) String stockStatus,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size,
                               @RequestParam(defaultValue = "materialCode") String sortBy,
                               @RequestParam(defaultValue = "asc") String sortDir,
                               Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<StockReportDTO> reportPage = reportService.getStockReport(
                tenantId, materialGroupId, status, stockStatus, pageable);

        model.addAttribute("stocks", reportPage.getContent());
        model.addAttribute("statuses", Status.values());
        model.addAttribute("stockStatuses", new String[]{"IN_STOCK", "LOW_STOCK", "OUT_OF_STOCK"});
        model.addAttribute("materialGroups", materialGroupRepository.findByTenantId(tenantId));

        addPaginationAttributes(model, page, size, sortBy, sortDir, reportPage);
        model.addAttribute("materialGroupId", materialGroupId);
        model.addAttribute("status", status);
        model.addAttribute("stockStatus", stockStatus);

        log.debug("Stock report: {} records for tenant {}", reportPage.getTotalElements(), tenantId);

        return "reports/stock";
    }

    @GetMapping("/stock/export")
    public ResponseEntity<byte[]> exportStock(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @RequestParam(required = false) Long materialGroupId,
                                               @RequestParam(required = false) Status status,
                                               @RequestParam(required = false) String stockStatus) {
        Long tenantId = userDetails.getTenantId();
        String tenantName = getTenantName(tenantId);

        List<StockReportDTO> stocks = reportService.getStockReportForExport(
                tenantId, materialGroupId, status, stockStatus);

        byte[] excelBytes = excelExportService.exportStockToExcel(stocks, tenantName);
        String filename = "StockReport_" + LocalDate.now().format(FILE_DATE_FORMAT) + ".xlsx";

        log.info("Exported {} stock items to Excel for tenant {}", stocks.size(), tenantId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(excelBytes);
    }

    // ===== Customer Reports =====

    @GetMapping("/customers")
    public String customerReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @RequestParam(required = false) Status status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size,
                                  @RequestParam(defaultValue = "name") String sortBy,
                                  @RequestParam(defaultValue = "asc") String sortDir,
                                  Model model) {
        Long tenantId = userDetails.getTenantId();

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CustomerReportDTO> reportPage = reportService.getCustomerReport(tenantId, status, pageable);

        model.addAttribute("customers", reportPage.getContent());
        model.addAttribute("statuses", Status.values());

        addPaginationAttributes(model, page, size, sortBy, sortDir, reportPage);
        model.addAttribute("status", status);

        log.debug("Customer report: {} records for tenant {}", reportPage.getTotalElements(), tenantId);

        return "reports/customers";
    }

    @GetMapping("/customers/export")
    public ResponseEntity<byte[]> exportCustomers(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestParam(required = false) Status status) {
        Long tenantId = userDetails.getTenantId();
        String tenantName = getTenantName(tenantId);

        List<CustomerReportDTO> customers = reportService.getCustomerReportForExport(tenantId, status);

        byte[] excelBytes = excelExportService.exportCustomersToExcel(customers, tenantName);
        String filename = "CustomerReport_" + LocalDate.now().format(FILE_DATE_FORMAT) + ".xlsx";

        log.info("Exported {} customers to Excel for tenant {}", customers.size(), tenantId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(EXCEL_CONTENT_TYPE))
                .body(excelBytes);
    }

    // ===== Pending Balance Report =====

    @GetMapping("/pending-balance")
    public String pendingBalanceReport(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        Model model) {
        Long tenantId = userDetails.getTenantId();

        List<CustomerReportDTO> customers = reportService.getCustomersWithPendingBalance(tenantId);

        model.addAttribute("customers", customers);

        log.debug("Pending balance report: {} customers for tenant {}", customers.size(), tenantId);

        return "reports/pending-balance";
    }

    // ===== Private Helper Methods =====

    private void addPaginationAttributes(Model model, int page, int size, String sortBy,
                                          String sortDir, Page<?> reportPage) {
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        model.addAttribute("totalItems", reportPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
    }

    private void addFilterAttributes(Model model, OrderStatus orderStatus, Long customerId,
                                      Long eventTypeId, PaymentStatus paymentStatus,
                                      PaymentMethod paymentMethod, LocalDate fromDate,
                                      LocalDate toDate, String stockStatus) {
        if (orderStatus != null) model.addAttribute("status", orderStatus);
        if (paymentStatus != null) model.addAttribute("status", paymentStatus);
        if (customerId != null) model.addAttribute("customerId", customerId);
        if (eventTypeId != null) model.addAttribute("eventTypeId", eventTypeId);
        if (paymentMethod != null) model.addAttribute("method", paymentMethod);
        if (fromDate != null) model.addAttribute("fromDate", fromDate);
        if (toDate != null) model.addAttribute("toDate", toDate);
        if (stockStatus != null) model.addAttribute("stockStatus", stockStatus);
    }

    private String getTenantName(Long tenantId) {
        return tenantService.findById(tenantId)
                .map(t -> t.getBusinessName())
                .orElse("Unknown");
    }
}
