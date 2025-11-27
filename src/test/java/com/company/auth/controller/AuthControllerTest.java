package com.company.auth.controller;

import com.company.auth.dto.LoginRequest;
import com.company.auth.dto.LoginResponse;
import com.company.auth.entity.UserAccount;
import com.company.auth.jwt.JwtTokenProvider;
import com.company.auth.model.AuthenticatedUser;
import com.company.auth.model.Role;
import com.company.auth.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_shouldReturnTokenAndUserInfo_onValidCredentials() {
        var request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("password");
        var account = UserAccount.builder()
                .id(1L)
                .username("alice")
                .passwordHash("hashed")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtTokenProvider.createToken(any(AuthenticatedUser.class))).thenReturn("jwt-token");

        ResponseEntity<LoginResponse> response = authController.login(request);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getToken()).isEqualTo("jwt-token");
        assertThat(body.getUserId()).isEqualTo(1L);
        assertThat(body.getUsername()).isEqualTo("alice");
        assertThat(body.getRoles()).containsExactly(Role.EMPLOYEE);
        verify(userAccountRepository).findByUsername("alice");
        verify(passwordEncoder).matches("password", "hashed");
        verify(jwtTokenProvider).createToken(any(AuthenticatedUser.class));
    }

    @Test
    void login_shouldThrowBadCredentials_onUnknownUser() {
        var request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword("password");
        when(userAccountRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtTokenProvider, never()).createToken(any());
    }

    @Test
    void login_shouldThrowBadCredentials_onWrongPassword() {
        var request = new LoginRequest();
        request.setUsername("alice");
        request.setPassword("wrong");
        var account = UserAccount.builder()
                .id(1L)
                .username("alice")
                .passwordHash("hashed")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authController.login(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid username or password");
        verify(jwtTokenProvider, never()).createToken(any());
    }
}
