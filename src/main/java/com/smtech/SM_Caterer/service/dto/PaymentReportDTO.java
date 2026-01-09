package com.smtech.SM_Caterer.service.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for payment report data.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String paymentNumber;
    private String orderNumber;
    private String customerName;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionReference;
    private String upiId;
    private LocalDate paymentDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private String createdByName;
}
