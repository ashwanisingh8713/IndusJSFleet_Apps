# Drivers Module

## Overview

The Drivers module manages driver registration, license tracking, availability management, and assignment coordination.

---

## Features

- Driver CRUD operations
- License management with expiry tracking
- Status management (active, on_route, on_leave)
- Vehicle assignment tracking
- Caretaker assignment
- Trip history per driver
- Performance metrics

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Drivers List | `FleetRoute.Drivers` | List all drivers with filters |
| Driver Detail | `FleetRoute.DriverDetail` | Driver details with tabs |
| Add Driver | `FleetRoute.CreateDriver` | Register new driver |

---

## Driver Entity

### Personal Information

| Field | Description | Required |
|-------|-------------|:--------:|
| First Name | Driver's first name | ✅ |
| Last Name | Driver's last name | ✅ |
| Mobile | Primary contact number | ✅ |
| Email | Email address | ❌ |
| Date of Birth | Must be 18+ years old | ❌ |
| Address | Residential address | ❌ |
| Emergency Contact | Emergency phone number | ❌ |
| Blood Group | Blood type | ❌ |

### License Information

| Field | Description | Required |
|-------|-------------|:--------:|
| License Number | Driving license number | ✅ |
| License Type | LMV, HMV, MCWG, MCWOG | ✅ |
| License Expiry | License expiry date | ✅ |

### Employment Information

| Field | Description | Required |
|-------|-------------|:--------:|
| Joining Date | Date joined fleet | ❌ |
| Status | Current status | Auto |
| Assigned Vehicle | Current vehicle | Auto |
| Caretaker | Assigned manager/supervisor | ❌ |

---

## Driver Status

| Status | Description | Available for Trip |
|--------|-------------|:------------------:|
| Active | Available for assignment | ✅ |
| On Route | Currently on a trip | ❌ |
| On Leave | Taking leave | ❌ |
| Inactive | Not working | ❌ |
| Suspended | Temporarily suspended | ❌ |
| Terminated | No longer employed | ❌ |

### Status Transitions

```
            ┌─────────────┐
            │   ACTIVE    │◄────────────────┐
            └──────┬──────┘                 │
                   │                        │
        Start Trip │        ┌───────────────┤
                   ▼        │               │
            ┌─────────────┐ │               │
            │  ON_ROUTE   │─┘               │
            └──────┬──────┘ End Trip        │
                   │                        │
                   │                        │
            ┌──────▼──────┐                 │
            │  ON_LEAVE   │─────────────────┘
            └─────────────┘ Return from Leave
```

---

## Driver Detail Tabs

### 1. Overview Tab

Displays driver information:
- Personal details
- License information
- Current status
- Assigned vehicle
- Caretaker information
- Contact options (call icon)

### 2. Trips Tab

Shows trip history for this driver:
- Trip list with pagination
- Filter by status
- Click to view trip details

### 3. Costs Tab (Owner/GM only)

Shows driver-related costs:
- Driver allowances
- Advances
- Other expenses

---

## Add Driver Form

### Sections

1. **Personal Details**
   - First Name (required)
   - Last Name (required)
   - Mobile Number (required, 10 digits)
   - Email (optional)
   - Date of Birth (must be 18+ years)
   - Address

2. **License Details**
   - License Number (required)
   - License Type (dropdown, required)
   - License Expiry Date (required)

3. **Additional Details**
   - Emergency Contact
   - Blood Group (dropdown)
   - Joining Date

### Validation Rules

| Field | Validation |
|-------|------------|
| Mobile | 10 digits, numeric only |
| Email | Valid email format if provided |
| Date of Birth | Must be at least 18 years old |
| License Expiry | Must be future date |

---

## License Types

| Type | Full Name | Description |
|------|-----------|-------------|
| LMV | Light Motor Vehicle | Cars, jeeps, light trucks |
| HMV | Heavy Motor Vehicle | Trucks, buses, trailers |
| MCWG | Motorcycle With Gear | Geared motorcycles |
| MCWOG | Motorcycle Without Gear | Non-geared scooters |

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Drivers | ✅ | ✅ | ✅ | ✅ |
| Add Driver | ✅ | ✅ | ✅ | ❌ |
| Edit Driver | ✅ | ✅ | ✅ | ❌ |
| Delete Driver | ✅ | ❌ | ❌ | ❌ |
| Change Status | ✅ | ✅ | ✅ | ❌ |
| Assign Caretaker | ✅ | ✅ | ❌ | ❌ |
| View Driver Costs | ✅ | ✅ | ❌ | ❌ |
| Add Driver Cost | ✅ | ✅ | ❌ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/drivers` | GET | List drivers with pagination |
| `/drivers` | POST | Create driver |
| `/drivers/{id}` | GET | Get driver details |
| `/drivers/{id}` | PUT | Update driver |
| `/drivers/{id}` | DELETE | Delete driver |
| `/drivers/{id}/toggle-active` | PATCH | Toggle active status |
| `/drivers/{id}/trips` | GET | Get driver trip history |
| `/drivers/{id}/costs` | GET | Get driver costs |

---

## Date Format

All dates must be sent in ISO 8601 format:

| Field | Format | Example |
|-------|--------|---------|
| License Expiry | ISO 8601 | 2027-01-15T00:00:00Z |
| Date of Birth | ISO 8601 | 1990-05-20T00:00:00Z |
| Joining Date | ISO 8601 | 2024-01-10T00:00:00Z |

---

## Call Integration

Driver mobile numbers support direct calling:
- Click call icon next to mobile number
- Opens device phone dialer
- Available in detail screen and list items

---

## Related Modules

- [Vehicles](../vehicles/) - Vehicle assignment
- [Trips](../trips/) - Trip assignment
- [Costs](../costs/) - Driver costs
- [Alerts](../alerts/) - License expiry alerts

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
