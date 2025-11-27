package com.company.feedback.repository;

import com.company.feedback.entity.Feedback;
import com.company.feedback.model.FeedbackVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * All feedback for the given employee visible to employee and manager.
     */
    Page<Feedback> findByEmployeeIdAndVisibilityIn(
            Long employeeId,
            Iterable<FeedbackVisibility> visibilities,
            Pageable pageable
    );
}
