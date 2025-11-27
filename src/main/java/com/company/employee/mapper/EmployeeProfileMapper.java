package com.company.employee.mapper;

import com.company.employee.dto.EmployeeProfilePrivateDto;
import com.company.employee.dto.EmployeeProfilePublicDto;
import com.company.employee.entity.EmployeeProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeProfileMapper {

    @Mappings({
            @Mapping(source = "employee.id",        target = "employeeId"),
            @Mapping(source = "employee.firstName", target = "firstName"),
            @Mapping(source = "employee.lastName",  target = "lastName"),
            @Mapping(source = "employee.email",     target = "email"),
            @Mapping(source = "jobTitle",  target = "jobTitle"),
            @Mapping(source = "department",      target = "department")
    })
    EmployeeProfilePrivateDto toPrivateDto(EmployeeProfile profile);

    @Mappings({
            @Mapping(source = "employee.id",        target = "employeeId"),
            @Mapping(source = "employee.firstName", target = "firstName"),
            @Mapping(source = "employee.lastName",  target = "lastName"),
            @Mapping(source = "jobTitle",  target = "jobTitle"),
            @Mapping(source = "department",      target = "department"),
    })
    EmployeeProfilePublicDto toPublicDto(EmployeeProfile profile);

    List<EmployeeProfilePublicDto> toPublicDtoList(List<EmployeeProfile> profiles);
}
