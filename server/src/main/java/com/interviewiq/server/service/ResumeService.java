package com.interviewiq.server.service;

import com.interviewiq.server.dto.resume.ResumeAnalyzeRequest;
import com.interviewiq.server.dto.resume.ResumeAnalyzeResponse;
import com.interviewiq.server.dto.resume.ResumeResponse;
import com.interviewiq.server.model.Resume;
import com.interviewiq.server.model.User;
import com.interviewiq.server.repository.ResumeRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CurrentUserService currentUserService;

    public ResumeService(ResumeRepository resumeRepository, CurrentUserService currentUserService) {
        this.resumeRepository = resumeRepository;
        this.currentUserService = currentUserService;
    }

    public ResumeResponse upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Resume file is required");
        }

        User user = currentUserService.currentUser();

        Resume resume = new Resume();
        resume.setUser(user);
        resume.setFileUrl("uploads/" + UUID.randomUUID() + "-" + file.getOriginalFilename());
        resume.setAtsScore(0);
        resume.setAiFeedback("Uploaded. Click analyze to generate AI feedback.");

        Resume saved = resumeRepository.save(resume);
        return toResponse(saved);
    }

    public ResumeResponse getById(Long id) {
        User user = currentUserService.currentUser();
        Resume resume = resumeRepository.findById(id)
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        return toResponse(resume);
    }

    public ResumeAnalyzeResponse analyze(ResumeAnalyzeRequest request) {
        User user = currentUserService.currentUser();
        Resume resume = resumeRepository.findById(request.getResumeId())
                .filter(r -> r.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        int score = 78 + (int) (Math.random() * 16);
        resume.setAtsScore(score);
        resume.setAiFeedback("Strong structure. Add quantified project impact and align keywords to target role.");
        Resume saved = resumeRepository.save(resume);

        return new ResumeAnalyzeResponse(saved.getId(), saved.getAtsScore(), saved.getAiFeedback());
    }

    private ResumeResponse toResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getFileUrl(),
                resume.getAtsScore(),
                resume.getAiFeedback(),
                resume.getCreatedAt().toString()
        );
    }
}
