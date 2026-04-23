package com.example.subscription;

import com.example.subscription.dto.CreateSubscriptionRequest;
import com.example.subscription.exception.DuplicateEmailException;
import com.example.subscription.exception.InvalidEmailException;
import com.example.subscription.exception.SubscriptionNotFoundException;
import com.example.subscription.dto.SubscriptionResponse;
import com.example.subscription.service.SubscriptionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SubscriptionService subscriptionService;

    private static final String BASE_URL = "/api/subscriptions";

    @Test
    @DisplayName("POST /api/subscriptions - should create subscription with valid email")
    void createSubscription_validEmail_returns201() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest("user@example.com", "John Doe");
        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(1L).email("user@example.com").subscriberName("John Doe")
                .status("ACTIVE").createdAt(LocalDateTime.now()).build();

        when(subscriptionService.create(any())).thenReturn(response);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("user@example.com"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("POST /api/subscriptions - should return 422 for invalid email")
    void createSubscription_invalidEmail_returns422() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest("bad@fake.xyz", "John Doe");

        when(subscriptionService.create(any())).thenThrow(new InvalidEmailException("bad@fake.xyz"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/subscriptions - should return 409 for duplicate email")
    void createSubscription_duplicateEmail_returns409() throws Exception {
        CreateSubscriptionRequest request = new CreateSubscriptionRequest("existing@example.com", "Jane Doe");

        when(subscriptionService.create(any())).thenThrow(new DuplicateEmailException("existing@example.com"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/subscriptions - should return 400 when fields are missing")
    void createSubscription_missingFields_returns400() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/subscriptions - should return all subscriptions")
    void getAllSubscriptions_returnsList() throws Exception {
        List<SubscriptionResponse> list = List.of(
                SubscriptionResponse.builder().id(1L).email("a@example.com").subscriberName("Alice")
                        .status("ACTIVE").createdAt(LocalDateTime.now()).build(),
                SubscriptionResponse.builder().id(2L).email("b@example.com").subscriberName("Bob")
                        .status("ACTIVE").createdAt(LocalDateTime.now()).build()
        );

        when(subscriptionService.findAll()).thenReturn(list);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id} - should return subscription by id")
    void getById_existingId_returnsSubscription() throws Exception {
        SubscriptionResponse response = SubscriptionResponse.builder()
                .id(1L).email("a@example.com").subscriberName("Alice")
                .status("ACTIVE").createdAt(LocalDateTime.now()).build();

        when(subscriptionService.findById(1L)).thenReturn(response);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id} - should return 404 for non-existing id")
    void getById_nonExistingId_returns404() throws Exception {
        when(subscriptionService.findById(99L)).thenThrow(new SubscriptionNotFoundException(99L));

        mockMvc.perform(get(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("DELETE /api/subscriptions/{id} - should delete existing subscription")
    void delete_existingId_returns200() throws Exception {
        doNothing().when(subscriptionService).delete(1L);

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/subscriptions/{id} - should return 404 for non-existing id")
    void delete_nonExistingId_returns404() throws Exception {
        doThrow(new SubscriptionNotFoundException(99L)).when(subscriptionService).delete(99L);

        mockMvc.perform(delete(BASE_URL + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
