package com.company.employee.service;

import com.company.employee.dto.EmployeeProfilePrivateDto;
import com.company.employee.dto.EmployeeProfilePublicDto;
import com.company.employee.dto.EmployeeProfileUpdateRequest;

import java.util.List;

public interface EmployeeProfileService {

    /**
     * Profile for the currently authenticated user (always private view).
     */
    EmployeeProfilePrivateDto getMyProfile();

    /**
     * Private profile of any employee (for managers / self).
     */
    EmployeeProfilePrivateDto getPrivateProfile(Long employeeId);

    /**
     * Public profile of an employee (for co-workers). Any authenticated employee can view public profile inside company
     */
    EmployeeProfilePublicDto getPublicProfile(Long employeeId);

    /**
     * Public profile of all employees.
     */
    List<EmployeeProfilePublicDto> getAllPublicProfiles();

    /**
     * Update profile data for the given employee.
     * Restricted to profile owner, their manager.
     */
    EmployeeProfilePrivateDto updateProfile(Long employeeId, EmployeeProfileUpdateRequest request);
}
