# curl Examples

All examples assume the API is running at `http://localhost:8080`.

---

## Create Subscription — Valid Email

```bash
curl -s -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"email": "john.doe@gmail.com", "subscriberName": "John Doe"}' | jq .
```

**Expected:** `201 Created`

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
  },
  "timestamp": "2024-11-01T10:00:00"
}
```

---

## Create Subscription — Invalid Email

```bash
curl -s -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"email": "notreal@fakexyz12345.com", "subscriberName": "Ghost User"}' | jq .
```

**Expected:** `422 Unprocessable Entity`

```json
{
  "success": false,
  "message": "The email address 'notreal@fakexyz12345.com' failed external validation. Please provide a valid, deliverable email address.",
  "timestamp": "2024-11-01T10:00:01"
}
```

---

## Create Subscription — Duplicate Email

```bash
# First, create the subscription (should succeed)
curl -s -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"email": "jane.doe@gmail.com", "subscriberName": "Jane Doe"}' | jq .

# Then try to create the same email again (should fail)
curl -s -X POST http://localhost:8080/api/subscriptions \
  -H "Content-Type: application/json" \
  -d '{"email": "jane.doe@gmail.com", "subscriberName": "Jane Duplicate"}' | jq .
```

**Expected on second request:** `409 Conflict`

```json
{
  "success": false,
  "message": "A subscription already exists for email: jane.doe@gmail.com",
  "timestamp": "2024-11-01T10:00:02"
}
```

---

## Get All Subscriptions

```bash
curl -s http://localhost:8080/api/subscriptions | jq .
```

**Expected:** `200 OK`

```json
{
  "success": true,
  "message": "Subscriptions retrieved successfully.",
  "data": [
    {
      "id": 1,
      "email": "john.doe@gmail.com",
      "subscriberName": "John Doe",
      "status": "ACTIVE",
      "createdAt": "2024-11-01T10:00:00"
    }
  ],
  "timestamp": "2024-11-01T10:00:03"
}
```

---

## Get Subscription by ID — Existing

```bash
curl -s http://localhost:8080/api/subscriptions/1 | jq .
```

**Expected:** `200 OK`

```json
{
  "success": true,
  "message": "Subscription retrieved successfully.",
  "data": {
    "id": 1,
    "email": "john.doe@gmail.com",
    "subscriberName": "John Doe",
    "status": "ACTIVE",
    "createdAt": "2024-11-01T10:00:00"
  },
  "timestamp": "2024-11-01T10:00:04"
}
```

---

## Get Subscription by ID — Non-Existing

```bash
curl -s http://localhost:8080/api/subscriptions/9999 | jq .
```

**Expected:** `404 Not Found`

```json
{
  "success": false,
  "message": "Subscription not found with id: 9999",
  "timestamp": "2024-11-01T10:00:05"
}
```

---

## Delete Subscription — Existing

```bash
curl -s -X DELETE http://localhost:8080/api/subscriptions/1 | jq .
```

**Expected:** `200 OK`

```json
{
  "success": true,
  "message": "Subscription deleted successfully.",
  "timestamp": "2024-11-01T10:00:06"
}
```

---

## Delete Subscription — Non-Existing

```bash
curl -s -X DELETE http://localhost:8080/api/subscriptions/9999 | jq .
```

**Expected:** `404 Not Found`

```json
{
  "success": false,
  "message": "Subscription not found with id: 9999",
  "timestamp": "2024-11-01T10:00:07"
}
```
