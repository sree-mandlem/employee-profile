package com.company.feedback.service.impl;

import com.company.auth.model.AuthenticatedUser;
import com.company.auth.model.Role;
import com.company.auth.security.SecurityUtils;
import com.company.employee.entity.Employee;
import com.company.employee.repository.EmployeeRepository;
import com.company.feedback.dto.FeedbackCreateRequest;
import com.company.feedback.dto.FeedbackDto;
import com.company.feedback.entity.Feedback;
import com.company.feedback.mapper.FeedbackMapper;
import com.company.feedback.repository.FeedbackRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.company.feedback.model.FeedbackVisibility.EMPLOYEE_AND_MANAGER;
import static com.company.feedback.model.FeedbackVisibility.MANAGER_ONLY;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private FeedbackMapper feedbackMapper;

    @InjectMocks
    private FeedbackServiceImpl service;

    @Test
    void getFeedbackForEmployee_selfShouldSeeAllVisibilities() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var alice = Employee.builder().id(1L).firstName("Alice").build();
        var feedback1 = Feedback.builder().id(100L).employee(alice).build();
        var feedback2 = Feedback.builder().id(101L).employee(alice).build();
        var dto1 = FeedbackDto.builder().id(100L).build();
        var dto2 = FeedbackDto.builder().id(101L).build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
            when(feedbackRepository.findByEmployeeIdAndVisibilityIn(eq(1L), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(feedback1, feedback2)));
            when(feedbackMapper.toDtoList(List.of(feedback1, feedback2)))
                    .thenReturn(List.of(dto1, dto2));

            var result = service.getFeedbackForEmployee(1L);

            assertThat(result).hasSize(2);
            verify(feedbackRepository).findByEmployeeIdAndVisibilityIn(
                    eq(1L),
                    argThat(it -> {
                        var set = new HashSet<>();
                        it.forEach(set::add);
                        return set.containsAll(Set.of(EMPLOYEE_AND_MANAGER, MANAGER_ONLY));
                    }),
                    any(Pageable.class)
            );
        }
    }

    @Test
    void getFeedbackForEmployee_coworkerShouldSeeOnlyEmployeeAndManager() {
        var coworker = AuthenticatedUser.builder()
                .userId(20L)
                .employeeId(2L)
                .username("bob")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var alice = Employee.builder().id(1L).firstName("Alice").build();
        var feedback1 = Feedback.builder().id(100L).employee(alice).build();
        var dto1 = FeedbackDto.builder().id(100L).build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(coworker);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
            when(feedbackRepository.findByEmployeeIdAndVisibilityIn(eq(1L), any(), any(Pageable.class)))
                    .thenReturn(new PageImpl<>(List.of(feedback1)));
            when(feedbackMapper.toDtoList(List.of(feedback1))).thenReturn(List.of(dto1));

            var result = service.getFeedbackForEmployee(1L);

            assertThat(result).hasSize(1);
            verify(feedbackRepository).findByEmployeeIdAndVisibilityIn(
                    eq(1L),
                    argThat(it -> {
                        var set = new HashSet<>();
                        it.forEach(set::add);
                        return set.size() == 1 && set.contains(EMPLOYEE_AND_MANAGER);
                    }),
                    any(Pageable.class)
            );
        }
    }

    @Test
    void createFeedback_shouldPersistFeedbackAndMapToDto() {
        var user = AuthenticatedUser.builder()
                .userId(20L)
                .employeeId(2L)
                .username("bob")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var alice = Employee.builder().id(1L).firstName("Alice").build();
        var bob   = Employee.builder().id(2L).firstName("Bob").build();
        var request = FeedbackCreateRequest.builder()
                .text("Nice work!")
                .visibility(EMPLOYEE_AND_MANAGER)
                .build();
        var saved = Feedback.builder()
                .id(100L)
                .employee(alice)
                .author(bob)
                .text("Nice work!")
                .visibility(EMPLOYEE_AND_MANAGER)
                .build();
        var dto = FeedbackDto.builder()
                .id(100L)
                .text("Nice work!")
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
            when(employeeRepository.findById(2L)).thenReturn(Optional.of(bob));
            when(feedbackRepository.save(any(Feedback.class))).thenReturn(saved);
            when(feedbackMapper.toDto(saved)).thenReturn(dto);

            FeedbackDto result = service.createFeedback(1L, request);

            assertThat(result.getId()).isEqualTo(100L);
            verify(feedbackRepository).save(any(Feedback.class));
        }
    }

    @Test
    void createFeedback_shouldNotAllowSelfFeedback() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var request = FeedbackCreateRequest.builder()
                .text("I am awesome")
                .visibility(EMPLOYEE_AND_MANAGER)
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);

            assertThatThrownBy(() -> service.createFeedback(1L, request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("cannot leave feedback about yourself");
        }
    }

    @Test
    void getFeedbackForEmployee_shouldThrowIfEmployeeNotFound() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        try (var su = mockStatic(SecurityUtils.class)) {
            su.when(SecurityUtils::getCurrentUser).thenReturn(user);

            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getFeedbackForEmployee(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Employee not found");
        }
    }
}
