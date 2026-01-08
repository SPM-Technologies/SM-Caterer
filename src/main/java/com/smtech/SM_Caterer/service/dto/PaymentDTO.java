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
import java.time.LocalDate;

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

    private String paymentNumber;

    @NotNull(message = "Order is required")
    private Long orderId;
    private String orderNumber; // For display
    private String customerName; // For display

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @Size(max = 100, message = "Transaction reference must not exceed 100 characters")
    private String transactionReference;

    @Size(max = 100, message = "UPI ID must not exceed 100 characters")
    private String upiId;

    @NotNull(message = "Status is required")
    @Builder.Default
    private PaymentStatus status = PaymentStatus.COMPLETED;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    private String receiptPath;

    @Builder.Default
    private Boolean emailSent = false;
}
