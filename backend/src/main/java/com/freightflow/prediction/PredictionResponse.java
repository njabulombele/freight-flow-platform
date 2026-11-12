package com.freightflow.prediction;

import java.util.List;
import java.util.Map;

/**
 * Prediction returned by the Python service.
 */
public record PredictionResponse(
        double lateProbability,
        int estimatedDelayMinutes,
        String riskCategory,
        Map<String, Double> contributingFactors
) {
}
