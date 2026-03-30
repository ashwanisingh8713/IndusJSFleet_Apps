# Entity States & Cost Types — IndusJS Fleet

## Vehicle States

```
inactive ←→ active → on_route → active
                   → maintenance ←→ damaged → decommissioned
```

| State | Description |
|-------|-------------|
| `inactive` | Not operational |
| `active` | Available for trips |
| `on_route` | Currently on a trip |
| `maintenance` | Under repair |
| `damaged` | Damaged, needs assessment |
| `decommissioned` | Permanently retired (terminal) |

## Driver States

```
inactive ←→ active → on_route → active
                   → on_leave → active
                   → suspended → active
                   → terminated (terminal)
```

## Trip States

```
planned → on_route → completed
planned → cancelled
on_route → failed
on_route → delayed
```

## Cost Types

### Trip Costs
`fuel, toll, driver_allowance, parking, loading_charges, unloading_charges, chalan, permit, insurance, other`

### Maintenance Costs
`tyre, battery, servicing, engine_repair, body_repair, electrical, ac_repair, other`

### Driver Costs
`salary, advance, bonus, penalty`

## Cargo Types
`Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others`

## Payment Status
`pending, partial, paid`

## Payment Modes
`cash, upi, bank_transfer, cheque, card`

## Key Files

- `ijs-core-lib/.../core/constants/StatusConstants.kt` — All state enums
- `ijs-core-lib/.../core/util/CostTypeUtils.kt` — Cost type labels/mappings
- `ijs-core-lib/.../data/model/CostModels.kt` — Shared cost DTOs

