package com.smtech.SM_Caterer.service.dto;

import com.smtech.SM_Caterer.domain.enums.UpiQrCodeStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for UpiQrCode entity.
 * Stores UPI payment details for generating QR codes.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpiQrCodeDTO extends BaseDTO {

    private Long tenantId;

    @NotBlank(message = "Label is required")
    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    @NotBlank(message = "UPI ID is required")
    @Size(max = 100, message = "UPI ID must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$",
             message = "Invalid UPI ID format (e.g., user@bank)")
    private String upiId;

    @Size(max = 100, message = "Merchant name must not exceed 100 characters")
    private String merchantName;

    @Size(max = 50, message = "Merchant code must not exceed 50 characters")
    private String merchantCode;

    private UpiQrCodeStatus status;
}
