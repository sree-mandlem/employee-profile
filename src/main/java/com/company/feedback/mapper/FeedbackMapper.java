package com.company.feedback.mapper;

import com.company.feedback.dto.FeedbackDto;
import com.company.feedback.entity.Feedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeedbackMapper {

    @Mapping(source = "employee.id",        target = "employeeId")
    @Mapping(source = "author.id",          target = "authorId")
    @Mapping(source = "author.firstName",   target = "authorFirstName")
    @Mapping(source = "author.lastName",    target = "authorLastName")
    FeedbackDto toDto(Feedback feedback);

    List<FeedbackDto> toDtoList(List<Feedback> feedback);
}
