package com.company.employee.service.impl;

import com.company.auth.model.AuthenticatedUser;
import com.company.auth.security.SecurityUtils;
import com.company.employee.dto.EmployeeProfilePrivateDto;
import com.company.employee.dto.EmployeeProfilePublicDto;
import com.company.employee.dto.EmployeeProfileUpdateRequest;
import com.company.employee.entity.Employee;
import com.company.employee.entity.EmployeeProfile;
import com.company.employee.mapper.EmployeeProfileMapper;
import com.company.employee.repository.EmployeeProfileRepository;
import com.company.employee.repository.EmployeeRepository;
import com.company.employee.service.EmployeeProfileService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EmployeeProfileServiceImpl implements EmployeeProfileService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeProfileRepository employeeProfileRepository;
    private final EmployeeProfileMapper employeeProfileMapper;

    @Override
    @Transactional(readOnly = true)
    public EmployeeProfilePrivateDto getMyProfile() {
        var user = SecurityUtils.getCurrentUser();

        log.info("Retrieving user profile, by" + user.getUserId());

        var employeeId = user.getEmployeeId();
        if (employeeId == null) {
            log.error("Authenticated user has no employeeId assigned");
            throw new IllegalStateException("Authenticated user has no employeeId assigned");
        }
        var profile = findProfileByEmployeeIdOrThrow(employeeId);
        return employeeProfileMapper.toPrivateDto(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeProfilePrivateDto getPrivateProfile(Long employeeId) {
        var user = SecurityUtils.getCurrentUser();

        log.info("Retrieving private profile of employee:" + employeeId + ", by: " + user.getUserId());

        var targetEmployee = findEmployeeOrThrow(employeeId);

        if (!canViewPrivateProfile(user, targetEmployee)) {
            log.error("Not allowed to view private profile of this employee");
            throw new AccessDeniedException("Not allowed to view private profile of this employee");
        }

        var profile = findProfileByEmployeeIdOrThrow(employeeId);
        return employeeProfileMapper.toPrivateDto(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeProfilePublicDto getPublicProfile(Long employeeId) {
        var user = SecurityUtils.getCurrentUser();

        log.info("Retrieving public profile of employee:" + employeeId + ", by: " + user.getUserId());

        findEmployeeOrThrow(employeeId);

        var profile = findProfileByEmployeeIdOrThrow(employeeId);
        return employeeProfileMapper.toPublicDto(profile);
    }

    @Override
    public List<EmployeeProfilePublicDto> getAllPublicProfiles() {
        var user = SecurityUtils.getCurrentUser();

        log.info("Retrieving all employees public profiles, by: " + user.getUserId());

        var allProfiles = employeeProfileRepository.findAll();
        return employeeProfileMapper.toPublicDtoList(allProfiles);
    }

    @Override
    public EmployeeProfilePrivateDto updateProfile(Long employeeId, EmployeeProfileUpdateRequest request) {
        var user = SecurityUtils.getCurrentUser();

        log.info("Attempting to update employee profile:" + employeeId + ", by: " + user.getUserId());

        var employee = findEmployeeOrThrow(employeeId);

        if (!canEditProfile(user, employee)) {
            log.error("Not allowed to edit this profile");
            throw new AccessDeniedException("Not allowed to edit this profile");
        }

        var profile = findProfileByEmployeeIdOrThrow(employeeId);

        applyUpdates(profile, request);

        var saved = employeeProfileRepository.save(profile);
        return employeeProfileMapper.toPrivateDto(saved);
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("Employee not found: " + employeeId);
                    return new EntityNotFoundException("Employee not found: " + employeeId);
                });
    }

    private EmployeeProfile findProfileByEmployeeIdOrThrow(Long employeeId) {
        return employeeProfileRepository.findByEmployeeId(employeeId)
                .orElseThrow(() -> {
                    log.error("Profile not found for employee: " + employeeId);
                    return new EntityNotFoundException("Profile not found for employee: " + employeeId);
                });
    }

    private boolean canViewPrivateProfile(AuthenticatedUser user, Employee targetEmployee) {
        var currentEmpId = user.getEmployeeId();
        if (currentEmpId != null && currentEmpId.equals(targetEmployee.getId())) {
            log.debug("Current user can view their own private profile");
            return true;
        }

        var manager = targetEmployee.getManager();
        return manager != null && manager.getId().equals(currentEmpId);
    }

    private boolean canEditProfile(AuthenticatedUser user, Employee targetEmployee) {
        return canViewPrivateProfile(user, targetEmployee);
    }

    private void applyUpdates(EmployeeProfile profile, EmployeeProfileUpdateRequest request) {
        log.debug("Applying updates on profile");
        // Non-sensitive
        profile.setJobTitle(request.getJobTitle());
        profile.setDepartment(request.getDepartment());
        profile.setSkills(request.getSkills());
        profile.setBio(request.getBio());
        profile.setAvatarUrl(request.getAvatarUrl());

        // Sensitive
        profile.setSalary(request.getSalary());
        profile.setPerformanceNotes(request.getPerformanceNotes());
        profile.setHomeAddress(request.getHomeAddress());
        profile.setPersonalPhone(request.getPersonalPhone());
    }
}
