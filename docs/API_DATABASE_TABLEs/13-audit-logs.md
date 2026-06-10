# Audit Logs Table

## Table: `audit_logs`

Stores comprehensive audit trail for all system activities. Essential for compliance, debugging, and security monitoring.

---

## Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| **User Info** |
| `user_id` | `bigint` | NOT NULL, INDEX | Acting user ID |
| `user_email` | `varchar(255)` | | User email |
| `user_role` | `varchar(50)` | | User role at action time |
| `owner_id` | `bigint` | NOT NULL, INDEX | Owner context |
| **Action Details** |
| `action` | `varchar(20)` | NOT NULL | Action type |
| `entity_type` | `varchar(50)` | NOT NULL, INDEX | Entity type |
| `entity_id` | `bigint` | INDEX | Entity ID |
| `entity_name` | `varchar(255)` | | Entity identifier |
| `description` | `text` | | Action description |
| **Change Data** |
| `old_value` | `jsonb` | | Previous state (JSON) |
| `new_value` | `jsonb` | | New state (JSON) |
| `changes` | `jsonb` | | Changed fields (JSON) |
| **Request Info** |
| `ip_address` | `varchar(50)` | | Client IP address |
| `user_agent` | `varchar(500)` | | Client user agent |
| `request_path` | `varchar(500)` | | API endpoint |
| `request_method` | `varchar(10)` | | HTTP method |
| `response_code` | `int` | | HTTP response code |
| `duration_ms` | `bigint` | | Request duration (ms) |
| **Timestamp** |
| `created_at` | `timestamp` | INDEX | Action timestamp |

---

## Audit Actions

```go
const (
    AuditActionCreate AuditAction = "create"
    AuditActionUpdate AuditAction = "update"
    AuditActionDelete AuditAction = "delete"
    AuditActionLogin  AuditAction = "login"
    AuditActionLogout AuditAction = "logout"
    AuditActionView   AuditAction = "view"
    AuditActionExport AuditAction = "export"
)
```

---

## Entity Types

| Entity Type | Description |
|-------------|-------------|
| `user` | User accounts |
| `vehicle` | Vehicles |
| `driver` | Drivers |
| `trip` | Trips |
| `trip_cost` | Trip costs |
| `maintenance_cost` | Maintenance costs |
| `driver_cost` | Driver costs |
| `document` | Documents |
| `customer` | Customers |
| `payment` | Payments |
| `geofence` | Geofences |
| `report` | Reports |

---

## Indexes

```sql
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_owner_id ON audit_logs(owner_id);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_entity_id ON audit_logs(entity_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);

-- Composite index for common queries
CREATE INDEX idx_audit_logs_owner_entity_date 
ON audit_logs(owner_id, entity_type, created_at DESC);
```

---

## Sample Data

### User Login
```json
{
  "id": 1,
  "user_id": 1,
  "user_email": "owner@company.com",
  "user_role": "owner",
  "owner_id": 1,
  "action": "login",
  "entity_type": "user",
  "entity_id": 1,
  "entity_name": "owner@company.com",
  "description": "User logged in successfully",
  "ip_address": "103.25.12.45",
  "user_agent": "Mozilla/5.0 (Android 14; Mobile)",
  "request_path": "/api/v2/auth/login",
  "request_method": "POST",
  "response_code": 200,
  "duration_ms": 125,
  "created_at": "2026-01-15T09:00:00Z"
}
```

### Trip State Change
```json
{
  "id": 2,
  "user_id": 2,
  "user_email": "manager@company.com",
  "user_role": "manager",
  "owner_id": 1,
  "action": "update",
  "entity_type": "trip",
  "entity_id": 5,
  "entity_name": "Trip #5 - MH12AB1234",
  "description": "Trip state changed from 'planned' to 'on_route'",
  "old_value": {"state": "planned"},
  "new_value": {"state": "on_route"},
  "changes": {"state": {"from": "planned", "to": "on_route"}},
  "request_path": "/api/v2/trips/5/state",
  "request_method": "PATCH",
  "response_code": 200,
  "duration_ms": 89,
  "created_at": "2026-01-15T10:30:00Z"
}
```

### Vehicle Update
```json
{
  "id": 3,
  "user_id": 2,
  "user_email": "manager@company.com",
  "user_role": "manager",
  "owner_id": 1,
  "action": "update",
  "entity_type": "vehicle",
  "entity_id": 1,
  "entity_name": "MH12AB1234",
  "description": "Vehicle updated",
  "old_value": {"mileage": 45000, "caretaker_id": null},
  "new_value": {"mileage": 45250, "caretaker_id": 3},
  "changes": {
    "mileage": {"from": 45000, "to": 45250},
    "caretaker_id": {"from": null, "to": 3}
  },
  "request_path": "/api/v2/vehicles/1",
  "request_method": "PUT",
  "response_code": 200,
  "duration_ms": 156,
  "created_at": "2026-01-15T11:00:00Z"
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/v2/audit-logs` | List audit logs | Owner, GM |
| GET | `/api/v2/audit-logs/:id` | Get log details | Owner, GM |
| GET | `/api/v2/audit-logs/entity/:type/:id` | Get entity history | Owner, GM |
| GET | `/api/v2/audit-logs/user/:id` | Get user activity | Owner |
| GET | `/api/v2/audit-logs/summary` | Get summary | Owner, GM |

---

## Query Parameters

```go
type AuditLogQuery struct {
    UserID     *uint       `form:"user_id"`
    Action     AuditAction `form:"action"`
    EntityType string      `form:"entity_type"`
    EntityID   *uint       `form:"entity_id"`
    StartDate  string      `form:"start_date"`  // YYYY-MM-DD
    EndDate    string      `form:"end_date"`    // YYYY-MM-DD
    Page       int         `form:"page"`
    PerPage    int         `form:"per_page"`
}
```

---

## Audit Log Summary

```go
type AuditLogSummary struct {
    TotalActions     int            `json:"total_actions"`
    ActionsByType    map[string]int `json:"actions_by_type"`
    ActionsByEntity  map[string]int `json:"actions_by_entity"`
    MostActiveUsers  []UserActivity `json:"most_active_users"`
    RecentActivities []AuditLog     `json:"recent_activities"`
}

type UserActivity struct {
    UserID      uint   `json:"user_id"`
    UserEmail   string `json:"user_email"`
    ActionCount int    `json:"action_count"`
}
```

---

## Entity History View

Get complete history of an entity:

```sql
SELECT * FROM audit_logs
WHERE entity_type = 'vehicle'
  AND entity_id = 1
ORDER BY created_at DESC
LIMIT 50;
```

### Response
```json
{
  "entity_type": "vehicle",
  "entity_id": 1,
  "entity_name": "MH12AB1234",
  "history": [
    {
      "action": "update",
      "description": "Mileage updated",
      "user": "manager@company.com",
      "timestamp": "2026-01-15T11:00:00Z",
      "changes": {"mileage": {"from": 45000, "to": 45250}}
    },
    {
      "action": "update",
      "description": "State changed to on_route",
      "user": "owner@company.com",
      "timestamp": "2026-01-15T09:30:00Z",
      "changes": {"state": {"from": "active", "to": "on_route"}}
    },
    {
      "action": "create",
      "description": "Vehicle registered",
      "user": "owner@company.com",
      "timestamp": "2026-01-01T10:00:00Z"
    }
  ]
}
```

---

## Creating Audit Logs

```go
type CreateAuditLogInput struct {
    UserID        uint
    UserEmail     string
    UserRole      string
    OwnerID       uint
    Action        AuditAction
    EntityType    string
    EntityID      uint
    EntityName    string
    Description   string
    OldValue      interface{}
    NewValue      interface{}
    IPAddress     string
    UserAgent     string
    RequestPath   string
    RequestMethod string
    ResponseCode  int
    Duration      int64
}

// Helper function
func LogAudit(c *gin.Context, action AuditAction, entityType string, entityID uint, description string, oldValue, newValue interface{}) {
    // Extract request info from context
    // Create audit log entry
}
```

---

## Retention Policy

| Data Age | Storage | Access |
|----------|---------|--------|
| 0-30 days | Hot (Primary DB) | Fast queries |
| 30-90 days | Warm (Archive table) | Slower queries |
| 90+ days | Cold (GCS Archive) | Manual retrieval |

### Archive Query
```sql
-- Move old logs to archive
INSERT INTO audit_logs_archive
SELECT * FROM audit_logs
WHERE created_at < NOW() - INTERVAL '90 days';

DELETE FROM audit_logs
WHERE created_at < NOW() - INTERVAL '90 days';
```

---

## Security & Compliance

1. **Tamper-proof**: Audit logs cannot be modified or deleted via API
2. **Complete Trail**: All CRUD operations logged automatically
3. **User Attribution**: Every action tied to authenticated user
4. **IP Tracking**: Client IP recorded for security
5. **Response Codes**: Track failed attempts (401, 403, etc.)

---

## Notes

1. **No Soft Delete**: Audit logs are permanent records
2. **Auto-capture**: Middleware automatically logs API requests
3. **Performance**: Use background job for non-blocking logging
4. **Storage**: Plan for high volume (millions of records)
5. **Export**: Support CSV/JSON export for compliance

---

*Last Updated: January 2026*

