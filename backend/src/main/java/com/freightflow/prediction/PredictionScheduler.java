package com.freightflow.prediction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightflow.deliveries.Delivery;
import com.freightflow.deliveries.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Periodically calls the Python prediction service for all active deliveries
 * and writes results back to the delivery record. If the prediction service is
 * down the scheduler skips quietly — the existing predictions stay in place.
 */
@Component
public class PredictionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PredictionScheduler.class);

    private final PredictionClient client;
    private final DeliveryService deliveryService;
    private final ObjectMapper objectMapper;

    public PredictionScheduler(PredictionClient client, DeliveryService deliveryService, ObjectMapper objectMapper) {
        this.client = client;
        this.deliveryService = deliveryService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 60_000)   // every 60 seconds
    public void refreshPredictions() {
        List<Delivery> active;
        try {
            active = deliveryService.activeDeliveries();
        } catch (Exception e) {
            log.warn("Could not load active deliveries for prediction refresh: {}", e.getMessage());
            return;
        }

        if (active.isEmpty()) {
            return;
        }

        log.debug("Refreshing predictions for {} active deliveries", active.size());

        for (Delivery delivery : active) {
            try {
                ZonedDateTime scheduled = delivery.getScheduledAt().atZone(ZoneOffset.UTC);
                PredictionRequest request = new PredictionRequest(
                        delivery.getId(),
                        delivery.getPlannedDistanceKm() != null ? delivery.getPlannedDistanceKm() : 50.0,
                        delivery.getPlannedDurationMin() != null ? delivery.getPlannedDurationMin() : 90,
                        scheduled.getHour(),
                        scheduled.getDayOfWeek().getValue(),  // 1=Mon … 7=Sun
                        delivery.getCustomer().getCity(),
                        0.0,   // historical rate — prediction service will substitute model prior
                        delivery.getDelayMinutes()
                );

                client.predict(request).subscribe(response -> {
                    if (response != null) {
                        try {
                            Delivery.RiskCategory risk = Delivery.RiskCategory.valueOf(
                                    response.riskCategory().toUpperCase());
                            String factorsJson = objectMapper.writeValueAsString(response.contributingFactors());
                            deliveryService.applyPrediction(delivery.getId(),
                                    response.lateProbability(),
                                    response.estimatedDelayMinutes(),
                                    risk,
                                    factorsJson);
                        } catch (Exception e) {
                            log.warn("Failed to apply prediction for delivery {}: {}", delivery.getId(), e.getMessage());
                        }
                    }
                }, error -> log.warn("Prediction error for {}: {}", delivery.getId(), error.getMessage()));

            } catch (Exception e) {
                log.warn("Could not build prediction request for delivery {}: {}", delivery.getId(), e.getMessage());
            }
        }
    }
}
