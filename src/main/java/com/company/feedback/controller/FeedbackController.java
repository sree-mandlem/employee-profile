package com.company.feedback.controller;

import com.company.feedback.api.FeedbackApi;
import com.company.feedback.dto.FeedbackCreateRequest;
import com.company.feedback.dto.FeedbackDto;
import com.company.feedback.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FeedbackController implements FeedbackApi {

    private final FeedbackService feedbackService;

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<List<FeedbackDto>> getFeedback(Long employeeId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForEmployee(employeeId));
    }

    @Override
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER')")
    public ResponseEntity<FeedbackDto> createFeedback(Long employeeId, FeedbackCreateRequest request) {
        return ResponseEntity.ok(feedbackService.createFeedback(employeeId, request));
    }
}
