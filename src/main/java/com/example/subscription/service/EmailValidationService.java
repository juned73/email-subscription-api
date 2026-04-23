package com.example.subscription.service;

import com.example.subscription.dto.EmailValidationResponse;
import com.example.subscription.exception.EmailValidationServiceException;
import com.example.subscription.exception.InvalidEmailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailValidationService {

    private final RestTemplate restTemplate;

    @Value("${email.validation.api.url}")
    private String apiUrl;

    @Value("${email.validation.api.key}")
    private String apiKey;

    /**
     * Validates the given email address against the Abstract Email Validation API.
     * Throws {@link InvalidEmailException} if the email is not deliverable.
     * Throws {@link EmailValidationServiceException} if the external service is unreachable or returns an error.
     */
    public void validateEmail(String email) {
        log.info("Validating email address: {}", email);

        String uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("api_key", apiKey)
                .queryParam("email", email)
                .toUriString();
        
        try {
            EmailValidationResponse response = restTemplate.getForObject(uri, EmailValidationResponse.class);
            
            if (response == null) {
                throw new EmailValidationServiceException(
                        "Received an empty response from the email validation service.");
            }

            log.debug("Email validation response for {}: deliverable={}, qualityScore={}",
                    email, response.isDeliverable(), response);

            if (!response.isDeliverable()) {
                throw new InvalidEmailException(email);
            }

        } catch (InvalidEmailException | EmailValidationServiceException ex) {
            // Re-throw our own exceptions
            throw ex;
        } catch (ResourceAccessException ex) {
            log.error("Email validation service is unreachable: {}", ex.getMessage());
            throw new EmailValidationServiceException(
                    "Email validation service is unreachable. Please try again later.", ex);
        } catch (HttpClientErrorException ex) {
            log.error("Email validation API returned an error: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new EmailValidationServiceException(
                    "Email validation service returned an error: " + ex.getStatusCode(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error during email validation: {}", ex.getMessage(), ex);
            throw new EmailValidationServiceException(
                    "Unexpected error while validating email. Please try again later.", ex);
        }
    }
}
