package com.interviewiq.server.repository;

import com.interviewiq.server.model.Resume;
import com.interviewiq.server.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
}
