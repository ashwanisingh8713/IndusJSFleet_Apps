# Role-Based Access Control

> Use this prompt when implementing role-restricted features, hiding/showing UI elements based on user role, or understanding the permission hierarchy.

## Role Hierarchy

```
Owner (full access)
  └── General Manager (financial access, manage M/S)
        └── Manager (operational access, NO financials)
              └── Supervisor (view-only, can update trip status)
```

## Permission Matrix

| Feature | Owner | GM | Manager | Supervisor |
|---------|-------|----|---------|------------|
| View Dashboard (basic) | ✅ | ✅ | ✅ | ✅ |
| View Financial Summary | ✅ | ✅ | ❌ | ❌ |
| View `trip_price` field | ✅ | ✅ | ❌ | ❌ |
| Create Vehicle | ✅ | ✅ | ✅ | ❌ |
| Edit Vehicle | ✅ | ✅ | ✅ | ❌ |
| Delete Vehicle | ✅ | ✅ | ❌ | ❌ |
| Create Driver | ✅ | ✅ | ✅ | ❌ |
| Create Trip | ✅ | ✅ | ✅ | ❌ |
| Update Trip Status | ✅ | ✅ | ✅ | ✅ |
| Cancel Trip | ✅ | ✅ | ✅ | ❌ |
| Add Trip Cost | ✅ | ✅ | ✅ | ✅ |
| Delete Trip Cost | ✅ | ✅ | ✅ | ❌ |
| Add Maintenance Cost | ✅ | ✅ | ✅ | ✅ |
| View Reports (P&L) | ✅ | ✅ | ❌ | ❌ |
| Create Team Member | ✅ | ✅ (M/S only) | ❌ | ❌ |
| Delete Team Member | ✅ | ✅ (M/S only) | ❌ | ❌ |
| View Payments | ✅ | ✅ | ✅ | ✅ |
| Add Payment | ✅ | ✅ | ✅ | ❌ |
| Vehicle Finance | ✅ | ✅ | ❌ | ❌ |
| Export PDF Reports | ✅ | ✅ | ❌ | ❌ |

## How to Check Role

The user's role is stored in `UserLocalDataSource` after login:

```kotlin
// In ViewModel — get current user role
val userRole = userLocalDataSource.getUserRole() // "owner", "general_manager", "manager", "supervisor"
```

## UI Conditional Rendering

```kotlin
// Hide financial data from Manager/Supervisor
val showFinancials = state.userRole in listOf("owner", "general_manager")

if (showFinancials) {
    Text("Trip Price: ₹${trip.tripPrice}")
    Text("Revenue: ₹${report.totalRevenue}")
}

// Hide action buttons for Supervisor
val canEdit = state.userRole != "supervisor"

if (canEdit) {
    FleetPrimaryButton(text = "Edit", onClick = { ... })
}

// Team member creation — GM can only create Manager/Supervisor
if (state.userRole == "general_manager") {
    // Show only Manager and Supervisor role options
    // Use CreateTeamMember(excludeGeneralManager = true)
}
```

## Dashboard Sections by Role

| Section | Owner | GM | Manager | Supervisor |
|---------|-------|----|---------|------------|
| Fleet Overview | ✅ | ✅ | ✅ | ✅ |
| Cost Overview | ✅ | ✅ | ✅ | ✅ |
| Financial Summary | ✅ | ✅ | ❌ | ❌ |
| Pending Payments | ✅ | ✅ | ❌ | ❌ |
| Alerts | ✅ | ✅ | ✅ | ✅ |
| Quick Actions | ✅ | ✅ | ✅ (limited) | ✅ (limited) |

## Navigation Menu Items

```kotlin
// Hamburger/drawer menu items filtered by role
val menuItems = buildList {
    add(MenuItem.Dashboard)
    add(MenuItem.Vehicles)
    add(MenuItem.Drivers)
    add(MenuItem.Trips)
    add(MenuItem.Customers)
    add(MenuItem.Payments)
    add(MenuItem.Alerts)
    add(MenuItem.Maps)
    if (userRole in listOf("owner", "general_manager")) {
        add(MenuItem.Reports)
        add(MenuItem.VehicleFinance)
        add(MenuItem.Team)
    }
    add(MenuItem.Profile)
}
```

## API Role Values

| Display Name | API Value | `@SerialName` |
|-------------|-----------|---------------|
| Owner | `owner` | `"owner"` |
| General Manager | `general_manager` | `"general_manager"` |
| Manager | `manager` | `"manager"` |
| Supervisor | `supervisor` | `"supervisor"` |

