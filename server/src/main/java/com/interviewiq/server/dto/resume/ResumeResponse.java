package com.interviewiq.server.dto.resume;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeResponse {
    private Long id;
    private String fileUrl;
    private Integer atsScore;
    private String aiFeedback;
    private String createdAt;
}
