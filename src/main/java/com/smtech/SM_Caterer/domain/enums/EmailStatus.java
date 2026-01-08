package com.smtech.SM_Caterer.domain.enums;

/**
 * Status of email sending attempts.
 */
public enum EmailStatus {
    PENDING("Pending"),
    SENT("Sent"),
    FAILED("Failed"),
    RETRY("Retry");

    private final String displayName;

    EmailStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
