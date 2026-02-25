package com.interviewiq.server.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeAnalyzeResponse {
    private Long resumeId;
    private Integer atsScore;
    private String aiFeedback;
    private String projectInsights;
    private String recommendedRoles;
}
