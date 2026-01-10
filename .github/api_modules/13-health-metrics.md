# 13. Health & Metrics

API health check and metrics endpoints.

---

## Endpoints

### API Health Check
```http
GET {{base_url}}/health
```

No authentication required.

**Response:**
```json
{
    "success": true,
    "message": "API is healthy",
    "data": {
        "status": "healthy",
        "version": "v2",
        "timestamp": "2025-01-15T10:30:00Z",
        "database": "connected",
        "uptime": "5d 12h 30m"
    }
}
```

---

### API Status
```http
GET {{api_base}}/status
```

**Response:**
```json
{
    "success": true,
    "data": {
        "api_version": "v2",
        "status": "operational",
        "services": {
            "database": "up",
            "redis": "up",
            "mqtt": "up"
        }
    }
}
```

---

### Get Metrics (Admin)
```http
GET {{base_url}}/metrics
Authorization: Bearer {{token}}
```

**Response:**
```json
{
    "success": true,
    "data": {
        "requests": {
            "total": 150000,
            "today": 5000,
            "per_minute": 85
        },
        "response_times": {
            "avg_ms": 45,
            "p95_ms": 120,
            "p99_ms": 250
        },
        "errors": {
            "total": 150,
            "rate": 0.1
        }
    }
}
```

---

## Rate Limits

| Endpoint Type | Limit | Window |
|---------------|-------|--------|
| Standard APIs | 100 requests | 1 minute |
| Location APIs | 1000 requests | 1 minute |
| Auth APIs | 10 requests | 1 minute |

### Rate Limit Headers
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1705312260
```

### Rate Limit Exceeded Response
```json
{
    "success": false,
    "message": "Rate limit exceeded",
    "error": "too many requests, please try again in 45 seconds"
}
```

