package com.example.subscription.service;

import com.example.subscription.dto.CreateSubscriptionRequest;
import com.example.subscription.dto.SubscriptionResponse;
import com.example.subscription.exception.DuplicateEmailException;
import com.example.subscription.exception.SubscriptionNotFoundException;
import com.example.subscription.model.EmailSubscription;
import com.example.subscription.repository.EmailSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final EmailSubscriptionRepository repository;
    private final EmailValidationService emailValidationService;

    /**
     * Creates a new email subscription.
     * Email is validated via an external API before persistence.
     */
    @Transactional
    public SubscriptionResponse create(CreateSubscriptionRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Check for duplicate before calling external API (fast-fail)
        if (repository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }

        // 2. Validate email via external API
        emailValidationService.validateEmail(email);

        // 3. Persist and return
        EmailSubscription subscription = EmailSubscription.builder()
                .email(email)
                .subscriberName(request.getSubscriberName().trim())
                .status(EmailSubscription.SubscriptionStatus.ACTIVE)
                .build();

        EmailSubscription saved = repository.save(subscription);
        log.info("Created subscription id={} for email={}", saved.getId(), saved.getEmail());

        return SubscriptionResponse.from(saved);
    }

    /**
     * Returns all subscriptions.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> findAll() {
        return repository.findAll()
                .stream()
                .map(SubscriptionResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single subscription by id.
     */
    @Transactional(readOnly = true)
    public SubscriptionResponse findById(Long id) {
        return repository.findById(id)
                .map(SubscriptionResponse::from)
                .orElseThrow(() -> new SubscriptionNotFoundException(id));
    }

    /**
     * Deletes a subscription by id.
     */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new SubscriptionNotFoundException(id);
        }
        repository.deleteById(id);
        log.info("Deleted subscription id={}", id);
    }
}
