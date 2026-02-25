package com.interviewiq.server.interview.controller;

import com.interviewiq.server.interview.dto.InterviewAnswerRequest;
import com.interviewiq.server.interview.dto.InterviewDto;
import com.interviewiq.server.interview.dto.InterviewStartRequest;
import com.interviewiq.server.interview.service.InterviewService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    public ResponseEntity<InterviewDto> start(@RequestBody InterviewStartRequest request) {
        return ResponseEntity.ok(interviewService.start(request));
    }

    @PostMapping("/answer")
    public ResponseEntity<InterviewDto> answer(@Valid @RequestBody InterviewAnswerRequest request) {
        return ResponseEntity.ok(interviewService.answer(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<InterviewDto>> history() {
        return ResponseEntity.ok(interviewService.history());
    }
}
