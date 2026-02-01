# Fleet Management API - Postman Collections

This folder contains modular Postman collections for the Fleet Management API v2.

## 🚀 Quick Start

### Option 1: Master Collection (Recommended for quick start)
Import the single master collection file:
```
/Fleet_Management_API_v2.postman_collection.json
```

### Option 2: Modular Collections (Recommended for development)
Import individual module collections from this folder for better organization.

---

## Modular Collections

| File | Module | Description |
|------|--------|-------------|
| `00-api-info.postman_collection.json` | API Info | API versioning & health (no auth) |
| `01-authentication.postman_collection.json` | Authentication | Sign up, login, password reset |
| `02-profile.postman_collection.json` | Profile | User profile management |
| `03-team-management.postman_collection.json` | Team | Team member CRUD |
| `03b-caretaker.postman_collection.json` | Caretaker | Caretaker assignments |
| `04-drivers.postman_collection.json` | Drivers | Driver management |
| `05-vehicles.postman_collection.json` | Vehicles | Vehicle management |
| `06-documents.postman_collection.json` | Documents | Document upload & tracking |
| `07-trips.postman_collection.json` | Trips | Trip management |
| `08-trip-costs.postman_collection.json` | Trip Costs | Trip cost tracking |
| `09-maintenance-costs.postman_collection.json` | Maintenance | Vehicle maintenance costs |
| `10-dashboard.postman_collection.json` | Dashboard | Dashboard & overview |
| `11-reports.postman_collection.json` | Reports | P&L reports (Owner/GM only) |
| `12-driver-costs.postman_collection.json` | Driver Costs | Driver cost tracking |
| `12-location.postman_collection.json` | Location | GPS tracking |
| `13-health.postman_collection.json` | Health | API health & metrics |
| `14-customers.postman_collection.json` | Customers | Customer management |
| `15-vehicle-purchase-emi.postman_collection.json` | Purchase & EMI | Vehicle purchase & EMI tracking (Owner/GM) |

---

## How to Import

### Import Master Collection
1. Open Postman
2. Click **Import** button
3. Select `/Fleet_Management_API_v2.postman_collection.json` from root folder
4. The complete collection will be added

### Import Individual Modules
1. Open Postman
2. Click **Import** button
3. Select specific `.json` files from this folder
4. Each module will be added as a separate collection

### Import All Modules at Once
1. Open Postman
2. Click **Import** button
3. Select all `.json` files from this folder
4. All 18 collections will be added to your workspace

---

## Variables

Each collection includes these common variables:

| Variable | Default Value |
|----------|---------------|
| `base_url` | `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2` |
| `api_base` | `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api` |
| `token` | (empty - set after login) |
| `vehicle_id` | `1` |
| `driver_id` | `1` |
| `trip_id` | `1` |
| `customer_id` | `1` |

### Setting Token After Login
1. Run the **Login** request in `01-authentication` collection
2. The token is automatically saved to the `token` variable
3. All other requests use this token for authentication

---

## Role-Based Access

### Owner & General Manager
- ✅ Full access to all collections

### Manager
- ✅ Access most endpoints
- ❌ Cannot access: `11-reports` (P&L endpoints)
- ❌ Cannot see `trip_price` in trip responses
- ❌ Cannot access financial data in dashboard

### Supervisor
- ✅ View-only access
- ❌ Cannot access: `11-reports` (P&L endpoints)
- ❌ Cannot delete costs
- ❌ Cannot create/edit vehicles, drivers, trips
- ❌ Cannot see financial data

---

## Troubleshooting

### Token Not Working
1. Make sure you ran the Login request first
2. Check that the test script saved the token
3. Verify the token variable is set in collection variables

### 401 Unauthorized
1. Token may have expired - login again
2. Check that Authorization header has `Bearer {{token}}`

### Import Errors
If you experience import issues:
1. Try importing the modular collections individually
2. Ensure you have the latest version of Postman

---

*Last Updated: January 2026*
