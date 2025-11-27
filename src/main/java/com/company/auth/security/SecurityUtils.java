package com.company.auth.security;

import com.company.auth.model.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static AuthenticatedUser getCurrentUser() {
        log.info("Retrieving principal from security context");

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            log.error("No authenticated user found");
            throw new AccessDeniedException("No authenticated user found");
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
