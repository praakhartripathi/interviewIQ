package com.interviewiq.server.dto.resume;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeAnalyzeRequest {
    @NotNull
    private Long resumeId;
}
