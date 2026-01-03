package com.smtech.SM_Caterer.web.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;

/**
 * Form DTO for quick customer creation in order wizard.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerQuickCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Customer name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Pattern(regexp = "^$|^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    /**
     * Validate the quick create form.
     */
    public boolean isValid() {
        return name != null && !name.isBlank() &&
               phone != null && phone.matches("^[0-9]{10}$");
    }
}
