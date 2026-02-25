package com.interviewiq.server.repository;

import com.interviewiq.server.model.Interview;
import com.interviewiq.server.model.InterviewQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByInterviewOrderByIdAsc(Interview interview);
}
