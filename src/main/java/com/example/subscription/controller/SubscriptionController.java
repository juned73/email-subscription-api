package com.example.subscription.controller;

import com.example.subscription.dto.ApiResponse;
import com.example.subscription.dto.CreateSubscriptionRequest;
import com.example.subscription.dto.SubscriptionResponse;
import com.example.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * POST /api/subscriptions
     * Creates a new email subscription. Validates email externally before persisting.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SubscriptionResponse>> create(
            @Valid @RequestBody CreateSubscriptionRequest request) {

        log.info("POST /api/subscriptions - email={}", request.getEmail());
        SubscriptionResponse response = subscriptionService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Subscription created successfully.", response));
    }

    /**
     * GET /api/subscriptions
     * Returns all subscriptions.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getAll() {
        log.info("GET /api/subscriptions");
        List<SubscriptionResponse> subscriptions = subscriptionService.findAll();

        return ResponseEntity.ok(
                ApiResponse.success("Subscriptions retrieved successfully.", subscriptions));
    }

    /**
     * GET /api/subscriptions/{id}
     * Returns a single subscription by id.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getById(@PathVariable Long id) {
        log.info("GET /api/subscriptions/{}", id);
        SubscriptionResponse response = subscriptionService.findById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription retrieved successfully.", response));
    }

    /**
     * DELETE /api/subscriptions/{id}
     * Deletes a subscription by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        log.info("DELETE /api/subscriptions/{}", id);
        subscriptionService.delete(id);

        return ResponseEntity.ok(
                ApiResponse.success("Subscription deleted successfully.", null));
    }
}
