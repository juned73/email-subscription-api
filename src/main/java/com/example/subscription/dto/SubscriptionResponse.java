package com.example.subscription.dto;

import com.example.subscription.model.EmailSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private String email;
    private String subscriberName;
    private String status;
    private LocalDateTime createdAt;

    public static SubscriptionResponse from(EmailSubscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .email(subscription.getEmail())
                .subscriberName(subscription.getSubscriberName())
                .status(subscription.getStatus().name())
                .createdAt(subscription.getCreatedAt())
                .build();
    }
}
