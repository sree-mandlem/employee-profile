package com.company.employee.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfilePublicDto {

    // Employee identity (safe to expose inside company)
    private Long employeeId;
    private String firstName;
    private String lastName;

    // Non-sensitive profile fields
    private String jobTitle;
    private String department;
    private String skills;
    private String bio;
    private String avatarUrl;
}
