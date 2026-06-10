# Reports Module

## Overview

The Reports module provides financial analytics and profit/loss reporting for vehicles, trips, and the entire fleet. It includes date range filtering, multi-vehicle comparison, and PDF export capabilities.

---

## Features

- Vehicle profit/loss analysis
- Fleet-wide P&L consolidation
- Multi-vehicle comparison
- Date range filtering (daily, weekly, monthly, quarterly, custom)
- Cost breakdown by category
- PDF report generation
- Visual charts and graphs

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Reports Dashboard | `FleetRoute.Reports` | Reports hub with options |
| Vehicle P&L | `FleetRoute.VehicleProfitLoss` | Vehicle profit/loss |
| Trip P&L | `FleetRoute.TripProfitLoss` | Trip-wise analysis |
| Consolidated P&L | `FleetRoute.ConsolidatedPL` | Fleet overview |
| Cost Analysis | `FleetRoute.CostAnalysis` | Cost breakdown |

---

## Reports Dashboard

### Available Reports

| Report | Description | Access |
|--------|-------------|--------|
| Vehicle P&L | Profit/loss per vehicle | Owner, GM |
| Fleet P&L | Consolidated fleet P&L | Owner, GM |
| Trip Analysis | Trip-wise cost analysis | Owner, GM |
| Cost Analysis | Cost breakdown by category | Owner, GM |

---

## Vehicle P&L Screen

### Overview Mode

Displays all vehicles with their P&L summary:

| Metric | Description |
|--------|-------------|
| Vehicle | Registration number |
| Revenue | Total trip income |
| Expenses | Total costs |
| Profit/Loss | Net result |
| Margin | Profit percentage |

### Period Selector

| Period | Description |
|--------|-------------|
| Daily | Current day |
| Weekly | Current week |
| Monthly | Current month (default) |
| Quarterly | Current quarter |
| Custom | User-defined range |

### Vehicle Detail Mode

When a vehicle is selected:

| Metric | Description |
|--------|-------------|
| Total Revenue | Sum of trip prices |
| Total Expenses | Sum of all costs |
| Net Profit/Loss | Revenue - Expenses |
| Profit Margin | (Profit / Revenue) × 100 |
| Total Trips | Number of completed trips |
| Total Distance | Kilometers covered |
| Avg Profit per Trip | Profit / Trip Count |
| Avg Profit per KM | Profit / Distance |

### Cost Breakdown

| Category | Types Included |
|----------|----------------|
| Fuel Cost | Petrol, Diesel, CNG |
| Toll Cost | Toll charges, Border tax |
| Maintenance | All maintenance costs |
| Driver Costs | Allowance, advances |
| Other Costs | Loading, unloading, parking |

---

## Fleet P&L Screen

### Summary Metrics

| Metric | Description |
|--------|-------------|
| Total Vehicles | Number of vehicles in fleet |
| Total Revenue | Combined trip income |
| Total Expenses | Combined costs |
| Net Profit/Loss | Overall result |
| Average Margin | Fleet-wide margin |
| Profitable Vehicles | Count with profit |
| Loss-Making Vehicles | Count with loss |

### Vehicle Comparison

Table showing each vehicle's:
- Registration number
- Revenue
- Expenses
- Profit/Loss
- Status (Profit/Loss indicator)

### Visual Charts

| Chart | Description |
|-------|-------------|
| Revenue vs Expenses | Bar chart comparison |
| Profit Distribution | Pie chart by vehicle |
| Cost Breakdown | Pie chart by category |
| Trend | Line chart over time |

---

## Date Range Filtering

### Period Options

| Option | Date Range |
|--------|------------|
| Daily | Today |
| Weekly | Current week (Mon-Sun) |
| Monthly | Current calendar month |
| Quarterly | Current quarter |
| Custom | User-defined start/end |

### Custom Range

- Uses ijs-datetime-picker
- Start date (From)
- End date (To)
- Apply button to refresh data

---

## Vehicle Filter

### Filter Bottom Sheet

- Multi-select vehicle list
- Select All / Deselect All
- Search by registration
- Apply/Reset buttons

---

## PDF Report Generation

### Report Contents

1. **Header**
   - Company name
   - Report title
   - Date range
   - Generated date

2. **Summary Section**
   - Key metrics
   - Status indicators

3. **Vehicle Details** (for fleet P&L)
   - Table with all vehicles
   - Individual P&L values

4. **Cost Breakdown**
   - By category
   - Percentages

5. **Charts** (if supported)
   - Visual representations

### Export Options

| Format | Description |
|--------|-------------|
| PDF | Printable report |
| Share | Share via device options |

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Reports Dashboard | ✅ | ✅ | ❌ | ❌ |
| View Vehicle P&L | ✅ | ✅ | ❌ | ❌ |
| View Fleet P&L | ✅ | ✅ | ❌ | ❌ |
| View Cost Analysis | ✅ | ✅ | ❌ | ❌ |
| Export PDF | ✅ | ✅ | ❌ | ❌ |
| Custom Date Range | ✅ | ✅ | ❌ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/vehicles/{id}/profit-loss` | GET | Single vehicle P&L |
| `/reports/profit-loss/vehicles` | POST | Multi-vehicle P&L |
| `/reports/consolidated` | GET | Fleet P&L summary |
| `/reports/cost-analysis` | GET | Cost breakdown |
| `/reports/trends` | GET | Historical trends |

### Request Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| vehicle_ids | Array | List of vehicle IDs |
| start_date | String | Start date (DD-MM-YYYY) |
| end_date | String | End date (DD-MM-YYYY) |
| period | String | daily/weekly/monthly/quarterly |

---

## Calculations

### Profit/Loss

```
Net Profit = Total Revenue - Total Expenses

Where:
- Total Revenue = Sum of all trip prices (completed trips)
- Total Expenses = Trip Costs + Maintenance Costs
```

### Profit Margin

```
Profit Margin = (Net Profit / Total Revenue) × 100

If Revenue = 0, Margin = 0
```

### Average Per Trip

```
Avg Profit per Trip = Net Profit / Number of Completed Trips
```

### Average Per KM

```
Avg Profit per KM = Net Profit / Total Distance Covered
```

---

## Data Refresh

| Trigger | Action |
|---------|--------|
| Screen Load | Fetch data for default period |
| Period Change | Refresh with new date range |
| Pull to Refresh | Manual refresh |
| Vehicle Filter | Refresh with selected vehicles |

---

## Integration with Other Modules

### Vehicles Module

- Vehicle list for selection
- Vehicle details in report

### Trips Module

- Trip data for revenue calculation
- Trip costs included

### Costs Module

- Cost data for expense calculation
- Cost breakdown by type

### Dashboard Module

- Financial overview uses report data

---

## Related Modules

- [Vehicles](../vehicles/) - Vehicle data
- [Trips](../trips/) - Trip revenue
- [Costs](../costs/) - Cost data
- [Dashboard](../dashboard/) - Overview

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
