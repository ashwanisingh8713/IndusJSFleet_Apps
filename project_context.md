# IndusJS Fleet — Project Context

> A single document that explains **what this project is, how it is built, and how to work
> inside it**. Written for new developers and AI coding assistants.
> Keep it updated whenever architecture or conventions change.

---

## 1. One-Paragraph Summary

IndusJS Fleet is a **Kotlin Multiplatform (KMP)** fleet-management SaaS app for
transport businesses. One shared codebase produces **Android, iOS, and Web (JS + WasmJS)**
apps. It manages vehicles, drivers, trips, costs, customer payments, team members,
vehicle loans/EMIs, reports (P&L), document-expiry alerts, and real-time GPS tracking.

The full ecosystem is **three sibling repositories** under `/Users/ashwani/Upgrade/Pragmatic/`:

| Repo | Role |
|------|------|
| `IndusJSFleet_Apps` | This repo — the KMP mobile/web app |
| `IndusJSFleet_GoLang_Backend` | Fleet REST API (Go + Gin + Postgres) on Google Cloud Run |
| `IndusJS-IAM` | Identity & Access Management service — users, tenants, roles, permissions, JWTs, billing plans |

---

## 2. Repository Layout (28 Gradle modules)

```
IndusJSFleet/
│
│  ── App shells (thin) ─────────────────────────────────────────
├── androidApp/            Android entry (FleetApplication + AppActivity, Crashlytics)
├── iosApp/                Xcode project, consumes SharedUI framework
├── webApp/                JS + WasmJS browser entry (single main.kt)
├── locationTracker/       SEPARATE Android APK — driver GPS publisher via MQTT
│
│  ── Orchestrator ─────────────────────────────────────────────
├── sharedUI/              Navigation, DI wiring, theming, App.kt.
│                          ⚠️ NO screens, NO business logic live here.
│
│  ── Feature modules (data + domain + presentation) ───────────
├── screen-vehicle/        com.ijs.vehicle    — vehicles, documents, maintenance costs
├── screen-driver/         com.ijs.driver     — drivers, licenses, driver costs
├── screen-trip/           com.ijs.trip       — trips, routes, trip costs
├── screen-customer/       com.ijs.customer   — customers (+ local caching)
├── screen-trip-payment/   trip payment data layer
├── screen-payment/        com.ijs.subscription — payments UI + SaaS subscription/billing
├── screen-team/           com.ijs.team       — team members (+ local caching)
├── screen-report/         com.ijs.reports    — P&L reports (requires `financials:read`)
├── screen-finance/        com.ijs.finance    — vehicle purchase, loans, EMI
│
│  ── Feature modules (presentation only; data in ijs-network-lib) ──
├── screen-user/           com.ijs.user       — login, signup, OTP, profile
├── screen-onboarding/     com.ijs.onboarding — first-run onboarding
├── screen-dashboard/      com.ijs.dashboard  — main dashboard + nav drawer
├── screen-alerts/         com.ijs.alerts     — expiry alerts list
├── screen-map/            com.ijs.map        — live tracking map (MQTT subscribe)
│
│  ── Foundation libraries ─────────────────────────────────────
├── ijs-core-lib/          MVI base classes, shared DTOs, validation, constants
├── ijs-network-lib/       Ktor client, auth, ApiConfig, shared data sources
├── ijs-error-lib/         Result<T>, exception hierarchy (zero deps)
├── ijs-dispatcher-lib/    DispatcherProvider abstraction (never use Dispatchers.IO directly)
├── ijs-datetime-utils/    FleetDateTime — DD-MM-YYYY ↔ ISO 8601 conversion
├── ijs-datetime-picker/   Compose date/time picker component
├── ijs-pdf-report/        HTML → PDF per platform (WebView / WKWebView / browser)
├── ijs-ui-components-lib/ Fleet* components, theme, icons, strings (en + hi)
├── ijs-logger-lib/        KermitFleetLogger (implements FleetLogger from core)
│
│  ── Docs & meta ──────────────────────────────────────────────
├── Docs/                  API specs, postman collections, DB schemas, plans
├── cursor_doc/            Generated architecture documentation
├── TODO/Todo.txt          Current roadmap notes
└── .github/               copilot-instructions.md + 32 instruction files + prompts
```

### Dependency direction (simplified)
```
app shells → sharedUI → screen-* → ijs-network-lib → ijs-core-lib
                                                       ├── ijs-error-lib
                                                       ├── ijs-dispatcher-lib
                                                       └── ijs-datetime-utils
locationTracker → (standalone, no sharedUI dependency)
```

---

## 3. Architecture Rules (STRICT)

### 3.1 Clean Architecture per feature module
```
screen-{feature}/src/commonMain/kotlin/com/ijs/{feature}/
├── domain/
│   ├── entity/        pure Kotlin data classes
│   ├── repository/    interfaces only
│   └── usecase/       business actions
├── data/
│   ├── model/         DTOs (@Serializable + @SerialName, snake_case)
│   ├── mapper/        DTO ↔ Entity
│   ├── datasource/    remote (+ local where cached)
│   └── repository/    implementations
└── presentation/
    ├── {Feature}Contract.kt    State / Intent / Effect
    ├── {Feature}ViewModel.kt   extends MviViewModel
    ├── {Feature}Screen.kt      internal Compose UI
    └── {Feature}FeatureFacade.kt   ← ONLY public API of the module
```

### 3.2 MVI everywhere
- `MviViewModel<State, Intent, Effect>` from `ijs-core-lib`.
- State is immutable; update only via `updateState { copy(...) }`.
- Effects are one-time events (snackbars). **Navigation is via lambda callbacks**, not effects.
- Never `mutableStateOf` inside a ViewModel.

### 3.3 Facade + Navigation isolation
- Feature modules **never import `FleetRoute`**. Navigation comes in as lambdas.
- `sharedUI/navigation/FleetRoute.kt` — all ~45 routes (`@Serializable sealed interface`);
  `Notifications` / `NotificationSettings` are planned additions (PRD Feat-NTF).
- `sharedUI/navigation/FleetNavigation.kt` — maps routes → facade entries.
- Cross-feature data needs use **adapter interfaces** (e.g. `TripProviderForPayment`
  in screen-payment, implemented by `TripProviderAdapter` in `sharedUI/di/adapter/`).

### 3.4 DI (Metro + manual wiring)
```
App.kt → DefaultViewModelProvider (singleton)
  → NetworkDataGraph (HttpClient, Json, Settings, UserLocalDataSource)
  → FeatureRepositoryFactory (builds all feature repositories)
  → use cases → ViewModels
```
- `@Inject` goes **on the class**, not the constructor.
- Compose access: `rememberViewModel { xViewModel() }` or
  `rememberSharedViewModel("key") { ... }` for ViewModels shared across screens
  (e.g. `"finance_$vehicleId"`, `"driverDetail_$driverId"`).

### 3.5 Hard limits
- **Max 500 lines per class file.** Split by responsibility when exceeded.
- No backward-compat shims — project is in initial development.
- Centralized build conventions: never set `compileSdk`/`minSdk`/`jvmTarget` in module
  build files — `gradle/fleet-android-conventions.gradle` does it
  (compileSdk 36, minSdk 23, JVM 17).

---

## 4. Backend & API

| Item | Value |
|------|-------|
| Backend repo | `../IndusJSFleet_GoLang_Backend` (Go + Gin + Postgres) |
| IAM repo | `../IndusJS-IAM` (identity, tenants, RBAC, tokens, plans) |
| Current base URL | `https://indusjsfleet-api-clean-architecture-refactor-960880113496.asia-south1.run.app/api/v1` |
| Config location | `ijs-network-lib/.../core/network/ApiConfig.kt` |
| Auth | `Authorization: Bearer <token>` (JWT from IAM, must contain `tid` claim); 401 → `AuthenticationManager.notifyAuthExpired()` → Login; 403 → permission denied (do NOT logout) |
| Response wrapper | `{ "success": bool, "message": str, "data": ... }`; errors: `{ errorCode, errorMessage, developerMessage, statusCode }` |
| API docs | `Docs/api_modules/`, `Docs/postman_collections/` (canonical schemas) |
| IAM specs | `Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md`, `Docs/BACKEND_DRIVER_IAM_ROLE_SETUP.md` |

### Date/time formats (CRITICAL — most common bug source)
| Context | Format | How |
|---------|--------|-----|
| Trip create/update | ISO 8601 (`2026-06-11T14:30:00Z`) | `FleetDateTime.toIso8601(date, time)` |
| Cost entries (trip/maintenance/driver) | `DD-MM-YYYY` + `HH:MM` | send as-is from UI |
| Driver cost `month` | `YYYY-MM` | derive from date |
| Document/license expiry | `DD-MM-YYYY` | send as-is |
| UI display | `DD-MM-YYYY`, 24h `HH:MM` | default |

### Known backend response quirks (handle defensively)
1. **Double nesting**: some endpoints (e.g. `GET /drivers/{id}/costs`) wrap payloads as
   `{ data: { data: {costs,...}, page, total_pages, ... } }`. DTOs must match the real shape —
   always verify against postman collections, not assumptions.
2. **Pagination at root**: sometimes `pagination` is a sibling of `data`, not inside it.
3. **Field-name drift**: backend Go structs are the source of truth
   (e.g. `total_salary`/`net_earnings`, `created_count`).

### DTO safety rules (learned from production bugs)
- **Every DTO field gets a default value.** A missing field must never throw
  `MissingFieldException` — that failure mode silently blanks screens.
- All DTOs use `@JsonIgnoreUnknownKeys` (or lenient Json config).
- Repositories must **propagate** backend errors via `Result.Error` — never
  swallow and return empty lists ("no data" is not the same as "request failed").

---

## 5. App Startup & Subscription Gate

On every launch, `App.kt`:
1. Checks onboarding-completed flag → if not, show Onboarding.
2. Checks `isLoggedIn` (token in Settings) → if not, show Login.
3. Calls `checkSubscriptionGate()` (`DefaultViewModelProvider`) which queries
   `GET /onboarding/status` and resolves to one of:

| Gate result | Initial route |
|-------------|---------------|
| `RequiresPlanSelection` | SubscriptionPlans |
| `RequiresPayment` | SubscriptionPlans(isPaymentPending = true) |
| `RequiresTenantCreation` | CreateOrganization |
| `RequiresTeamMemberCreation` | (team setup) |
| `NoGate` | Dashboard |

**Fallback rule:** if the status API fails and the user has no tenant locally,
default to `RequiresPlanSelection` (start of the funnel) — never skip ahead.

Full new-owner funnel:
`SignUp → OTP → Plan → Razorpay checkout → Success → CreateOrganization → Team → Dashboard`

---

## 6. Domain Reference

### State machines
```
Vehicle:  inactive ←→ active → on_route → active
                            → maintenance ←→ damaged → decommissioned (final)
Driver:   inactive ←→ active → on_route | on_leave | suspended → active
                            → terminated (final)
Trip:     planned → on_route → completed
          planned → cancelled        on_route → failed | delayed
```

### Cost types (3-level taxonomy: category → group → item)

Every cost entry stores `cost_id` (item), `cost_label`, `group_id`. Groups
(source: backend `internal/shared/costtype/costtype.go`; full item lists in PRD §7.4):

| Category | Groups |
|----------|--------|
| Trip (TC-001) | `TC-G-001` Fuel & Energy · `TC-G-002` Toll & Parking · `TC-G-003` Loading & Unloading · `TC-G-004` Driver Expenses · `TC-G-005` Permits & Compliance · `TC-G-006` Miscellaneous |
| Maintenance (VMC-001) | `VMC-G-001` Regular Maintenance · `VMC-G-002` Repairs & Replacements · `VMC-G-003` Electrical & AC · `VMC-G-004` Body & Exterior · `VMC-G-005` Engine & Transmission · `VMC-G-006` Miscellaneous · `VMC-G-007` Wheels & Tires |
| Driver (DC-001) | `DC-G-001` Salary & Wages · `DC-G-002` Incentives & Bonuses · `DC-G-003` Deductions · `DC-G-004` Other |

### Payments
- Modes: cash, upi, bank_transfer, cheque, card
- Payment status: received, pending, cancelled · Trip-level: pending, partial, paid

### Roles & Permissions (IAM RBAC — permission-first model)

The system is **multi-tenant**: each organization is a tenant in **IndusJS-IAM**.
**The product is permission-first, not title-first** — a role is just a *named
bundle of permissions* with a numeric level (IAM `rbac.Role.Level`: owner=100,
admin=50, moderator=30, member=10). Any user with the right permissions can access
any feature/screen; levels only govern who may manage whom.
Tenant onboarding auto-seeds three role bundles; a `driver` bundle is provisioned per app/tenant.

| IAM bundle | Level | Default contents | Legacy Fleet label (deprecated, being removed) |
|------------|:-----:|------------------|------------------------------------------------|
| `owner` | 100 | Everything incl. `financials:read`, `users:*` | owner / general_manager |
| `admin` | 50 | Operational CRUD; **no** `financials:read` | manager |
| `user` | 10 | View + trip status + own cost entries | supervisor |
| `driver` | app-scoped | GPS tracker login; (future) cost upload via companion app (FE-07) | driver |

**Authorization is permission-based.** Fleet backend routes are guarded by
`RequireIAMPermission(...)` with strings like:
`vehicles:create|update|delete`, `drivers:*`, `trips:*`, `costs:update|delete`,
`trip-costs:delete`, `maintenance:*`, `documents:*`, `financials:read`,
`users:read|create|invite|update|delete|toggle_active|change_role|reset_password`,
`dashboard:owner_view|manager_view|supervisor_view`, `caretakers:assign`.
(Defined in backend `internal/infrastructure/iam/mapper.go`.)

**App-side mirrors (UI gating only — server is the authority):**
- `PermissionUtils` in `ijs-core-lib` (`canViewTripPrice`, `canViewFinancials`,
  `canEditTrip(role, tripStatus)`, `canManageTeam`, `getCreatableRoles`, …).
- `UserRole` enum: OWNER, GENERAL_MANAGER, MANAGER, SUPERVISOR.
- Team module fetches assignable roles from `GET /team/members/roles`
  (fallback: static `owner`/`admin`/`user`), then `TeamRepositoryImpl` maps
  IAM names → legacy Fleet names for the create call (transition period —
  see `Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md`).

**JWT `tid` claim (critical):** after tenant creation, IAM issues a new JWT with
the tenant id (`tid`). The app decodes the JWT locally (`JwtHelper.kt`) and
prompts re-login if `tid` is missing — a token without it gets 403 on all
tenant-scoped APIs. Known failure: IAM `CreateTenant` may silently fail to
return new tokens (documented in `Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md`).

### Cargo types
Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others

### Report calculations (source of truth: backend `internal/application/report/usecase.go`)

```
Revenue           = Σ trip_price of completed trips in period
Total Expenses    = Σ trip costs + Σ maintenance costs        (operating view)
Net Profit        = Revenue − Total Expenses
Profit Margin %   = Net Profit ÷ Revenue × 100   (0 if Revenue ≤ 0)
Trip Gross Profit = selling_value − purchase_price
Trip Net Profit   = Gross − Σ trip costs
Vehicle P&L cost  = fuel (TC-G-001) + other trip costs + maintenance
Avg Profit/Km     = Net Profit ÷ Σ actual_distance
Received Payments = Σ (full ? trip_price : partial_payment_amount)
Pending Payments  = Σ pending_amount (pending/partial trips)
Trend             = half-over-half change; ±5% threshold → increasing/decreasing/stable
```

Cost taxonomy is 3-level (category → group → item): trip groups `TC-G-001…006`,
maintenance `VMC-G-001…007`, driver `DC-G-001…004` (see PRD §7.4). Driver costs
and EMI are **not yet** included in P&L (roadmap FE-15).

---

## 7. UI Conventions

- **Always reuse** components from `ijs-ui-components-lib`:
  `FleetTextField`, `FleetDateField`, `FleetTimeField`, `FleetMobileField`,
  `FleetEmailField`, `FleetPasswordField`, `LoadingContent`, `ErrorContent`,
  `EmptyContent`, `ScreenContent`, `FleetCard`, `FleetPrimaryButton`, `FleetSecondaryButton`.
- **Never hardcode colors** — only `MaterialTheme.colorScheme.*`.
- Every screen must handle **loading, error (with retry), and empty** states.
- Strings live in `ijs-ui-components-lib` resources (English + Hindi);
  use `stringResource(Res.string.xxx)` — no hardcoded user-facing text.
- Icons: SVG drawables in composeResources, via `painterResource(Res.drawable.ic_*)`.
- Dark/light theme toggle available in top bars (`isAppInDarkTheme()` / `rememberThemeToggle()`).

---

## 8. Build & Run

```bash
# Android debug APK
./gradlew :androidApp:assembleDebug

# Quick compile check of a single module (fastest validation)
./gradlew :screen-driver:compileCommonMainKotlinMetadata

# Web dev servers
./gradlew :webApp:jsBrowserDevelopmentRun
./gradlew :webApp:wasmJsBrowserDevelopmentRun

# Driver GPS tracker APK
./gradlew :locationTracker:assembleDebug
```

Secrets: `GOOGLE_PLACES_API_KEY` (and similar) belong in `local.properties` — never commit.

---

## 9. Key Files Cheat Sheet

| Purpose | Path |
|---------|------|
| Compose root + startup gates | `sharedUI/.../App.kt` |
| All routes | `sharedUI/.../navigation/FleetRoute.kt` |
| Route → screen wiring | `sharedUI/.../navigation/FleetNavigation.kt` |
| DI container + subscription gate | `sharedUI/.../di/DefaultViewModelProvider.kt` |
| ViewModel interface | `sharedUI/.../di/ViewModelProvider.kt` |
| Repository wiring | `sharedUI/.../di/FeatureRepositoryFactory.kt` |
| API endpoints | `ijs-network-lib/.../core/network/ApiConfig.kt` |
| Auth/401 handling | `ijs-network-lib/.../core/auth/AuthenticationManager.kt` |
| MVI base | `ijs-core-lib/.../core/mvi/MviViewModel.kt` |
| Entity states | `ijs-core-lib/.../core/constants/StatusConstants.kt` |
| Role/permission checks (UI gating) | `ijs-core-lib/.../core/util/PermissionUtils.kt` |
| JWT decoding (`tid` check) | `ijs-network-lib/.../core/auth/JwtHelper.kt` |
| Date utils | `ijs-datetime-utils/.../FleetDateTime.kt` |
| Result/exceptions | `ijs-error-lib/.../result/Result.kt`, `.../exception/` |
| Shared strings (en/hi) | `ijs-ui-components-lib` composeResources |
| Coding rules for AI | `.github/copilot-instructions.md`, `.github/instructions/*.md` |

---

## 10. Adding a New Feature — Checklist

1. Entity → `screen-{f}/domain/entity/`
2. Repository interface → `screen-{f}/domain/repository/`
3. Use cases → `screen-{f}/domain/usecase/`
4. DTOs (with defaults!) → `screen-{f}/data/model/`
5. Mapper → `screen-{f}/data/mapper/`
6. DataSource → `screen-{f}/data/datasource/`
7. Repository impl → `screen-{f}/data/repository/`
8. Wire repo → `sharedUI/di/FeatureRepositoryFactory.kt`
9. Contract / ViewModel / Screen → `screen-{f}/presentation/`
10. Facade entry → `{Feature}FeatureFacade.kt`
11. Factory method → `sharedUI/di/ViewModelProvider.kt` + `DefaultViewModelProvider.kt`
12. Route → `FleetRoute.kt`; NavEntry → `FleetNavigation.kt`
13. Strings (en + hi) → `ijs-ui-components-lib`
14. Verify: `./gradlew :screen-{f}:compileCommonMainKotlinMetadata` then `:androidApp:assembleDebug`

---

## 11. Current Status & Known Work

| Item | Status |
|------|--------|
| Core CRUD (vehicles, drivers, trips, customers, payments, team, finance) | ✅ Done |
| Dashboard + alerts + reports + PDF export | ✅ Done |
| Onboarding flow | ✅ Done |
| Subscription/billing funnel (Razorpay) | 🔶 In progress |
| Localization (en + hi) | 🔶 Core done; list/detail screens pending |
| Live MQTT tracking on Maps screen | 🔶 Mock data; real wiring pending |
| Push Notifications + Notification Center/Settings screens (PRD Feat-NTF, FE-06) | 🔮 Planned — FCM/APNs/Web Push; routes `Notifications`, `NotificationSettings` |
| Backend env `ijsfm_users` table | ⚠️ Missing on refactor Cloud Run env (500 on `/onboarding/status`) |
| Desktop target | 🔮 Future |

---

## 12. Lessons Learned (do not repeat these bugs)

1. **Required DTO fields without defaults** silently kill deserialization → empty UI.
   Always add defaults; always test against real backend JSON.
2. **Backend wraps payloads twice** on some endpoints — verify against the actual
   handler/postman, not the obvious shape.
3. **Wrong date format to cost APIs** (ISO instead of DD-MM-YYYY) makes the backend
   reject rows while the app thinks it succeeded — surface backend errors loudly.
4. **Per-route ViewModels go stale** when returning from child screens —
   use `rememberSharedViewModel` + explicit refresh intents for detail↔entry flows.
5. **Gate fallbacks matter**: a failed status API must route the user to the *earliest*
   sensible step, never past a paywall or required setup.
6. **Tokens without `tid` cause endless 403s**: after tenant creation, always verify
   the new JWT actually contains the tenant claim before calling tenant-scoped APIs;
   prompt re-login if not.
7. **403 ≠ 401**: permission-denied must show a friendly message (and possibly a
   re-login hint), but must NOT clear the session like an expired token does.

