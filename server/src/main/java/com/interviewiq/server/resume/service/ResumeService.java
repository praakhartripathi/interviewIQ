package com.interviewiq.server.resume.service;

import com.interviewiq.server.common.security.CurrentUserService;
import com.interviewiq.server.resume.dto.ResumeAnalyzeRequest;
import com.interviewiq.server.resume.dto.ResumeAnalyzeResponse;
import com.interviewiq.server.resume.dto.ResumeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interviewiq.server.resume.entity.Resume;
import com.interviewiq.server.user.entity.User;
import com.interviewiq.server.resume.repository.ResumeRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final CurrentUserService currentUserService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.mistral.api-key:}")
    private String mistralApiKey;

    @Value("${app.mistral.model:mistral-small-latest}")
    private String mistralModel;

    @Value("${app.groq.api-key:}")
    private String groqApiKey;

    @Value("${app.groq.model:llama-3.1-8b-instant}")
    private String groqModel;

    public ResumeService(
            ResumeRepository resumeRepository,
            CurrentUserService currentUserService,
            ObjectMapper objectMapper
    ) {
        this.resumeRepository = resumeRepository;
        this.currentUserService = currentUserService;
        this.objectMapper = objectMapper;
        this.restTemplate = new RestTemplate();
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
        resume.setAiFeedback("Resume uploaded. Click analyze to get ATS score and role recommendations.");
        resume.setContentText(extractText(file));
        resume.setProjectInsights("Analyze after upload to detect project strengths.");
        resume.setRecommendedRoles("Analyze after upload to receive role recommendations.");

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

        String text = safeText(resume.getContentText());
        if (text.length() < 30) {
            throw new IllegalArgumentException("Could not read enough text from resume PDF. Upload a text-based PDF and try again.");
        }

        AnalysisResult result = analyzeWithGroq(text);
        if (result == null) {
            result = analyzeWithMistral(text);
        }
        if (result == null) {
            int score = calculateAtsScore(text);
            String projectInsights = buildProjectInsights(text);
            String recommendedRoles = buildRecommendedRoles(text);
            result = new AnalysisResult(
                    score,
                    buildFeedback(score, text),
                    projectInsights,
                    recommendedRoles
            );
        }

        resume.setAtsScore(result.atsScore());
        resume.setProjectInsights(result.projectInsights());
        resume.setRecommendedRoles(result.recommendedRoles());
        resume.setAiFeedback(result.aiFeedback());
        Resume saved = resumeRepository.save(resume);

        return new ResumeAnalyzeResponse(
                saved.getId(),
                saved.getAtsScore(),
                saved.getAiFeedback(),
                saved.getProjectInsights(),
                saved.getRecommendedRoles()
        );
    }

    private ResumeResponse toResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getFileUrl(),
                resume.getAtsScore(),
                resume.getAiFeedback(),
                resume.getProjectInsights(),
                resume.getRecommendedRoles(),
                resume.getCreatedAt().toString()
        );
    }

    private String extractText(MultipartFile file) {
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!contentType.contains("pdf")) {
            return "";
        }

        try (InputStream inputStream = file.getInputStream(); PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return safeText(stripper.getText(document));
        } catch (IOException ex) {
            return "";
        }
    }

    private int calculateAtsScore(String text) {
        int score = 55;
        if (text.contains("experience")) score += 8;
        if (text.contains("skills")) score += 8;
        if (text.contains("project")) score += 9;
        if (text.contains("education")) score += 6;
        if (text.contains("spring") || text.contains("java")) score += 5;
        if (text.contains("react") || text.contains("frontend")) score += 5;
        if (text.contains("aws") || text.contains("docker") || text.contains("kubernetes")) score += 4;
        return Math.min(score, 98);
    }

    private String buildProjectInsights(String text) {
        List<String> insights = new ArrayList<>();
        if (text.contains("spring") || text.contains("java")) {
            insights.add("Strong backend project signals (Java/Spring ecosystem).");
        }
        if (text.contains("react") || text.contains("javascript") || text.contains("typescript")) {
            insights.add("Good frontend project coverage (React/JS/TS).");
        }
        if (text.contains("mysql") || text.contains("postgres") || text.contains("mongodb")) {
            insights.add("Database-handling experience visible in projects.");
        }
        if (text.contains("aws") || text.contains("docker") || text.contains("ci/cd")) {
            insights.add("DevOps or cloud project exposure detected.");
        }
        if (insights.isEmpty()) {
            insights.add("Project section exists but needs clearer tech stack and measurable outcomes.");
        }
        return String.join(" ", insights);
    }

    private String buildRecommendedRoles(String text) {
        List<String> roles = new ArrayList<>();
        if (text.contains("react") || text.contains("javascript") || text.contains("typescript")) {
            roles.add("Frontend Developer");
        }
        if (text.contains("spring") || text.contains("java")) {
            roles.add("Backend Developer");
        }
        if (text.contains("react") && text.contains("spring")) {
            roles.add("Full Stack Developer");
        }
        if (text.contains("aws") || text.contains("docker") || text.contains("kubernetes")) {
            roles.add("Cloud/DevOps Engineer");
        }
        if (roles.isEmpty()) {
            roles.add("Software Engineer (Generalist)");
        }
        return String.join(", ", roles);
    }

    private String buildFeedback(int score, String text) {
        StringBuilder feedback = new StringBuilder();
        if (score >= 85) {
            feedback.append("ATS alignment is strong. ");
        } else if (score >= 70) {
            feedback.append("ATS alignment is moderate; improve keyword depth. ");
        } else {
            feedback.append("ATS alignment is low; improve structure and role-specific keywords. ");
        }

        if (!text.contains("project")) {
            feedback.append("Add a dedicated Projects section.");
        } else {
            feedback.append("For each project, include impact metrics and your exact contribution.");
        }
        return feedback.toString().trim();
    }

    private String safeText(String input) {
        if (input == null) return "";
        return input.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }

    private AnalysisResult analyzeWithMistral(String text) {
        if (mistralApiKey == null || mistralApiKey.isBlank()) {
            return null;
        }

        String prompt = """
                Analyze this resume text and return strict JSON only:
                {
                  "atsScore": number from 0-100,
                  "aiFeedback": "short actionable feedback",
                  "projectInsights": "project quality and strengths summary",
                  "recommendedRoles": "comma separated best-fit job roles"
                }
                Resume text:
                """ + truncate(text, 9000);

        Map<String, Object> body = Map.of(
                "model", mistralModel,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an expert ATS resume reviewer."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(mistralApiKey);

        try {
            Map<?, ?> response = restTemplate.postForObject(
                    "https://api.mistral.ai/v1/chat/completions",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            if (response == null) return null;

            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Object firstChoice = choices.get(0);
            if (!(firstChoice instanceof Map<?, ?> choiceMap)) return null;
            Object messageObj = choiceMap.get("message");
            if (!(messageObj instanceof Map<?, ?> messageMap)) return null;
            String content = String.valueOf(messageMap.get("content"));
            if (content == null || content.isBlank()) return null;

            JsonNode node = objectMapper.readTree(cleanJson(content));
            int atsScore = clamp(node.path("atsScore").asInt(calculateAtsScore(text)));
            String aiFeedback = nonBlank(node.path("aiFeedback").asText(), buildFeedback(atsScore, text));
            String projectInsights = nonBlank(node.path("projectInsights").asText(), buildProjectInsights(text));
            String recommendedRoles = nonBlank(node.path("recommendedRoles").asText(), buildRecommendedRoles(text));

            return new AnalysisResult(atsScore, aiFeedback, projectInsights, recommendedRoles);
        } catch (Exception ignored) {
            return null;
        }
    }

    private AnalysisResult analyzeWithGroq(String text) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return null;
        }

        String prompt = """
                Analyze this resume text and return strict JSON only:
                {
                  "atsScore": number from 0-100,
                  "aiFeedback": "short actionable feedback",
                  "projectInsights": "project quality and strengths summary",
                  "recommendedRoles": "comma separated best-fit job roles"
                }
                Resume text:
                """ + truncate(text, 9000);

        Map<String, Object> body = Map.of(
                "model", groqModel,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an expert ATS resume reviewer."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        try {
            Map<?, ?> response = restTemplate.postForObject(
                    "https://api.groq.com/openai/v1/chat/completions",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
            if (response == null) return null;

            List<?> choices = (List<?>) response.get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Object firstChoice = choices.get(0);
            if (!(firstChoice instanceof Map<?, ?> choiceMap)) return null;
            Object messageObj = choiceMap.get("message");
            if (!(messageObj instanceof Map<?, ?> messageMap)) return null;
            String content = String.valueOf(messageMap.get("content"));
            if (content == null || content.isBlank()) return null;

            JsonNode node = objectMapper.readTree(cleanJson(content));
            int atsScore = clamp(node.path("atsScore").asInt(calculateAtsScore(text)));
            String aiFeedback = nonBlank(node.path("aiFeedback").asText(), buildFeedback(atsScore, text));
            String projectInsights = nonBlank(node.path("projectInsights").asText(), buildProjectInsights(text));
            String recommendedRoles = nonBlank(node.path("recommendedRoles").asText(), buildRecommendedRoles(text));

            return new AnalysisResult(atsScore, aiFeedback, projectInsights, recommendedRoles);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String cleanJson(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.replaceAll("^```[a-zA-Z]*", "").replaceAll("```$", "").trim();
        }
        return cleaned;
    }

    private String nonBlank(String input, String fallback) {
        return input == null || input.isBlank() ? fallback : input.trim();
    }

    private int clamp(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }

    private record AnalysisResult(int atsScore, String aiFeedback, String projectInsights, String recommendedRoles) {
    }
}
