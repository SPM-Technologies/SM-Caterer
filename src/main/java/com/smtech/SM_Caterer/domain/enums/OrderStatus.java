package com.smtech.SM_Caterer.domain.enums;

/**
 * Order lifecycle statuses with workflow transitions.
 *
 * Flow: DRAFT -> PENDING -> CONFIRMED -> IN_PROGRESS -> COMPLETED
 *       Any status (except COMPLETED) can transition to CANCELLED
 */
public enum OrderStatus {
    DRAFT("Draft", "Order being created"),
    PENDING("Pending Approval", "Awaiting manager approval"),
    CONFIRMED("Confirmed", "Order approved and scheduled"),
    IN_PROGRESS("In Progress", "Event is ongoing"),
    COMPLETED("Completed", "Event completed successfully"),
    CANCELLED("Cancelled", "Order cancelled");

    private final String displayName;
    private final String description;

    OrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if order can be edited in this status.
     */
    public boolean isEditable() {
        return this == DRAFT || this == PENDING;
    }

    /**
     * Check if order can be cancelled from this status.
     */
    public boolean isCancellable() {
        return this != COMPLETED && this != CANCELLED;
    }

    /**
     * Check if order is in a terminal state.
     */
    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    /**
     * Check if this is an active (non-terminal) status.
     */
    public boolean isActive() {
        return !isTerminal();
    }

    /**
     * Get valid next statuses from current status.
     */
    public OrderStatus[] getNextStatuses() {
        return switch (this) {
            case DRAFT -> new OrderStatus[]{PENDING, CANCELLED};
            case PENDING -> new OrderStatus[]{CONFIRMED, CANCELLED};
            case CONFIRMED -> new OrderStatus[]{IN_PROGRESS, CANCELLED};
            case IN_PROGRESS -> new OrderStatus[]{COMPLETED, CANCELLED};
            case COMPLETED, CANCELLED -> new OrderStatus[]{};
        };
    }

    /**
     * Check if transition to target status is valid.
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (target == null) return false;
        for (OrderStatus next : getNextStatuses()) {
            if (next == target) return true;
        }
        return false;
    }

    /**
     * Get Bootstrap badge class for this status.
     */
    public String getBadgeClass() {
        return switch (this) {
            case DRAFT -> "bg-secondary";
            case PENDING -> "bg-warning text-dark";
            case CONFIRMED -> "bg-info";
            case IN_PROGRESS -> "bg-primary";
            case COMPLETED -> "bg-success";
            case CANCELLED -> "bg-danger";
        };
    }
}
