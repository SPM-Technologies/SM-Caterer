package com.smtech.SM_Caterer.service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for customer report data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Long totalOrders;
    private BigDecimal totalValue;
    private BigDecimal totalPaid;
    private BigDecimal totalBalance;
    private Boolean isActive;
}
