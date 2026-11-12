package com.freightflow.common.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider(
            "test-secret-that-is-at-least-32-bytes-long",
            1
    );

    {
        try {
            var init = JwtTokenProvider.class.getDeclaredMethod("init");
            init.setAccessible(true);
            init.invoke(provider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generatesAndParsesToken() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId, "test@example.com", "acme", "DISPATCHER");

        Claims claims = provider.parse(token);
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(userId.toString());
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("DISPATCHER");
        assertThat(claims.get("tenantId", String.class)).isEqualTo("acme");
    }

    @Test
    void returnsNullForInvalidToken() {
        assertThat(provider.parse("not.a.valid.token")).isNull();
    }

    @Test
    void returnsNullForTamperedToken() {
        UUID userId = UUID.randomUUID();
        String token = provider.generateToken(userId, "x@y.com", "tenant", "ADMIN");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThat(provider.parse(tampered)).isNull();
    }
}
