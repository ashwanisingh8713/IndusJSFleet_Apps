# Plan — Trip↔Customer reporting (A's additive CONTRACT_CHANGE 1782091226)

## Trigger
Backend additive changes (nothing breaks if deferred):
1. Trip P&L (`GET /trips/:id/profit-loss`) now returns `customer_id` + `customer_name`.
2. Trips list supports `?customer_id=<id>` filter.
3. New `GET /reports/profit-loss/customers` (P&L by customer).

## Scope decision
- **DO now (the explicit "ACTION FOR B"):** add `customer_id` + `customer_name` to the trip P&L DTO
  (`TripProfitLossDto`) and thread through the domain entity (`TripProfitLoss`) + mapper so the value is
  available to the UI; show the customer on the trip P&L display if a spot exists.
- **DEFER (explicitly optional):** trips-list `?customer_id=` filter UI, and the new P&L-by-customer
  endpoint + screen (a sizeable new feature). Note as follow-ups. ignoreUnknownKeys means nothing breaks
  meanwhile.

## Approach
- `TripProfitLossDto`: add `@SerialName("customer_id") customerId: Int? = null`,
  `@SerialName("customer_name") customerName: String? = null` (nullable + default, per DTO rules).
- `TripProfitLoss` domain + `ProfitLossMapper`: carry the two fields through.
- UI: surface `customerName` on the trip P&L detail if such a screen renders it.

## Best practices
- Additive, backward-compatible; DTO fields nullable+default; no contract break. MVI/data-flow unchanged.

## Verification
- `:androidApp:assembleDebug`; trip P&L now carries customer; defer items noted.
