# Fleet Management API - Postman Collections

This folder contains modular Postman collections for the Fleet Management API v2.

## Collections

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
| `12-location.postman_collection.json` | Location | GPS tracking |
| `13-health.postman_collection.json` | Health | API health & metrics |

## How to Import

### Import Individual Collection
1. Open Postman
2. Click **Import** button
3. Select the specific collection file you need
4. The collection will be added to your workspace

### Import All Collections
1. Open Postman
2. Click **Import** button
3. Select all `.json` files from this folder
4. All collections will be added to your workspace

## Variables

Each collection includes these common variables:

| Variable | Default Value |
|----------|---------------|
| `base_url` | `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2` |
| `api_base` | `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api` |
| `token` | (empty - set after login) |

### Setting Token After Login
1. Run the **Login** request in `01-authentication` collection
2. The token is automatically saved to the `token` variable
3. All other collections use this token for authentication

## Role-Based Access

### Owner & General Manager
- Full access to all collections

### Manager
- ❌ Cannot access: `11-reports` (P&L endpoints)
- ❌ Cannot see `trip_price` in trip responses
- ❌ Cannot access cost overview in dashboard

### Supervisor
- ❌ Cannot access: `11-reports` (P&L endpoints)
- ❌ Cannot delete costs
- ❌ Cannot create/edit vehicles, drivers, trips
- ❌ Cannot see financial data

## Full Collection

The complete combined collection is available at:
```
/Fleet_Management_API_v2.postman_collection.json
```

This master collection includes all endpoints from all modules in one file.

