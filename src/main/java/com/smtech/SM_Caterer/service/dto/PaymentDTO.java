package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.PaymentMethod;
import com.smtech.SM_Caterer.domain.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Payment entity.
 * Represents a payment transaction for an order.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO extends BaseDTO {

    private Long tenantId;

    @NotNull(message = "Order is required")
    private Long orderId;
    private String orderNumber; // For display

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 200, message = "Transaction reference must not exceed 200 characters")
    private String transactionReference;

    private LocalDateTime paymentDate;

    @NotNull(message = "Status is required")
    private PaymentStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
