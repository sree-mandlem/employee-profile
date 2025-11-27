package com.company.feedback.mapper;

import com.company.employee.entity.Employee;
import com.company.feedback.entity.Feedback;
import com.company.feedback.model.FeedbackVisibility;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FeedbackMapperTest {

    private final FeedbackMapper mapper = Mappers.getMapper(FeedbackMapper.class);

    @Test
    void toDto_shouldMapAllFieldsCorrectly() {
        var target = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .build();
        var author = Employee.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("Miller")
                .build();
        var createdAt = LocalDateTime.of(2025, 1, 1, 10, 0);
        var feedback = Feedback.builder()
                .id(100L)
                .employee(target)
                .author(author)
                .text("Nice work!")
                .visibility(FeedbackVisibility.EMPLOYEE_AND_MANAGER)
                .createdAt(createdAt)
                .build();

        var dto = mapper.toDto(feedback);

        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getEmployeeId()).isEqualTo(1L);
        assertThat(dto.getAuthorId()).isEqualTo(2L);
        assertThat(dto.getAuthorFirstName()).isEqualTo("Bob");
        assertThat(dto.getAuthorLastName()).isEqualTo("Miller");
        assertThat(dto.getText()).isEqualTo("Nice work!");
        assertThat(dto.getVisibility()).isEqualTo(FeedbackVisibility.EMPLOYEE_AND_MANAGER);
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void toDtoList_shouldMapListOfEntities() {
        var target = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .build();
        var author = Employee.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("Miller")
                .build();
        var feedback1 = Feedback.builder()
                .id(100L)
                .employee(target)
                .author(author)
                .text("Feedback 1")
                .visibility(FeedbackVisibility.EMPLOYEE_AND_MANAGER)
                .build();
        var feedback2 = Feedback.builder()
                .id(101L)
                .employee(target)
                .author(author)
                .text("Feedback 2")
                .visibility(FeedbackVisibility.MANAGER_ONLY)
                .build();

        var dtos = mapper.toDtoList(List.of(feedback1, feedback2));

        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getId()).isEqualTo(100L);
        assertThat(dtos.get(0).getText()).isEqualTo("Feedback 1");
        assertThat(dtos.get(1).getId()).isEqualTo(101L);
        assertThat(dtos.get(1).getText()).isEqualTo("Feedback 2");
    }
}
