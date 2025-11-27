package com.company.absence.mapper;

import com.company.absence.dto.AbsenceRequestDto;
import com.company.absence.entity.AbsenceRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AbsenceRequestMapper {

    @Mapping(source = "employee.id",        target = "employeeId")
    @Mapping(source = "employee.firstName", target = "employeeFirstName")
    @Mapping(source = "employee.lastName",  target = "employeeLastName")
    @Mapping(source = "manager.id",         target = "managerId")
    AbsenceRequestDto toDto(AbsenceRequest request);

    List<AbsenceRequestDto> toDtoList(List<AbsenceRequest> requests);
}
