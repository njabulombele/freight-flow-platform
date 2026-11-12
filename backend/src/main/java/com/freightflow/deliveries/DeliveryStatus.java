package com.freightflow.deliveries;

/**
 * Delivery lifecycle. Allowed transitions are enforced in the service layer.
 */
public enum DeliveryStatus {
    PENDING,        // created, not yet assigned
    ASSIGNED,        // driver + vehicle assigned
    IN_TRANSIT,      // driver has started the route
    DELIVERED,       // successful delivery with proof
    FAILED,          // delivery could not be completed
    CANCELLED;       // cancelled before/at dispatch

    /**
     * Returns true if the transition from this status to the target is allowed.
     */
    public boolean canTransitionTo(DeliveryStatus target) {
        return switch (this) {
            case PENDING -> target == ASSIGNED || target == CANCELLED;
            case ASSIGNED -> target == IN_TRANSIT || target == CANCELLED || target == FAILED;
            case IN_TRANSIT -> target == DELIVERED || target == FAILED || target == ASSIGNED;
            case DELIVERED, FAILED, CANCELLED -> false;
        };
    }
}
