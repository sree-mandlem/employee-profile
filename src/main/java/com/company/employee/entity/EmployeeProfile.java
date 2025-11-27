package com.company.employee.entity;

import com.company.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "employee_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class EmployeeProfile extends BaseEntity {

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false, unique = true)
    private Employee employee;

    // ---------- Non-sensitive fields ----------

    @Column(length = 100)
    private String jobTitle;

    @Column(length = 100)
    private String department;

    @Column(length = 500)
    private String skills;

    @Column(length = 1000)
    private String bio;

    @Column(length = 500)
    private String avatarUrl;

    // ---------- Sensitive fields ----------

    @Column(precision = 15, scale = 2)
    private BigDecimal salary;

    @Column(length = 2000)
    private String performanceNotes;

    @Column(length = 500)
    private String homeAddress;

    @Column(length = 50)
    private String personalPhone;
}