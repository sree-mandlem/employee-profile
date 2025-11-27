package com.company.employee.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfilePrivateDto {

    // Employee identity
    private Long employeeId;
    private String firstName;
    private String lastName;
    private String email;

    // Non-sensitive profile fields
    private String jobTitle;
    private String department;
    private String skills;
    private String bio;
    private String avatarUrl;

    // Sensitive fields
    private BigDecimal salary;
    private String performanceNotes;
    private String homeAddress;
    private String personalPhone;
}
