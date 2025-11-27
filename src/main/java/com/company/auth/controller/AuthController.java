package com.company.auth.controller;

import com.company.auth.dto.LoginRequest;
import com.company.auth.dto.LoginResponse;
import com.company.auth.jwt.JwtTokenProvider;
import com.company.auth.model.AuthenticatedUser;
import com.company.auth.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        log.info("Attempt to login by: " + request.getUsername());

        var account = userAccountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.error("Login failed: Invalid username or password");
                    return new BadCredentialsException("Invalid username or password");
                });

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            log.error("Login failed: Invalid username or password");
            throw new BadCredentialsException("Invalid username or password");
        }

        var employeeId = account.getEmployee() != null ? account.getEmployee().getId() : null;

        var principal = AuthenticatedUser.builder()
                .userId(account.getId())
                .employeeId(employeeId)
                .username(account.getUsername())
                .password(account.getPasswordHash())
                .roles(account.getRoles())
                .build();

        var token = tokenProvider.createToken(principal);

        var response = LoginResponse.builder()
                .token(token)
                .userId(account.getId())
                .employeeId(employeeId)
                .username(account.getUsername())
                .roles(account.getRoles())
                .build();

        log.debug("Login attempt successful");
        return ResponseEntity.ok(response);
    }
}
