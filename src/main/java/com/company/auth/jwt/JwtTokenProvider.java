package com.company.auth.jwt;

import com.company.auth.model.AuthenticatedUser;
import com.company.auth.model.Role;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtProperties properties;

    private SecretKey getSigningKey() {
        var keyBytes = Decoders.BASE64.decode(properties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String createToken(AuthenticatedUser user) {
        log.info("Creating token for user:" + user.getUserId());
        var now = new Date();
        var expiry = new Date(now.getTime() + properties.getValidityInMs());

        var roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        log.debug("Creating jst token");
        var builder = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .setIssuedAt(now)
                .setExpiration(expiry)
                .claim("username", user.getUsername())
                .claim("roles", roleNames);

        if (user.getEmployeeId() != null) {
            builder.claim("employeeId", user.getEmployeeId());
        }

        return builder
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        log.info("Validating token");
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public AuthenticatedUser getUserFromToken(String token) {
        log.debug("Getting user for token");
        var claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        var userId = Long.valueOf(claims.getSubject());
        var username = claims.get("username", String.class);
        var employeeId = claims.get("employeeId", Long.class);

        log.debug("User from token is: " + userId);
        @SuppressWarnings("unchecked")
        var roleNames = (Iterable<String>) claims.get("roles");

        Set<Role> roles = roleNames == null
                ? Set.of()
                : toRoleSet(roleNames);

        return AuthenticatedUser.builder()
                .userId(userId)
                .employeeId(employeeId)
                .username(username)
                .password("") // FIXME: Why this exists and why this is not needed for authenticated principal
                .roles(roles)
                .build();
    }

    private Set<Role> toRoleSet(Iterable<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        if (roleNames != null) {
            roleNames.forEach(r -> roles.add(Role.valueOf(r)));
        }
        return roles;
    }

}
