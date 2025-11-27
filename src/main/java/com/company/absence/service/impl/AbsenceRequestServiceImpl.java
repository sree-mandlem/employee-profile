package com.company.absence.service.impl;

import com.company.absence.dto.AbsenceRequestCreateRequest;
import com.company.absence.dto.AbsenceRequestDto;
import com.company.absence.entity.AbsenceRequest;
import com.company.absence.mapper.AbsenceRequestMapper;
import com.company.absence.model.AbsenceStatus;
import com.company.absence.model.AbsenceType;
import com.company.absence.repository.AbsenceRequestRepository;
import com.company.absence.service.AbsenceRequestService;
import com.company.auth.model.AuthenticatedUser;
import com.company.auth.model.Role;
import com.company.auth.security.SecurityUtils;
import com.company.employee.entity.Employee;
import com.company.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.data.domain.Pageable.unpaged;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AbsenceRequestServiceImpl implements AbsenceRequestService {

    private final AbsenceRequestRepository absenceRequestRepository;
    private final EmployeeRepository employeeRepository;
    private final AbsenceRequestMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AbsenceRequestDto> getMyAbsences() {
        var currentUser = SecurityUtils.getCurrentUser();
        log.info("Retrieving all absences of current user: " + currentUser.getUserId());

        ensureCurrentUserHasEmployee(currentUser);

        var page = absenceRequestRepository.findByEmployeeIdOrderByFromDateDesc(currentUser.getEmployeeId(), unpaged());

        return mapper.toDtoList(page.getContent());
    }

    @Override
    public AbsenceRequestDto requestAbsence(AbsenceRequestCreateRequest request) {
        var currentUser = SecurityUtils.getCurrentUser();

        log.info("Creating a new absence, by: " + currentUser.getUserId());

        ensureCurrentUserHasEmployee(currentUser);

        var employee = findEmployeeOrThrow(currentUser.getEmployeeId());

        if (request.getFromDate() == null || request.getToDate() == null) {
            log.error("Validation failed: fromDate must be before or equal to toDate");
            throw new IllegalArgumentException("fromDate and toDate must be provided");
        }
        if (request.getFromDate().isAfter(request.getToDate())) {
            log.error("Validation failed: fromDate must be before or equal to toDate");
            throw new IllegalArgumentException("fromDate must be before or equal to toDate");
        }

        var type = request.getType() != null ? request.getType() : AbsenceType.VACATION;

        // Assign manager at creation so manager can see pending requests
        var manager = employee.getManager();
        if (manager == null) {
            log.error("Employee has no manager assigned for absence approval");
            throw new IllegalStateException("Employee has no manager assigned for absence approval");
        }

        var absence = AbsenceRequest.builder()
                .employee(employee)
                .manager(manager)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .type(type)
                .status(AbsenceStatus.PENDING)
                .build();

        var saved = absenceRequestRepository.save(absence);
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenceRequestDto> getPendingAbsencesForMyTeam() {
        var currentUser = SecurityUtils.getCurrentUser();

        log.info("Retrieving all pending absences for team, by: " + currentUser.getUserId());

        ensureCurrentUserHasEmployee(currentUser);
        ensureCurrentUserIsAManager(currentUser);

        var page = absenceRequestRepository.findByManagerIdAndStatusOrderByFromDateDesc(
                currentUser.getEmployeeId(),
                AbsenceStatus.PENDING,
                unpaged()
        );

        return mapper.toDtoList(page.getContent());
    }

    @Override
    public AbsenceRequestDto approveAbsence(Long requestId) {
        var currentUser = SecurityUtils.getCurrentUser();

        log.info("Approving absence: " + requestId + ", by: " + currentUser.getUserId());

        var absenceRequest = findAbsenceRequestOrThrow(requestId);

        ensureCurrentUserIsAManager(currentUser);
        ensureCurrentUserIsAssignedManager(currentUser, absenceRequest);
        ensureStatusIsPending(absenceRequest);

        absenceRequest.setStatus(AbsenceStatus.APPROVED);
        absenceRequest.setDecisionAt(LocalDateTime.now());

        var saved = absenceRequestRepository.save(absenceRequest);
        log.debug("Successfully approved absence request with id:"  + requestId);
        return mapper.toDto(saved);
    }

    @Override
    public AbsenceRequestDto rejectAbsence(Long requestId) {
        var currentUser = SecurityUtils.getCurrentUser();

        log.info("Approving absence: " + requestId + ", by: " + currentUser.getUserId());

        var absenceRequest = findAbsenceRequestOrThrow(requestId);

        ensureCurrentUserIsAManager(currentUser);
        ensureCurrentUserIsAssignedManager(currentUser, absenceRequest);
        ensureStatusIsPending(absenceRequest);

        absenceRequest.setStatus(AbsenceStatus.REJECTED);
        absenceRequest.setDecisionAt(LocalDateTime.now());

        var saved = absenceRequestRepository.save(absenceRequest);
        log.debug("Successfully rejected absence request with id:"  + requestId);
        return mapper.toDto(saved);
    }

    @Override
    public AbsenceRequestDto cancelAbsence(Long requestId) {

        var currentUser = SecurityUtils.getCurrentUser();

        log.info("Cancelling absence: " + requestId + ", by: " + currentUser.getUserId());

        ensureCurrentUserHasEmployee(currentUser);

        var absenceRequest = findAbsenceRequestOrThrow(requestId);

        ensureCurrentUserIsRequestAuthor(currentUser, absenceRequest);

        absenceRequest.setStatus(AbsenceStatus.CANCELLED);
        absenceRequest.setDecisionAt(LocalDateTime.now());

        var saved = absenceRequestRepository.save(absenceRequest);
        log.debug("Successfully cancelled absence request with id:"  + requestId);
        return mapper.toDto(saved);
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("Employee not found: " + employeeId);
                    return new EntityNotFoundException("Employee not found: " + employeeId);
                });
    }

    private AbsenceRequest findAbsenceRequestOrThrow(Long absenceRequestId) {
        return absenceRequestRepository.findById(absenceRequestId)
                .orElseThrow(() -> {
                    log.error("AbsenceRequest not found: " + absenceRequestId);
                    return new EntityNotFoundException("AbsenceRequest not found: " + absenceRequestId);
                });
    }

    private void ensureCurrentUserIsRequestAuthor(AuthenticatedUser user, AbsenceRequest request) {
        var employee = request.getEmployee();
        if (employee == null || employee.getId() == null) {
            log.error("Absence request has no associated employee");
            throw new IllegalStateException("Absence request has no associated employee");
        }

        if (!employee.getId().equals(user.getEmployeeId())) {
            log.error("Only the original requester can cancel the absence");
            throw new AccessDeniedException("Only the original requester can cancel the absence");
        }
    }


    private void ensureCurrentUserIsAManager(AuthenticatedUser currentUser) {
        if (!currentUser.getRoles().contains(Role.MANAGER)) {
            log.error("Only managers can approve absences");
            throw new AccessDeniedException("Only managers can approve absences");
        }
    }

    private void ensureCurrentUserHasEmployee(AuthenticatedUser currentUser) {
        if (currentUser.getEmployeeId() == null) {
            log.error("Authenticated user has no employeeId");
            throw new IllegalStateException("Authenticated user has no employeeId");
        }
    }

    private void ensureCurrentUserIsAssignedManager(AuthenticatedUser currentUser, AbsenceRequest absenceRequest) {
        var manager = absenceRequest.getManager();
        if (manager == null) {
            log.error("Employee has no manager assigned for absence approval");
            throw new IllegalStateException("Employee has no manager assigned for absence approval");
        }

        var managerUserAccount = manager.getUserAccount();
        if (managerUserAccount == null || !currentUser.getUserId().equals(managerUserAccount.getId())) {
            log.error("Only assigned manager can approve absence request");
            throw new AccessDeniedException("Only assigned manager can approve absence request");
        }
    }

    private void ensureStatusIsPending(AbsenceRequest absenceRequest) {
        if (absenceRequest.getStatus() != AbsenceStatus.PENDING) {
            log.error("Only PENDING absence requests can be updated");
            throw new IllegalStateException("Only PENDING absence requests can be updated");
        }
    }
}
