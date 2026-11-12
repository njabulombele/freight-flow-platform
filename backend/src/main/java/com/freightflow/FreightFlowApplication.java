package com.freightflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * FreightFlow backend entry point.
 *
 * A modular monolith: each business module (deliveries, fleet, customers,
 * incidents, reporting, prediction) is organised as a self-contained package
 * sharing common infrastructure for security, audit, outbox and idempotency.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableScheduling
public class FreightFlowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FreightFlowApplication.class, args);
    }
}
