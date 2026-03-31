# All Screens & Routes (41 Routes)

## Route Definitions

All routes are `@Serializable` data objects/classes implementing `FleetRoute : NavKey`.

Defined in: `sharedUI/.../navigation/FleetRoute.kt`

### Authentication Flow

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Login` | object | — | Email/password login |
| `SignUp` | object | — | Owner registration |
| `ForgotPassword` | object | — | Email reset link |
| `Profile` | object | — | View/edit profile, logout |
| `ChangePassword` | object | — | Update password |

### Dashboard & General

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Onboarding` | object | — | First-time user flow |
| `Dashboard` | object | — | Main fleet overview |
| `AlertsList` | object | — | Document/license expiry alerts |
| `Maps` | object | — | Real-time vehicle tracking |

### Vehicles

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Vehicles` | object | — | Vehicle list with search/filter |
| `VehicleDetail` | class | `vehicleId: String` | Tabs: Overview, Trips, Documents, Costs |
| `AddVehicle` | object | — | Register vehicle with documents |
| `MaintenanceCostEntry` | class | `vehicleId: String? = null` | Record maintenance expense |

### Drivers

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Drivers` | object | — | Driver list with search/filter |
| `DriverDetail` | class | `driverId: String` | Driver info, license, history, costs |
| `CreateDriver` | object | — | Register driver with license |
| `DriverCostEntry` | class | `driverId: String? = null` | Record driver cost |

### Trips

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Trips` | object | — | Trip list, filter by status |
| `TripDetail` | class | `tripId: String` | Route, cargo, schedule, costs, payments |
| `CreateTrip` | object | — | Plan trip (vehicle + driver + route + schedule + cargo) |
| `TripCostEntry` | class | `tripId: String? = null, vehicleId: String? = null` | Record trip expense |

### Customers

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Customers` | object | — | Customer list with search |
| `CustomerDetail` | class | `customerId: String` | Info, trip history, financials |
| `CreateCustomer` | object | — | Company + contact + GST + address |

### Payments

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Payments` | object | — | Payment list with filters |
| `PaymentDetail` | class | `paymentId: String` | Payment details |
| `AddPayment` | class | `tripId: String? = null, vehicleId: String? = null` | Record payment |
| `EditPayment` | class | `paymentId: String` | Update existing payment |

### Reports (Owner/GM Only)

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `Reports` | object | — | Reports hub with period filter |
| `VehicleProfitLoss` | object | — | Revenue vs expenses per vehicle |
| `TripProfitLoss` | object | — | Revenue vs expenses per trip |
| `CostAnalysis` | object | — | Breakdown by cost type |
| `ConsolidatedPL` | object | — | Overall P&L statement |

### Team Management

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `TeamList` | object | — | All team members |
| `CreateTeamMember` | class | `excludeGeneralManager: Boolean = false` | Add GM/Manager/Supervisor |
| `TeamMemberDetail` | class | `memberId: String` | Member details + permissions |

### Vehicle Finance

| Route | Type | Parameters | Screen |
|-------|------|------------|--------|
| `VehicleFinance` | object | — | All vehicles with finance status |
| `VehicleFinanceDetail` | class | `vehicleId: String` | Purchase info, loan summary |
| `AddPurchaseInfo` | object | — | Record purchase (cash/loan) |
| `EditPurchaseInfo` | class | `vehicleId: String` | Update purchase info |
| `EmiPaymentHistory` | class | `vehicleId: String` | EMI payments list |

## Navigation Flow

```
Login ──→ Dashboard ──┬──→ Vehicles ──→ VehicleDetail ──→ (Docs, Costs, Trips tabs)
                      │                └──→ AddVehicle
                      │                └──→ MaintenanceCostEntry
                      ├──→ Drivers ──→ DriverDetail
                      │              └──→ CreateDriver
                      │              └──→ DriverCostEntry
                      ├──→ Trips ──→ TripDetail ──→ (Costs, Payments tabs)
                      │            └──→ CreateTrip
                      │            └──→ TripCostEntry
                      ├──→ Customers ──→ CustomerDetail
                      │               └──→ CreateCustomer
                      ├──→ Payments ──→ PaymentDetail
                      │              └──→ AddPayment / EditPayment
                      ├──→ Reports ──→ VehiclePL / TripPL / CostAnalysis / ConsolidatedPL
                      ├──→ Team ──→ TeamMemberDetail / CreateTeamMember
                      ├──→ Finance ──→ VehicleFinanceDetail ──→ EMI / Purchase
                      ├──→ Maps
                      ├──→ Alerts
                      └──→ Profile ──→ ChangePassword
```

## Entity State Machines

### Vehicle States
```
inactive ←→ active → on_route → active
                   → maintenance ←→ damaged → decommissioned
```

### Trip States
```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
```

### Driver States
```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```
