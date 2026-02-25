package com.interviewiq.server.interview.repository;

import com.interviewiq.server.interview.entity.Interview;
import com.interviewiq.server.interview.entity.InterviewQuestion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewQuestionRepository extends JpaRepository<InterviewQuestion, Long> {
    List<InterviewQuestion> findByInterviewOrderByIdAsc(Interview interview);
}
