package com.interviewiq.server.repository;

import com.interviewiq.server.model.Interview;
import com.interviewiq.server.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserOrderByCreatedAtDesc(User user);
}
