package com.company.feedback.dto;

import com.company.feedback.model.FeedbackVisibility;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCreateRequest {

    private String text;

    private FeedbackVisibility visibility;
}
