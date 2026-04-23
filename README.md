# Email Subscription Management API

A Java Spring Boot REST API for managing email subscriptions. Before saving, every email address is validated against the [Abstract Email Validation API](https://www.abstractapi.com/) to ensure it is well-formed and has resolvable MX records.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
  - [1. Clone the repository](#1-clone-the-repository)
  - [2. Configure secrets](#2-configure-secrets)
  - [3. Run with Docker Compose](#3-run-with-docker-compose)
  - [4. Run locally (without Docker)](#4-run-locally-without-docker)
- [API Reference](#api-reference)
- [curl Examples](#curl-examples)
- [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [Assumptions & Trade-offs](#assumptions--trade-offs)

---

## Tech Stack

| Concern            | Choice                                   |
|--------------------|------------------------------------------|
| Language / Runtime | Java 17                                  |
| Framework          | Spring Boot 3.2                          |
| Persistence        | Spring Data JPA + H2          |
| Email Validation   | Abstract Email Validation API (free tier)|
| Build Tool         | Maven                                    |
| Containerization   | Docker + Docker Compose                  |
| Testing            | JUnit 5, Mockito, Spring MockMvc         |

---

## Prerequisites

- **Docker** and **Docker Compose** (for the recommended path)
- **Java 17** and **Maven 3.9+** (for running locally)
- A free **Abstract API** key — sign up at [abstractapi.com](https://www.abstractapi.com/) to obtain one (the free tier provides 100 validations/month)

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/juned73/email-subscription-api.git
cd email-subscription-api
```

### 2. Configure secrets

Copy the example environment file and fill in your values:

```bash
cp .env.example .env
```

Open `.env` and set your Abstract API key:

```env
EMAIL_VALIDATION_API_KEY=your_abstract_api_key_here
```

> **Important:** Never commit `.env` to version control — it is already in `.gitignore`.

### 3. Run with Docker Compose

```bash
docker compose up --build
```

This starts:
- **`app`** — Spring Boot API on port `8080`

The application will be available at `http://localhost:8080`.

To stop:

```bash
docker compose down
```



### 4. Run locally (without Docker)

```bash
mvn clean package -DskipTests
java -jar target/email-subscription-api-1.0.0.jar
```

---

## API Reference

### Base URL

```
http://localhost:8080/api/subscriptions
```

### Endpoints

| Method   | Path                        | Description                   |
|----------|-----------------------------|-------------------------------|
| `POST`   | `/api/subscriptions`        | Create a new subscription     |
| `GET`    | `/api/subscriptions`        | List all subscriptions        |
| `GET`    | `/api/subscriptions/{id}`   | Get a subscription by ID      |
| `DELETE` | `/api/subscriptions/{id}`   | Delete a subscription by ID   |

### Request / Response Examples

#### POST `/api/subscriptions`

**Request body:**
```json
{
  "email": "john.doe@gmail.com",
  "subscriberName": "John Doe"
}
```

**Success `201 Created`:**
```json
{
  "success": true,
  "message": "Subscription created successfully.",
  "data": {
    "id": 1,
    "email": "john.doe@gmail.com",
    "subscriberName": "John Doe",
    "status": "ACTIVE",
    "createdAt": "2024-11-01T10:00:00"
  }
}
```

### Error Responses

| Scenario                        | HTTP Status                  |
|---------------------------------|------------------------------|
| Missing or malformed fields     | `400 Bad Request`            |
| Email fails external validation | `422 Unprocessable Entity`   |
| Email already exists            | `409 Conflict`               |
| Subscription ID not found       | `404 Not Found`              |
| Validation service unavailable  | `503 Service Unavailable`    |

---

## curl Examples

See [`docs/curl-examples.md`](docs/curl-examples.md) for the full set of examples covering all endpoints and error cases.

---

## Running Tests

Tests use JUnit 5, Mockito, and Spring MockMvc. The service layer is unit-tested in isolation; the controller layer uses `@WebMvcTest` with mocked service dependencies. No external API calls are made during tests.

```bash
mvn test
```

---

## Project Structure

```
src/main/java/com/example/subscription/
├── EmailSubscriptionApiApplication.java   # Entry point
├── config/
│   └── AppConfig.java                     # RestTemplate bean with timeouts
├── controller/
│   └── SubscriptionController.java        # REST endpoints
├── dto/
│   ├── ApiResponse.java                   # Uniform response envelope
│   ├── CreateSubscriptionRequest.java     # Input DTO with validation
│   ├── EmailValidationResponse.java       # Maps Abstract API response
│   └── SubscriptionResponse.java          # Output DTO
├── exception/
│   ├── DuplicateEmailException.java
│   ├── EmailValidationServiceException.java
│   ├── GlobalExceptionHandler.java        # Centralised error handling
│   ├── InvalidEmailException.java
│   └── SubscriptionNotFoundException.java
├── model/
│   └── EmailSubscription.java             # JPA entity
├── repository/
│   └── EmailSubscriptionRepository.java   # Spring Data JPA
└── service/
    ├── EmailValidationService.java        # Calls Abstract API
    └── SubscriptionService.java           # Business logic
```

---

## Assumptions & Trade-offs

**Email validation strategy**

The Abstract Email Validation API was chosen because it offers a generous free tier and returns structured data (`is_valid_format`, `is_mx_found`, `is_smtp_valid`). The service considers an email valid when both the format is correct **and** MX records are found — this catches disposable domains and obvious typos while avoiding false rejections on SMTP-level checks, which can be unreliable.

**Duplicate check before external call**

The duplicate email check runs against the database *before* the external API call. This avoids an unnecessary paid/rate-limited HTTP request for a request we already know will be rejected.

**Email normalisation**

Emails are lowercased and trimmed on ingestion so that `User@EXAMPLE.COM` and `user@example.com` are treated as the same address.

**External API failure behaviour**

If the validation service is unreachable or returns an unexpected error, the API responds with `503 Service Unavailable`. This is a conservative choice — silently allowing unvalidated emails would undermine the feature's purpose.


**No authentication**

The API has no authentication layer. For a production deployment, adding Spring Security with API key or JWT-based auth would be a natural next step.

**Pagination**

`GET /api/subscriptions` returns all records without pagination. For a production workload with large datasets, cursor- or page-based pagination would be added.
