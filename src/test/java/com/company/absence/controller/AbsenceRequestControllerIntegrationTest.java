package com.company.absence.controller;

import com.company.absence.dto.AbsenceRequestCreateRequest;
import com.company.absence.dto.AbsenceRequestDto;
import com.company.absence.model.AbsenceStatus;
import com.company.absence.model.AbsenceType;
import com.company.absence.service.AbsenceRequestService;
import com.company.common.api.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AbsenceRequestController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AbsenceRequestControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AbsenceRequestService absenceRequestService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getMyAbsences_shouldReturnList() throws Exception {
        var dto = AbsenceRequestDto.builder()
                .id(1L)
                .employeeId(10L)
                .fromDate(LocalDate.of(2025, 1, 1))
                .toDate(LocalDate.of(2025, 1, 5))
                .type(AbsenceType.VACATION)
                .status(AbsenceStatus.APPROVED)
                .build();

        when(absenceRequestService.getMyAbsences())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/me/absences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));

        verify(absenceRequestService).getMyAbsences();
    }

    @Test
    void requestAbsence_shouldBeOk() throws Exception {
        var request = AbsenceRequestCreateRequest.builder()
                .fromDate(LocalDate.of(2025, 2, 1))
                .toDate(LocalDate.of(2025, 2, 3))
                .type(AbsenceType.SICK)
                .build();

        var dto = AbsenceRequestDto.builder()
                .id(5L)
                .employeeId(10L)
                .fromDate(request.getFromDate())
                .toDate(request.getToDate())
                .type(AbsenceType.SICK)
                .status(AbsenceStatus.PENDING)
                .build();

        when(absenceRequestService.requestAbsence(org.mockito.ArgumentMatchers.any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/me/absences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(absenceRequestService).requestAbsence(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getPendingAbsencesForMyTeam_shouldRequireManagerRole() throws Exception {
        var dto = AbsenceRequestDto.builder()
                .id(1L)
                .status(AbsenceStatus.PENDING)
                .build();

        when(absenceRequestService.getPendingAbsencesForMyTeam())
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/managers/me/absences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(absenceRequestService).getPendingAbsencesForMyTeam();
    }

    @Test
    void approveAbsence_shouldBeOk() throws Exception {
        var dto = AbsenceRequestDto.builder()
                .id(1L)
                .status(AbsenceStatus.APPROVED)
                .build();

        when(absenceRequestService.approveAbsence(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/absences/1/approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(absenceRequestService).approveAbsence(1L);
    }

    @Test
    void rejectAbsence_shouldBeOk() throws Exception {
        var dto = AbsenceRequestDto.builder()
                .id(1L)
                .status(AbsenceStatus.REJECTED)
                .build();

        when(absenceRequestService.rejectAbsence(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/absences/1/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(absenceRequestService).rejectAbsence(1L);
    }

    @Test
    void cancelAbsence_shouldBeOk() throws Exception {
        var dto = AbsenceRequestDto.builder()
                .id(42L)
                .status(AbsenceStatus.CANCELLED)
                .fromDate(LocalDate.of(2025, 1, 1))
                .toDate(LocalDate.of(2025, 1, 5))
                .build();

        when(absenceRequestService.cancelAbsence(42L)).thenReturn(dto);

        mockMvc.perform(post("/api/absences/42/cancel")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
