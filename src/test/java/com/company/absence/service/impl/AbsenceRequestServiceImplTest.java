package com.company.absence.service.impl;

import com.company.absence.dto.AbsenceRequestCreateRequest;
import com.company.absence.dto.AbsenceRequestDto;
import com.company.absence.entity.AbsenceRequest;
import com.company.absence.mapper.AbsenceRequestMapper;
import com.company.absence.model.AbsenceStatus;
import com.company.absence.model.AbsenceType;
import com.company.absence.repository.AbsenceRequestRepository;
import com.company.auth.entity.UserAccount;
import com.company.auth.model.AuthenticatedUser;
import com.company.auth.model.Role;
import com.company.auth.security.SecurityUtils;
import com.company.employee.entity.Employee;
import com.company.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbsenceRequestServiceImplTest {

    @Mock
    private AbsenceRequestRepository absenceRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AbsenceRequestMapper mapper;

    @InjectMocks
    private AbsenceRequestServiceImpl service;

    @Test
    void getMyAbsences_shouldReturnMappedDtos() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var req1 = AbsenceRequest.builder().id(100L).build();
        var req2 = AbsenceRequest.builder().id(101L).build();
        var dto1 = AbsenceRequestDto.builder().id(100L).build();
        var dto2 = AbsenceRequestDto.builder().id(101L).build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(absenceRequestRepository.findByEmployeeIdOrderByFromDateDesc(eq(1L), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(req1, req2)));
            when(mapper.toDtoList(List.of(req1, req2))).thenReturn(List.of(dto1, dto2));

            var result = service.getMyAbsences();

            assertThat(result).hasSize(2);
            verify(absenceRequestRepository).findByEmployeeIdOrderByFromDateDesc(eq(1L), any(Pageable.class));
            verify(mapper).toDtoList(List.of(req1, req2));
        }
    }

    @Test
    void requestAbsence_shouldCreatePendingRequestWithManagerAssigned() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var manager = Employee.builder().id(2L).firstName("Manager").build();
        var alice = Employee.builder().id(1L).firstName("Alice").manager(manager).build();
        var createRequest = AbsenceRequestCreateRequest.builder()
                .fromDate(LocalDate.of(2025, 1, 1))
                .toDate(LocalDate.of(2025, 1, 10))
                .type(AbsenceType.VACATION)
                .build();
        var saved = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager)
                .fromDate(createRequest.getFromDate())
                .toDate(createRequest.getToDate())
                .type(AbsenceType.VACATION)
                .status(AbsenceStatus.PENDING)
                .build();
        var dto = AbsenceRequestDto.builder()
                .id(100L)
                .employeeId(1L)
                .status(AbsenceStatus.PENDING)
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
            when(absenceRequestRepository.save(any(AbsenceRequest.class))).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            AbsenceRequestDto result = service.requestAbsence(createRequest);

            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getStatus()).isEqualTo(AbsenceStatus.PENDING);
            verify(absenceRequestRepository).save(argThat(req -> {
                assertThat(req.getEmployee().getId()).isEqualTo(1L);
                assertThat(req.getManager().getId()).isEqualTo(2L);
                assertThat(req.getStatus()).isEqualTo(AbsenceStatus.PENDING);
                return true;
            }));
        }
    }

    @Test
    void getPendingAbsencesForMyTeam_shouldRequireManagerOrAdmin() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE)) // not manager
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);

            assertThatThrownBy(() -> service.getPendingAbsencesForMyTeam())
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only managers");
        }
    }

    @Test
    void getPendingAbsencesForMyTeam_shouldReturnDtosForManager() {
        var managerUser = AuthenticatedUser.builder()
                .userId(20L)
                .employeeId(2L)
                .username("manager")
                .roles(Set.of(Role.MANAGER))
                .build();
        var req1 = AbsenceRequest.builder().id(100L).build();
        var dto1 = AbsenceRequestDto.builder().id(100L).build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(managerUser);
            when(absenceRequestRepository.findByManagerIdAndStatusOrderByFromDateDesc(
                    eq(2L),
                    eq(AbsenceStatus.PENDING),
                    any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(req1)));
            when(mapper.toDtoList(List.of(req1))).thenReturn(List.of(dto1));

            List<AbsenceRequestDto> result = service.getPendingAbsencesForMyTeam();

            assertThat(result).hasSize(1);
        }
    }

    @Test
    void approveAbsence_shouldChangeStatusIfPendingAndManagerMatches() {
        var managerUser = AuthenticatedUser.builder().userId(20L).roles(Set.of(Role.MANAGER)).build();
        var managerAccount = UserAccount.builder().id(20L).build();
        var manager = Employee.builder().userAccount(managerAccount).build();
        var alice   = Employee.builder().build();
        var request = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager)
                .status(AbsenceStatus.PENDING)
                .build();
        var saved = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager)
                .status(AbsenceStatus.APPROVED)
                .decisionAt(LocalDateTime.now())
                .build();
        var dto = AbsenceRequestDto.builder()
                .id(100L)
                .status(AbsenceStatus.APPROVED)
                .build();

        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(managerUser);
            when(absenceRequestRepository.findById(100L)).thenReturn(Optional.of(request));
            when(absenceRequestRepository.save(request)).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            AbsenceRequestDto result = service.approveAbsence(100L);

            assertThat(result.getStatus()).isEqualTo(AbsenceStatus.APPROVED);
            assertThat(request.getStatus()).isEqualTo(AbsenceStatus.APPROVED);
            assertThat(request.getDecisionAt()).isNotNull();
        }
    }

    @Test
    void approveAbsence_shouldDenyIfManagerDoesNotMatchAndNotAdmin() {
        var otherManager = AuthenticatedUser.builder()
                .userId(30L)
                .employeeId(3L)
                .username("otherManager")
                .roles(Set.of(Role.MANAGER))
                .build();
        var manager2 = Employee.builder().id(2L).firstName("Manager2").build();
        var alice    = Employee.builder().id(1L).firstName("Alice").build();
        var request = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager2)
                .status(AbsenceStatus.PENDING)
                .build();

        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(otherManager);
            when(absenceRequestRepository.findById(100L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> service.approveAbsence(100L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only assigned manager can approve absence request");
        }
    }

    @Test
    void approveAbsence_shouldThrowIfNotPending() {
        var managerUser = AuthenticatedUser.builder()
                .userId(20L)
                .roles(Set.of(Role.MANAGER))
                .build();

        var managerAccount = UserAccount.builder().id(20L).build();
        var manager = Employee.builder().userAccount(managerAccount).build();

        var alice   = Employee.builder().build();
        var request = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager)
                .status(AbsenceStatus.APPROVED)
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(managerUser);
            when(absenceRequestRepository.findById(100L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> service.approveAbsence(100L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only PENDING absence requests");
        }
    }

    @Test
    void requestAbsence_shouldValidateDates() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var invalid = AbsenceRequestCreateRequest.builder()
                .fromDate(LocalDate.of(2025, 1, 10))
                .toDate(LocalDate.of(2025, 1, 1))
                .build();
        var manager = Employee.builder().id(2L).build();
        var alice   = Employee.builder().id(1L).manager(manager).build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));

            assertThatThrownBy(() -> service.requestAbsence(invalid))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void getMyAbsences_shouldThrowIfEmployeeNotSet() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(null)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);

            assertThatThrownBy(() -> service.getMyAbsences())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no employeeId");
        }
    }

    @Test
    void approveAbsence_shouldThrowIfRequestNotFound() {
        var managerUser = AuthenticatedUser.builder()
                .userId(20L)
                .employeeId(2L)
                .username("manager")
                .roles(Set.of(Role.MANAGER))
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(managerUser);
            when(absenceRequestRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.approveAbsence(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("AbsenceRequest not found");
        }
    }

    @Test
    void rejectAbsence_shouldRejectWhenCurrentUserIsAssignedManagerAndPending() {
        var managerAccount = UserAccount.builder().id(20L).build();
        var manager = Employee.builder().id(2L).userAccount(managerAccount).build();
        var alice = Employee.builder().id(1L).build();
        var currentUser = AuthenticatedUser.builder()
                .userId(20L)
                .employeeId(2L)
                .roles(Set.of(Role.MANAGER))
                .build();
        var request = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager)
                .status(AbsenceStatus.PENDING)
                .build();
        var saved = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager)
                .status(AbsenceStatus.REJECTED)
                .decisionAt(LocalDateTime.now())
                .build();
        var dto = AbsenceRequestDto.builder()
                .id(100L)
                .status(AbsenceStatus.REJECTED)
                .build();

        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);
            when(absenceRequestRepository.findById(100L)).thenReturn(Optional.of(request));
            when(absenceRequestRepository.save(any(AbsenceRequest.class))).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            var result = service.rejectAbsence(100L);

            assertThat(result.getId()).isEqualTo(100L);
            assertThat(result.getStatus()).isEqualTo(AbsenceStatus.REJECTED);
            var captor = ArgumentCaptor.forClass(AbsenceRequest.class);
            verify(absenceRequestRepository).save(captor.capture());
            var savedArgument = captor.getValue();
            assertThat(savedArgument.getStatus()).isEqualTo(AbsenceStatus.REJECTED);
            assertThat(savedArgument.getDecisionAt()).isNotNull();
        }
    }

    @Test
    void rejectAbsence_shouldThrowWhenCurrentUserIsNotAssignedManager() {
        var managerAccount = UserAccount.builder().id(20L).build();
        var manager = Employee.builder().id(2L).userAccount(managerAccount).build();
        var alice = Employee.builder().id(1L).build();

        var otherUser = AuthenticatedUser.builder()
                .userId(99L) // does NOT match managerAccount.id
                .employeeId(3L)
                .roles(Set.of(Role.MANAGER))
                .build();

        var request = AbsenceRequest.builder()
                .id(100L)
                .employee(alice)
                .manager(manager)
                .status(AbsenceStatus.PENDING)
                .build();

        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(otherUser);
            when(absenceRequestRepository.findById(100L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> service.rejectAbsence(100L))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }

    @Test
    void cancelAbsence_shouldCancelWhenCurrentUserIsAuthor() {
        var currentUser = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)     // logged-in employee
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var employee = Employee.builder()
                .id(1L)              // request author = logged-in user
                .build();
        var request = AbsenceRequest.builder()
                .id(200L)
                .employee(employee)
                .status(AbsenceStatus.PENDING)
                .build();
        var saved = AbsenceRequest.builder()
                .status(AbsenceStatus.CANCELLED)
                .decisionAt(LocalDateTime.now())
                .build();

        var dto = AbsenceRequestDto.builder()
                .id(200L)
                .status(AbsenceStatus.CANCELLED)
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);
            when(absenceRequestRepository.findById(200L)).thenReturn(Optional.of(request));
            when(absenceRequestRepository.save(any(AbsenceRequest.class))).thenReturn(saved);
            when(mapper.toDto(saved)).thenReturn(dto);

            var result = service.cancelAbsence(200L);

            assertThat(result.getId()).isEqualTo(200L);
            assertThat(result.getStatus()).isEqualTo(AbsenceStatus.CANCELLED);
            var captor = ArgumentCaptor.forClass(AbsenceRequest.class);
            verify(absenceRequestRepository).save(captor.capture());
            var savedArgument = captor.getValue();
            assertThat(savedArgument.getStatus()).isEqualTo(AbsenceStatus.CANCELLED);
            assertThat(savedArgument.getDecisionAt()).isNotNull();
        }
    }

    @Test
    void cancelAbsence_shouldThrowAccessDenied_whenCurrentUserIsNotAuthor() {
        var currentUser = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(99L)     // NOT the employee in the request
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var employee = Employee.builder()
                .id(1L)              // request author
                .build();
        var request = AbsenceRequest.builder()
                .id(200L)
                .employee(employee)
                .status(AbsenceStatus.PENDING)
                .build();

        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);
            when(absenceRequestRepository.findById(200L)).thenReturn(Optional.of(request));

            assertThatThrownBy(() -> service.cancelAbsence(200L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Only the original requester can cancel");
        }
    }

    @Test
    void cancelAbsence_shouldThrowWhenCurrentUserHasNoEmployeeId() {
        var currentUser = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(null)
                .roles(Set.of(Role.EMPLOYEE))
                .build();

        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(currentUser);

            assertThatThrownBy(() -> service.cancelAbsence(200L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

}
