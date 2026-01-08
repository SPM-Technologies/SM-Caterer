package com.smtech.SM_Caterer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO for email settings form.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSettingsDTO {

    private boolean emailEnabled;

    @Size(max = 255, message = "SMTP host must not exceed 255 characters")
    private String smtpHost;

    private Integer smtpPort;

    @Size(max = 255, message = "SMTP username must not exceed 255 characters")
    private String smtpUsername;

    @Size(max = 255, message = "SMTP password must not exceed 255 characters")
    private String smtpPassword;

    @Email(message = "Invalid from email address")
    @Size(max = 255, message = "From email must not exceed 255 characters")
    private String smtpFromEmail;

    @Size(max = 100, message = "From name must not exceed 100 characters")
    private String smtpFromName;

    private boolean smtpUseTls;
}
