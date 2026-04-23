package com.example.subscription.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a well-formed address")
    private String email;

    @NotBlank(message = "Subscriber name is required")
    private String subscriberName;
}
