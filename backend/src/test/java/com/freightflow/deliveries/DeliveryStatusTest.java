package com.freightflow.deliveries;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the DeliveryStatus state machine.
 * No Spring context — just pure domain logic.
 */
class DeliveryStatusTest {

    @Test
    void pendingCanTransitionToAssigned() {
        assertThat(DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.ASSIGNED)).isTrue();
    }

    @Test
    void pendingCanTransitionToCancelled() {
        assertThat(DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.CANCELLED)).isTrue();
    }

    @Test
    void pendingCannotTransitionToDelivered() {
        assertThat(DeliveryStatus.PENDING.canTransitionTo(DeliveryStatus.DELIVERED)).isFalse();
    }

    @Test
    void inTransitCanTransitionToDelivered() {
        assertThat(DeliveryStatus.IN_TRANSIT.canTransitionTo(DeliveryStatus.DELIVERED)).isTrue();
    }

    @Test
    void inTransitCanTransitionToFailed() {
        assertThat(DeliveryStatus.IN_TRANSIT.canTransitionTo(DeliveryStatus.FAILED)).isTrue();
    }

    @Test
    void deliveredIsTerminal() {
        for (DeliveryStatus target : DeliveryStatus.values()) {
            assertThat(DeliveryStatus.DELIVERED.canTransitionTo(target))
                    .as("DELIVERED -> %s should be false", target)
                    .isFalse();
        }
    }

    @Test
    void failedIsTerminal() {
        for (DeliveryStatus target : DeliveryStatus.values()) {
            assertThat(DeliveryStatus.FAILED.canTransitionTo(target))
                    .as("FAILED -> %s should be false", target)
                    .isFalse();
        }
    }
}
