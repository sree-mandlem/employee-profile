package com.company.auth.dto;

import com.company.auth.model.Role;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;

    private Long userId;
    private Long employeeId;
    private String username;

    private Set<Role> roles;
}
