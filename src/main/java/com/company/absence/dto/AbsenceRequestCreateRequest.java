package com.company.absence.dto;

import com.company.absence.model.AbsenceType;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceRequestCreateRequest {

    private LocalDate fromDate;
    private LocalDate toDate;
    private AbsenceType type = AbsenceType.VACATION;
}
