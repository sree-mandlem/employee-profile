package com.company.feedback.service;

import com.company.feedback.dto.FeedbackCreateRequest;
import com.company.feedback.dto.FeedbackDto;

import java.util.List;

public interface FeedbackService {

    /**
     * Get feedback for a given employee, filtered according to the current user's permissions.
     */
    List<FeedbackDto> getFeedbackForEmployee(Long employeeId);

    /**
     * Create feedback about a given employee as the current authenticated co-worker.
     */
    FeedbackDto createFeedback(Long employeeId, FeedbackCreateRequest request);
}
