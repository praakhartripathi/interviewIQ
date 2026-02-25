package com.interviewiq.server.repository;

import com.interviewiq.server.model.Subscription;
import com.interviewiq.server.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByUserOrderByIdDesc(User user);
}
