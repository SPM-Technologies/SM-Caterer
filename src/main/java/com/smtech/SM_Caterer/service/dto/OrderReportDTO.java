package com.smtech.SM_Caterer.service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for order report data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNumber;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private LocalDate eventDate;
    private String eventTypeName;
    private Integer guestCount;
    private BigDecimal menuSubtotal;
    private BigDecimal utilitySubtotal;
    private BigDecimal grandTotal;
    private BigDecimal advanceAmount;
    private BigDecimal balanceAmount;
    private String status;
    private LocalDateTime createdAt;
    private String createdByName;
}
