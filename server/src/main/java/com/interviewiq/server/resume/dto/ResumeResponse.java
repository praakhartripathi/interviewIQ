package com.interviewiq.server.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private String fileUrl;
    private Integer atsScore;
    private String aiFeedback;
    private String projectInsights;
    private String recommendedRoles;
    private String createdAt;
}
