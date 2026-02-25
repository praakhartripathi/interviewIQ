package com.interviewiq.server.resume.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeAnalyzeRequest {
    @NotNull
    private Long resumeId;
}
