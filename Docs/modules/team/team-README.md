# Team Module

## Overview

The Team module manages organization team members including Managers and Supervisors. Owners and General Managers can invite new members and assign roles.

---

## Features

- Team member invitation
- Role assignment (General Manager, Manager, Supervisor)
- Team member details view
- Member status management
- Member removal
- Role-based creation restrictions

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Team List | `FleetRoute.TeamList` | List all team members |
| Create Team Member | `FleetRoute.CreateTeamMember` | Invite new member |
| Team Member Detail | `FleetRoute.TeamMemberDetail` | Member details |

---

## User Roles in Team

### Role Hierarchy

```
Owner (Account Holder)
   │
   ├── General Manager (Senior Management)
   │      │
   │      └── Manager (Operations)
   │             │
   │             └── Supervisor (Field)
```

### Role Descriptions

| Role | Description | Created By |
|------|-------------|------------|
| General Manager | Senior management with financial access | Owner only |
| Manager | Operations management | Owner, GM |
| Supervisor | Field supervision, view-only | Owner, GM, Manager |

---

## Team Member Entity

### Fields

| Field | Description | Required |
|-------|-------------|:--------:|
| First Name | Member's first name | ✅ |
| Last Name | Member's last name | ✅ |
| Email | Login email | ✅ |
| Mobile | Contact number | ✅ |
| Role | Assigned role | ✅ |
| Is Active | Account status | Auto |
| Created At | Registration date | Auto |

---

## Team List Screen

### Display

| Column | Description |
|--------|-------------|
| Name | Full name |
| Role | Role badge |
| Email | Email address |
| Mobile | Phone number |
| Status | Active/Inactive |

### Actions

| Action | Description |
|--------|-------------|
| View | Open member details |
| Add | Invite new member |
| Refresh | Reload list |

---

## Create Team Member Screen

### Role Selection

Visual role selector with:
- Role icon
- Role title
- Role description

**Visibility by Creator:**

| Creator | Can Create |
|---------|------------|
| Owner | GM, Manager, Supervisor |
| General Manager | Manager, Supervisor |
| Manager | Supervisor |
| Supervisor | None |

**Route Parameter:**
- `excludeGeneralManager`: When true, hides GM option (used when navigating from vehicle caretaker)

### Form Fields

| Field | Description | Validation |
|-------|-------------|------------|
| First Name | Member's first name | Required |
| Last Name | Member's last name | Required |
| Email | Login email | Required, valid format |
| Mobile | Contact number | Required, 10 digits |
| Role | Selected role | Required |

### Invitation Flow

1. Fill member details
2. Select role
3. Submit invitation
4. Member receives email with temporary password
5. Member logs in and sets new password

---

## Team Member Detail Screen

### Sections

1. **Basic Information**
   - Full name
   - Email
   - Mobile (with call option)
   - Role badge
   - Status

2. **Account Information**
   - Created date
   - Last login
   - Account status

3. **Assigned Resources** (if applicable)
   - Vehicles as caretaker
   - Drivers as caretaker

### Actions

| Action | Permission |
|--------|------------|
| Edit | Owner, GM |
| Deactivate | Owner, GM |
| Remove | Owner only |

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Team List | ✅ | ✅ | ❌ | ❌ |
| View Member Detail | ✅ | ✅ | ❌ | ❌ |
| Invite GM | ✅ | ❌ | ❌ | ❌ |
| Invite Manager | ✅ | ✅ | ❌ | ❌ |
| Invite Supervisor | ✅ | ✅ | ✅ | ❌ |
| Edit Member | ✅ | ✅ | ❌ | ❌ |
| Deactivate Member | ✅ | ✅ | ❌ | ❌ |
| Remove Member | ✅ | ❌ | ❌ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/team` | GET | List team members |
| `/team/invite` | POST | Invite new member |
| `/team/{id}` | GET | Get member details |
| `/team/{id}` | PUT | Update member |
| `/team/{id}` | DELETE | Remove member |
| `/team/{id}/toggle-active` | PATCH | Toggle active status |

---

## Caretaker Assignment

Team members (Manager/Supervisor) can be assigned as caretakers for:

### Vehicles

- Assigned in Add/Edit Vehicle screen
- Responsible for vehicle maintenance
- Receives alerts for document expiry

### Drivers

- Assigned in Add/Edit Driver screen
- Responsible for driver coordination
- Receives alerts for license expiry

---

## Integration Points

### Vehicle Module

- Caretaker selection in Add/Edit Vehicle
- "Add Team Member" option when no caretakers exist

### Driver Module

- Caretaker selection in Add/Edit Driver

### Dashboard Module

- Team count in fleet overview (Owner/GM)

---

## Related Modules

- [Vehicles](../vehicles/) - Caretaker assignment
- [Drivers](../drivers/) - Caretaker assignment
- [Dashboard](../dashboard/) - Team overview

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
