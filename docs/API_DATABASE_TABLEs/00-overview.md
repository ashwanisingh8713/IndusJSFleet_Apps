# Database Tables Overview

## Fleet Management Backend - PostgreSQL Database Schema

This folder contains detailed schema documentation for all database tables used in the Fleet Management Backend.

---

## Table Summary

| # | Table Name | Description | Records Est. |
|---|------------|-------------|--------------|
| 1 | `users` | User accounts (Owner, GM, Manager, Supervisor) | 1K+ |
| 2 | `vehicles` | Fleet vehicles | 50K+ |
| 3 | `drivers` | Driver profiles | 50K+ |
| 4 | `trips` | Trip records | 500K+ |
| 5 | `trip_stops` | Intermediate stops in trips | 1M+ |
| 6 | `trip_costs` | Trip expenses (fuel, toll, etc.) | 2M+ |
| 7 | `vehicle_maintenance_costs` | Vehicle maintenance expenses | 500K+ |
| 8 | `driver_costs` | Driver salary, incentives, deductions | 500K+ |
| 9 | `customers` | Customer/Client details | 100K+ |
| 10 | `documents` | Vehicle documents (RC, Insurance, etc.) | 250K+ |
| 11 | `locations` | GPS location tracking data | 100M+ |
| 12 | `fuel_records` | Fuel consumption records | 500K+ |
| 13 | `trip_payments` | Trip payment records with TDS/discounts | 500K+ |
| 14 | `audit_logs` | System audit trail | 10M+ |
| 15 | `geofences` | Geofence zone definitions | 10K+ |
| 16 | `geofence_events` | Geofence entry/exit events | 1M+ |
| 17 | `notifications` | User notifications | 1M+ |
| 18 | `notification_preferences` | User notification settings | 10K+ |
| 19 | `reports` | Generated reports | 50K+ |
| 20 | `profit_loss_summaries` | Aggregated P&L summaries | 100K+ |
| 21 | `vehicle_purchases` | Vehicle purchase & loan details | 50K+ |
| 22 | `vehicle_loan_payments` | Vehicle EMI/loan payment records | 2M+ |

**Total Tables: 22**

---

## Entity Relationships

```
┌──────────┐      ┌───────────┐      ┌──────────┐
│  users   │──────│  owners   │──────│customers │
└──────────┘      └───────────┘      └──────────┘
     │                  │
     │                  │
     ▼                  ▼
┌──────────┐      ┌───────────┐      ┌──────────┐
│ vehicles │◄────►│   trips   │◄────►│  drivers │
└──────────┘      └───────────┘      └──────────┘
     │                  │                  │
     │                  │                  │
     ▼                  ▼                  ▼
┌──────────┐      ┌───────────┐      ┌──────────┐
│documents │      │trip_costs │      │driver_   │
│locations │      │trip_stops │      │costs     │
│fuel_     │      │payments   │      └──────────┘
│records   │      └───────────┘
│maint_    │
│costs     │
└──────────┘
```

---

## Key Design Principles

### 1. Multi-Tenancy (Owner Isolation)
Every table with business data has `owner_id` for data isolation:
- Users can only access data belonging to their owner
- All queries filter by `owner_id`

### 2. Soft Deletes
Most tables use `deleted_at` for soft deletes:
- Records are never physically deleted
- Preserves audit trail and history
- GORM handles this automatically

### 3. Audit Trail
- `created_by` / `created_by_id` tracks who created records
- `updated_by_id` tracks who last modified records
- `audit_logs` table stores detailed change history

### 4. State Management
Entities use enum states with valid transitions:
- **Vehicle**: inactive → active → on_route/maintenance/damaged → decommissioned
- **Driver**: inactive → active → on_route/on_leave/suspended → terminated
- **Trip**: planned → assigned → on_route → completed/cancelled/failed

### 5. Cost Structure (Structured IDs)
All costs use structured identification:
- `cost_id`: Unique cost identifier (e.g., "TC-001-002")
- `cost_label`: Human-readable label (e.g., "Diesel")
- `group_id`: Group identifier (e.g., "TC-G-001")

---

## Files in This Folder

| File | Table(s) |
|------|----------|
| `01-users.md` | users |
| `02-vehicles.md` | vehicles |
| `03-drivers.md` | drivers |
| `04-trips.md` | trips, trip_stops |
| `05-trip-costs.md` | trip_costs |
| `06-vehicle-maintenance-costs.md` | vehicle_maintenance_costs |
| `07-driver-costs.md` | driver_costs |
| `08-customers.md` | customers |
| `09-documents.md` | documents |
| `10-locations.md` | locations |
| `11-fuel-records.md` | fuel_records |
| `12-trip-payments.md` | trip_payments |
| `13-audit-logs.md` | audit_logs |
| `14-geofences.md` | geofences, geofence_events |
| `15-notifications.md` | notifications, notification_preferences |
| `16-reports.md` | reports, profit_loss_summaries |

---

## Indexes Strategy

### Primary Indexes
- All tables have `id` as primary key
- `owner_id` indexed on all business tables
- `deleted_at` indexed for soft delete queries

### Foreign Key Indexes
- `vehicle_id`, `driver_id`, `trip_id` indexed where referenced
- `created_by_id`, `caretaker_id` indexed for user references

### Business Indexes
- `state` / `status` on vehicles, drivers, trips
- `cost_id`, `group_id` on cost tables
- `date` ranges for time-series queries

---

## Date/Time Conventions

| Usage | Format | Example |
|-------|--------|---------|
| Timestamps | ISO 8601 | `2026-01-15T09:00:00Z` |
| Date fields | ISO 8601 | `2026-01-15T00:00:00Z` |
| Time strings | HH:MM | `09:00` |
| Month period | YYYY-MM | `2026-01` |

---

## Data Types

| Go Type | PostgreSQL Type | Usage |
|---------|-----------------|-------|
| `uint` | `bigint` | IDs, foreign keys |
| `string` | `varchar(n)` | Short text |
| `string` | `text` | Long text |
| `float64` | `decimal(m,n)` | Money, distances |
| `bool` | `boolean` | Flags |
| `time.Time` | `timestamp` | Dates, timestamps |
| `*type` | `nullable` | Optional fields |
| `string` | `jsonb` | JSON data |

---

## Migration Notes

Tables are auto-migrated using GORM's `AutoMigrate`:
- New columns are added automatically
- Columns are never dropped (manual migration required)
- Index changes may require manual migration

```go
// In migrations/migrations.go
db.AutoMigrate(
    &models.User{},
    &models.Vehicle{},
    &models.Driver{},
    // ... all models
)
```

---

*Last Updated: January 2026*

