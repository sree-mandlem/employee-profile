package com.company.auth.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.company.auth.model.AuthenticatedUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
class SecurityUtilsTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_returnsAuthenticatedUser() {
        var user = mock(AuthenticatedUser.class);
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        var result = SecurityUtils.getCurrentUser();

        assertEquals(user, result);
    }

    @Test
    void getCurrentUser_throwsWhenAuthenticationIsNull() {
        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        assertThrows(AccessDeniedException.class, SecurityUtils::getCurrentUser);
    }

    @Test
    void getCurrentUser_throwsWhenPrincipalIsNotAuthenticatedUser() {
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn("not-a-user");
        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(context);

        assertThrows(AccessDeniedException.class, SecurityUtils::getCurrentUser);
    }
}