# Notifications & Notification Preferences Tables

## Table: `notifications`

Stores notification records for users including document expiry, trip updates, payment alerts, and system messages.

---

## Schema - Notifications

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `user_id` | `bigint` | NOT NULL, INDEX | Target user |
| `owner_id` | `bigint` | NOT NULL, INDEX | Owner context |
| **Content** |
| `type` | `varchar(50)` | NOT NULL | Notification type |
| `priority` | `varchar(20)` | NOT NULL, DEFAULT 'info' | Priority level |
| `title` | `varchar(255)` | NOT NULL | Notification title |
| `body` | `text` | | Notification body |
| `data` | `jsonb` | | Additional data (JSON) |
| **Entity Reference** |
| `entity_type` | `varchar(50)` | | Related entity type |
| `entity_id` | `bigint` | INDEX | Related entity ID |
| **Status** |
| `is_read` | `boolean` | DEFAULT false | Read status |
| `read_at` | `timestamp` | | Read timestamp |
| `is_sent` | `boolean` | DEFAULT false | Push sent status |
| `sent_at` | `timestamp` | | Push sent timestamp |
| **Scheduling** |
| `scheduled_for` | `timestamp` | | Scheduled send time |
| `expires_at` | `timestamp` | | Expiry timestamp |
| **Timestamps** |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |
| `deleted_at` | `timestamp` | INDEX | Soft delete timestamp |

---

## Notification Types

```go
const (
    NotificationTypeDocumentExpiry  NotificationType = "document_expiry"
    NotificationTypeLicenseExpiry   NotificationType = "license_expiry"
    NotificationTypeMaintenance     NotificationType = "maintenance"
    NotificationTypePaymentPending  NotificationType = "payment_pending"
    NotificationTypePaymentReceived NotificationType = "payment_received"
    NotificationTypeTripStarted     NotificationType = "trip_started"
    NotificationTypeTripCompleted   NotificationType = "trip_completed"
    NotificationTypeGeofenceEntry   NotificationType = "geofence_entry"
    NotificationTypeGeofenceExit    NotificationType = "geofence_exit"
    NotificationTypeSpeedViolation  NotificationType = "speed_violation"
    NotificationTypeDriverBehavior  NotificationType = "driver_behavior"
    NotificationTypeSystem          NotificationType = "system"
)
```

---

## Priority Levels

```go
const (
    NotificationPriorityCritical NotificationPriority = "critical"
    NotificationPriorityWarning  NotificationPriority = "warning"
    NotificationPriorityInfo     NotificationPriority = "info"
)
```

---

## Table: `notification_preferences`

Stores user preferences for different notification types.

### Schema

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | `bigint` | PRIMARY KEY | Unique identifier |
| `user_id` | `bigint` | NOT NULL | User ID |
| `type` | `varchar(50)` | NOT NULL | Notification type |
| `push_enabled` | `boolean` | DEFAULT true | Push notification |
| `email_enabled` | `boolean` | DEFAULT false | Email notification |
| `sms_enabled` | `boolean` | DEFAULT false | SMS notification |
| `days_before` | `int` | DEFAULT 7 | Days before expiry to alert |
| `created_at` | `timestamp` | | Creation timestamp |
| `updated_at` | `timestamp` | | Last update timestamp |

**Unique Constraint**: `(user_id, type)`

---

## Indexes

```sql
-- Notifications
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_owner_id ON notifications(owner_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_deleted_at ON notifications(deleted_at);

-- Notification Preferences
CREATE UNIQUE INDEX idx_notification_prefs_user_type 
ON notification_preferences(user_id, type);
```

---

## Sample Data

### Document Expiry Notification
```json
{
  "id": 1,
  "user_id": 1,
  "owner_id": 1,
  "type": "document_expiry",
  "priority": "warning",
  "title": "Insurance Expiring Soon",
  "body": "Vehicle MH12AB1234 insurance expires in 7 days",
  "data": {
    "vehicle_id": 1,
    "vehicle_registration": "MH12AB1234",
    "document_type": "insurance",
    "expiry_date": "2026-01-22",
    "days_remaining": 7
  },
  "entity_type": "document",
  "entity_id": 5,
  "is_read": false,
  "is_sent": true,
  "sent_at": "2026-01-15T09:00:00Z",
  "created_at": "2026-01-15T09:00:00Z"
}
```

### Trip Completed Notification
```json
{
  "id": 2,
  "user_id": 1,
  "owner_id": 1,
  "type": "trip_completed",
  "priority": "info",
  "title": "Trip Completed",
  "body": "Trip #5 Mumbai → Pune completed successfully",
  "data": {
    "trip_id": 5,
    "vehicle_registration": "MH12AB1234",
    "driver_name": "Ramesh Singh",
    "distance": 150.5,
    "duration_minutes": 240
  },
  "entity_type": "trip",
  "entity_id": 5,
  "is_read": true,
  "read_at": "2026-01-15T18:00:00Z",
  "created_at": "2026-01-15T17:30:00Z"
}
```

### Geofence Alert
```json
{
  "id": 3,
  "user_id": 1,
  "owner_id": 1,
  "type": "geofence_entry",
  "priority": "info",
  "title": "Vehicle Arrived at Warehouse",
  "body": "MH12AB1234 entered Warehouse A zone",
  "data": {
    "vehicle_id": 1,
    "vehicle_registration": "MH12AB1234",
    "geofence_id": 1,
    "geofence_name": "Warehouse A",
    "event_time": "2026-01-15T10:30:00Z"
  },
  "entity_type": "geofence",
  "entity_id": 1,
  "created_at": "2026-01-15T10:30:00Z"
}
```

---

## API Endpoints

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/v2/notifications` | List notifications | All |
| GET | `/api/v2/notifications/:id` | Get notification | All |
| PATCH | `/api/v2/notifications/:id/read` | Mark as read | All |
| POST | `/api/v2/notifications/read-all` | Mark all read | All |
| DELETE | `/api/v2/notifications/:id` | Delete notification | All |
| GET | `/api/v2/notifications/summary` | Get summary | All |
| **Preferences** |
| GET | `/api/v2/notification-preferences` | Get preferences | All |
| PUT | `/api/v2/notification-preferences` | Update preferences | All |

---

## Notification Summary

```go
type NotificationSummary struct {
    TotalUnread   int `json:"total_unread"`
    CriticalCount int `json:"critical_count"`
    WarningCount  int `json:"warning_count"`
    InfoCount     int `json:"info_count"`
}
```

### Response
```json
{
  "total_unread": 5,
  "critical_count": 1,
  "warning_count": 2,
  "info_count": 2
}
```

---

## Notification Preference Input

```go
type NotificationPreferenceInput struct {
    Type         NotificationType `json:"type" binding:"required"`
    PushEnabled  *bool            `json:"push_enabled"`
    EmailEnabled *bool            `json:"email_enabled"`
    SMSEnabled   *bool            `json:"sms_enabled"`
    DaysBefore   *int             `json:"days_before"`
}
```

### Sample Preferences
```json
{
  "preferences": [
    {
      "type": "document_expiry",
      "push_enabled": true,
      "email_enabled": true,
      "sms_enabled": false,
      "days_before": 15
    },
    {
      "type": "payment_pending",
      "push_enabled": true,
      "email_enabled": true,
      "sms_enabled": true,
      "days_before": 7
    }
  ]
}
```

---

## Notification Generation

### Document Expiry Job (Daily)
```go
func GenerateDocumentExpiryNotifications() {
    users := GetAllActiveUsers()
    
    for _, user := range users {
        prefs := GetNotificationPreferences(user.ID, "document_expiry")
        if !prefs.PushEnabled && !prefs.EmailEnabled {
            continue
        }
        
        docs := GetExpiringDocuments(user.OwnerID, prefs.DaysBefore)
        for _, doc := range docs {
            CreateNotification(NotificationInput{
                UserID:     user.ID,
                Type:       NotificationTypeDocumentExpiry,
                Priority:   getPriority(doc.DaysRemaining),
                Title:      fmt.Sprintf("%s Expiring Soon", doc.DocumentType),
                Body:       fmt.Sprintf("Vehicle %s %s expires in %d days", 
                           doc.VehicleRegistration, doc.DocumentType, doc.DaysRemaining),
                EntityType: "document",
                EntityID:   doc.ID,
                Data:       doc,
            })
        }
    }
}
```

---

## Push Notification Integration

```go
// Firebase Cloud Messaging integration
type PushPayload struct {
    To           string            `json:"to"`
    Notification PushNotification  `json:"notification"`
    Data         map[string]string `json:"data"`
}

type PushNotification struct {
    Title string `json:"title"`
    Body  string `json:"body"`
    Badge int    `json:"badge"`
    Sound string `json:"sound"`
}

func SendPushNotification(userID uint, notification *Notification) error {
    // Get user's FCM token
    // Send via Firebase
}
```

---

## Notes

1. **Multi-channel**: Support push, email, and SMS notifications
2. **User Preferences**: Users control which notifications they receive
3. **Scheduled Alerts**: Document expiry alerts scheduled in advance
4. **Priority Handling**: Critical notifications shown prominently
5. **Deduplication**: Avoid sending duplicate notifications

---

*Last Updated: January 2026*

