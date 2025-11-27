package com.company.employee.service.impl;

import com.company.auth.model.AuthenticatedUser;
import com.company.auth.model.Role;
import com.company.auth.security.SecurityUtils;
import com.company.employee.dto.EmployeeProfilePrivateDto;
import com.company.employee.dto.EmployeeProfilePublicDto;
import com.company.employee.dto.EmployeeProfileUpdateRequest;
import com.company.employee.entity.Employee;
import com.company.employee.entity.EmployeeProfile;
import com.company.employee.mapper.EmployeeProfileMapper;
import com.company.employee.repository.EmployeeProfileRepository;
import com.company.employee.repository.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeProfileServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeProfileRepository profileRepository;

    @Mock
    private EmployeeProfileMapper profileMapper;

    @InjectMocks
    private EmployeeProfileServiceImpl service;

    @Test
    void getMyProfile_shouldReturnPrivateProfileForCurrentUser() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var profile = new EmployeeProfile();
        profile.setId(100L);
        var dto = EmployeeProfilePrivateDto.builder()
                .employeeId(1L)
                .firstName("Alice")
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(profileRepository.findByEmployeeId(1L)).thenReturn(Optional.of(profile));
            when(profileMapper.toPrivateDto(profile)).thenReturn(dto);

            EmployeeProfilePrivateDto result = service.getMyProfile();

            assertThat(result.getEmployeeId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("Alice");
            verify(profileRepository).findByEmployeeId(1L);
            verify(profileMapper).toPrivateDto(profile);
        }
    }

    @Test
    void getMyProfile_shouldThrowIfNoEmployeeIdInUser() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(null)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);

            assertThatThrownBy(() -> service.getMyProfile())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no employeeId");
            verifyNoInteractions(profileRepository, profileMapper);
        }
    }

    @Test
    void getPrivateProfile_shouldAllowSelfAccess() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .build();
        var profile = EmployeeProfile.builder()
                .id(100L)
                .employee(employee)
                .build();
        var dto = EmployeeProfilePrivateDto.builder()
                .employeeId(1L)
                .firstName("Alice")
                .build();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(profileRepository.findByEmployeeId(1L)).thenReturn(Optional.of(profile));
            when(profileMapper.toPrivateDto(profile)).thenReturn(dto);

            EmployeeProfilePrivateDto result = service.getPrivateProfile(1L);

            assertThat(result.getEmployeeId()).isEqualTo(1L);
            verify(employeeRepository).findById(1L);
            verify(profileRepository).findByEmployeeId(1L);
            verify(profileMapper).toPrivateDto(profile);
        }
    }

    @Test
    void getPrivateProfile_shouldAllowManagerOfTargetEmployee() {
        var managerUser = AuthenticatedUser.builder()
                .userId(20L)
                .employeeId(2L)
                .username("manager")
                .roles(Set.of(Role.MANAGER))
                .build();
        var manager = Employee.builder()
                .id(2L)
                .firstName("Manager")
                .build();
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .manager(manager)
                .build();
        var profile = EmployeeProfile.builder()
                .id(100L)
                .employee(employee)
                .build();
        var dto = EmployeeProfilePrivateDto.builder()
                .employeeId(1L)
                .firstName("Alice")
                .build();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(managerUser);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(profileRepository.findByEmployeeId(1L)).thenReturn(Optional.of(profile));
            when(profileMapper.toPrivateDto(profile)).thenReturn(dto);

            var result = service.getPrivateProfile(1L);

            assertThat(result.getEmployeeId()).isEqualTo(1L);
        }
    }

    @Test
    void getPrivateProfile_shouldDenyRandomCoworker() {
        var coworkerUser = AuthenticatedUser.builder()
                .userId(30L)
                .employeeId(3L)
                .username("coworker")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(coworkerUser);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

            assertThatThrownBy(() -> service.getPrivateProfile(1L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Not allowed");

            verify(profileRepository, never()).findByEmployeeId(anyLong());
            verifyNoInteractions(profileMapper);
        }
    }

    @Test
    void updateProfile_shouldApplyChangesAndReturnPrivateDto() {
        var user = AuthenticatedUser.builder().userId(10L).employeeId(1L).roles(Set.of(Role.EMPLOYEE)).build();
        var employee = Employee.builder().id(1L).firstName("Alice").build();
        var existingProfile = EmployeeProfile.builder().id(100L).employee(employee).build();
        var request = EmployeeProfileUpdateRequest.builder()
                .jobTitle("New Title")
                .department("Engineering")
                .skills("Java,Spring")
                .bio("Updated bio")
                .avatarUrl("avatar.png")
                .salary(new BigDecimal("2000.00"))
                .performanceNotes("Great")
                .homeAddress("New Address")
                .personalPhone("+49 111 222 333")
                .build();
        var savedProfile = EmployeeProfile.builder().id(100L).employee(employee).build();
        var dto = EmployeeProfilePrivateDto.builder()
                .employeeId(1L)
                .jobTitle("New Title")
                .salary(new BigDecimal("2000.00"))
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(profileRepository.findByEmployeeId(1L)).thenReturn(Optional.of(existingProfile));
            when(profileRepository.save(existingProfile)).thenReturn(savedProfile);
            when(profileMapper.toPrivateDto(savedProfile)).thenReturn(dto);
            ArgumentCaptor<EmployeeProfile> captor = ArgumentCaptor.forClass(EmployeeProfile.class);

            var result = service.updateProfile(1L, request);

            assertThat(result.getEmployeeId()).isEqualTo(1L);
            assertThat(result.getJobTitle()).isEqualTo("New Title");
            assertThat(result.getSalary()).isEqualTo(new BigDecimal("2000.00"));

            verify(profileRepository).save(captor.capture());
            var capturedProfile = captor.getValue();
            assertThat(capturedProfile.getJobTitle()).isEqualTo("New Title");
            assertThat(capturedProfile.getDepartment()).isEqualTo("Engineering");
            assertThat(capturedProfile.getSkills()).isEqualTo("Java,Spring");
            assertThat(capturedProfile.getBio()).isEqualTo("Updated bio");
            assertThat(capturedProfile.getAvatarUrl()).isEqualTo("avatar.png");
            assertThat(capturedProfile.getSalary()).isEqualTo(new BigDecimal("2000.00"));
            assertThat(capturedProfile.getPerformanceNotes()).isEqualTo("Great");
            assertThat(capturedProfile.getHomeAddress()).isEqualTo("New Address");
            assertThat(capturedProfile.getPersonalPhone()).isEqualTo("+49 111 222 333");
        }
    }

    @Test
    void getPrivateProfile_shouldThrowIfEmployeeNotFound() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getPrivateProfile(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Employee not found");
        }
    }

    @Test
    void getPublicProfile_shouldAllowSelfAccess() {
        var user = AuthenticatedUser.builder()
                .userId(1L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .build();
        var profile = EmployeeProfile.builder()
                .id(1L)
                .employee(employee)
                .build();
        var dto = EmployeeProfilePublicDto.builder()
                .employeeId(1L)
                .firstName("Alice")
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(profileRepository.findByEmployeeId(1L)).thenReturn(Optional.of(profile));
            when(profileMapper.toPublicDto(profile)).thenReturn(dto);

            var result = service.getPublicProfile(1L);

            assertThat(result.getEmployeeId()).isEqualTo(1L);
        }
    }

    @Test
    void getPublicProfile_shouldAllowManagerOfTargetEmployee() {
        var managerUser = AuthenticatedUser.builder()
                .userId(20L)
                .employeeId(2L)
                .username("manager")
                .roles(Set.of(Role.MANAGER))
                .build();
        var manager = Employee.builder()
                .id(2L)
                .firstName("Manager")
                .build();
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .manager(manager)
                .build();
        var profile = EmployeeProfile.builder()
                .id(100L)
                .employee(employee)
                .build();
        var dto = EmployeeProfilePublicDto.builder()
                .employeeId(1L)
                .firstName("Alice")
                .build();

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(managerUser);
            when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
            when(profileRepository.findByEmployeeId(1L)).thenReturn(Optional.of(profile));
            when(profileMapper.toPublicDto(profile)).thenReturn(dto);

            var result = service.getPublicProfile(1L);

            assertThat(result.getEmployeeId()).isEqualTo(1L);
        }
    }

    @Test
    void getPublicProfile_shouldThrowIfEmployeeNotFound() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
            when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getPublicProfile(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Employee not found");
        }
    }

    @Test
    void getAllPublicProfiles_shouldReturnMappedDtos() {
        var user = AuthenticatedUser.builder()
                .userId(10L)
                .employeeId(1L)
                .username("alice")
                .roles(Set.of(Role.EMPLOYEE))
                .build();

        try (var securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUser).thenReturn(user);
            var profile1 = EmployeeProfile.builder().id(1L).build();
            var profile2 = EmployeeProfile.builder().id(2L).build();
            var allProfiles = List.of(profile1, profile2);
            var dto1 = EmployeeProfilePublicDto.builder().employeeId(1L).build();
            var dto2 = EmployeeProfilePublicDto.builder().employeeId(2L).build();
            var mappedDtos = List.of(dto1, dto2);
            when(profileRepository.findAll()).thenReturn(allProfiles);
            when(profileMapper.toPublicDtoList(allProfiles)).thenReturn(mappedDtos);

            List<EmployeeProfilePublicDto> result = service.getAllPublicProfiles();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(dto1, dto2);
            verify(profileRepository).findAll();
            verify(profileMapper).toPublicDtoList(allProfiles);
        }
    }

}
