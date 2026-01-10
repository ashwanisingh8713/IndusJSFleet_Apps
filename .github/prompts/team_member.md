# Team Member Hierarchy & Role-Based Access Control

## Role Hierarchy

```
Owner (Full Access - Fleet Owner)
  └── General Manager (Financial & Operational Control)
        └── Manager (Operational Control - Vehicles/Drivers/Trips)
              └── Supervisor (Limited - Costs & Monitoring Only)
```

---

## Team Member Types

### 1. Owner
- **Description**: Fleet business owner with complete system access
- **Sign Up**: Only role that can self-register via Sign Up
- **Account Creation**: Can create General Manager, Manager, and Supervisor accounts

**Permissions:**
- ✅ Full system access (Add/Edit/Delete/Read everything)
- ✅ Create accounts for all roles (General Manager, Manager, Supervisor)
- ✅ Enable/Disable any team member
- ✅ Delete (soft-delete) any team member
- ✅ View all financial data including `trip_price`
- ✅ Access all P&L reports and financial overviews
- ✅ Edit trips in any state (planned, in_progress, completed)
- ✅ Manage all vehicles and drivers
- ✅ Assign/reassign caretakers to vehicles and drivers

---

### 2. General Manager
- **Description**: Senior management with financial oversight
- **Sign Up**: Cannot self-register (created by Owner only)
- **Account Creation**: Can create Manager and Supervisor accounts

**Permissions:**
- ✅ All Manager permissions, plus:
- ✅ Create accounts for Manager and Supervisor
- ✅ Enable/Disable Manager and Supervisor accounts
- ✅ Delete (soft-delete) Manager and Supervisor
- ✅ View all financial data including `trip_price`
- ✅ Access all P&L reports and financial overviews
- ✅ Edit trips in any state (planned, in_progress, completed)
- ✅ Add/Edit/Delete vehicles and drivers
- ✅ Assign caretakers to vehicles and drivers
- ❌ Cannot create General Manager accounts
- ❌ Cannot modify Owner account
- ❌ Cannot delete General Manager accounts

---

### 3. Manager
- **Description**: Operational management for vehicles, drivers, and trips
- **Sign Up**: Cannot self-register (created by Owner or General Manager)
- **Account Creation**: Cannot create any accounts

**Permissions:**
- ✅ All Supervisor permissions, plus:
- ✅ Create vehicles
- ✅ Create drivers
- ✅ Edit vehicles (but not delete)
- ✅ Edit drivers (but not delete)
- ✅ Create trips
- ✅ Edit trips in planned state only
- ✅ Change trip state
- ✅ Add/Edit/Delete all types of costs (trip costs, maintenance costs)
- ✅ View trip costs and vehicle costs
- ✅ View routes and trip tracking
- ✅ Can be assigned as caretaker for vehicles/drivers
- ❌ Cannot see `trip_price` in trip responses
- ❌ Cannot see financial overview/dashboard
- ❌ Cannot access P&L reports
- ❌ Cannot edit trips in in_progress or completed state
- ❌ Cannot create any user accounts
- ❌ Cannot delete vehicles or drivers (only Owner/GM can disable)
- ❌ Cannot assign caretakers

---

### 4. Supervisor
- **Description**: Field supervision with cost tracking capabilities
- **Sign Up**: Cannot self-register (created by Owner or General Manager)
- **Account Creation**: Cannot create any accounts

**Permissions:**
- ✅ Add/Edit costs (trip costs, maintenance costs)
- ✅ View routes and trip tracking
- ✅ View assigned vehicles and drivers
- ✅ View team members list (read-only)
- ✅ Can be assigned as caretaker for vehicles/drivers
- ❌ Cannot delete costs
- ❌ Cannot see `trip_price` in trip responses
- ❌ Cannot see financial overview/dashboard
- ❌ Cannot access P&L reports
- ❌ Cannot edit vehicles or drivers
- ❌ Cannot edit trips
- ❌ Cannot create any user accounts

---

## Vehicle & Driver Caretaker System

### Ownership Model
- All Vehicles belong to the **Owner** (owner_id)
- All Drivers belong to the **Owner** (owner_id)
- Vehicles and Drivers can have a **Caretaker** (caretaker_id)

### Caretaker Rules
- Caretaker must be a **Manager** or **Supervisor**
- Caretaker is **optional** (can be null)
- One caretaker can manage **multiple** vehicles and drivers
- Caretaker can be **changed/reassigned** at any time
- Only **Owner** and **General Manager** can assign/change caretakers

### Caretaker Assignment
```
POST /api/v2/vehicles/{id}/assign-caretaker
Body: { "caretaker_id": 5 }

POST /api/v2/drivers/{id}/assign-caretaker
Body: { "caretaker_id": 5 }
```

### Bulk Caretaker Assignment
```
POST /api/v2/caretakers/assign-bulk
Body: {
    "caretaker_id": 5,
    "vehicle_ids": [1, 2, 3],
    "driver_ids": [1, 2]
}
```

---

## Soft Delete Policy (No Hard Deletes)

### Entities with Soft Delete Only:
1. **Users (Team Members)** - Use `is_active = false` to disable
2. **Vehicles** - Use `state = 'inactive'` or `is_active = false`
3. **Drivers** - Use `is_active = false` to disable

### Benefits:
- Preserves audit trail and history
- Maintains referential integrity
- Allows recovery of accidentally disabled entities
- Historical reports remain accurate

### Disable vs Delete Terminology:
| Action | What Happens | Who Can Do It |
|--------|--------------|---------------|
| Disable Team Member | `is_active = false` | Owner, General Manager |
| Disable Vehicle | `state = 'inactive'` | Owner, General Manager |
| Disable Driver | `is_active = false` | Owner, General Manager |

---

## Permission Matrix (Detailed)

### User Management Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Sign Up (Self-register) | ✅ | ❌ | ❌ | ❌ |
| Create General Manager | ✅ | ❌ | ❌ | ❌ |
| Create Manager | ✅ | ✅ | ❌ | ❌ |
| Create Supervisor | ✅ | ✅ | ❌ | ❌ |
| View Team Members | ✅ | ✅ | ✅ | ✅ |
| View Team Member Details | ✅ | ✅ | ✅ | ✅ |
| Edit General Manager | ✅ | ❌ | ❌ | ❌ |
| Edit Manager | ✅ | ✅ | ❌ | ❌ |
| Edit Supervisor | ✅ | ✅ | ❌ | ❌ |
| Disable/Enable General Manager | ✅ | ❌ | ❌ | ❌ |
| Disable/Enable Manager | ✅ | ✅ | ❌ | ❌ |
| Disable/Enable Supervisor | ✅ | ✅ | ❌ | ❌ |
| Change Role (Manager ↔ Supervisor) | ✅ | ✅ | ❌ | ❌ |
| Reset Password (Any) | ✅ | ✅ (M/S only) | ❌ | ❌ |

### Vehicle & Driver Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Create Vehicle | ✅ | ✅ | ✅ | ❌ |
| View Vehicle | ✅ | ✅ | ✅ | ✅ |
| Edit Vehicle | ✅ | ✅ | ✅ | ❌ |
| Disable Vehicle | ✅ | ✅ | ❌ | ❌ |
| Assign Caretaker to Vehicle | ✅ | ✅ | ❌ | ❌ |
| Create Driver | ✅ | ✅ | ✅ | ❌ |
| View Driver | ✅ | ✅ | ✅ | ✅ |
| Edit Driver | ✅ | ✅ | ✅ | ❌ |
| Disable Driver | ✅ | ✅ | ❌ | ❌ |
| Assign Caretaker to Driver | ✅ | ✅ | ❌ | ❌ |

### Trip Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Create Trip | ✅ | ✅ | ✅ | ❌ |
| View Trip | ✅ | ✅ | ✅ | ✅ |
| View `trip_price` | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Planned) | ✅ | ✅ | ✅ | ❌ |
| Edit Trip (In Progress) | ✅ | ✅ | ❌ | ❌ |
| Edit Trip (Completed) | ✅ | ✅ | ❌ | ❌ |
| Change Trip State | ✅ | ✅ | ✅ | ❌ |
| Delete Trip | ✅ | ✅ | ❌ | ❌ |

### Cost Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Edit Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |
| Add Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| Edit Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Maintenance Cost | ✅ | ✅ | ✅ | ❌ |
| View Cost Summary | ✅ | ✅ | ✅ | ❌ |

### Financial & Reports Permissions

| Action | Owner | General Manager | Manager | Supervisor |
|--------|-------|-----------------|---------|------------|
| View Dashboard (Full) | ✅ | ✅ | ❌ | ❌ |
| View Dashboard (Limited) | ✅ | ✅ | ✅ | ✅ |
| View Cost Overview | ✅ | ✅ | ❌ | ❌ |
| View P&L Reports | ✅ | ✅ | ❌ | ❌ |
| View Trip P&L | ✅ | ✅ | ❌ | ❌ |
| View Vehicle P&L | ✅ | ✅ | ❌ | ❌ |
| View Consolidated P&L | ✅ | ✅ | ❌ | ❌ |
| View Pending Payments | ✅ | ✅ | ❌ | ❌ |

---


---

## API Endpoints (New/Updated)

### Team Member Management
```
POST   /api/v2/team/members                    - Create team member
GET    /api/v2/team/members                    - List all team members
GET    /api/v2/team/members/:id                - Get team member details
PUT    /api/v2/team/members/:id                - Update team member
PATCH  /api/v2/team/members/:id/toggle-active  - Enable/Disable member
PATCH  /api/v2/team/members/:id/change-role    - Change role (M↔S, GM)
POST   /api/v2/team/members/:id/reset-password - Reset password
DELETE /api/v2/team/members/:id                - Delete team member (Owner only)
```

### Caretaker Assignment
```
POST   /api/v2/vehicles/:id/assign-caretaker   - Assign caretaker to vehicle
DELETE /api/v2/vehicles/:id/remove-caretaker   - Remove caretaker from vehicle
POST   /api/v2/drivers/:id/assign-caretaker    - Assign caretaker to driver
DELETE /api/v2/drivers/:id/remove-caretaker    - Remove caretaker from driver
POST   /api/v2/caretakers/assign-bulk          - Bulk assign caretaker
POST   /api/v2/caretakers/reassign-bulk        - Reassign from one to another
GET    /api/v2/caretakers/:id/assignments      - Get caretaker's assignments
GET    /api/v2/caretakers/orphaned-assignments - Get orphaned assignments
```

---

## Further Considerations (Implemented)

### 1. Caretaker Reassignment on Disable
**Scenario**: When a Manager/Supervisor is disabled, what happens to their assigned vehicles/drivers?

**Solution**:
- Vehicles/Drivers are **NOT automatically reassigned**
- Owner/General Manager receives a **warning alert** showing:
  - Count of vehicles without active caretaker
  - Count of drivers without active caretaker
- A dedicated endpoint to **view orphaned assignments**:
  ```
  GET /api/v2/caretakers/orphaned-assignments
  ```
- Bulk reassignment endpoint for quick fix:
  ```
  POST /api/v2/caretakers/reassign-bulk
  Body: {
      "from_caretaker_id": 5,
      "to_caretaker_id": 7
  }
  ```

### 2. Audit Trail
**Implementation**:
- Add `updated_by_id` field to critical entities:
  - Vehicles: Track who last updated
  - Drivers: Track who last updated
  - Trips: Track who last updated
  - Costs: Track who created/updated
- Add `created_by_id` to User model

### 3. Bulk Caretaker Assignment
**Endpoints**:
```
POST /api/v2/caretakers/assign-bulk
Body: {
    "caretaker_id": 5,
    "vehicle_ids": [1, 2, 3, 4, 5],
    "driver_ids": [1, 2, 3]
}

Response: {
    "success": true,
    "message": "Bulk assignment completed",
    "vehicles_assigned": 5,
    "drivers_assigned": 3
}
```

### 4. Role Change Restrictions
**Rules**:
- Owner can change any role except their own
- General Manager can only change between Manager ↔ Supervisor
- Cannot promote to General Manager (Owner only)
- Cannot demote General Manager (Owner only)

**Endpoint**:
```
PATCH /api/v2/team/members/:id/change-role
Body: { "new_role": "supervisor" }
```

### 5. Self-Modification Prevention
**Rules**:
- Users cannot disable their own account
- Users cannot change their own role
- Users cannot delete their own account
- Password change requires current password verification

### 6. Caretaker Visibility Filter
**For Managers and Supervisors**:
- Option to filter vehicles/drivers by "My Assignments" vs "All"
- Query parameter: `?caretaker=me` or `?caretaker=all`

```
GET /api/v2/vehicles?caretaker=me        - Only my assigned vehicles
GET /api/v2/vehicles?caretaker=all       - All vehicles (default)
GET /api/v2/drivers?caretaker=me         - Only my assigned drivers
```

### 7. Dashboard Alerts for Caretaker Issues
**New Alert Types**:
- `ORPHANED_VEHICLE`: Vehicle has no active caretaker
- `ORPHANED_DRIVER`: Driver has no active caretaker
- `CARETAKER_DISABLED`: Caretaker was recently disabled

---

## Response Field Filtering by Role

### Trip Response (Manager/Supervisor)
Fields **hidden** from Manager and Supervisor:
```json
{
    // Hidden fields:
    // "trip_price": 90000.00,
    // "expected_profit": 25000.00,
    // "actual_vs_expected": -5000.00,
    
    // Visible fields:
    "trip_id": 1,
    "vehicle_id": 1,
    "driver_id": 1,
    "start_location": "Mumbai",
    "end_location": "Pune",
    "total_cost": 15000.00
}
```

### Dashboard Response (Manager/Supervisor)
**Limited Dashboard**:
```json
{
    "user_info": {},
    "fleet_overview": {
        "total_vehicles": 50,
        "active_vehicles": 45,
        "total_drivers": 30,
        "available_drivers": 25
    },
    "today_summary": {
        "trips_planned": 10,
        "trips_in_progress": 5,
        "trips_completed": 3
    }
    // No: cost_overview, pending_payments, profit_loss
}
```

---

## Implementation Status

- [x] Update User model with `general_manager` role
- [x] Add `caretaker_id` to Vehicle model
- [x] Add `caretaker_id` to Driver model
- [x] Create permissions helper functions
- [x] Update User controller with role-based restrictions
- [x] Add caretaker assignment endpoints
- [x] Add bulk caretaker assignment endpoint
- [x] Add orphaned assignments endpoint
- [x] Add change-role endpoint
- [x] Add audit trail fields (`updated_by_id`, `created_by_id`)
- [x] Update Trip controller to hide `trip_price` for restricted roles
- [x] Update Report controller to block financial APIs for restricted roles
- [x] Update Dashboard controller with role-based filtering
- [x] Update database migrations (auto-migrate handles new columns)
- [x] Update Postman collection
- [ ] Test all role-based permissions
- [x] Deploy to Cloud Run
