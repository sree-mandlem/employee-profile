package com.company.auth.controller;

import com.company.auth.config.SecurityConfig;
import com.company.auth.dto.LoginRequest;
import com.company.auth.entity.UserAccount;
import com.company.auth.jwt.JwtTokenProvider;
import com.company.auth.model.Role;
import com.company.auth.repository.UserAccountRepository;
import com.company.common.api.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class})
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserAccountRepository userAccountRepository;

    @MockitoBean
    PasswordEncoder passwordEncoder;

    @MockitoBean
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void login_validCredentialsShouldReturnToken() throws Exception {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("password");
        var account = UserAccount.builder()
                .id(1L)
                .username("alice")
                .passwordHash("hashed")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        when(userAccountRepository.findByUsername("alice")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("password", "hashed")).thenReturn(true);
        when(jwtTokenProvider.createToken(any())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("alice"))
                .andExpect(jsonPath("$.roles[0]").value("EMPLOYEE"));
    }

    @Test
    void login_invalidCredentialsShouldReturnUnauthorizedWithErrorBody() throws Exception {
        var loginRequest = new LoginRequest();
        loginRequest.setUsername("alice");
        loginRequest.setPassword("wrong");
        when(userAccountRepository.findByUsername("alice"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }
}
