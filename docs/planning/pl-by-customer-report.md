# Plan — P&L by Customer report (best real-use-case of A's additive endpoint)

## Why this one
Of A's two optional items, the **P&L-by-customer report** delivers the most owner value: which customers
are actually profitable (revenue/cost/net/margin), how well they pay (collection rate, pending), and
avg profit/trip — directly actionable for pricing/retention. It also surfaces the new
`GET /api/v1/reports/profit-loss/customers` endpoint and fits the existing Reports hub. (The trips
`?customer_id=` filter is a minor convenience already partly covered by the customer-detail trips tab.)

## Endpoint
`GET /api/v1/reports/profit-loss/customers?period=monthly` (financial permission). Response wrapped in the
standard `ProfitLossResponse<...>` envelope. `data`: `{ period, start_date, end_date, summary{...},
customers:[{...}], customer_count }`.

## Design (Clean + MVI, mirror Consolidated/Vehicle P&L)
- **Data:** `CustomerPLDto` (report + summary + item) → domain `CustomerPLReport/Summary/Item` via
  `ProfitLossMapper` → `ReportsRemoteDataSource.getCustomerProfitLoss(token, period)` (GET + `period`,
  parse `ProfitLossResponse<CustomerPLReportDto>`) → `ReportsRepository.getCustomerProfitLoss(period)` →
  `GetCustomerPLUseCase`.
- **Presentation:** `CustomerPLContract` (state: selectedPeriod, isLoading, error, result; intents:
  SelectPeriod, Load, Refresh), `CustomerPLViewModel` (load on init + period change), `CustomerPLScreen`
  (period chips → summary KPIs → ranked customer list rows: name, trips, revenue, net, margin %,
  collection %, pending). Loading/error(retry)/empty handled. Reuse `FleetTokens` + theme colours; strings EN+HI.
- **Wiring:** `ReportsFeatureFacade.CustomerPLEntry` + `onNavigateToCustomerPL` on `ReportsHubEntry`;
  `ViewModelProvider.customerPLViewModel()`; `DefaultViewModelProvider` use case + VM; `FleetRoute.CustomerProfitLoss`;
  `FleetNavigation` entry + hub wiring; a "P&L by Customer" card in the Reports hub (financial-gated).
- `ApiConfig.REPORTS_PL_CUSTOMERS`.

## Best practices / reuse
- Mirrors the established report vertical slice (DTO→domain→mapper→ds→repo→usecase→MVI→nav). DTO fields
  nullable+default. No backend/contract risk (additive, GET).

## Edge cases
- No completed trips in period → empty state ("No customer P&L for this period").
- Financial permission gates the hub card (consistent with other P&L reports).
- Long lists → lazy list; long names → ellipsis.

## Verification
- `:androidApp:assembleDebug`; open Reports → P&L by Customer → period switch loads ranked customers. D verifies on-device.
