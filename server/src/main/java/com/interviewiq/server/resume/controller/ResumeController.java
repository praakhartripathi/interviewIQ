package com.interviewiq.server.resume.controller;

import com.interviewiq.server.resume.dto.ResumeAnalyzeRequest;
import com.interviewiq.server.resume.dto.ResumeAnalyzeResponse;
import com.interviewiq.server.resume.dto.ResumeResponse;
import com.interviewiq.server.resume.service.ResumeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping("/upload")
    public ResponseEntity<ResumeResponse> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(resumeService.upload(file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResume(@PathVariable Long id) {
        return ResponseEntity.ok(resumeService.getById(id));
    }

    @PostMapping("/analyze")
    public ResponseEntity<ResumeAnalyzeResponse> analyze(@Valid @RequestBody ResumeAnalyzeRequest request) {
        return ResponseEntity.ok(resumeService.analyze(request));
    }
}
