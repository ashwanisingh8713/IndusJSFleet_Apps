# Customers Module

## Overview

The Customers module manages customer information, contact details, and provides comprehensive financial tracking with trip history integration.

---

## Features

- Customer CRUD operations
- Contact management (primary, secondary)
- Address management (billing, shipping)
- GST and PAN tracking
- Trip history per customer
- Financial tracking (receivables, payments)
- Pending payment tracking
- Local database caching for offline access
- PDF report generation

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Customers List | `FleetRoute.Customers` | List all customers |
| Customer Detail | `FleetRoute.CustomerDetail` | Customer details with tabs |
| Add Customer | `FleetRoute.CreateCustomer` | Add new customer |

---

## Customer Entity

### Company Information

| Field | Description | Required |
|-------|-------------|:--------:|
| Company Name | Business name | ✅ |
| Person Name | Contact person | ✅ |
| GST Number | GST registration | ❌ |
| PAN Number | PAN card number | ❌ |

### Contact Details

| Field | Description | Required |
|-------|-------------|:--------:|
| Primary Contact | Main phone number | ✅ |
| Secondary Contact | Alternate phone | ❌ |
| Email | Email address | ❌ |

### Address

| Field | Description | Required |
|-------|-------------|:--------:|
| Billing Address | Invoice address | ❌ |
| Shipping Address | Delivery address | ❌ |

### Status

| Field | Description | Required |
|-------|-------------|:--------:|
| Is Active | Customer status | Auto |
| Notes | Additional remarks | ❌ |

### Aggregates (Read-only)

| Field | Description |
|-------|-------------|
| Total Trips | Number of trips for customer |
| Total Revenue | Total billed amount |
| Pending Amount | Outstanding balance |

---

## Customer Detail Tabs

### 1. Overview Tab

Displays customer information:
- Company details
- Contact information (with call icons)
- Addresses
- GST/PAN details
- Status badge
- Notes

### 2. Trips Tab

Shows trip history for customer:
- Trip list with pagination
- Filter by status
- Route, vehicle, date display
- Click to view trip details

### 3. Pending Tab

Outstanding payments:
- List of trips with pending amounts
- Due dates
- Payment status
- Total pending summary

### 4. Payments Tab

Payment history:
- All payments received
- Payment type and mode
- Date and amount
- Click for payment details

### 5. Financials Tab

Financial summary:
- Total revenue
- Total received
- Total pending
- Date range filter
- Export to PDF option

---

## Add Customer Form

### Sections

1. **Company Details**
   - Company Name (required)
   - Person Name (required)

2. **Contact Information**
   - Primary Contact (required, 10 digits)
   - Secondary Contact (optional)
   - Email (optional)

3. **Tax Information**
   - GST Number (15 characters, format validated)
   - PAN Number (10 characters, format validated)

4. **Addresses**
   - Billing Address
   - Shipping Address
   - Copy billing to shipping option

5. **Additional**
   - Notes

### Validation Rules

| Field | Validation |
|-------|------------|
| Primary Contact | 10 digits, numeric |
| Secondary Contact | 10 digits if provided |
| Email | Valid email format |
| GST Number | 15 alphanumeric, specific format |
| PAN Number | 10 alphanumeric, specific format |

---

## Customer Selection in Create Trip

When creating a trip, customers are selected from:

1. **Local Database** - Cached customers for quick access
2. **Search** - Type to filter customers
3. **Add New** - Create customer inline

Customer selection populates:
- Customer Name
- Customer Contact
- Customer ID (for linking)

---

## Local Database Caching

### Sync Strategy

| Trigger | Action |
|---------|--------|
| App Launch | Background sync |
| Pull to Refresh | Manual sync |
| Customer List Screen | Load from local first, then refresh |
| Create Trip Screen | Load from local database |
| After Create Customer | Save to local and sync to server |

### Cached Fields

- ID
- Company Name
- Person Name
- Primary Contact
- Secondary Contact
- Email
- Is Active

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Customers | ✅ | ✅ | ✅ | ✅ |
| Add Customer | ✅ | ✅ | ✅ | ❌ |
| Edit Customer | ✅ | ✅ | ✅ | ❌ |
| Delete Customer | ✅ | ✅ | ❌ | ❌ |
| View Financials | ✅ | ✅ | ❌ | ❌ |
| View Pending | ✅ | ✅ | ✅ | ❌ |
| Export PDF | ✅ | ✅ | ✅ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/customers` | GET | List customers with pagination |
| `/customers` | POST | Create customer |
| `/customers/{id}` | GET | Get customer details |
| `/customers/{id}` | PUT | Update customer |
| `/customers/{id}` | DELETE | Delete customer |
| `/customers/{id}/trips` | GET | Customer trip history |
| `/customers/{id}/pending-payments` | GET | Pending payments |
| `/customers/{id}/payments` | GET | Payment history |
| `/customers/{id}/financials` | GET | Financial summary |

---

## Customer Financials

### Summary Metrics

| Metric | Description |
|--------|-------------|
| Total Revenue | Sum of all trip prices |
| Total Received | Sum of all payments |
| Total Pending | Revenue minus received |
| Trip Count | Number of trips |
| Average Trip Value | Revenue / Trip Count |

### Date Range Filter

| Period | Description |
|--------|-------------|
| This Month | Current calendar month |
| Last Month | Previous calendar month |
| This Quarter | Current quarter |
| This Year | Current financial year |
| Custom | User-defined range |

---

## Call Integration

Contact numbers support direct calling:
- Click call icon next to phone number
- Opens device phone dialer
- Available for primary and secondary contacts

---

## PDF Export

### Customer Statement

Generated PDF includes:
- Customer details
- Trip list with dates and amounts
- Payment history
- Outstanding balance
- Date range

---

## Integration with Other Modules

### Trips Module

- Customer selection in Create Trip
- Customer info displayed in Trip Detail
- Trip history in Customer Detail

### Payments Module

- Customer-wise payment tracking
- Pending payments list
- Payment history

### Dashboard Module

- Customer pending payments in overview

---

## Related Modules

- [Trips](../trips/) - Trip creation
- [Payments](../payments/) - Payment tracking
- [Dashboard](../dashboard/) - Pending overview

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
