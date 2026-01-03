package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.OrderStatus;
import lombok.*;

import java.time.LocalDate;

/**
 * Criteria DTO for searching orders.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchCriteria {

    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private Long customerId;
    private Long eventTypeId;
    private OrderStatus status;
    private LocalDate eventDateFrom;
    private LocalDate eventDateTo;
    private LocalDate createdDateFrom;
    private LocalDate createdDateTo;

    /**
     * Check if any filter is set.
     */
    public boolean hasAnyFilter() {
        return orderNumber != null ||
               customerName != null ||
               customerPhone != null ||
               customerId != null ||
               eventTypeId != null ||
               status != null ||
               eventDateFrom != null ||
               eventDateTo != null ||
               createdDateFrom != null ||
               createdDateTo != null;
    }

    /**
     * Create empty criteria.
     */
    public static OrderSearchCriteria empty() {
        return new OrderSearchCriteria();
    }

    /**
     * Create criteria with status filter.
     */
    public static OrderSearchCriteria withStatus(OrderStatus status) {
        return OrderSearchCriteria.builder().status(status).build();
    }

    /**
     * Create criteria with customer name search.
     */
    public static OrderSearchCriteria withCustomerName(String customerName) {
        return OrderSearchCriteria.builder().customerName(customerName).build();
    }

    /**
     * Create criteria with date range.
     */
    public static OrderSearchCriteria withDateRange(LocalDate from, LocalDate to) {
        return OrderSearchCriteria.builder()
            .eventDateFrom(from)
            .eventDateTo(to)
            .build();
    }
}
