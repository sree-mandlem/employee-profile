package com.company.absence.mapper;

import com.company.absence.entity.AbsenceRequest;
import com.company.absence.model.AbsenceStatus;
import com.company.absence.model.AbsenceType;
import com.company.employee.entity.Employee;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AbsenceRequestMapperTest {

    private final AbsenceRequestMapper mapper = Mappers.getMapper(AbsenceRequestMapper.class);

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .build();

        var manager = Employee.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("Manager")
                .build();

        var from = LocalDate.of(2025, 1, 1);
        var to = LocalDate.of(2025, 1, 10);
        var decisionAt = LocalDateTime.of(2025, 1, 2, 9, 0);

        var entity = AbsenceRequest.builder()
                .id(100L)
                .employee(employee)
                .manager(manager)
                .fromDate(from)
                .toDate(to)
                .type(AbsenceType.VACATION)
                .status(AbsenceStatus.APPROVED)
                .decisionAt(decisionAt)
                .build();

        var dto = mapper.toDto(entity);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getEmployeeId()).isEqualTo(1L);
        assertThat(dto.getEmployeeFirstName()).isEqualTo("Alice");
        assertThat(dto.getEmployeeLastName()).isEqualTo("Smith");
        assertThat(dto.getManagerId()).isEqualTo(2L);
        assertThat(dto.getFromDate()).isEqualTo(from);
        assertThat(dto.getToDate()).isEqualTo(to);
        assertThat(dto.getType()).isEqualTo(AbsenceType.VACATION);
        assertThat(dto.getStatus()).isEqualTo(AbsenceStatus.APPROVED);
        assertThat(dto.getDecisionAt()).isEqualTo(decisionAt);
    }

    @Test
    void toDtoList_shouldMapListOfEntities() {
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .build();
        var req1 = AbsenceRequest.builder()
                .id(100L)
                .employee(employee)
                .fromDate(LocalDate.of(2025, 1, 1))
                .toDate(LocalDate.of(2025, 1, 5))
                .type(AbsenceType.VACATION)
                .status(AbsenceStatus.PENDING)
                .build();
        var req2 = AbsenceRequest.builder()
                .id(101L)
                .employee(employee)
                .fromDate(LocalDate.of(2025, 2, 1))
                .toDate(LocalDate.of(2025, 2, 3))
                .type(AbsenceType.SICK)
                .status(AbsenceStatus.APPROVED)
                .build();

        var dtos = mapper.toDtoList(List.of(req1, req2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(100L);
        assertThat(dtos.get(1).getId()).isEqualTo(101L);
        assertThat(dtos.get(0).getEmployeeId()).isEqualTo(1L);
        assertThat(dtos.get(1).getEmployeeId()).isEqualTo(1L);
    }
}
