package com.example.subscription.repository;

import com.example.subscription.model.EmailSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailSubscriptionRepository extends JpaRepository<EmailSubscription, Long> {

    boolean existsByEmail(String email);

    Optional<EmailSubscription> findByEmail(String email);
}
