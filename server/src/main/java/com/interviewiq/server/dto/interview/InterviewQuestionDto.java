package com.interviewiq.server.dto.interview;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InterviewQuestionDto {
    private Long id;
    private String questionText;
    private String userAnswer;
    private String aiFeedback;
    private Integer score;
}
