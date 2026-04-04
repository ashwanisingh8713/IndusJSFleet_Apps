# 16. Audit Logs

System audit log endpoints for tracking entity changes and user actions.

## Overview

Audit logs provide a complete trail of all create, update, and delete operations performed across the system. Useful for compliance, debugging, and accountability.

## Role Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| View Audit Logs | ✅ | ✅ | ❌ | ❌ |

---

## Endpoints

### Get Audit Logs

```http
GET {{base_url}}/audit-logs
Authorization: Bearer {{token}}
```

**Query Parameters:**

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `page` | integer | Page number (default: 1) | `?page=1` |
| `per_page` | integer | Items per page (default: 20, max: 100) | `?per_page=50` |
| `entity_type` | string | Filter by entity type | `?entity_type=vehicle` |
| `action` | string | Filter by action performed | `?action=create` |

**Entity Types:**

| Value | Description |
|-------|-------------|
| `vehicle` | Vehicle CRUD operations |
| `driver` | Driver CRUD operations |
| `trip` | Trip CRUD and state changes |
| `trip_cost` | Trip cost operations |
| `maintenance_cost` | Maintenance cost operations |
| `driver_cost` | Driver cost operations |
| `customer` | Customer CRUD operations |
| `trip_payment` | Trip payment operations |
| `team_member` | Team member management |
| `document` | Document upload/delete |
| `vehicle_purchase` | Vehicle purchase operations |
| `loan_payment` | Loan/EMI payment operations |

**Action Types:**

| Value | Description |
|-------|-------------|
| `create` | New record created |
| `update` | Record updated |
| `delete` | Record deleted |
| `state_change` | Entity state transition |
| `toggle_active` | Active status toggled |

**Response:**
```json
{
    "success": true,
    "message": "Audit logs retrieved",
    "data": {
        "items": [
            {
                "id": 1,
                "entity_type": "trip",
                "entity_id": 42,
                "action": "state_change",
                "changes": {
                    "state": {
                        "old": "planned",
                        "new": "on_route"
                    }
                },
                "performed_by": {
                    "id": 1,
                    "first_name": "John",
                    "last_name": "Doe",
                    "role": "owner"
                },
                "ip_address": "203.0.113.50",
                "created_at": "2026-03-15T10:30:00Z"
            },
            {
                "id": 2,
                "entity_type": "vehicle",
                "entity_id": 5,
                "action": "update",
                "changes": {
                    "make": {
                        "old": "Tata",
                        "new": "Tata Motors"
                    },
                    "capacity": {
                        "old": 1000,
                        "new": 1200
                    }
                },
                "performed_by": {
                    "id": 2,
                    "first_name": "Jane",
                    "last_name": "Manager",
                    "role": "general_manager"
                },
                "ip_address": "203.0.113.51",
                "created_at": "2026-03-15T09:15:00Z"
            }
        ],
        "count": 2,
        "page": 1,
        "per_page": 20,
        "total_pages": 1,
        "has_more": false
    }
}
```

---

## Usage Examples

### Get All Recent Logs
```http
GET {{base_url}}/audit-logs?page=1&per_page=50
```

### Get Vehicle Changes Only
```http
GET {{base_url}}/audit-logs?entity_type=vehicle
```

### Get All Create Actions
```http
GET {{base_url}}/audit-logs?action=create
```

### Get Trip State Changes
```http
GET {{base_url}}/audit-logs?entity_type=trip&action=state_change
```

---

## Error Responses

### Access Denied (403)
```json
{
    "success": false,
    "message": "Access denied",
    "error": "Audit logs are only available to Owner and General Manager"
}
```

---

*Last Updated: April 2026*

