# Alerts Module

## Overview

The Alerts module tracks and displays expiry warnings for vehicle documents and driver licenses. It helps fleet managers stay compliant by proactively notifying about upcoming expirations.

---

## Features

- Document expiry tracking
- License expiry tracking
- Priority-based alert levels
- Alert count on dashboard
- Quick navigation to resolve alerts
- Grouped alerts by entity

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Alerts List | `FleetRoute.AlertsList` | All active alerts |

---

## Alert Types

### Vehicle Document Alerts

| Document Type | Description |
|---------------|-------------|
| Insurance | Vehicle insurance policy |
| PUC Certificate | Pollution Under Control |
| Fitness Certificate | Vehicle fitness |
| Road Tax | Annual road tax |
| Permit | Route/cargo permit |
| Registration | Vehicle registration |

### Driver License Alerts

| Alert Type | Description |
|------------|-------------|
| License Expiry | Driving license expiration |

---

## Alert Priority Levels

| Priority | Days to Expiry | Color | Icon |
|----------|----------------|-------|------|
| Critical | < 7 days | Red | ⚠️ |
| Warning | 7-30 days | Orange | ⚡ |
| Info | 30-60 days | Blue | ℹ️ |

---

## Alerts List Screen

### Display

Alerts grouped by:
1. **Vehicle Alerts**
   - Grouped by vehicle
   - Shows document type
   - Expiry date
   - Days remaining

2. **Driver Alerts**
   - Grouped by driver
   - License expiry date
   - Days remaining

### Alert Item

| Field | Description |
|-------|-------------|
| Entity | Vehicle number / Driver name |
| Type | Document type / License |
| Expiry Date | When it expires |
| Days Left | Countdown |
| Priority | Color-coded badge |

### Actions

| Action | Description |
|--------|-------------|
| View Details | Navigate to entity detail |
| Refresh | Reload alerts |

---

## Dashboard Integration

### Alerts Widget

Shows summary counts:

| Metric | Description |
|--------|-------------|
| Total Alerts | All active alerts |
| Critical | Alerts < 7 days |
| Warning | Alerts 7-30 days |

### View All Button

Navigates to full Alerts List screen.

---

## Alert Resolution

Alerts are automatically resolved when:
- Document is renewed (new expiry date set)
- License is renewed
- Entity is deactivated/deleted

---

## Role-Based Visibility

| Access | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View All Alerts | ✅ | ✅ | ✅ | Assigned |
| Navigate to Entity | ✅ | ✅ | ✅ | ✅ |
| Update Document | ✅ | ✅ | ✅ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/dashboard/alerts-status` | GET | Alert counts summary |
| `/alerts` | GET | Full alerts list |
| `/alerts/vehicles` | GET | Vehicle document alerts |
| `/alerts/drivers` | GET | Driver license alerts |

### Response: Alert Status

| Field | Description |
|-------|-------------|
| total_alerts | Total active alerts |
| critical_count | Critical priority count |
| warning_count | Warning priority count |
| vehicle_alerts | Vehicle alert count |
| driver_alerts | Driver alert count |

---

## Notification Strategy

### In-App

- Badge count on navigation
- Dashboard widget
- Alert list screen

### Push Notifications (Future)

- Critical alerts: Immediate
- Warning alerts: Daily digest
- Info alerts: Weekly summary

---

## Alert Calculation

### Document Expiry Check

```
Days Remaining = Expiry Date - Today

If Days Remaining < 0: Expired
If Days Remaining < 7: Critical
If Days Remaining < 30: Warning
If Days Remaining < 60: Info
```

---

## Integration with Other Modules

### Dashboard Module

- Alerts summary widget
- Quick view of critical alerts

### Vehicles Module

- Navigate to vehicle for document update
- Document tab shows expiry status

### Drivers Module

- Navigate to driver for license update
- License expiry highlighted

---

## Caretaker Notifications

When caretaker is assigned:
- Receives alerts for assigned vehicles
- Receives alerts for assigned drivers
- Supervisor sees only assigned entity alerts

---

## Related Modules

- [Dashboard](../dashboard/) - Alert summary
- [Vehicles](../vehicles/) - Document management
- [Drivers](../drivers/) - License management

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
