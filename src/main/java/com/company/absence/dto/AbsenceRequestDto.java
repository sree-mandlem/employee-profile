package com.company.absence.dto;

import com.company.absence.model.AbsenceStatus;
import com.company.absence.model.AbsenceType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceRequestDto {

    private Long id;

    private Long employeeId;
    private String employeeFirstName;
    private String employeeLastName;

    private Long managerId;

    private LocalDate fromDate;
    private LocalDate toDate;
    private AbsenceType type;
    private AbsenceStatus status;

    private LocalDateTime decisionAt;
}
