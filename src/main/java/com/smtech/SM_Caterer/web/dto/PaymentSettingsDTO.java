package com.smtech.SM_Caterer.web.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for payment settings form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSettingsDTO {

    private boolean paymentEnabled;

    @Size(max = 100, message = "UPI ID must not exceed 100 characters")
    private String defaultUpiId;

    @Size(max = 200, message = "UPI payee name must not exceed 200 characters")
    private String upiPayeeName;
}
