package com.company.auth.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /**
     * Secret key for signing the JWT.
     * Recommended: long random string, Base64-encoded.
     */
    private String secret;

    /**
     * Token validity in milliseconds (for example: 3600000 = 1 hour).
     */
    private long validityInMs = 3600000L;
}
