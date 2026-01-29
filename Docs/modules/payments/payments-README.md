# Payments Module

## Overview

The Payments module handles payment recording, tracking, and financial reporting for trip-related transactions. It supports multiple payment types and modes with comprehensive tracking capabilities.

---

## Features

- Payment recording (advance, partial, final, refund)
- Multiple payment modes (cash, UPI, bank transfer, cheque)
- Customer-wise payment tracking
- Trip-wise payment history
- Payment status management
- TDS and discount handling
- Date range filtering
- PDF export for reports
- Payment summary statistics

---

## Screens

| Screen | Route | Description |
|--------|-------|-------------|
| Payments List | `FleetRoute.Payments` | List all payments with filters |
| Payment Detail | `FleetRoute.PaymentDetail` | Payment details |
| Add Payment | `FleetRoute.AddPayment` | Record new payment |
| Edit Payment | `FleetRoute.EditPayment` | Modify payment |

---

## Payment Entity

### Core Fields

| Field | Description | Required |
|-------|-------------|:--------:|
| Trip ID | Associated trip | ✅ |
| Amount | Payment amount | ✅ |
| Payment Type | Advance/Partial/Final/Refund | ✅ |
| Payment Mode | Cash/UPI/Bank Transfer/etc. | ✅ |
| Payment Date | Date of payment | ✅ |

### Deductions

| Field | Description | Required |
|-------|-------------|:--------:|
| TDS Amount | Tax deducted at source | ❌ |
| Discount Amount | Discount given | ❌ |
| Net Amount | Amount after deductions | Auto |

### Transaction Details

| Field | Description | Applicable Mode |
|-------|-------------|-----------------|
| Transaction ID | Reference number | UPI |
| UPI ID | UPI address | UPI |
| Bank Name | Bank name | Bank Transfer |
| Account Number | Account details | Bank Transfer |
| Cheque Number | Cheque reference | Cheque |

### Additional

| Field | Description | Required |
|-------|-------------|:--------:|
| Due Date | Payment due date | ❌ |
| Notes | Additional notes | ❌ |
| Receipt Number | Receipt reference | ❌ |

---

## Payment Types

| Type | Description | Use Case |
|------|-------------|----------|
| Advance | Booking payment | Before trip starts |
| Partial | Part payment | During or after trip |
| Final | Full settlement | Closes trip payment |
| Refund | Money returned | Cancellation/overpayment |

---

## Payment Modes

| Mode | Icon | Required Fields |
|------|------|-----------------|
| Cash | 💵 | None additional |
| UPI | 📱 | UPI ID |
| Bank Transfer | 🏦 | Bank Name, Account Number |
| Cheque | 📝 | Cheque Number, Bank Name |
| Card | 💳 | Transaction ID |

---

## Payment Status

| Status | Description | Color |
|--------|-------------|-------|
| Pending | Payment not received | Orange |
| Received | Payment collected | Green |
| Cancelled | Payment cancelled | Red |

---

## Payments List Screen

### Summary Cards (Top)

| Card | Description |
|------|-------------|
| Received | Total received amount in period |
| Pending | Total pending amount across all trips |
| This Month | Payments received this month |

### Filter Options

| Filter | Options |
|--------|---------|
| Date Range | From Date, To Date |
| Payment Type | All/Advance/Partial/Final/Refund |
| Payment Mode | All/Cash/UPI/Bank Transfer/etc. |
| Trip | Specific trip |
| Customer | Specific customer |

### List Item Display

| Field | Description |
|-------|-------------|
| Amount | Payment amount |
| Trip Route | Start → End location |
| Customer | Customer name |
| Date | Payment date |
| Mode | Payment mode icon |
| Status | Status badge |

---

## Add Payment Form

### Sections

1. **Trip Selection**
   - Select trip (dropdown with search)
   - Shows trip route and customer info

2. **Payment Details**
   - Amount (required)
   - Payment Type (dropdown)
   - Payment Mode (dropdown)
   - Payment Date & Time

3. **Deductions** (Collapsible)
   - TDS Amount
   - Discount Amount
   - Net Amount (calculated)

4. **Transaction Details**
   - Fields based on selected payment mode:
     - UPI: UPI ID
     - Bank Transfer: Bank Name, Account Number
     - Cheque: Cheque Number, Bank Name
     - Card: Transaction ID

5. **Due Date** (for Advance/Partial)
   - Due date for next payment

6. **Notes**
   - Additional remarks

### Validation Rules

| Rule | Description |
|------|-------------|
| Amount | Must be positive number |
| Amount vs Pending | Cannot exceed pending amount (except refund) |
| Payment Date | Cannot be future date |
| Transaction Details | Required based on payment mode |

---

## Payment Detail Screen

### Sections

1. **Header**
   - Amount (large)
   - Status badge
   - Payment date

2. **Trip Information**
   - Route (Start → End)
   - Vehicle number
   - Customer name
   - Customer contact (clickable)

3. **Payment Details**
   - Payment Type
   - Payment Mode
   - Transaction details (if any)

4. **Deductions** (if applicable)
   - TDS Amount
   - Discount Amount
   - Net Amount

5. **Additional Info**
   - Notes
   - Created by
   - Created date

### Actions

| Action | Description |
|--------|-------------|
| Edit | Modify payment details |
| Delete | Remove payment |
| Export PDF | Generate receipt |
| Call Customer | Dial customer number |

---

## Role-Based Permissions

| Action | Owner | GM | Manager | Supervisor |
|--------|:-----:|:--:|:-------:|:----------:|
| View Payments | ✅ | ✅ | ✅ | ❌ |
| Add Payment | ✅ | ✅ | ❌ | ❌ |
| Edit Payment | ✅ | ✅ | ❌ | ❌ |
| Delete Payment | ✅ | ✅ | ❌ | ❌ |
| Export PDF | ✅ | ✅ | ✅ | ❌ |
| View Summary | ✅ | ✅ | ✅ | ❌ |

---

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/trip-payments` | GET | List payments with filters |
| `/trip-payments` | POST | Create payment |
| `/trip-payments/{id}` | GET | Get payment details |
| `/trip-payments/{id}` | PUT | Update payment |
| `/trip-payments/{id}` | DELETE | Delete payment |
| `/trip-payments/summary` | GET | Payment summary stats |
| `/trip-payments/tds-report` | GET | TDS report |
| `/trips/{id}/payments` | GET | Payments for specific trip |
| `/trips/{id}/payments` | POST | Add payment to trip |

---

## PDF Export

### Payment Receipt

Generated PDF includes:
- Company header
- Payment details
- Trip information
- Customer details
- Amount breakdown
- Signature area

### Payments Report

Generated for date range with:
- Summary statistics
- Detailed payment list
- Total received/pending
- Grouped by payment type/mode

---

## Integration with Other Modules

### Trips Module

- Payment linked to specific trip
- Updates trip payment status
- Shows in trip cost summary

### Customers Module

- Customer-wise payment tracking
- Customer financial summary
- Pending payments per customer

### Dashboard Module

- Pending payments widget
- Financial overview statistics

---

## Date Handling

| Field | Format | Example |
|-------|--------|---------|
| Payment Date | ISO 8601 | 2026-01-15T14:30:00Z |
| Due Date | ISO 8601 | 2026-01-25T00:00:00Z |
| Display Format | DD MMM YYYY | 15 Jan 2026 |

---

## Related Modules

- [Trips](../trips/) - Trip payment tracking
- [Customers](../customers/) - Customer payments
- [Dashboard](../dashboard/) - Payment overview
- [Reports](../reports/) - Financial reports

---

## Related Documentation

- [Modules Overview](../README.md)
- [User Roles](../../user-roles/README.md)
