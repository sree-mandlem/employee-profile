package com.company.feedback.service.impl;

import com.company.auth.model.AuthenticatedUser;
import com.company.auth.security.SecurityUtils;
import com.company.employee.entity.Employee;
import com.company.employee.repository.EmployeeRepository;
import com.company.feedback.dto.FeedbackCreateRequest;
import com.company.feedback.dto.FeedbackDto;
import com.company.feedback.entity.Feedback;
import com.company.feedback.mapper.FeedbackMapper;
import com.company.feedback.model.FeedbackVisibility;
import com.company.feedback.repository.FeedbackRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.springframework.data.domain.Pageable.unpaged;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FeedbackServiceImpl implements com.company.feedback.service.FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EmployeeRepository employeeRepository;
    private final FeedbackMapper feedbackMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getFeedbackForEmployee(Long employeeId) {
        var currentUser = SecurityUtils.getCurrentUser();

        log.info("Retrieving feedback for employee:" + employeeId + ", by: " + currentUser.getUserId());

        var target = findEmployeeOrThrow(employeeId);

        var visibilities = determineVisibleFeedbackVisibilities(currentUser, target);
        var page = feedbackRepository.findByEmployeeIdAndVisibilityIn(employeeId, visibilities, unpaged());
        return feedbackMapper.toDtoList(page.getContent());
    }

    @Override
    public FeedbackDto createFeedback(Long employeeId, FeedbackCreateRequest request) {
        var currentUser = SecurityUtils.getCurrentUser();

        log.info("Creating feedback for employee:" + employeeId + ", by: " + currentUser.getUserId());

        var authorEmployeeId = currentUser.getEmployeeId();
        if (authorEmployeeId == null) {
            log.error("Authenticated user has no employeeId");
            throw new IllegalStateException("Authenticated user has no employeeId");
        }

        if (authorEmployeeId.equals(employeeId)) {
            log.error("You cannot leave feedback about yourself");
            throw new AccessDeniedException("You cannot leave feedback about yourself");
        }

        var target = findEmployeeOrThrow(employeeId);
        var author = findEmployeeOrThrow(authorEmployeeId);

        var feedback = Feedback.builder()
                .employee(target)
                .author(author)
                .text(request.getText())
                .visibility(request.getVisibility() != null ? request.getVisibility()
                        : FeedbackVisibility.EMPLOYEE_AND_MANAGER)
                .build();

        var saved = feedbackRepository.save(feedback);

        log.debug("Successfully created feedback");
        return feedbackMapper.toDto(saved);
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("Employee not found: " + employeeId);
                    return new EntityNotFoundException("Employee not found: " + employeeId);
                });
    }

    private Set<FeedbackVisibility> determineVisibleFeedbackVisibilities(AuthenticatedUser user, Employee targetEmployee) {
        log.error("Checking the feedback visibility options for " + user.getUserId() + " to leave feedback for:" + targetEmployee.getId());

        var currentEmpId = user.getEmployeeId();

        var isSelf = currentEmpId != null && currentEmpId.equals(targetEmployee.getId());
        var isManager = false;

        var manager = targetEmployee.getManager();
        if (manager != null && currentEmpId != null) {
            isManager = manager.getId().equals(currentEmpId);
        }

        if (isSelf || isManager) {
            return EnumSet.allOf(FeedbackVisibility.class);
        }

        return EnumSet.of(FeedbackVisibility.EMPLOYEE_AND_MANAGER);
    }
}
