package com.company.absence.entity;

import com.company.absence.model.AbsenceStatus;
import com.company.absence.model.AbsenceType;
import com.company.common.model.BaseEntity;
import com.company.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "absence_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AbsenceRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Manager who approved / rejected the request.
     * Null while the request is still pending.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private Employee manager;

    @Column(nullable = false)
    private LocalDate fromDate;

    @Column(nullable = false)
    private LocalDate toDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AbsenceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AbsenceStatus status;

    private LocalDateTime decisionAt;
}