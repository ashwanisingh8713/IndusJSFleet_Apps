# Fleet Management API v2 - Documentation

Complete API documentation for Fleet Management System with 50K+ vehicle support.

## Base URLs

| Environment | URL |
|-------------|-----|
| Production | `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2` |
| Local | `http://localhost:8080/api/v2` |

## API Versioning (Hybrid)
- **URL Path**: `/api/v2/...` (primary)
- **Header**: `X-API-Version: v2`
- **Query**: `?api_version=v2`

## Date & Time Formats (IMPORTANT)
- **Date**: `DD-MM-YYYY` format ONLY (e.g., 31-12-2025)
- **Time**: `HH:MM` 24-hour format (e.g., 14:30)
- **DateTime (ISO)**: `2025-12-31T14:30:00Z` for timestamp fields

---

## API Modules

| Module | File | Description |
|--------|------|-------------|
| 00 | [00-api-info.md](00-api-info.md) | API info & versioning |
| 01 | [01-authentication.md](01-authentication.md) | Sign up, login, password reset |
| 02 | [02-profile.md](02-profile.md) | User profile management |
| 03 | [03-team-management.md](03-team-management.md) | Team member CRUD |
| 03b | [03b-caretaker-management.md](03b-caretaker-management.md) | Caretaker assignments |
| 04 | [04-drivers.md](04-drivers.md) | Driver management |
| 05 | [05-vehicles.md](05-vehicles.md) | Vehicle management |
| 06 | [06-documents.md](06-documents.md) | Document upload & tracking |
| 07 | [07-trips.md](07-trips.md) | Trip management |
| 08 | [08-trip-costs.md](08-trip-costs.md) | Trip cost tracking |
| 09 | [09-maintenance-costs.md](09-maintenance-costs.md) | Vehicle maintenance costs |
| 10 | [10-dashboard.md](10-dashboard.md) | Dashboard & overview |
| 11 | [11-reports.md](11-reports.md) | Reports & P&L |
| 12 | [12-location-tracking.md](12-location-tracking.md) | GPS location tracking |
| 13 | [13-health-metrics.md](13-health-metrics.md) | Health & metrics |

---

## User Roles & Hierarchy

```
Owner (Full Access)
  └── General Manager (Financial & Operational)
        └── Manager (Operational)
              └── Supervisor (Limited)
```

### Role Permission Summary

| Feature | Owner | GM | Manager | Supervisor |
|---------|-------|----|---------|------------|
| Create Team Members | ✅ | ✅ (M/S) | ❌ | ❌ |
| Create Vehicle | ✅ | ✅ | ✅ | ❌ |
| Create Driver | ✅ | ✅ | ✅ | ❌ |
| Create Trip | ✅ | ✅ | ✅ | ❌ |
| Edit Vehicle/Driver | ✅ | ✅ | ✅ | ❌ |
| View trip_price | ✅ | ✅ | ❌ | ❌ |
| View P&L Reports | ✅ | ✅ | ❌ | ❌ |
| Add Costs | ✅ | ✅ | ✅ | ✅ |
| Delete Costs | ✅ | ✅ | ✅ | ❌ |
| Assign Caretakers | ✅ | ✅ | ❌ | ❌ |

---

## Authentication

All authenticated endpoints require:
```
Authorization: Bearer {{token}}
```

Token is obtained from `/auth/login` response.

---

## Response Format

### Success Response
```json
{
    "success": true,
    "message": "Operation successful",
    "data": { ... }
}
```

### Error Response
```json
{
    "success": false,
    "message": "Error message",
    "error": "Detailed error description"
}
```

### Paginated Response
```json
{
    "success": true,
    "message": "Items retrieved",
    "data": [ ... ],
    "pagination": {
        "page": 1,
        "per_page": 20,
        "total": 100,
        "total_pages": 5
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

---

## Postman Collection

The original Postman collection is available at:
`Fleet_Management_API_v2.postman_collection.json` (root directory)

### Collection Variables
| Variable | Description |
|----------|-------------|
| `base_url` | API base URL with version |
| `api_base` | API base URL without version |
| `token` | JWT token (auto-set on login) |
| `vehicle_id` | Current vehicle ID |
| `driver_id` | Current driver ID |
| `trip_id` | Current trip ID |

---

## Tech Stack

- **Backend**: Go 1.21+ with Gin Framework
- **Database**: PostgreSQL 14+ with GORM
- **Authentication**: JWT
- **Real-time**: MQTT (EMQX) for GPS, WebSocket
- **Deployment**: Google Cloud Run

---

## Support

For API support or issues, refer to the main [README.md](../../README.md) or documentation in `/doc_prompts/`.

