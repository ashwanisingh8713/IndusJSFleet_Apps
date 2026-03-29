# Cost Types — Overview & Navigation

IndusJS Fleet tracks **five distinct financial categories** across the fleet lifecycle. Each operational cost category follows a hierarchical structure: **Category → Group → Item**, with structured IDs using a prefix convention (`TC-`, `MC-`, `DC-`). Cost types are **dynamically fetched from the API** and cached locally; hardcoded fallback values exist for offline use.

---

## Fleet Cost Universe

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        FLEET COST UNIVERSE                                  │
│                                                                             │
│  ┌──────────────┐  ┌──────────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ Trip Costs   │  │ Maintenance Costs │  │ Driver Costs │  │ Payments & │  │
│  │  (TC-xxx)    │  │   (MC-xxx)        │  │  (DC-xxx)    │  │  Finance   │  │
│  │              │  │                   │  │              │  │            │  │
│  │ Per-trip     │  │ Per-vehicle       │  │ Per-driver   │  │ Revenue +  │  │
│  │ operational  │  │ maintenance &     │  │ salary,      │  │ Capital    │  │
│  │ expenses     │  │ repair expenses   │  │ bonus,       │  │ expenditure│  │
│  │              │  │                   │  │ deductions   │  │            │  │
│  └──────┬───────┘  └────────┬──────────┘  └──────┬───────┘  └─────┬──────┘  │
│         │                   │                    │                │          │
│         └───────────────────┴────────────────────┘                │          │
│                          │                                        │          │
│                   Aggregated into                           Tracked by       ���
│                   P&L Reports                              Finance module    │
│                   (screen-report)                          (screen-finance)  │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Quick Summary

| Category | Prefix | Scope | Groups | Items | Entry Module | Linked To |
|----------|--------|-------|--------|-------|--------------|-----------|
| Trip Costs | `TC-` | Per-trip | 6 | 22 | `screen-trip` | Trip + Vehicle |
| Maintenance Costs | `MC-` | Per-vehicle | 6 | 23 | `screen-vehicle` | Vehicle |
| Driver Costs | `DC-` | Per-driver | 4 | 19 | `screen-driver` | Driver (+ Trip optional) |
| Trip Payments | — | Per-trip | — | — | `screen-payment` | Trip + Customer |
| EMI / Loan | — | Per-vehicle | — | — | `screen-finance` | Vehicle Purchase |

## Shared DTO Hierarchy (defined in `ijs-core-lib`)

```
CostTypeCategoryDto            ← Top-level container (category_id, category_name)
  └── CostTypeGroupDto[]       ← Groups (group_id, group_name)
        └── CostTypeItemDto[]  ← Items (id, value, label)
```

All three operational cost categories (`TC`, `MC`, `DC`) share this same DTO hierarchy.

## ID Convention

| Pattern | Meaning | Example |
|---------|---------|---------|
| `TC-G-xxx` | Trip Cost **Group** | `TC-G-001` = Fuel & Energy |
| `TC-xxx-xxx` | Trip Cost **Item** | `TC-001-002` = Diesel |
| `MC-G-xxx` | Maintenance Cost **Group** | `MC-G-003` = Tyres & Wheels |
| `MC-xxx-xxx` | Maintenance Cost **Item** | `MC-003-001` = Tyre Replacement |
| `DC-G-xxx` | Driver Cost **Group** | `DC-G-003` = Deductions |
| `DC-xxx-xxx` | Driver Cost **Item** | `DC-003-003` = Fine |

**ID structure:** `{Prefix}-{GroupNum}-{ItemNum}`
**Group ID structure:** `{Prefix}-G-{GroupNum}`

---

## Split Documentation

| Doc | Contents |
|-----|----------|
| [01-trip-costs.md](01-trip-costs.md) | TC-* groups & items, fuel-specific fields, date format, legacy mapping, API endpoints |
| [02-maintenance-costs.md](02-maintenance-costs.md) | MC-* groups & items, unique vendor fields, date format, legacy mapping, API endpoints |
| [03-driver-costs.md](03-driver-costs.md) | DC-* groups & items, deduction logic, summary formula, trip linkage, API endpoints |
| [04-cost-relations-and-aggregation.md](04-cost-relations-and-aggregation.md) | TC→DC auto-sync, P&L formulas, Dashboard cost overview, module usage matrix |
| [05-payments-and-finance.md](05-payments-and-finance.md) | Combined overview (legacy — see 05a and 05b for details) |
| [05a-trip-payments.md](05a-trip-payments.md) | Trip payment lifecycle, enums (type/mode/status), domain entities, filtering, relation to P&L, API endpoints, role-based access |
| [05b-vehicle-finance.md](05b-vehicle-finance.md) | Vehicle purchase, loan/EMI tracking, enums (loan status/payment status/mode), domain entities, EMI calculation, filtering, alerts, API endpoints |
| [06-cost-infrastructure.md](06-cost-infrastructure.md) | Dynamic fetch + cache + fallback chain, API response format, UI utilities, source file reference |
| [07-profit-loss-analysis.md](07-profit-loss-analysis.md) | **Comprehensive P&L:** Trip/Vehicle/Driver/Customer profitability, formulas, filters, sorts, profitability thresholds, included vs excluded costs, API reference |

