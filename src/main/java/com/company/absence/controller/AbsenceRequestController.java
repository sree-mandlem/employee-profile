package com.company.absence.controller;

import com.company.absence.api.AbsenceRequestApi;
import com.company.absence.dto.AbsenceRequestCreateRequest;
import com.company.absence.dto.AbsenceRequestDto;
import com.company.absence.service.AbsenceRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AbsenceRequestController implements AbsenceRequestApi {

    private final AbsenceRequestService absenceRequestService;

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<List<AbsenceRequestDto>> getMyAbsences() {
        return ResponseEntity.ok(absenceRequestService.getMyAbsences());
    }

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<AbsenceRequestDto> requestAbsence(AbsenceRequestCreateRequest request) {
        return ResponseEntity.ok(absenceRequestService.requestAbsence(request));
    }

    @Override
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<List<AbsenceRequestDto>> getPendingAbsencesForMyTeam() {
        return ResponseEntity.ok(absenceRequestService.getPendingAbsencesForMyTeam());
    }

    @Override
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<AbsenceRequestDto> approveAbsence(Long id) {
        return ResponseEntity.ok(absenceRequestService.approveAbsence(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('MANAGER')")
    public ResponseEntity<AbsenceRequestDto> rejectAbsence(Long id) {
        return ResponseEntity.ok(absenceRequestService.rejectAbsence(id));
    }

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE')")
    public ResponseEntity<AbsenceRequestDto> cancelAbsence(Long id) {
        return ResponseEntity.ok(absenceRequestService.cancelAbsence(id));
    }
}
