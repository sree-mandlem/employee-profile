package com.company.employee.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeProfileUpdateRequest {

    // Non-sensitive fields
    private String jobTitle;
    private String department;
    private String skills;
    private String bio;
    private String avatarUrl;

    // Sensitive fields (you may restrict who can change these)
    private BigDecimal salary;
    private String performanceNotes;
    private String homeAddress;
    private String personalPhone;
}
