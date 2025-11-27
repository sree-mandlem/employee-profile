package com.company.employee.controller;

import com.company.common.api.GlobalExceptionHandler;
import com.company.employee.dto.EmployeeProfilePrivateDto;
import com.company.employee.dto.EmployeeProfilePublicDto;
import com.company.employee.dto.EmployeeProfileUpdateRequest;
import com.company.employee.service.EmployeeProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmployeeProfileController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeProfileControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    EmployeeProfileService employeeProfileService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getMyProfile_shouldReturnPrivateProfile() throws Exception {
        var dto = EmployeeProfilePrivateDto.builder()
                .employeeId(1L)
                .firstName("Alice")
                .lastName("Smith")
                .salary(new BigDecimal("1000.00"))
                .build();
        when(employeeProfileService.getMyProfile()).thenReturn(dto);

        mockMvc.perform(get("/api/employees/me/profile"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.employeeId").value(1L))
                .andExpect(jsonPath("$.firstName").value("Alice"));
        verify(employeeProfileService).getMyProfile();
    }

    @Test
    void getPrivateProfile_shouldCallServiceWithPathVariable() throws Exception {
        var dto = EmployeeProfilePrivateDto.builder()
                .employeeId(5L)
                .firstName("Bob")
                .build();
        when(employeeProfileService.getPrivateProfile(5L)).thenReturn(dto);

        mockMvc.perform(get("/api/employees/5/profile/private"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(5L));

        verify(employeeProfileService).getPrivateProfile(5L);
    }

    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void getPublicProfile_shouldReturnPublicDto() throws Exception {
        var dto = EmployeeProfilePublicDto.builder()
                .employeeId(3L)
                .firstName("Carol")
                .jobTitle("Developer")
                .build();
        when(employeeProfileService.getPublicProfile(3L)).thenReturn(dto);

        mockMvc.perform(get("/api/employees/3/profile/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(3L))
                .andExpect(jsonPath("$.jobTitle").value("Developer"));

        verify(employeeProfileService).getPublicProfile(3L);
    }

    @Test
    void updateProfile_shouldPassBodyAndIdToService() throws Exception {
        var request = EmployeeProfileUpdateRequest.builder()
                .jobTitle("Senior Dev")
                .department("Engineering")
                .salary(new BigDecimal("2000.00"))
                .build();
        var resultDto = EmployeeProfilePrivateDto.builder()
                .employeeId(7L)
                .jobTitle("Senior Dev")
                .salary(new BigDecimal("2000.00"))
                .build();
        when(employeeProfileService.updateProfile(eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(resultDto);

        mockMvc.perform(put("/api/employees/7/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(7L))
                .andExpect(jsonPath("$.jobTitle").value("Senior Dev"));

        var captor = ArgumentCaptor.forClass(EmployeeProfileUpdateRequest.class);
        verify(employeeProfileService).updateProfile(eq(7L), captor.capture());
        var passed = captor.getValue();
        assertThat(passed.getJobTitle()).isEqualTo("Senior Dev");
        assertThat(passed.getSalary()).isEqualByComparingTo("2000.00");
    }

    @WithMockUser(roles = "EMPLOYEE")
    @Test
    void getAllPublicProfiles_shouldBeAllowedForEmployee() throws Exception {
        var dto = EmployeeProfilePublicDto.builder().employeeId(1L).build();

        when(employeeProfileService.getAllPublicProfiles()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/employees/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeId").value(1));
    }

    @WithMockUser(roles = "MANAGER")
    @Test
    void getAllPublicProfiles_shouldBeAllowedForManager() throws Exception {
        mockMvc.perform(get("/api/employees/public"))
                .andExpect(status().isOk());
    }
}
