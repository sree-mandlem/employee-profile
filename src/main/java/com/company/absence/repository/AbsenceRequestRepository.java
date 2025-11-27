package com.company.absence.repository;

import com.company.absence.entity.AbsenceRequest;
import com.company.absence.model.AbsenceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, Long> {

    /**
     * All absence requests for a given employee (for "My absences" view).
     */
    Page<AbsenceRequest> findByEmployeeIdOrderByFromDateDesc(Long employeeId, Pageable pageable);

    /**
     * All absence requests of direct reports of a manager, optionally filtered by status.
     */
    Page<AbsenceRequest> findByManagerIdAndStatusOrderByFromDateDesc(
            Long managerId,
            AbsenceStatus status,
            Pageable pageable
    );
}