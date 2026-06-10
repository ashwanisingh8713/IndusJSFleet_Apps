# Dashboard Module

## Overview

The Dashboard module serves as the central hub displaying fleet statistics, financial overview, pending payments, and alerts. It provides quick navigation to key actions and insights.

---

## Features

- Fleet statistics (vehicles, drivers, trips)
- Financial overview (revenue, expenses, profit)
- Pending payments summary
- Document/license expiry alerts
- Recent trips list
- Quick action buttons
- Navigation drawer menu

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Dashboard | `FleetRoute.Dashboard` | Main dashboard with all sections |

---

## Dashboard Sections

### 1. Fleet Overview

Displays current fleet statistics.

| Metric | Description |
|--------|-------------|
| Total Vehicles | Count of registered vehicles |
| Active Vehicles | Vehicles currently in use |
| Total Drivers | Count of registered drivers |
| Active Drivers | Drivers currently available |
| Active Trips | Trips currently on route |
| Completed Trips (Month) | Trips completed this month |

### 2. Financial Overview

Displays financial summary for selected period.

| Metric | Description |
|--------|-------------|
| Total Revenue | Income from trips |
| Total Expenses | Costs (fuel, maintenance, etc.) |
| Net Profit/Loss | Revenue minus expenses |
| Profit Margin | Percentage profit |

**Filters:**
- This Week
- This Month
- This Quarter
- Custom Date Range

### 3. Pending Payments

Shows outstanding customer payments.

| Field | Description |
|-------|-------------|
| Customer Name | Customer with pending payment |
| Trip Info | Route details |
| Amount Due | Pending amount |
| Due Date | Payment deadline |

**Actions:**
- View All Payments
- Record Payment

### 4. Alerts

Displays expiry warnings for documents and licenses.

| Alert Type | Description |
|------------|-------------|
| Vehicle Documents | Insurance, PUC, Permit expiring |
| Driver Licenses | License expiry warnings |

**Priority Levels:**
- Critical (< 7 days)
- Warning (< 30 days)
- Info (< 60 days)

### 5. Trips Section

Shows recent and active trips.

| Field | Description |
|-------|-------------|
| Trip Route | Start → End location |
| Vehicle | Vehicle number |
| Status | Current trip state |
| Date | Scheduled date |

**Actions:**
- View All Trips
- Create New Trip
- Add Trip Cost

### 6. Vehicles Section

Vehicle status summary.

**Actions:**
- View All Vehicles
- Add Vehicle
- Add Maintenance Cost

### 7. Drivers Section

Driver availability summary.

**Actions:**
- View All Drivers
- Add Driver
- Add Driver Cost

---

## Navigation Menu

The dashboard includes a navigation drawer with the following items:

| Menu Item | Route | Icon |
|-----------|-------|------|
| Dashboard | FleetRoute.Dashboard | Home |
| Vehicles | FleetRoute.Vehicles | Truck |
| Drivers | FleetRoute.Drivers | Person |
| Trips | FleetRoute.Trips | Route |
| Customers | FleetRoute.Customers | People |
| Payments | FleetRoute.Payments | Payment |
| Reports | FleetRoute.Reports | Chart |
| Team | FleetRoute.TeamList | Group |
| Settings | FleetRoute.Settings | Settings |

---

## Role-Based Visibility

| Section | Owner | GM | Manager | Supervisor |
|---------|:-----:|:--:|:-------:|:----------:|
| Fleet Overview | ✅ | ✅ | ✅ | ✅ |
| Financial Overview | ✅ | ✅ | ❌ | ❌ |
| Pending Payments | ✅ | ✅ | ✅ | ❌ |
| Alerts | ✅ | ✅ | ✅ | ✅ |
| Trips | ✅ | ✅ | ✅ | ✅ |
| Quick Actions | ✅ | ✅ | ✅ | Limited |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/dashboard` | GET | Fleet statistics |
| `/dashboard/cost-overview` | GET | Financial summary |
| `/dashboard/pending-payments` | GET | Payment dues |
| `/dashboard/alerts-status` | GET | Expiry alerts count |

---

## Refresh Behavior

- Pull-to-refresh on main content
- Auto-refresh on return from other screens
- Refresh button in top bar
- Loading indicators for each section

---

## Related Modules

- [Vehicles](../vehicles/) - Vehicle details
- [Drivers](../drivers/) - Driver details
- [Trips](../trips/) - Trip management
- [Payments](../payments/) - Payment tracking
- [Alerts](../alerts/) - Alert details

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
