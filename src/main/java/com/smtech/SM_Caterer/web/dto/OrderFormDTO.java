package com.smtech.SM_Caterer.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Form DTO for multi-step order creation wizard.
 * Stored in HTTP session during wizard flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFormDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Current wizard step (1-5)
    @Builder.Default
    private int currentStep = 1;

    // ===== Step 1: Customer =====

    private Long customerId;
    private String customerName;      // Display only
    private String customerPhone;     // Display only
    private String customerCode;      // Display only

    // Quick customer creation (if new)
    @Valid
    private CustomerQuickCreateDTO newCustomer;

    @Builder.Default
    private boolean createNewCustomer = false;

    // ===== Step 2: Event Details =====

    @NotNull(message = "Event type is required")
    private Long eventTypeId;
    private String eventTypeName;     // Display only
    private String eventTypeCode;     // Display only

    @NotNull(message = "Event date is required")
    private LocalDate eventDate;

    private LocalTime eventTime;

    @NotBlank(message = "Venue name is required")
    @Size(max = 200, message = "Venue name must not exceed 200 characters")
    private String venueName;

    @Size(max = 1000, message = "Venue address must not exceed 1000 characters")
    private String venueAddress;

    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "Guest count must be at least 1")
    @Max(value = 10000, message = "Guest count cannot exceed 10,000")
    private Integer guestCount;

    // ===== Step 3: Menu Items =====

    @Valid
    @Builder.Default
    private List<OrderMenuItemFormDTO> menuItems = new ArrayList<>();

    // ===== Step 4: Utilities =====

    @Valid
    @Builder.Default
    private List<OrderUtilityFormDTO> utilities = new ArrayList<>();

    // ===== Step 5: Pricing & Notes =====

    @DecimalMin(value = "0.00", message = "Discount must be non-negative")
    @DecimalMax(value = "100.00", message = "Discount cannot exceed 100%")
    @Builder.Default
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @DecimalMin(value = "0.00", message = "Tax must be non-negative")
    @Builder.Default
    private BigDecimal taxPercent = BigDecimal.ZERO;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    // ===== Calculated Totals =====

    @Builder.Default
    private BigDecimal menuSubtotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal utilitySubtotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    // ===== Validation Methods =====

    /**
     * Validates Step 1 - Customer selection.
     */
    public boolean isStep1Valid() {
        if (createNewCustomer) {
            return newCustomer != null &&
                   newCustomer.getName() != null && !newCustomer.getName().isBlank() &&
                   newCustomer.getPhone() != null && !newCustomer.getPhone().isBlank();
        }
        return customerId != null;
    }

    /**
     * Validates Step 2 - Event details.
     */
    public boolean isStep2Valid() {
        return eventTypeId != null &&
               eventDate != null &&
               venueName != null && !venueName.isBlank() &&
               guestCount != null && guestCount > 0;
    }

    /**
     * Validates Step 3 - Menu items.
     */
    public boolean isStep3Valid() {
        return menuItems != null && !menuItems.isEmpty() &&
               menuItems.stream().allMatch(OrderMenuItemFormDTO::isValid);
    }

    /**
     * Validates Step 4 - Utilities (optional but items must be valid if present).
     */
    public boolean isStep4Valid() {
        if (utilities == null || utilities.isEmpty()) {
            return true; // Utilities are optional
        }
        return utilities.stream().allMatch(OrderUtilityFormDTO::isValid);
    }

    /**
     * Validates all steps.
     */
    public boolean isAllStepsValid() {
        return isStep1Valid() && isStep2Valid() && isStep3Valid() && isStep4Valid();
    }

    // ===== Calculation Methods =====

    /**
     * Calculate all totals based on items.
     */
    public void recalculateTotals() {
        // Menu subtotal
        this.menuSubtotal = menuItems.stream()
            .peek(OrderMenuItemFormDTO::calculateSubtotal)
            .map(OrderMenuItemFormDTO::getSubtotal)
            .filter(s -> s != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Utility subtotal
        this.utilitySubtotal = utilities.stream()
            .peek(OrderUtilityFormDTO::calculateSubtotal)
            .map(OrderUtilityFormDTO::getSubtotal)
            .filter(s -> s != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Subtotal
        this.subtotal = menuSubtotal.add(utilitySubtotal);

        // Discount
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = subtotal.multiply(discountPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            this.discountAmount = BigDecimal.ZERO;
        }

        // After discount
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        // Tax
        if (taxPercent != null && taxPercent.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = afterDiscount.multiply(taxPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            this.taxAmount = BigDecimal.ZERO;
        }

        // Grand total
        this.grandTotal = afterDiscount.add(taxAmount);
    }

    // ===== Helper Methods =====

    /**
     * Add a menu item.
     */
    public void addMenuItem(OrderMenuItemFormDTO item) {
        if (menuItems == null) {
            menuItems = new ArrayList<>();
        }
        menuItems.add(item);
    }

    /**
     * Remove a menu item by index.
     */
    public void removeMenuItem(int index) {
        if (menuItems != null && index >= 0 && index < menuItems.size()) {
            menuItems.remove(index);
        }
    }

    /**
     * Add a utility item.
     */
    public void addUtility(OrderUtilityFormDTO item) {
        if (utilities == null) {
            utilities = new ArrayList<>();
        }
        utilities.add(item);
    }

    /**
     * Remove a utility item by index.
     */
    public void removeUtility(int index) {
        if (utilities != null && index >= 0 && index < utilities.size()) {
            utilities.remove(index);
        }
    }

    /**
     * Reset form to initial state.
     */
    public void reset() {
        this.currentStep = 1;
        this.customerId = null;
        this.customerName = null;
        this.customerPhone = null;
        this.customerCode = null;
        this.newCustomer = null;
        this.createNewCustomer = false;
        this.eventTypeId = null;
        this.eventTypeName = null;
        this.eventTypeCode = null;
        this.eventDate = null;
        this.eventTime = null;
        this.venueName = null;
        this.venueAddress = null;
        this.guestCount = null;
        this.menuItems = new ArrayList<>();
        this.utilities = new ArrayList<>();
        this.discountPercent = BigDecimal.ZERO;
        this.taxPercent = BigDecimal.ZERO;
        this.notes = null;
        this.menuSubtotal = BigDecimal.ZERO;
        this.utilitySubtotal = BigDecimal.ZERO;
        this.subtotal = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.taxAmount = BigDecimal.ZERO;
        this.grandTotal = BigDecimal.ZERO;
    }
}
