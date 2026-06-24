# Fleet Apps — Requirements

> What the client apps must do, in plain words, taken from the product PRD
> (v2.2) and the roles/subscription specs. System-wide requirements are in
> [`../REQUIREMENTS.md`](../REQUIREMENTS.md).

## Purpose

Give fleet owners and their teams one app (Android, iOS, Web) to run the fleet:
vehicles, drivers, trips, costs, customers, payments, reports and live tracking —
talking to the Go fleet backend and logging in through IAM.

## Access tiers (how permissions map to people)

Access is decided by permissions, not job titles. The PRD describes five tiers:

- **L0 Tenant Owner** — the person who created the tenant; every permission plus
  billing.
- **L1 Full-access user** — the `owner` bundle given to a trusted deputy:
  everything **except** billing.
- **L2 Operations user** — `admin` bundle (level 50): create/update vehicles,
  drivers, trips and costs, but **no money visibility**.
- **L3 Basic user** — `user` bundle (level 10): view data, update trip status,
  record own cost entries.
- **L4 Driver** — `driver` bundle: GPS tracker login.

The **`financials:read`** permission is the money boundary: trip price, reports,
payments and vehicle finance are visible only to L0/L1. The UI gates actions with
`PermissionUtils`, but the backend is the real authority.

## Features and screens (by product code)

Screens are coded `SCR-{FEATURE}-{NN}`. Highlights with their key rules:

- **Feat-ONB Onboarding** (`SCR-ONB-01`) — one-time welcome; sets a local flag so
  it never reappears.
- **Feat-AUT Authentication** — Login (email/mobile + password), Sign Up (owner
  only: name, email, **mobile = 10 digits starting 6–9**, **password ≥ 8** +
  confirm), Sign Up Success, OTP Verification (email and/or mobile), Forgot
  Password, Profile, Change Password (**new password ≥ 8 and different from
  current**).
- **Feat-SUB Subscription & Billing** (owner-only) — Subscription Plans (must
  pick one; monthly↔annual toggle with discount/savings; optional trial days),
  Checkout (pays via Razorpay; needs `data.order_id` from the backend or it
  fails), Success, and **Create Organization** (org name ≥ 2 chars; IAM returns a
  fresh JWT with the `tid` claim).
- **Feat-DSH Dashboard** (`SCR-DSH-01`) — six sections, permission-filtered:
  fleet overview (all), cost overview (not L3), financial summary (L0/L1 only),
  vehicle & driver status (all), alerts preview (all), quick actions (by tier).
  Caches offline.
- **Feat-VEH Vehicles** — list (search + status filter), detail (4 tabs:
  overview, trips, documents, costs), Add Vehicle (**2-step wizard**: registration
  number matches the Indian plate pattern and is auto-uppercased, year between
  1990 and next year; then RC/Insurance/Fitness/Permit/PUC documents with expiry
  dates), and bulk Maintenance Cost Entry.
- **Feat-DRV Drivers** — list, detail, Create Driver (name, **mobile**, password,
  licence number/type/**expiry**, optional joining date which becomes the minimum
  date for that driver's costs; new drivers start ACTIVE; can create an IAM
  `driver` login), and bulk Driver Cost Entry (salary/advance/bonus/penalty; date
  between joining date and tomorrow).
- **Feat-TRP Trips** — list, detail (edit is L0/L1 any state, L2 planned-only,
  status updates all), Create Trip (vehicle + driver must be active and not
  occupied; Google Places start/end with auto distance; cargo type + weight > 0;
  customer name + 10-digit contact; **trip price required only with
  `financials:read`, hidden otherwise**; sent as ISO 8601), and bulk Trip Cost
  Entry (date ≥ trip start).
- **Feat-CUS Customers** — list, detail (money summary needs `financials:read`),
  Create Customer (company, contact, **10-digit mobile**, optional **15-char
  GST**).
- **Feat-PAY Trip Payments** (L0/L1) — list, detail, Add/Edit Payment (amount > 0;
  mode = cash/upi/bank_transfer/cheque/card; trip payment status auto-derives
  pending → partial → paid).
- **Feat-RPT Reports** (L0/L1) — Reports Hub plus Vehicle P&L, Trip P&L, Cost
  Analysis and Consolidated P&L; period filter (today … yearly + custom); PDF
  export. (Revenue = sum of completed trips' price; "Net Profit" is operating
  profit today — see gaps.)
- **Feat-FIN Vehicle Finance** (L0/L1) — list, detail, Add/Edit Purchase Info
  (loan fields appear only for loan purchases), EMI Payment History.
- **Feat-TEM Team** (L0/L1) — list, member detail (gated change-role,
  enable/disable, reset-password), Create Team Member (role picker shows only
  bundles below your own level; loads from `GET /api/v1/team/members/roles`).
- **Feat-MAP Maps** — live vehicle map over MQTT (currently mock data; live
  wiring is FE-04). Companion `locationTracker` APK publishes GPS.
- **Feat-ALR Alerts** — document expiry, licence expiry, maintenance due; deep
  link to the entity; clear when a new document/expiry is saved.
- **Feat-LOC Localization** (English + Hindi) and **Feat-THM Theming** (Material 3
  light/dark).
- **Feat-NTF Notifications** — *planned* (FE-06): push (FCM/APNs/Web Push) plus a
  Notification Center inbox and Notification Settings; each user only sees
  notifications they're permitted to (e.g. payment alerts need `financials:read`).

## Entity state machines

- **Vehicle:** `inactive ↔ active → on_route → active`; `active → maintenance ↔
  damaged → decommissioned` (final).
- **Trip:** `planned → on_route → completed`; `planned → cancelled`; `on_route →
  failed / delayed`.
- **Driver:** `inactive ↔ active → on_route / on_leave / suspended → active`;
  `→ terminated` (final).

## Cross-cutting requirements

- **Subscription gate** on startup routes a new owner through SignUp → OTP →
  Plan → Razorpay → Create Organization → Team → Dashboard; if the status call
  fails and no org exists, fail safe to the Plan screen.
- Every screen handles **loading / error (with retry) / empty**; forms disable
  submit while saving (no double-tap).
- **Defensive parsing**: every DTO field has `@SerialName` **and** a default so a
  missing field doesn't blank the screen; repositories surface errors as
  `Result.Error`, never empty lists.
- **Correct date format per field** (ISO 8601 trips; `DD-MM-YYYY` costs/expiry;
  `YYYY-MM` driver-cost month); UI shows `DD-MM-YYYY` 24-hour.
- Handle **401 → re-login** vs **403 → deny, stay logged in**; verify the `tid`
  claim locally and prompt re-login if missing.

## Non-functional / technical

- One Kotlin Multiplatform codebase → Android, iOS, Web (~95% in `commonMain`);
  Desktop is future (FE-16).
- Clean Architecture + MVI in every feature; Metro DI; Navigation 3; a single
  `Facade` per feature module.
- Base URL must include `/api/v1`; DTOs are `@Serializable` snake_case; response
  wrapper `{ success, message, data }`, errors `{ errorCode, errorMessage,
  developerMessage, statusCode }`.
- Crash reporting via Firebase Crashlytics (Android); Kermit logging.

## Constraints (current dev phase)

- No backward-compatibility shims; keep classes under ~500 lines; never set
  SDK/JVM versions per module (the `gradle/` convention files own that).
- The Go backend structs are the source of truth for field names.

## Known gaps & blockers (documented)

- Legacy role labels (`general_manager`/`manager`/`supervisor`) still appear in
  `UserRole`/`TeamRepositoryImpl`; removal is FE-01.
- Live payment blocker: create-order returns `400 "BillingInterval … required"`
  despite the app sending `billing_interval` (backend must forward it).
- Plan price unit (paise vs rupees) is ambiguous and affects ₹ display and the
  Razorpay amount.

## Future (not built yet)

Desktop target (FE-16); field cost-capture companion app (FE-07); push
notifications / Notification Center (FE-06); photo attachments (FE-08); offline
writes (FE-11); custom role editor (FE-14); true net profit (FE-15).
