package com.example.subscription;

import com.example.subscription.dto.CreateSubscriptionRequest;
import com.example.subscription.dto.SubscriptionResponse;
import com.example.subscription.exception.DuplicateEmailException;
import com.example.subscription.exception.SubscriptionNotFoundException;
import com.example.subscription.model.EmailSubscription;
import com.example.subscription.repository.EmailSubscriptionRepository;
import com.example.subscription.service.EmailValidationService;
import com.example.subscription.service.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private EmailSubscriptionRepository repository;

    @Mock
    private EmailValidationService emailValidationService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private EmailSubscription sampleSubscription;

    @BeforeEach
    void setUp() {
        sampleSubscription = EmailSubscription.builder()
                .id(1L)
                .email("user@example.com")
                .subscriberName("John Doe")
                .status(EmailSubscription.SubscriptionStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("create - should save and return subscription when email is valid and unique")
    void create_validAndUnique_savesSubscription() {
        when(repository.existsByEmail(anyString())).thenReturn(false);
        doNothing().when(emailValidationService).validateEmail(anyString());
        when(repository.save(any())).thenReturn(sampleSubscription);

        CreateSubscriptionRequest request = new CreateSubscriptionRequest("user@example.com", "John Doe");
        SubscriptionResponse result = subscriptionService.create(request);

        assertThat(result.getEmail()).isEqualTo("user@example.com");
        assertThat(result.getSubscriberName()).isEqualTo("John Doe");
        verify(repository).save(any(EmailSubscription.class));
    }

    @Test
    @DisplayName("create - should throw DuplicateEmailException for existing email")
    void create_duplicateEmail_throwsException() {
        when(repository.existsByEmail(anyString())).thenReturn(true);

        CreateSubscriptionRequest request = new CreateSubscriptionRequest("user@example.com", "John Doe");

        assertThatThrownBy(() -> subscriptionService.create(request))
                .isInstanceOf(DuplicateEmailException.class);

        verifyNoInteractions(emailValidationService);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("create - should normalize email to lowercase")
    void create_normalizesEmailToLowercase() {
        when(repository.existsByEmail("user@example.com")).thenReturn(false);
        doNothing().when(emailValidationService).validateEmail("user@example.com");
        when(repository.save(any())).thenReturn(sampleSubscription);

        CreateSubscriptionRequest request = new CreateSubscriptionRequest("USER@EXAMPLE.COM", "John Doe");
        subscriptionService.create(request);

        verify(emailValidationService).validateEmail("user@example.com");
    }

    @Test
    @DisplayName("findAll - should return all subscriptions")
    void findAll_returnsAllSubscriptions() {
        when(repository.findAll()).thenReturn(List.of(sampleSubscription));

        List<SubscriptionResponse> results = subscriptionService.findAll();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEmail()).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("findById - should return subscription for valid id")
    void findById_validId_returnsSubscription() {
        when(repository.findById(1L)).thenReturn(Optional.of(sampleSubscription));

        SubscriptionResponse result = subscriptionService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById - should throw SubscriptionNotFoundException for unknown id")
    void findById_unknownId_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.findById(99L))
                .isInstanceOf(SubscriptionNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("delete - should delete subscription for valid id")
    void delete_validId_deletesSubscription() {
        when(repository.existsById(1L)).thenReturn(true);

        subscriptionService.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("delete - should throw SubscriptionNotFoundException for unknown id")
    void delete_unknownId_throwsException() {
        when(repository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> subscriptionService.delete(99L))
                .isInstanceOf(SubscriptionNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }
}
