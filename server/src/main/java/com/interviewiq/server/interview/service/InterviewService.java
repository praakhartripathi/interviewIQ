package com.interviewiq.server.interview.service;

import com.interviewiq.server.common.security.CurrentUserService;
import com.interviewiq.server.interview.dto.InterviewAnswerRequest;
import com.interviewiq.server.interview.dto.InterviewDto;
import com.interviewiq.server.interview.dto.InterviewQuestionDto;
import com.interviewiq.server.interview.dto.InterviewStartRequest;
import com.interviewiq.server.interview.entity.Interview;
import com.interviewiq.server.interview.entity.InterviewQuestion;
import com.interviewiq.server.user.entity.User;
import com.interviewiq.server.interview.repository.InterviewQuestionRepository;
import com.interviewiq.server.interview.repository.InterviewRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewQuestionRepository interviewQuestionRepository;
    private final CurrentUserService currentUserService;

    public InterviewService(
            InterviewRepository interviewRepository,
            InterviewQuestionRepository interviewQuestionRepository,
            CurrentUserService currentUserService
    ) {
        this.interviewRepository = interviewRepository;
        this.interviewQuestionRepository = interviewQuestionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public InterviewDto start(InterviewStartRequest request) {
        User user = currentUserService.currentUser();

        Interview interview = new Interview();
        interview.setUser(user);
        interview.setTitle(request.getTitle() == null || request.getTitle().isBlank()
                ? "AI Mock Interview"
                : request.getTitle().trim());
        interview.setTotalScore(0);

        Interview savedInterview = interviewRepository.save(interview);

        InterviewQuestion firstQuestion = new InterviewQuestion();
        firstQuestion.setInterview(savedInterview);
        firstQuestion.setQuestionText("Tell me about yourself and why you are a strong fit for this role.");
        firstQuestion.setAiFeedback("Pending your answer.");
        firstQuestion.setScore(0);
        interviewQuestionRepository.save(firstQuestion);

        return toDto(savedInterview);
    }

    @Transactional
    public InterviewDto answer(InterviewAnswerRequest request) {
        User user = currentUserService.currentUser();

        Interview interview = interviewRepository.findById(request.getInterviewId())
                .filter(i -> i.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Interview not found"));

        InterviewQuestion question = interviewQuestionRepository.findById(request.getQuestionId())
                .filter(q -> q.getInterview().getId().equals(interview.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        int score = scoreFromAnswer(request.getAnswer());
        question.setUserAnswer(request.getAnswer().trim());
        question.setScore(score);
        question.setAiFeedback(buildFeedback(score));
        interviewQuestionRepository.save(question);

        interview.setTotalScore(calculateTotalScore(interview));
        interviewRepository.save(interview);

        return toDto(interview);
    }

    public List<InterviewDto> history() {
        User user = currentUserService.currentUser();
        return interviewRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private InterviewDto toDto(Interview interview) {
        List<InterviewQuestionDto> questions = interviewQuestionRepository.findByInterviewOrderByIdAsc(interview)
                .stream()
                .map(q -> new InterviewQuestionDto(
                        q.getId(),
                        q.getQuestionText(),
                        q.getUserAnswer(),
                        q.getAiFeedback(),
                        q.getScore()
                ))
                .toList();

        return new InterviewDto(
                interview.getId(),
                interview.getTitle(),
                interview.getTotalScore(),
                interview.getCreatedAt().toString(),
                questions
        );
    }

    private int calculateTotalScore(Interview interview) {
        List<InterviewQuestion> questions = interviewQuestionRepository.findByInterviewOrderByIdAsc(interview);
        return (int) questions.stream()
                .map(InterviewQuestion::getScore)
                .filter(s -> s != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    private int scoreFromAnswer(String answer) {
        int base = Math.min(100, 50 + answer.length() / 2);
        String lower = answer.toLowerCase(Locale.ROOT);
        if (lower.contains("impact") || lower.contains("result") || lower.contains("improved")) {
            base += 10;
        }
        return Math.min(base, 100);
    }

    private String buildFeedback(int score) {
        if (score >= 85) {
            return "Strong answer. Keep this concise structure and include one measurable business outcome.";
        }
        if (score >= 70) {
            return "Good start. Add clearer metrics and briefly explain your decision-making process.";
        }
        return "Answer is too generic. Add context, actions you took, and measurable outcomes.";
    }
}
