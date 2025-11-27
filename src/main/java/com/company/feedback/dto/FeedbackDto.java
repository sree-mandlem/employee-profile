package com.company.feedback.dto;

import com.company.feedback.model.FeedbackVisibility;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDto {

    private Long id;

    private Long employeeId;      // target
    private Long authorId;        // co-worker

    private String authorFirstName;
    private String authorLastName;

    private String text;
    private FeedbackVisibility visibility;

    private LocalDateTime createdAt;
}
