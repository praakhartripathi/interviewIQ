package com.interviewiq.server.user.repository;

import com.interviewiq.server.user.entity.Subscription;
import com.interviewiq.server.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findFirstByUserOrderByIdDesc(User user);
}
