package com.company.employee.controller;

import com.company.employee.api.EmployeeProfileApi;
import com.company.employee.dto.EmployeeProfilePrivateDto;
import com.company.employee.dto.EmployeeProfilePublicDto;
import com.company.employee.dto.EmployeeProfileUpdateRequest;
import com.company.employee.service.EmployeeProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EmployeeProfileController implements EmployeeProfileApi {

    private final EmployeeProfileService employeeProfileService;

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<EmployeeProfilePrivateDto> getMyProfile() {
        return ResponseEntity.ok(employeeProfileService.getMyProfile());
    }

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<EmployeeProfilePrivateDto> getPrivateProfile(Long employeeId) {
        return ResponseEntity.ok(employeeProfileService.getPrivateProfile(employeeId));
    }

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<EmployeeProfilePublicDto> getPublicProfile(Long employeeId) {
        return ResponseEntity.ok(employeeProfileService.getPublicProfile(employeeId));
    }

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<List<EmployeeProfilePublicDto>> getAllPublicProfiles() {
        return ResponseEntity.ok(employeeProfileService.getAllPublicProfiles());
    }

    @Override
    @PreAuthorize("hasAnyRole('xEMPLOYEE','MANAGER')")
    public ResponseEntity<EmployeeProfilePrivateDto> updateProfile(Long employeeId, EmployeeProfileUpdateRequest request) {
        return ResponseEntity.ok(employeeProfileService.updateProfile(employeeId, request));
    }
}
