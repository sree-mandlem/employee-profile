package com.company.employee.mapper;

import com.company.employee.entity.Employee;
import com.company.employee.entity.EmployeeProfile;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EmployeeProfileMapperTest {

    private final EmployeeProfileMapper mapper = Mappers.getMapper(EmployeeProfileMapper.class);

    @Test
    void toPrivateDto_shouldMapAllFieldsIncludingSensitiveOnes() {
        var employee = Employee.builder()
                .id(1L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .build();
        var profile = EmployeeProfile.builder()
                .employee(employee)
                .jobTitle("Senior Developer")
                .department("Engineering")
                .skills("Java,Spring,React")
                .bio("Loves clean architecture")
                .avatarUrl("https://cdn.example.com/avatar/alice.png")
                .salary(new BigDecimal("85000.00"))
                .performanceNotes("Top performer")
                .homeAddress("123 Main Street")
                .personalPhone("+49 123 456 789")
                .build();

        var dto = mapper.toPrivateDto(profile);

        assertThat(dto.getEmployeeId()).isEqualTo(1L);
        assertThat(dto.getFirstName()).isEqualTo("Alice");
        assertThat(dto.getLastName()).isEqualTo("Smith");
        assertThat(dto.getEmail()).isEqualTo("alice@example.com");
        assertThat(dto.getDepartment()).isEqualTo("Engineering");
        assertThat(dto.getJobTitle()).isEqualTo("Senior Developer");
        assertThat(dto.getSkills()).isEqualTo("Java,Spring,React");
        assertThat(dto.getBio()).isEqualTo("Loves clean architecture");
        assertThat(dto.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/alice.png");
        assertThat(dto.getSalary()).isEqualTo(new BigDecimal("85000.00"));
        assertThat(dto.getPerformanceNotes()).isEqualTo("Top performer");
        assertThat(dto.getHomeAddress()).isEqualTo("123 Main Street");
        assertThat(dto.getPersonalPhone()).isEqualTo("+49 123 456 789");
    }

    @Test
    void toPublicDto_shouldNotExposeSensitiveFields() {
        var employee = Employee.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("Miller")
                .build();
        var profile = EmployeeProfile.builder()
                .employee(employee)
                .jobTitle("Developer")
                .department("Engineering")
                .skills("Java,Spring")
                .bio("Likes refactoring")
                .avatarUrl("https://cdn.example.com/avatar/bob.png")
                .salary(new BigDecimal("65000.00"))
                .performanceNotes("Improving")
                .homeAddress("Another Street 5")
                .personalPhone("+49 987 654 321")
                .build();

        var dto = mapper.toPublicDto(profile);

        assertThat(dto.getEmployeeId()).isEqualTo(2L);
        assertThat(dto.getFirstName()).isEqualTo("Bob");
        assertThat(dto.getLastName()).isEqualTo("Miller");
        assertThat(dto.getJobTitle()).isEqualTo("Developer");
        assertThat(dto.getDepartment()).isEqualTo("Engineering");
        assertThat(dto.getSkills()).isEqualTo("Java,Spring");
        assertThat(dto.getBio()).isEqualTo("Likes refactoring");
        assertThat(dto.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/bob.png");
    }

    @Test
    void toPublicDtoList_shouldNotExposeSensitiveFields() {
        var employee = Employee.builder()
                .id(2L)
                .firstName("Bob")
                .lastName("Miller")
                .build();
        var profile = EmployeeProfile.builder()
                .employee(employee)
                .jobTitle("Developer")
                .department("Engineering")
                .skills("Java,Spring")
                .bio("Likes refactoring")
                .avatarUrl("https://cdn.example.com/avatar/bob.png")
                .salary(new BigDecimal("65000.00"))
                .performanceNotes("Improving")
                .homeAddress("Another Street 5")
                .personalPhone("+49 987 654 321")
                .build();

        var dtos = mapper.toPublicDtoList(List.of(profile));

        assertThat(dtos.size()).isEqualTo(1);
        var dto = dtos.get(0);
        assertThat(dto.getEmployeeId()).isEqualTo(2L);
        assertThat(dto.getFirstName()).isEqualTo("Bob");
        assertThat(dto.getLastName()).isEqualTo("Miller");
        assertThat(dto.getJobTitle()).isEqualTo("Developer");
        assertThat(dto.getDepartment()).isEqualTo("Engineering");
        assertThat(dto.getSkills()).isEqualTo("Java,Spring");
        assertThat(dto.getBio()).isEqualTo("Likes refactoring");
        assertThat(dto.getAvatarUrl()).isEqualTo("https://cdn.example.com/avatar/bob.png");
    }
}
