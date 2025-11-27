package com.company.feedback.controller;

import com.company.common.api.GlobalExceptionHandler;
import com.company.feedback.dto.FeedbackCreateRequest;
import com.company.feedback.dto.FeedbackDto;
import com.company.feedback.model.FeedbackVisibility;
import com.company.feedback.service.FeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = FeedbackController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class FeedbackControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    FeedbackService feedbackService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void getFeedback_shouldReturnList() throws Exception {
        var feedback1 = FeedbackDto.builder()
                .id(100L)
                .employeeId(1L)
                .authorId(2L)
                .text("Great job")
                .visibility(FeedbackVisibility.EMPLOYEE_AND_MANAGER)
                .createdAt(LocalDateTime.now())
                .build();
        var feedback2 = FeedbackDto.builder()
                .id(101L)
                .employeeId(1L)
                .authorId(3L)
                .text("Nice collaboration")
                .visibility(FeedbackVisibility.MANAGER_ONLY)
                .createdAt(LocalDateTime.now())
                .build();
        when(feedbackService.getFeedbackForEmployee(1L))
                .thenReturn(List.of(feedback1, feedback2));

        mockMvc.perform(get("/api/employees/1/feedback"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(100L))
                .andExpect(jsonPath("$[0].text").value("Great job"));

        verify(feedbackService).getFeedbackForEmployee(1L);
    }

    @Test
    void createFeedback_shouldCallServiceAndReturnDto() throws Exception {
        var request = FeedbackCreateRequest.builder()
                .text("Nice work!")
                .visibility(FeedbackVisibility.EMPLOYEE_AND_MANAGER)
                .build();
        var dto = FeedbackDto.builder()
                .id(200L)
                .employeeId(1L)
                .authorId(2L)
                .text("Nice work!")
                .visibility(FeedbackVisibility.EMPLOYEE_AND_MANAGER)
                .build();
        when(feedbackService.createFeedback(eq(1L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(dto);

        mockMvc.perform(post("/api/employees/1/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(200L))
                .andExpect(jsonPath("$.text").value("Nice work!"));

        verify(feedbackService).createFeedback(eq(1L), org.mockito.ArgumentMatchers.any());
    }
}
