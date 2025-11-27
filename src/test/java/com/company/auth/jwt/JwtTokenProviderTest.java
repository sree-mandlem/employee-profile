package com.company.auth.jwt;

import com.company.auth.model.AuthenticatedUser;
import com.company.auth.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();

        // Build a 256-bit key and Base64-encode it
        String rawKey = "this-is-a-very-long-secret-key-for-tests-123456";
        String base64Key = Base64.getEncoder()
                .encodeToString(rawKey.getBytes(StandardCharsets.UTF_8));

        props.setSecret(base64Key);
        props.setValidityInMs(3600_000L); // 1 hour

        jwtTokenProvider = new JwtTokenProvider(props);
    }

    @Test
    void createToken_and_validateToken_and_getUserFromToken_shouldRoundTrip() {
        AuthenticatedUser user = AuthenticatedUser.builder()
                .userId(42L)
                .employeeId(7L)
                .username("alice")
                .password("ignored")
                .roles(Set.of(Role.EMPLOYEE, Role.MANAGER))
                .build();

        String token = jwtTokenProvider.createToken(user);

        assertThat(token).isNotBlank();

        boolean valid = jwtTokenProvider.validateToken(token);
        assertThat(valid).isTrue();

        AuthenticatedUser parsed = jwtTokenProvider.getUserFromToken(token);

        assertThat(parsed.getUserId()).isEqualTo(42L);
        assertThat(parsed.getEmployeeId()).isEqualTo(7L);
        assertThat(parsed.getUsername()).isEqualTo("alice");
        assertThat(parsed.getRoles()).containsExactlyInAnyOrder(Role.EMPLOYEE, Role.MANAGER);
    }

    @Test
    void validateToken_shouldReturnFalseForGarbageToken() {
        String token = "this-is-not-a-jwt";
        boolean valid = jwtTokenProvider.validateToken(token);
        assertThat(valid).isFalse();
    }
}
