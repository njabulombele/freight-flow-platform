package com.freightflow.prediction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * HTTP client for the Python prediction service. Uses WebClient (non-blocking) and
 * gracefully degrades when the prediction service is unavailable, so the backend
 * keeps working without AI.
 */
@Component
public class PredictionClient {

    private static final Logger log = LoggerFactory.getLogger(PredictionClient.class);

    private final WebClient webClient;
    private final String baseUrl;

    public PredictionClient(@Value("${app.prediction.service-url:http://localhost:8000}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public Mono<PredictionResponse> predict(PredictionRequest request) {
        return webClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PredictionResponse.class)
                .onErrorResume(ex -> {
                    log.warn("Prediction service unavailable: {}", ex.getMessage());
                    return Mono.empty();
                });
    }

    public boolean isAvailable() {
        try {
            webClient.get().uri("/health").retrieve().bodyToMono(String.class)
                    .block(java.time.Duration.ofSeconds(2));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
