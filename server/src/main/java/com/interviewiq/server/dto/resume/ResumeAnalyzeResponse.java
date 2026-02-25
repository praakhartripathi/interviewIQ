package com.interviewiq.server.dto.resume;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeAnalyzeResponse {
    private Long resumeId;
    private Integer atsScore;
    private String aiFeedback;
}
