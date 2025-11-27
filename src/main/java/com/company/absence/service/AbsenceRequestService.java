package com.company.absence.service;

import com.company.absence.dto.AbsenceRequestCreateRequest;
import com.company.absence.dto.AbsenceRequestDto;

import java.util.List;

public interface AbsenceRequestService {

    /**
     * Absence requests of the current employee.
     */
    List<AbsenceRequestDto> getMyAbsences();

    /**
     * Create a new absence request for the current employee.
     */
    AbsenceRequestDto requestAbsence(AbsenceRequestCreateRequest request);

    /**
     * Cancel an absence request as a employee that created it.
     */
    AbsenceRequestDto cancelAbsence(Long requestId);

    /**
     * Pending absence requests for the manager's direct reports.
     */
    List<AbsenceRequestDto> getPendingAbsencesForMyTeam();

    /**
     * Approve an absence request as the responsible manager.
     */
    AbsenceRequestDto approveAbsence(Long requestId);

    /**
     * Reject an absence request as the responsible manager.
     */
    AbsenceRequestDto rejectAbsence(Long requestId);
}
