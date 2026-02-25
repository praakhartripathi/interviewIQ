package com.interviewiq.server.resume.repository;

import com.interviewiq.server.resume.entity.Resume;
import com.interviewiq.server.user.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUserOrderByCreatedAtDesc(User user);
}
