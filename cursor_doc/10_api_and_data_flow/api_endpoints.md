# API Endpoints Reference

**Base URL:** `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2`

## Authentication

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/auth/signup` | Owner registration | No |
| POST | `/auth/login` | Email/password login → token | No |
| POST | `/auth/forgot-password` | Send reset email | No |
| POST | `/auth/reset-password` | Reset with token | No |

## Profile

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/profile` | Get user profile | Bearer |
| PUT | `/profile` | Update profile | Bearer |
| POST | `/profile/change-password` | Change password | Bearer |

## Dashboard

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/dashboard` | Fleet overview stats | Bearer |
| GET | `/dashboard/cost-overview?filter={period}` | Cost summary | Bearer |
| GET | `/dashboard/pending-payments?page&per_page` | Outstanding payments | Bearer |
| GET | `/dashboard/alerts-status` | Document/license expiry | Bearer |
| GET | `/dashboard/financial-summary?period={period}` | Revenue/expense/profit (Owner/GM) | Bearer |

## Vehicles

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/vehicles` | List all vehicles | Bearer |
| POST | `/vehicles` | Create vehicle | Bearer |
| GET | `/vehicles/{id}` | Vehicle details | Bearer |
| PUT | `/vehicles/{id}` | Update vehicle | Bearer |
| DELETE | `/vehicles/{id}` | Delete vehicle | Bearer |
| POST | `/vehicles/{id}/documents` | Upload document | Bearer |
| GET | `/vehicles/{id}/maintenance-costs` | Maintenance costs | Bearer |
| POST | `/vehicles/{id}/maintenance-costs/bulk` | Bulk maintenance costs | Bearer |
| GET | `/vehicles/{id}/trip-costs` | Vehicle's trip costs | Bearer |

## Drivers

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/drivers` | List all drivers | Bearer |
| POST | `/drivers` | Create driver | Bearer |
| GET | `/drivers/{id}` | Driver details | Bearer |
| PUT | `/drivers/{id}` | Update driver | Bearer |
| DELETE | `/drivers/{id}` | Delete driver | Bearer |
| PATCH | `/drivers/{id}/toggle-active` | Activate/deactivate | Bearer |
| GET | `/drivers/{id}/costs` | Driver costs | Bearer |
| POST | `/drivers/{id}/costs` | Add driver cost | Bearer |
| POST | `/drivers/{id}/costs/bulk` | Bulk driver costs | Bearer |

## Trips

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/trips` | List all trips | Bearer |
| POST | `/trips` | Create trip | Bearer |
| GET | `/trips/{id}` | Trip details | Bearer |
| PUT | `/trips/{id}` | Update trip | Bearer |
| DELETE | `/trips/{id}` | Delete trip | Bearer |
| PATCH | `/trips/{id}/cancel` | Cancel trip | Bearer |
| GET | `/trips/{id}/costs` | Trip costs | Bearer |
| POST | `/trips/{id}/costs` | Add trip cost | Bearer |
| POST | `/trips/{id}/costs/bulk` | Bulk trip costs | Bearer |
| GET | `/trips/{id}/costs/summary` | Cost summary | Bearer |

## Customers

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/customers` | List customers | Bearer |
| POST | `/customers` | Create customer | Bearer |
| GET | `/customers/{id}` | Customer details | Bearer |
| PUT | `/customers/{id}` | Update customer | Bearer |

## Payments

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/payments` | List payments | Bearer |
| POST | `/payments` | Add payment | Bearer |
| GET | `/payments/{id}` | Payment details | Bearer |
| PUT | `/payments/{id}` | Update payment | Bearer |

## Team

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/team` | List team members | Bearer |
| POST | `/team` | Add team member | Bearer |
| GET | `/team/{id}` | Member details | Bearer |

## Reports (Owner/GM Only)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/reports/vehicle-pl` | Vehicle P&L | Bearer |
| GET | `/reports/trip-pl` | Trip P&L | Bearer |
| GET | `/reports/cost-analysis` | Cost analysis | Bearer |
| GET | `/reports/consolidated-pl` | Consolidated P&L | Bearer |

## Vehicle Finance

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/vehicle-finance` | List finance records | Bearer |
| POST | `/vehicle-finance` | Add purchase info | Bearer |
| GET | `/vehicle-finance/{id}` | Finance details | Bearer |
| PUT | `/vehicle-finance/{id}` | Update purchase info | Bearer |
| GET | `/vehicle-finance/{id}/payments` | EMI payment history | Bearer |

## Cost Types (No Auth Required)

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/cost-types/trip` | Trip cost type catalog | No |
| GET | `/cost-types/maintenance` | Maintenance cost type catalog | No |
| GET | `/cost-types/driver` | Driver cost type catalog | No |

## Cost Operations

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| POST | `/trip-costs` | Add single trip cost | Bearer |
| DELETE | `/trip-costs/{id}` | Delete trip cost | Bearer |
| POST | `/maintenance-costs` | Add maintenance cost | Bearer |
| DELETE | `/maintenance-costs/{id}` | Delete maintenance cost | Bearer |

## External APIs

### Google Places
| Endpoint | Purpose |
|----------|---------|
| `maps.googleapis.com/maps/api/place/autocomplete/json` | Location autocomplete (India bias) |
| `maps.googleapis.com/maps/api/place/details/json` | Place details (address, coordinates) |
| `maps.googleapis.com/maps/api/distancematrix/json` | Road distance calculation (driving) |
