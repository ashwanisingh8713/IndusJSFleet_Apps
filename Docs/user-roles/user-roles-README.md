# User Roles & Permissions

## Overview

IndusJS Fleet implements a role-based access control (RBAC) system with four distinct user roles. Each role has specific permissions and responsibilities within the fleet management ecosystem.

---

## Role Hierarchy

```
┌─────────────────────────────────────────────────────────────────┐
│                          OWNER                                   │
│              (Full system access & control)                      │
├─────────────────────────────────────────────────────────────────┤
│                     GENERAL MANAGER                              │
│           (Financial & operational access)                       │
├─────────────────────────────────────────────────────────────────┤
│                         MANAGER                                  │
│              (Operational management)                            │
├─────────────────────────────────────────────────────────────────┤
│                       SUPERVISOR                                 │
│                (View & track only)                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Role Definitions

### 1. Owner

**Description:** The primary account holder with complete system access and control. Only one Owner exists per organization.

| Aspect | Details |
|--------|---------|
| **API Role Value** | `owner` |
| **Created By** | Self-registration (Sign Up) |
| **Can Create** | General Manager, Manager, Supervisor |

**Key Responsibilities:**
- Organization configuration and settings
- Create and manage all team members
- Full financial access (view/edit trip prices, payments)
- Manage billing and subscription
- Delete any entity (vehicles, drivers, trips, costs)
- Access all reports and analytics

---

### 2. General Manager (GM)

**Description:** Senior management role with nearly full access. Trusted with financial data and team management.

| Aspect | Details |
|--------|---------|
| **API Role Value** | `general_manager` |
| **Created By** | Owner only |
| **Can Create** | Manager, Supervisor |

**Key Responsibilities:**
- View and manage financial data (trip prices, P&L reports)
- Edit trips in any state
- Manage team members (except Owner)
- Assign caretakers to vehicles/drivers
- Full access to payments and costs
- Generate financial reports

---

### 3. Manager

**Description:** Operational managers handling day-to-day fleet operations without financial visibility.

| Aspect | Details |
|--------|---------|
| **API Role Value** | `manager` |
| **Created By** | Owner or General Manager |
| **Can Create** | Supervisor (limited) |

**Key Responsibilities:**
- Manage vehicles and drivers
- Create and manage trips
- Record trip costs and maintenance
- View trip details (without pricing)
- Monitor trip progress
- Coordinate with supervisors and drivers

---

### 4. Supervisor

**Description:** Field supervisors with view-only access for monitoring assigned operations.

| Aspect | Details |
|--------|---------|
| **API Role Value** | `supervisor` |
| **Created By** | Owner, General Manager, or Manager |
| **Can Create** | None |

**Key Responsibilities:**
- View assigned vehicles and trips
- Monitor trip progress
- View driver assignments
- Report issues (read-only for most features)

---

## Permission Matrix

### Module-Level Permissions

| Module | Owner | General Manager | Manager | Supervisor |
|--------|:-----:|:---------------:|:-------:|:----------:|
| **Dashboard** | Full | Full | Limited | Minimal |
| **Vehicles** | Full CRUD | Full CRUD | Full CRUD | View Only |
| **Drivers** | Full CRUD | Full CRUD | Full CRUD | View Only |
| **Trips** | Full CRUD | Full CRUD | CRUD (no price) | View Assigned |
| **Costs** | Full CRUD | Full CRUD | Create/View | Create Own |
| **Payments** | Full CRUD | Full CRUD | View Only | View Only |
| **Customers** | Full CRUD | Full CRUD | Full CRUD | View Only |
| **Reports** | Full | Full | Limited | None |
| **Team** | Full CRUD | CRUD (no Owner) | View Only | None |
| **Settings** | Full | Limited | Profile Only | Profile Only |

### Feature-Specific Permissions

| Feature | Owner | GM | Manager | Supervisor |
|---------|:-----:|:--:|:-------:|:----------:|
| View Trip Price | ✅ | ✅ | ❌ | ❌ |
| Edit Trip Price | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Any State) | ✅ | ✅ | ❌ | ❌ |
| Delete Costs | ✅ | ✅ | ✅ | ❌ |
| Manage Team | ✅ | ✅ | ❌ | ❌ |
| Assign Caretaker | ✅ | ✅ | ❌ | ❌ |
| View P&L Reports | ✅ | ✅ | ❌ | ❌ |
| Record Payments | ✅ | ✅ | ❌ | ❌ |
| Export PDF Reports | ✅ | ✅ | ✅ | ❌ |
| View Driver Costs | ✅ | ✅ | ❌ | ❌ |

---

## Screen-Level Access

### Navigation Menu Visibility

| Menu Item | Owner | GM | Manager | Supervisor |
|-----------|:-----:|:--:|:-------:|:----------:|
| Dashboard | ✅ | ✅ | ✅ | ✅ |
| Vehicles | ✅ | ✅ | ✅ | ✅ |
| Drivers | ✅ | ✅ | ✅ | ✅ |
| Trips | ✅ | ✅ | ✅ | ✅ |
| Customers | ✅ | ✅ | ✅ | ✅ |
| Payments | ✅ | ✅ | ✅ | ❌ |
| Reports | ✅ | ✅ | ❌ | ❌ |
| Team | ✅ | ✅ | ❌ | ❌ |
| Settings | ✅ | ✅ | ✅ | ✅ |

### Action Button Visibility

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| Add Vehicle | ✅ | ✅ | ✅ | ❌ |
| Edit Vehicle | ✅ | ✅ | ✅ | ❌ |
| Delete Vehicle | ✅ | ✅ | ❌ | ❌ |
| Add Driver | ✅ | ✅ | ✅ | ❌ |
| Edit Driver | ✅ | ✅ | ✅ | ❌ |
| Delete Driver | ✅ | ❌ | ❌ | ❌ |
| Create Trip | ✅ | ✅ | ✅ | ❌ |
| Edit Trip | ✅ | ✅ | Planned Only | ❌ |
| Cancel Trip | ✅ | ✅ | ✅ | ❌ |
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |
| Record Payment | ✅ | ✅ | ❌ | ❌ |
| Add Team Member | ✅ | ✅ | ❌ | ❌ |

---

## User Entity Capabilities

The User entity provides helper properties for permission checking:

| Property | Returns True For |
|----------|------------------|
| isOwner | Owner only |
| isGeneralManager | General Manager only |
| isManager | Manager only |
| isSupervisor | Supervisor only |
| hasFinancialAccess | Owner, General Manager |
| canEditTripInAnyState | Owner, General Manager |
| canManageTeam | Owner, General Manager |
| canAssignCaretaker | Owner, General Manager |
| canViewTripPrice | Owner, General Manager |
| canDeleteCosts | Owner, General Manager, Manager |

---

## API Authorization

All API endpoints validate user role server-side. Unauthorized access returns:

| Status Code | Response |
|-------------|----------|
| 403 Forbidden | "Insufficient permissions" message |

### Rate Limits by Role

| Role | Standard API | Location API | Auth API |
|------|-------------|--------------|----------|
| Owner | 100 req/min | 1000 req/min | 10 req/min |
| GM | 100 req/min | 1000 req/min | 10 req/min |
| Manager | 100 req/min | 500 req/min | 10 req/min |
| Supervisor | 50 req/min | 200 req/min | 10 req/min |

---

## Best Practices

### Always Check Permissions Before Actions

- Verify user role before showing UI elements
- Double-check permissions in ViewModel before API calls
- Server validates permissions as final authority

### Hide UI Elements Based on Role

- Don't show disabled elements user can't use
- Completely hide actions user lacks permission for
- Provide clear feedback when action is restricted

### Fail Gracefully on Permission Errors

- Handle 403 responses gracefully
- Show user-friendly error messages
- Don't expose internal error details

---

## Related Documentation

- [Modules Overview](../modules/README.md)
- [Architecture Guide](../architecture/architecture-README.md)
- [API Reference](../postman_collections/Fleet_Management_API_v2.postman_collection.json)
