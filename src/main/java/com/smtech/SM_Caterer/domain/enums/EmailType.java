package com.smtech.SM_Caterer.domain.enums;

/**
 * Types of emails sent by the system.
 */
public enum EmailType {
    ORDER_CONFIRMATION("Order Confirmation"),
    PAYMENT_RECEIPT("Payment Receipt"),
    ORDER_STATUS_UPDATE("Order Status Update"),
    TEST_EMAIL("Test Email");

    private final String displayName;

    EmailType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
