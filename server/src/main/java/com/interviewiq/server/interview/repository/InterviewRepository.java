package com.interviewiq.server.interview.repository;

import com.interviewiq.server.interview.entity.Interview;
import com.interviewiq.server.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, Long> {
    List<Interview> findByUserOrderByCreatedAtDesc(User user);
}
