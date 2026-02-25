package com.interviewiq.server.interview.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterviewDto {
    private Long id;
    private String title;
    private Integer totalScore;
    private String createdAt;
    private List<InterviewQuestionDto> questions;
}
