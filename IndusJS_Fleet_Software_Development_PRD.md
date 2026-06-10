# IndusJS Fleet — Software Development PRD
*(Product Requirements Document)*

> **Document Version:** 2.2
> **Last Updated:** 11 June 2026
> **Status:** Living document — update as the product grows
> **Owner:** IndusJS Product Team
> **v2.1 changes:** §1 elaborated; §2 rewritten as a **permission-first** access model
> (legacy `general_manager`/`manager`/`supervisor` titles removed in favor of IAM
> role bundles + access tiers); §5.10 Reports expanded with full calculation
> formulas verified against the backend implementation; cost taxonomy corrected
> to the real 3-level structure; roadmap FE-07/FE-14/FE-15 refined/added.
> **v2.2 changes:** feature-code prefix renamed `F-` → `Feat-`; roadmap codes
> (FE-NN) renumbered strictly sequentially; new feature **Feat-NTF — Push
> Notifications & Notification Center** added (§5.15, screens SCR-NTF-01/02,
> implemented by FE-06); Localization/Theming renumbered to §5.16.

---

## Table of Contents

- [1. Product Overview](#1-product-overview)
  - [1.1 Product Vision](#11-product-vision)
  - [1.2 What the Product Does](#12-what-the-product-does)
  - [1.3 Product Pillars](#13-product-pillars)
  - [1.4 Product Ecosystem (3 Codebases)](#14-product-ecosystem-3-codebases)
  - [1.5 Supported Platforms](#15-supported-platforms)
  - [1.6 Business Model](#16-business-model)
- [2. Target Users, Roles & Access Hierarchy](#2-target-users-roles--access-hierarchy)
  - [2.1 Target Market & Personas](#21-target-market--personas)
  - [2.2 Access Model — Permissions First, Levels for Precedence](#22-access-model--permissions-first-levels-for-precedence)
  - [2.3 Standard Role Bundles (Seeded per Tenant)](#23-standard-role-bundles-seeded-per-tenant)
  - [2.4 How Permissions Actually Work (IAM RBAC)](#24-how-permissions-actually-work-iam-rbac)
  - [2.5 Capability Matrix (by Access Tier)](#25-capability-matrix-by-access-tier)
  - [2.6 Screen-Level Access Matrix](#26-screen-level-access-matrix)
- [3. The Problem We Solve](#3-the-problem-we-solve)
- [4. Feature Catalog (with Feature Codes)](#4-feature-catalog-with-feature-codes)
- [5. Complete Screen Catalog (with Screen Codes, Inputs & Validations)](#5-complete-screen-catalog-with-screen-codes-inputs--validations)
  - [5.1 Feat-ONB Onboarding](#51-feat-onb--onboarding)
  - [5.2 Feat-AUT Authentication & User Account](#52-feat-aut--authentication--user-account)
  - [5.3 Feat-SUB Subscription & Billing](#53-feat-sub--subscription--billing)
  - [5.4 Feat-DSH Dashboard](#54-feat-dsh--dashboard)
  - [5.5 Feat-VEH Vehicle Management](#55-feat-veh--vehicle-management)
  - [5.6 Feat-DRV Driver Management](#56-feat-drv--driver-management)
  - [5.7 Feat-TRP Trip Management](#57-feat-trp--trip-management)
  - [5.8 Feat-CUS Customer Management](#58-feat-cus--customer-management)
  - [5.9 Feat-PAY Trip Payment Collection](#59-feat-pay--trip-payment-collection)
  - [5.10 Feat-RPT Reports & Analytics](#510-feat-rpt--reports--analytics)
  - [5.11 Feat-FIN Vehicle Finance](#511-feat-fin--vehicle-finance)
  - [5.12 Feat-TEM Team Management](#512-feat-tem--team-management)
  - [5.13 Feat-MAP Real-Time GPS Tracking](#513-feat-map--real-time-gps-tracking)
  - [5.14 Feat-ALR Alerts](#514-feat-alr--alerts)
  - [5.15 Feat-NTF Push Notifications & Notification Center](#515-feat-ntf--push-notifications--notification-center-planned--fe-06)
  - [5.16 Feat-LOC Localization & Feat-THM Theming](#516-feat-loc--localization--feat-thm--theming)
- [6. Key User Journeys (Flows)](#6-key-user-journeys-flows)
- [7. Entity State Machines & Data Types](#7-entity-state-machines--data-types)
- [8. Technical Requirements](#8-technical-requirements)
- [9. Edge Cases & Corner Scenarios](#9-edge-cases--corner-scenarios)
- [10. Future Enhancements & Roadmap](#10-future-enhancements--roadmap)
- [11. Success Metrics](#11-success-metrics)
- [12. Out of Scope (For Now)](#12-out-of-scope-for-now)
- [13. Glossary](#13-glossary)

---

## 1. Product Overview

### 1.1 Product Vision

> *"Give every small and medium fleet owner in India the same operational visibility
> and financial control that large logistics companies have — from a single app
> on the phone already in their pocket."*

**IndusJS Fleet** is a multi-tenant **SaaS Fleet Management System** for transport and
logistics businesses (truck owners, van fleet operators, goods carriers).

**The gap we close.** Large logistics companies run on dedicated Transport Management
Systems and ERPs with full-time IT teams. A typical 2–100 vehicle operator in India
runs the same business on **paper registers, WhatsApp threads, phone calls, and
memory**. The economics of their business are identical — revenue per trip, cost per
kilometre, EMI per vehicle, compliance deadlines — but they have **zero tooling** to
see those numbers. IndusJS Fleet brings enterprise-grade fleet operations down to a
₹-per-month subscription on an ordinary Android phone.

**What "vision" means concretely, in priority order:**

1. **One source of truth** — every vehicle, driver, trip, rupee, document, and GPS
   ping lives in one system, owned by the business (the *tenant*), not scattered
   across personal phones.
2. **Money is never approximate** — every inflow (trip price, customer payment) and
   outflow (fuel, toll, maintenance, driver salary, EMI) is a structured record that
   rolls up into profit & loss the owner can trust (see §5.10 for the exact
   calculations).
3. **Compliance before penalty** — the system warns *before* an insurance policy,
   permit, PUC, fitness certificate, or driving license expires — not after a fine.
4. **Safe delegation** — the owner can hand day-to-day operations to staff without
   handing over pricing and profit data, because every feature is gated by
   fine-grained IAM permissions (§2).
5. **Works where the business works** — offline-tolerant, bilingual (English/Hindi),
   low-end-Android friendly, and multi-platform from a single codebase.

### 1.2 What the Product Does

In everyday terms, the product helps a fleet business answer:

| Question | Answered by |
|----------|-------------|
| *Where are my trucks right now?* | Maps (live GPS via MQTT) |
| *Which driver is on which trip?* | Trips + Drivers modules |
| *How much did I spend on fuel, tolls, and repairs this month?* | Cost tracking + Cost Analysis report |
| *Which customer still has to pay me, and how much?* | Payments + Customer financial summary |
| *Am I making a profit or loss on each vehicle / each trip?* | Vehicle P&L / Trip P&L reports (§5.10) |
| *Which documents (Insurance, RC, Permit, PUC) or licenses are about to expire?* | Alerts + document expiry tracking |
| *How much loan/EMI is pending on each vehicle?* | Vehicle Finance module |
| *Who on my staff can see or change what?* | Team Management + IAM permissions |
| *Is my business trending up or down?* | Consolidated P&L with trend analysis |
| *Did something important happen while I was away?* | Push Notifications + Notification Center (planned, Feat-NTF) |

**How it does it — the operating loop.** The product mirrors the real operating
cycle of a fleet business:

```
        ┌──────────────────────────────────────────────────────────┐
        │  SETUP        Register vehicles + documents, drivers +   │
        │               licenses, customers, purchase/loan info    │
        ├──────────────────────────────────────────────────────────┤
        │  OPERATE      Plan trip (vehicle + driver + route +      │
        │               cargo + price) → on_route → completed      │
        ├──────────────────────────────────────────────────────────┤
        │  SPEND        Record trip costs, maintenance costs,      │
        │               driver costs, EMI payments — as they occur │
        ├──────────────────────────────────────────────────────────┤
        │  COLLECT      Record customer payments against trips     │
        │               (pending → partial → paid)                 │
        ├──────────────────────────────────────────────────────────┤
        │  REVIEW       Dashboard, P&L reports, cost analysis,     │
        │               trends → decisions → back to OPERATE       │
        └──────────────────────────────────────────────────────────┘
```

Every screen in the app (§5) serves exactly one step of this loop, and every record
created in steps 2–4 feeds the calculations in step 5.

### 1.3 Product Pillars

1. **Operations** — vehicles, drivers, and trips each follow a strict lifecycle
   state machine (§7). The state machines are not decorative: they *prevent real
   mistakes* — a decommissioned vehicle or terminated driver can never be assigned
   to a new trip; a vehicle already on a trip is "occupied" and blocked from
   double-booking.
2. **Money** — every rupee in (trip price, customer payments) and out (trip costs,
   maintenance, driver salary/advances, EMIs) is a structured, typed record using a
   three-level cost taxonomy (*category → group → item*, §7.4). Structure is what
   makes the P&L reports (§5.10) calculable instead of guessable.
3. **Compliance** — five vehicle document types (RC, Insurance, Fitness, Permit,
   PUC) and driver licenses carry expiry dates; the Alerts engine surfaces upcoming
   expiries on the dashboard and the alerts screen before they become fines.
4. **Visibility** — a permission-aware dashboard, live map tracking, and exportable
   PDF reports give the decision-maker the same numbers an enterprise TMS would —
   per trip, per vehicle, per cost type, and consolidated, with trend direction.
5. **Control** — access is governed by **fine-grained IAM permissions** (e.g.,
   `financials:read`, `vehicles:create`). Sensitive money data is invisible to any
   user whose role bundle lacks the permission — enforced in the UI *and* on the
   server (§2.4).

### 1.4 Product Ecosystem (3 Codebases + Companion Apps)

The product is delivered by three cooperating codebases:

| Repository | Responsibility | Tech |
|------------|---------------|------|
| `IndusJSFleet_Apps` | The mobile/web app — all screens, offline caching, PDF export — **this repo** | Kotlin Multiplatform + Compose Multiplatform |
| `IndusJSFleet_GoLang_Backend` | Fleet domain REST API — vehicles, drivers, trips, costs, payments, reports, alerts | Go + Gin + Postgres, on Google Cloud Run |
| `IndusJS-IAM` | **Identity & Access Management** — users, tenants (organizations), roles, permissions, JWT tokens, billing plans, Razorpay subscription payments | Go, separate service |

**How they talk:**

```
App ──(login / signup / org & team / billing)──────────────▶ IndusJS-IAM
App ──(Bearer JWT with `tid` claim)───────▶ Fleet API ──(fetch caller's
                                                          permissions)──▶ IAM
                                            Fleet API enforces a specific
                                            permission on every route
```

**Companion apps:**

| App | Status | What it is |
|-----|--------|-----------|
| `locationTracker` (in this repo) | ✅ Shipped | Standalone Android APK on the driver's phone — driver logs in with an IAM account, a foreground service publishes GPS over MQTT (HiveMQ). No dependency on the main app. |
| **Cost-capture app** | 🔮 Planned (FE-07) | A separate, lightweight mobile app through which a **driver — or any user whose role grants the cost permissions — can upload vehicle and trip costs** (fuel receipts, tolls, repairs) from the field. Same IAM login, same permission model, same Fleet API. |

### 1.5 Supported Platforms

| Platform | Status | Entry Point | Notes |
|----------|--------|-------------|-------|
| Android | ✅ Primary | `androidApp` (thin shell) | Main target; Firebase Crashlytics |
| iOS | ✅ Supported | `iosApp` via `SharedUI` framework | Swift shell over the shared framework |
| Web (JS) | ✅ Supported | `webApp` | Browser, Kotlin/JS |
| Web (WasmJS) | ✅ Supported | `webApp` | Browser, Kotlin/Wasm |
| Desktop | 🔮 Future | — | Compose for Desktop (FE-16) |

**Why Kotlin Multiplatform:** ~95% of the code — domain logic, networking, MVI
ViewModels, and the entire Compose UI — is written **once** in `commonMain` and runs
on every platform. Each platform ships only a thin entry shell. One bug fix, one
feature, four platforms.

### 1.6 Business Model

- **Subscription SaaS** — each fleet business is one paying **tenant
  (organization)** inside IAM. All data is tenant-isolated; one tenant can never
  see another tenant's vehicles, trips, or money.
- **Plans** are defined in the IAM service: monthly or annual billing (annual shows
  a discount % and computed savings), optional free-trial days, and per-plan
  feature limits.
- **Payment** is collected through **Razorpay** (card / UPI / netbanking) via a
  platform-specific checkout bridge.
- **The setup funnel** — a new owner must complete every step before reaching the
  product:
  `Sign Up → Verify OTP → Choose Plan → Pay (Razorpay) → Create Organization → Add Team → Dashboard`.
- **The subscription gate** runs on every app launch: it asks the backend for
  onboarding/subscription status and routes the user to the **first incomplete
  step**. If the status call itself fails and no organization exists locally, the
  safe fallback is the **Plan screen** — a failure can never skip a user past the
  paywall.
- **Renewals & failed payments** re-enter the same funnel (`SubscriptionPlans`
  opens with `isPaymentPending = true`).

---

## 2. Target Users, Roles & Access Hierarchy

> **Design principle:** IndusJS Fleet is **permission-first, not title-first.**
> The product does not hard-code job titles like "manager" or "supervisor".
> Access to every feature and screen is decided by **fine-grained IAM permissions**
> (e.g., `financials:read`, `vehicles:create`). A **role** is nothing more than a
> *named bundle of permissions* with a numeric hierarchy level — so **any user, with
> the appropriate permissions, can access any feature or screen.** Job titles belong
> to the customer's organization, not to our data model.

### 2.1 Target Market & Personas

**Primary market:** small and medium fleet operators in India (2–100 vehicles) —
goods carriers, construction-material transporters (gitti/balu/brick), regional
logistics contractors.

Personas describe **what people do**, not what role record they hold. The same
person may hold different permission bundles in different organizations.

| Persona | What they do | What they need from the product | Typical device |
|---------|--------------|--------------------------------|----------------|
| **Business Owner** | Owns the fleet business; signs up, pays, creates the organization | Everything: money, reports, team, billing | Android phone |
| **Trusted Deputy** | Owner's right hand (often family/senior partner) | Owner-level access including financials — without billing control | Android phone / web |
| **Operations Staff** | Plans trips, assigns vehicles/drivers, records costs day-to-day | Fast trip + cost entry; **no need (or right) to see prices/profit** | Android phone |
| **Field / Yard Staff** | At the loading point; watches arrivals/departures | View fleet status; update trip status; record costs they witness | Android phone |
| **Driver** | Drives the vehicle | Share GPS via the tracker app; *(future)* upload fuel/toll/repair costs from the road via a separate cost-capture app | Low-end Android |
| **Accountant** *(indirect)* | Owner's external CA | Receives exported PDF P&L reports | — |
| **Customer** *(indirect, future)* | The party paying for trips | *(Future portal)* see own trips, pay online | — |

### 2.2 Access Model — Permissions First, Levels for Precedence

This section reflects how authorization is **actually implemented** in IndusJS-IAM
(`internal/domain/rbac`), refined after deep analysis of the IAM codebase.

**1. The atom is a Permission.** A permission is a `resource` + `action` pair,
optionally scoped to an application:

```
Permission = resource : action          e.g.  vehicles:create
                                              financials:read
                                              users:change_role
Wildcards supported:  *:read   vehicles:*   *:*
```

**2. A Role is a named permission bundle + a level.** Roles carry no behavior of
their own — they exist so an owner doesn't have to assign 25 permissions one by one.
Each role has a **DB-driven hierarchy level** used only for *management precedence*
(who may manage whom), never for feature access:

| IAM Role Level | Constant | Meaning |
|:--:|----------|---------|
| **100** | `RoleLevelOwner` | Highest authority in the tenant |
| **50** | `RoleLevelAdmin` | Administrative authority |
| **30** | `RoleLevelModerator` | Intermediate (reserved; available for custom bundles) |
| **10** | `RoleLevelMember` | Baseline member |

**3. Access checks are per-permission, per-request.** Every protected Fleet API
route demands one specific permission (`RequireIAMPermission`). The app mirrors the
same checks to hide UI early. **Nothing in the system asks "is this user a
manager?" — it asks "does this user hold `trips:create`?"**

**4. Levels answer only the management questions:**
- A user can manage (create / edit / disable / change role of) only users whose
  highest role level is **below** their own.
- A user can never assign a role bundle at or above their own level.
- The tenant creator (level 100) is the only one who can manage billing.

**Access Tiers (L0–L4).** For readability, this PRD groups the standard permission
bundles into five *access tiers*. These are **documentation shorthand for permission
sets** — not job titles, and not separate mechanisms:

```
L0  Tenant Owner       ──  the tenant creator: every permission + billing
 │                          (IAM role `owner`, level 100)
L1  Full-Access User   ──  the complete `owner` bundle assigned to a staff
 │                          member: everything incl. financials — except billing
 │   ════════════════  💰 FINANCIAL VISIBILITY BOUNDARY (`financials:read`) ═══════
L2  Operations User    ──  the `admin` bundle (level 50): create/update vehicles,
 │                          drivers, trips, costs — NO financial permissions
L3  Basic User         ──  the `user` bundle (level 10): view fleet data, update
 │                          trip status, record own cost entries
L4  Driver             ──  the `driver` bundle: GPS tracker login today;
                            (future) cost-upload permissions via companion app
```

The line that matters most commercially is the **financial visibility boundary**:
everything money-related (trip price, revenue, profit, payments, reports, vehicle
finance) requires `financials:read`, which only L0/L1 bundles include by default.
An owner who *wants* a trusted operations user to see money can simply grant that
permission — the product does not stand in the way, because **permissions, not
titles, are the law.**

> **Legacy naming (deprecated):** earlier versions of the Fleet app/DB used the
> labels `general_manager`, `manager`, and `supervisor`. These map to the IAM
> bundles `owner`, `admin`, and `user` respectively and are being **removed** from
> code, UI, and documentation (see `Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md`,
> roadmap FE-01). This PRD uses only IAM bundle names and tier numbers.

### 2.3 Standard Role Bundles (Seeded per Tenant)

When a tenant (organization) is created, IAM **automatically seeds** three role
bundles (`tenant_onboarding_usecase.go`); the `driver` bundle is provisioned per
tenant/app:

| Bundle (IAM role) | Level | Seeded description | Default contents (summary) |
|-------------------|:-----:|--------------------|----------------------------|
| `owner` | 100 | "Organization Owner — Full Access" | All permissions, incl. `financials:read`, all `users:*` |
| `admin` | 50 | "Administrator — Manage users and content" | Operational CRUD (`vehicles:*`, `drivers:*`, `trips:*`, cost permissions) — **no** `financials:read`, **no** team management |
| `user` | 10 | "Standard User — Basic Access" | Read access + trip status updates + own cost entries |
| `driver` | app-scoped | Driver login for tracker / future cost app | GPS publish; *(future)* `trip-costs:create`, `maintenance:create` |

- The **Add Team Member** screen fetches assignable bundles live from
  `GET /api/v1/team/members/roles`; if unavailable it falls back to the static
  `owner` / `admin` / `user` list. A user can only assign bundles **below their
  own level**.
- **Custom role bundles** (e.g., an "Accountant" bundle = read-only +
  `financials:read`) are a natural extension of this model — the IAM data model
  already supports them (roles are just rows with permission links and a level);
  exposing a bundle editor in the app is roadmap item **FE-14**.

### 2.4 How Permissions Actually Work (IAM RBAC)

Authorization flow on every request:

```
App ──(Bearer JWT, must contain `tid` claim)──▶ Fleet backend
Fleet backend ──(GET /me/permissions)──▶ IAM  → caller's effective permission set
Fleet middleware: RequireIAMPermission("trips:create") → 403 if absent
```

**Fine-grained permissions in use today (Fleet backend, `iam/mapper.go`):**

| Domain | Permissions |
|--------|------------|
| Vehicles | `vehicles:create`, `vehicles:update`, `vehicles:delete` |
| Drivers | `drivers:create`, `drivers:update`, `drivers:delete` |
| Trips | `trips:create`, `trips:update`, `trips:delete` |
| Costs | `costs:update`, `costs:delete`, `trip-costs:delete` |
| Maintenance | `maintenance:update`, `maintenance:delete` |
| Documents | `documents:update`, `documents:delete` |
| Financials | `financials:read` (gates P&L, trip price, reports, payments, vehicle finance) |
| Team/Users | `users:read`, `users:create`, `users:invite`, `users:update`, `users:delete`, `users:toggle_active`, `users:change_role`, `users:reset_password` |
| Dashboard | `dashboard:owner_view`, `dashboard:manager_view`, `dashboard:supervisor_view` |
| Caretakers | `caretakers:assign` |

**JWT requirements:** after the owner creates their organization, IAM issues a fresh
token containing the **`tid` (tenant id) claim**. The app verifies this claim locally
(`JwtHelper.kt`) before calling tenant APIs; if missing, it prompts re-login
(a token without `tid` has no tenant permissions and every call would 403).

**Client-side enforcement:** the app mirrors permission checks in `PermissionUtils`
(`ijs-core-lib`) to hide UI elements early (e.g., the trip price field is never
rendered for a user without `financials:read`). **The server (IAM-enforced) is
always the final authority** and returns **403 Forbidden** for anything not
permitted. 403 means "not allowed" (stay logged in); 401 means "session invalid"
(auto-logout).

### 2.5 Capability Matrix (by Access Tier)

Each capability is unlocked by a permission — the tier columns just show which
**default bundles** include it. Granting the permission to any bundle moves the ✅.

| Capability | Gating permission | L0 Owner | L1 Full | L2 Ops | L3 Basic | L4 Driver |
|------------|-------------------|:--:|:--:|:--:|:--:|:--:|
| See revenue / profit / trip price | `financials:read` | ✅ | ✅ | ❌ | ❌ | ❌ |
| View Reports (P&L) | `financials:read` | ✅ | ✅ | ❌ | ❌ | ❌ |
| Record customer payments | `financials:read` | ✅ | ✅ | ❌ | ❌ | ❌ |
| Vehicle finance (loans/EMI) | `financials:read` | ✅ | ✅ | ❌ | ❌ | ❌ |
| Manage team members | `users:*` family | ✅ | ✅ (below own level) | ❌ | ❌ | ❌ |
| Add / edit vehicles, drivers, trips | `vehicles/drivers/trips:create|update` | ✅ | ✅ | ✅ | ❌ | ❌ |
| Delete vehicles / trips | `vehicles:delete`, `trips:delete` | ✅ | ✅ | ❌ | ❌ | ❌ |
| Edit trip in any state | `trips:update` (+ level rule) | ✅ | ✅ | planned-only | ❌ | ❌ |
| Record costs | cost permissions | ✅ | ✅ | ✅ | own entries | 🔮 FE-07 |
| Delete cost entries | `costs:delete`, `trip-costs:delete` | ✅ | ✅ | ✅ | ❌ | ❌ |
| Update trip status | (member baseline) | ✅ | ✅ | ✅ | ✅ | 🔮 own trips |
| View vehicles / drivers / trips | (member baseline) | ✅ | ✅ | ✅ | ✅ | 🔮 own data |
| Assign caretakers | `caretakers:assign` | ✅ | ✅ | ❌ | ❌ | ❌ |
| Manage billing & subscription | (tenant creator only) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Share GPS location (tracker app) | driver bundle | — | — | — | — | ✅ |

### 2.6 Screen-Level Access Matrix

Quick reference: which tiers can open which screen group with the **default**
bundles. (Per-screen details in §5; the real gate is always the permission shown.)

| Screen Group | Gate | L0/L1 | L2 Ops | L3 Basic |
|--------------|------|:--:|:--:|:--:|
| Dashboard | `dashboard:*_view` variants | ✅ full | ✅ no money sections | ✅ minimal |
| Vehicles / Drivers / Trips / Customers (list & detail) | baseline read | ✅ | ✅ | ✅ view |
| Add Vehicle / Create Driver / Create Trip / Create Customer | `*:create` | ✅ | ✅ | ❌ |
| Cost Entry (trip / maintenance / driver) | cost permissions | ✅ | ✅ | own entries |
| Payments (all 4 screens) | `financials:read` | ✅ | ❌ | ❌ |
| Reports (all 5 screens) | `financials:read` | ✅ | ❌ | ❌ |
| Vehicle Finance (all 5 screens) | `financials:read` | ✅ | ❌ | ❌ |
| Team (all 3 screens) | `users:read` + | ✅ | ❌ | ❌ |
| Maps, Alerts | baseline read | ✅ | ✅ | ✅ |
| Profile, Change Password | any authenticated | ✅ | ✅ | ✅ |
| Subscription / Billing | tenant creator | L0 only | ❌ | ❌ |

---

## 3. The Problem We Solve

Small and medium fleet businesses in India usually manage everything on
**paper registers, WhatsApp messages, and memory**. This causes:

1. **Lost money** — forgotten customer payments, untracked fuel/toll costs.
2. **Compliance risk** — expired insurance, permits, and driver licenses go unnoticed
   until a fine or an impounded vehicle.
3. **No visibility** — the owner doesn't know which vehicle or trip is profitable.
4. **Driver disputes** — salary, advance, and bonus records get messy.
5. **No real-time tracking** — the owner cannot see where vehicles actually are.
6. **Information leakage** — staff with full access to records can see (and leak)
   pricing and profit data.

IndusJS Fleet replaces all of that with one app and a strict permission model.

---

## 4. Feature Catalog (with Feature Codes)

Every feature has a stable code used across PRD, tickets, test cases, and analytics.

| Code | Feature | Module(s) | One-line summary |
|------|---------|-----------|------------------|
| **Feat-ONB** | Onboarding | `screen-onboarding` | First-run welcome / feature highlight pages |
| **Feat-AUT** | Authentication & User Account | `screen-user`, `ijs-network-lib` | Sign up, OTP, login, password, profile |
| **Feat-SUB** | Subscription & Billing | `screen-payment` (`com.ijs.subscription`) | Plans, Razorpay checkout, organization creation, launch gate |
| **Feat-DSH** | Dashboard | `screen-dashboard` | Role-aware home with 6 sections + quick actions |
| **Feat-VEH** | Vehicle Management | `screen-vehicle` | Vehicle CRUD, documents, maintenance costs, lifecycle |
| **Feat-DRV** | Driver Management | `screen-driver` | Driver CRUD, licenses, driver costs, lifecycle |
| **Feat-TRP** | Trip Management | `screen-trip` | Trip planning, routes, cargo, trip costs, lifecycle |
| **Feat-CUS** | Customer Management | `screen-customer` | Customer CRUD, GST, trip history, financial summary |
| **Feat-PAY** | Trip Payment Collection | `screen-trip-payment` | Record/edit customer payments against trips |
| **Feat-RPT** | Reports & Analytics | `screen-report`, `ijs-pdf-report` | P&L by vehicle/trip, cost analysis, consolidated; PDF export |
| **Feat-FIN** | Vehicle Finance | `screen-finance` | Purchase records, loans, EMI history |
| **Feat-TEM** | Team Management | `screen-team` | IAM-backed staff accounts, roles, enable/disable, reset password |
| **Feat-MAP** | Real-Time GPS Tracking | `screen-map`, `locationTracker` | Live vehicle positions over MQTT |
| **Feat-ALR** | Alerts | `screen-alerts` | Document/license expiry & maintenance-due warnings |
| **Feat-NTF** | Push Notifications & Notification Center | *(planned — FE-06)* | Push delivery (FCM/APNs/Web Push) of business events + in-app Notification Center screen |
| **Feat-LOC** | Localization | shared resources | English + Hindi |
| **Feat-THM** | Theming | `ijs-ui-components-lib` | Material 3 light/dark, user toggle |

---

## 5. Complete Screen Catalog (with Screen Codes, Inputs & Validations)

> **Conventions used below**
> - **Screen Code** = `SCR-{FEATURE}-{NN}` — unique, stable identifier.
> - **Route** = type-safe route in `FleetRoute.kt` (with parameters where applicable).
> - **Access** = role levels that may open the screen (see §2.2).
> - **Inputs & Validations** = every user-enterable field with its rule, as implemented.
> - All dates display/enter as `DD-MM-YYYY`; all times as `HH:MM` (24-hour).
> - Every screen implements **loading / error (with retry) / empty** states via `ScreenContent`.
> - Forms disable the submit button while saving (double-tap protection).

### 5.1 Feat-ONB — Onboarding

#### SCR-ONB-01 — Onboarding
| | |
|---|---|
| **Route** | `Onboarding` |
| **Access** | Anonymous (pre-login), shown once |
| **Purpose** | Introduce the product to first-time users with paged welcome/feature-highlight content; set the "completed onboarding" flag in local Settings so it never re-appears. |
| **Inputs** | None (swipe pages, "Skip", "Get Started"). |
| **Validation** | None. |
| **Exit** | → Login (`SCR-AUT-01`). Start route on launch is decided by `hasCompletedOnboarding()`. |

### 5.2 Feat-AUT — Authentication & User Account

#### SCR-AUT-01 — Login
| | |
|---|---|
| **Route** | `Login` |
| **Access** | Anonymous |
| **Purpose** | Authenticate an existing user (any role) against IndusJS-IAM and store the session token. Entry point after logout or 401 auto-logout. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Email / Mobile | text (toggle) | Required — "Email is required" or "Mobile is required" depending on selected mode |
| Password | password | Required |

**Behavior:** on success stores JWT + user data in Settings → Dashboard (or
subscription gate step). Errors are classified (network vs invalid credentials)
into user-friendly messages. Links to Forgot Password and Sign Up.

#### SCR-AUT-02 — Sign Up
| | |
|---|---|
| **Route** | `SignUp` |
| **Access** | Anonymous |
| **Purpose** | Register a new fleet **owner** account (the only self-service role). Leads into OTP verification and the subscription funnel. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| First Name | text | Required |
| Last Name | text | Required |
| Email | email | Required; must match email format |
| Mobile | numeric (10) | Required; digits only; exactly 10 digits; must start with 6–9 (Indian mobile) |
| Password | password | Required; minimum 8 characters |
| Confirm Password | password | Required; must match Password |

**Exit:** → SignUpSuccess / OtpVerification depending on backend response.

#### SCR-AUT-03 — Sign Up Success
| | |
|---|---|
| **Route** | `SignUpSuccess(message, isResend)` |
| **Access** | Post-signup |
| **Purpose** | Confirmation screen after registration or after resending a verification message; shows backend-provided message. |
| **Inputs** | None (Continue button). |

#### SCR-AUT-04 — OTP Verification
| | |
|---|---|
| **Route** | `OtpVerification(email, mobile, needsEmailVerification, needsMobileVerification)` |
| **Access** | Post-signup |
| **Purpose** | Verify ownership of email and/or mobile via one-time codes before the account is activated. Supports either or both channels based on flags. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Email OTP | numeric code | Required when `needsEmailVerification`; length per backend (typically 6) |
| Mobile OTP | numeric code | Required when `needsMobileVerification` |

**Behavior:** resend option; on success continues into the subscription gate.

#### SCR-AUT-05 — Forgot Password
| | |
|---|---|
| **Route** | `ForgotPassword` |
| **Access** | Anonymous |
| **Purpose** | Send a password-reset link/code to the user's registered email. |

| Field | Type | Rules |
|-------|------|-------|
| Email | email | Required; valid email format |

#### SCR-AUT-06 — Profile
| | |
|---|---|
| **Route** | `Profile` |
| **Access** | All authenticated roles (L0–L3) |
| **Purpose** | View and edit own profile data; entry point for Change Password and Logout. Long emails/names must wrap (never ellipsized). |
| **Inputs** | Editable profile fields (name, contact details) — same format rules as Sign Up. |
| **Actions** | Save, Change Password →, Logout (clears session + back stack). |

#### SCR-AUT-07 — Change Password
| | |
|---|---|
| **Route** | `ChangePassword` |
| **Access** | All authenticated roles |
| **Purpose** | Update the account password while logged in. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Current Password | password | Required |
| New Password | password | Required; minimum 8 characters; **must differ from current password** |
| Confirm New Password | password | Required; must match New Password |

### 5.3 Feat-SUB — Subscription & Billing

> The SaaS gate. Owner-only. Implemented in module `screen-payment`
> (package `com.ijs.subscription`); plan/billing APIs live in **IndusJS-IAM**.

**Funnel:** `Sign Up → Verify OTP → Choose Plan → Pay (Razorpay) → Create Organization → Add Team → Dashboard`.
A launch-time **gate check** resumes the user at the first incomplete step; if the
status check fails, the safe fallback is the Plan screen (never skipped ahead).

#### SCR-SUB-01 — Subscription Plans
| | |
|---|---|
| **Route** | `SubscriptionPlans(isPaymentPending)` |
| **Access** | Owner (during setup or renewal) |
| **Purpose** | Present available plans with feature limits; toggle monthly ↔ annual billing (annual shows discount % and savings); optional trial days. |
| **Inputs** | Plan selection (single choice); billing interval toggle. |
| **Validation** | A plan must be selected to continue. |

#### SCR-SUB-02 — Subscription Checkout
| | |
|---|---|
| **Route** | `SubscriptionCheckout(planId, planName, monthlyPrice, annualPrice, discountPercent, effectiveMonthlyPriceAnnual, annualSavings, currency, trialDays, billingInterval)` |
| **Access** | Owner |
| **Purpose** | Review the order summary and pay via the **Razorpay** bridge (platform-specific). Creates a payment order on IAM; backend must return `data.order_id` or checkout fails with a clear error. |
| **Inputs** | Pay button (Razorpay sheet handles card/UPI/netbanking). |
| **Failure handling** | Razorpay failure codes surfaced; retry allowed; cancelled payment returns to plans with `isPaymentPending`. |

#### SCR-SUB-03 — Subscription Success
| | |
|---|---|
| **Route** | `SubscriptionSuccess(planName, amount, currency)` |
| **Access** | Owner |
| **Purpose** | Confirm successful payment; show plan name and charged amount; continue to organization creation. |

#### SCR-SUB-04 — Create Organization
| | |
|---|---|
| **Route** | `CreateOrganization` |
| **Access** | Owner |
| **Purpose** | Create the **tenant** in IndusJS-IAM. IAM auto-seeds `owner`/`admin`/`user` roles and issues a **fresh JWT containing the `tid` claim**. The app validates the claim locally (`JwtHelper`); if missing/blank, it forces re-login instead of looping on 403s. |

| Field | Type | Rules |
|-------|------|-------|
| Organization Name | text | Required; **minimum 2 characters** ("Organization name must be at least 2 characters.") |

### 5.4 Feat-DSH — Dashboard

#### SCR-DSH-01 — Dashboard
| | |
|---|---|
| **Route** | `Dashboard` |
| **Access** | All authenticated users; sections are permission-filtered (`dashboard:*_view`) |
| **Purpose** | The home screen. One-glance fleet health + the fastest path to every common action. Caches data offline (Room) so a cold/offline launch still renders. |

**Sections & visibility (by access tier, §2.2):**

| # | Section | Contents | L0/L1 | L2 Ops | L3 Basic |
|---|---------|----------|:--:|:--:|:--:|
| 1 | Fleet Overview | Vehicle / driver / trip counts by status | ✅ | ✅ | ✅ |
| 2 | Cost Overview | Today / weekly / monthly spend with filter | ✅ | ✅ | ❌ |
| 3 | Financial Summary | Revenue, expenses, profit (`financials:read`) | ✅ | ❌ | ❌ |
| 4 | Vehicle & Driver Status | active / on_route / maintenance counts | ✅ | ✅ | ✅ |
| 5 | Alerts | Expiring documents & licenses preview | ✅ | ✅ | ✅ |
| 6 | Quick Actions | Add vehicle, create trip, add cost, add payment… | all | no finance actions | minimal |

**Inputs:** Cost-overview period filter; pull-to-refresh. **Navigation:** hamburger
drawer with profile header → every feature (18+ destinations).

### 5.5 Feat-VEH — Vehicle Management

#### SCR-VEH-01 — Vehicles List
| | |
|---|---|
| **Route** | `Vehicles` |
| **Access** | L0–L3 (view) |
| **Purpose** | Browse the fleet; find a vehicle fast. |
| **Inputs** | Search text (registration/make/model); status filter chips (`inactive, active, on_route, maintenance, damaged, decommissioned`). |
| **Actions** | Tap card → Vehicle Detail; FAB / top-bar "+" → Add Vehicle (L0–L2). |

#### SCR-VEH-02 — Vehicle Detail
| | |
|---|---|
| **Route** | `VehicleDetail(vehicleId)` |
| **Access** | L0–L3 (view); edit/status/document actions L0–L2 per permissions |
| **Purpose** | Single place to manage one vehicle. Four tabs: **Overview** (info, status transitions, assigned driver), **Trips** (history), **Documents** (upload, view, expiry), **Costs** (maintenance cost list with date-range / type filters). |
| **Inputs (edit mode)** | Same field set + rules as Add Vehicle (SCR-VEH-03). Status changes constrained by the state machine (§7.1). Document upload requires a file + expiry date (`DD-MM-YYYY`). |

#### SCR-VEH-03 — Add Vehicle
| | |
|---|---|
| **Route** | `AddVehicle` |
| **Access** | L0–L2 (`vehicles:create`) |
| **Purpose** | Register a new vehicle with compliance documents in a 2-step wizard (Step 1 basic info → Step 2 documents). Step 1 must pass validation before Step 2 unlocks. |

**Step 1 — Basic info:**

| Field | Type | Rules |
|-------|------|-------|
| Registration Number | text (auto-uppercase) | Required; length 6–13; must match Indian format `^[A-Z]{2}[0-9]{1,2}[A-Z]{1,4}[0-9]{1,4}$` (e.g., `MH12AB1234`, `DL1CAB1234`, `KA01MG1234`) |
| Make | text | Required |
| Model | text | Required |
| Year | numeric | Required; integer; **≥ 1990**; **≤ current year + 1** ("Year cannot be in the future") |
| Vehicle Type / capacity etc. | dropdown / text | Optional descriptive fields |

**Step 2 — Documents (each optional, but expiry required when uploaded):**

| Document | Expiry |
|----------|--------|
| RC (Registration Certificate) | `DD-MM-YYYY` |
| Insurance | `DD-MM-YYYY` |
| Fitness | `DD-MM-YYYY` |
| Permit | `DD-MM-YYYY` |
| PUC | `DD-MM-YYYY` |

Upload shows a progress indicator; files are attached to the create call on submit.
Document expiries feed the Alerts system (Feat-ALR).

#### SCR-VEH-04 — Maintenance Cost Entry
| | |
|---|---|
| **Route** | `MaintenanceCostEntry(vehicleId?)` |
| **Access** | L0–L2; L3 may add own entries |
| **Purpose** | Record one or more maintenance expenses against a vehicle in a single visit (multi-row bulk entry). Reachable from Vehicle Detail (pre-selected vehicle) or Dashboard quick action (vehicle picker shown). |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Vehicle | picker | Required — "Please select a vehicle" |
| Cost rows (≥1) | repeating group | At least one **valid** row required — "Please add at least one valid cost entry" |
| → Cost Type | dropdown | Required; one of `tyre, battery, servicing, engine_repair, body_repair, electrical, ac_repair, other` |
| → Amount | numeric | Required; must parse as a positive number — "Invalid amount" |
| → Date | `FleetDateField` | Required; `DD-MM-YYYY` |
| → Time | `FleetTimeField` | `HH:MM` 24-hour |
| → Description / notes | text | Optional |

**API note:** maintenance costs are sent in `DD-MM-YYYY` + `HH:MM` format (not ISO).

### 5.6 Feat-DRV — Driver Management

#### SCR-DRV-01 — Drivers List
| | |
|---|---|
| **Route** | `Drivers` |
| **Access** | L0–L3 (view) |
| **Purpose** | Browse drivers with search and status filter (`inactive, active, on_route, on_leave, suspended, terminated`). |
| **Actions** | Tap → Driver Detail; "+" → Create Driver (L0–L2). |

#### SCR-DRV-02 — Driver Detail
| | |
|---|---|
| **Route** | `DriverDetail(driverId)` |
| **Access** | L0–L3 (view); edit & status toggle L0–L2 |
| **Purpose** | Full driver profile: personal info, license (number, type, expiry), trip history, cost history with earnings summary (salary vs advances vs bonus vs penalty). Status transitions per state machine (§7.2). Edit mode re-uses Create Driver validation (mobile re-validated). |

#### SCR-DRV-03 — Create Driver
| | |
|---|---|
| **Route** | `CreateDriver` |
| **Access** | L0–L2 (`drivers:create`) |
| **Purpose** | Register a driver. Optionally creates a real **IAM login account** (role `driver`) used by the Location Tracker app — hence the password field. Caretaker can be assigned (`caretakers:assign`). |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| First Name | text | Required |
| Last Name | text | Required |
| Mobile | numeric | Required; digits only; **at least 10 digits** |
| Password | password | Required; **min 8 characters**; **must not contain the driver's name or email** |
| License Number | text (auto-uppercase) | Required |
| License Type | dropdown | Selection (e.g., LMV/HMV) |
| License Expiry | `FleetDateField` | Required; `DD-MM-YYYY` — feeds license-expiry alerts |
| Email | email | Optional; if entered must match email format |
| Date of Birth | date | Optional |
| Address | text | Optional |
| Emergency Contact | numeric | Optional |
| Blood Group | dropdown | Optional |
| Joining Date | date | Optional — becomes the **minimum allowed date** for this driver's cost entries |
| Caretaker | picker | Optional |

New drivers start as `ACTIVE`.

#### SCR-DRV-04 — Driver Cost Entry
| | |
|---|---|
| **Route** | `DriverCostEntry(driverId?)` |
| **Access** | L0–L2; L3 own entries |
| **Purpose** | Record money paid to a driver — supports **bulk entry** (multiple rows in one submit). Keeps the salary/advance ledger clean to prevent driver disputes. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Driver | picker | Required — "Please select a driver" |
| Cost rows (≥1) | repeating group | All rows must be valid — "Please fill in all required fields correctly" |
| → Cost Type | dropdown | One of `salary, advance, bonus, penalty` |
| → Amount | numeric | Required; valid positive number |
| → Date | `FleetDateField` | Required — "Date is required"; **min = driver's joining date**, **max = tomorrow** |
| → Notes | text | Optional |

### 5.7 Feat-TRP — Trip Management

#### SCR-TRP-01 — Trips List
| | |
|---|---|
| **Route** | `Trips` |
| **Access** | L0–L3 (view) |
| **Purpose** | Browse trips, filtered by status (`planned, on_route, completed, cancelled, failed, delayed`). Trip price on cards visible only with `financials:read`. |
| **Actions** | Tap → Trip Detail; "+" → Create Trip (L0–L2). |

#### SCR-TRP-02 — Trip Detail
| | |
|---|---|
| **Route** | `TripDetail(tripId)` |
| **Access** | L0–L3 (view); edit L0–L1 any state, L2 planned-only; status updates L0–L3 |
| **Purpose** | Operate a single trip: route + distance, cargo, schedule, status transitions (per §7.3), trip cost list, linked payments (financial roles only). Cancel allowed from `planned`. Edit form re-uses Create Trip validation. |

#### SCR-TRP-03 — Create Trip
| | |
|---|---|
| **Route** | `CreateTrip` |
| **Access** | L0–L2 (`trips:create`) |
| **Purpose** | Plan a complete trip in one form: assign vehicle + driver, define route with **Google Places autocomplete** (distance auto-calculated via **Google Distance Matrix**), schedule, cargo, customer, and price. The richest form in the app. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Vehicle | picker (active only) | Required — "Please select a vehicle"; must have a valid id; **must not be occupied** ("Selected vehicle is currently occupied") |
| Driver | picker (active only) | Required; valid id; **must not be occupied** |
| Start Location | Places autocomplete | Required |
| End Location | Places autocomplete | Required |
| Distance | auto-calculated | From Distance Matrix; editable override |
| Departure Date | `FleetDateField` | Required; `DD-MM-YYYY` |
| Departure Time | `FleetTimeField` | Required; `HH:MM` |
| Arrival Date | `FleetDateField` | Optional — but if either arrival field is filled, **both** are required ("Please enter arrival time/date") |
| Arrival Time | `FleetTimeField` | With date: arrival **must be after departure** ("Arrival must be after departure") |
| Cargo Type | dropdown | Required; one of `Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others` |
| Cargo Weight | numeric | Required; valid number; **> 0** |
| Weight Unit | dropdown | Required (e.g., kg / ton) |
| Customer Name | text / customer picker | Required |
| Customer Contact | numeric | Required; exactly 10 digits; digits only; must start with 6/7/8/9 |
| Trip Price | numeric | **Required only when the user holds `financials:read`** (field hidden otherwise); valid amount; **> 0** |

**API note:** trip create/update sends ISO 8601 timestamps
(`FleetDateTime.toIso8601(date, time)`).

#### SCR-TRP-04 — Trip Cost Entry
| | |
|---|---|
| **Route** | `TripCostEntry(tripId?, vehicleId?)` |
| **Access** | L0–L2; L3 own entries |
| **Purpose** | Record expenses incurred on a trip (multi-row bulk entry). Reachable from Trip Detail (trip pre-selected) or Dashboard quick action (trip selector shown). |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Trip | selector | Required — "Please select a trip" |
| Cost rows (≥1) | repeating group | At least one valid row — "Please add at least one valid cost entry" |
| → Cost Type | dropdown | One of `fuel, toll, driver_allowance, parking, loading_charges, unloading_charges, chalan, permit, insurance, other` |
| → Amount | numeric | Required; valid number — "Invalid amount" |
| → Date | `FleetDateField` | Required; **cannot be before the trip start date** (`FleetDateTime.getMinDateForTripCost`) |
| → Time | `FleetTimeField` | `HH:MM` |
| → Notes | text | Optional |

### 5.8 Feat-CUS — Customer Management

#### SCR-CUS-01 — Customers List
| | |
|---|---|
| **Route** | `Customers` |
| **Access** | L0–L3 (view) |
| **Purpose** | Search and browse customers. Backed by **local caching** (`CustomerLocalDataSource`) for instant lists. |
| **Inputs** | Search text. **Actions:** tap → detail; "+" → create (L0–L2). |

#### SCR-CUS-02 — Customer Detail
| | |
|---|---|
| **Route** | `CustomerDetail(customerId)` |
| **Access** | L0–L3 (view); financial summary visible only with `financials:read` |
| **Purpose** | Company/contact/GST info, trip history with this customer, and money summary (total billed vs received vs outstanding). Edit mode uses Create Customer validation. |

#### SCR-CUS-03 — Create Customer
| | |
|---|---|
| **Route** | `CreateCustomer` |
| **Access** | L0–L2 |
| **Purpose** | Register a paying customer with company, contact, GST, and address — the anchor for trip billing and payment tracking. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Company Name | text | Required |
| Contact Person | text | Required |
| Primary Mobile | numeric | Required; exactly 10 digits; must start with 6–9 |
| Secondary Mobile | numeric | Optional; if entered: 10 digits, starts with 6–9 |
| Email | email | Optional; valid format if entered |
| GST Number | text | Optional; if entered: exactly **15 characters** matching `^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$` ("Invalid GST format") |
| Address | text | Optional |
| Notes | text | Optional |

### 5.9 Feat-PAY — Trip Payment Collection

> Module `screen-trip-payment`. **Financial feature — requires `financials:read` (default L0/L1).** Payment
> needs trip data but cannot depend on `screen-trip`; the `TripProviderForPayment`
> adapter (wired in `sharedUI`) bridges that gap.

#### SCR-PAY-01 — Payments List
| | |
|---|---|
| **Route** | `Payments` |
| **Access** | L0–L1 |
| **Purpose** | All recorded payments, grouped, with a filter bottom sheet (status, mode, date). Find outstanding/received money fast. |
| **Inputs** | Filters: payment status (`received, pending, cancelled`), mode, period. |

#### SCR-PAY-02 — Payment Detail
| | |
|---|---|
| **Route** | `PaymentDetail(paymentId)` |
| **Access** | L0–L1 |
| **Purpose** | One payment: amount, mode, reference number, date/time, **received-by** person, and the linked trip summary card. Entry point to Edit. |

#### SCR-PAY-03 — Add Payment
| | |
|---|---|
| **Route** | `AddPayment(tripId?, vehicleId?)` |
| **Access** | L0–L1 |
| **Purpose** | Record a customer payment against a trip. Trip pre-selected when navigated from Trip Detail; otherwise a trip selector bottom sheet is shown. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Trip | selector bottom sheet | Required — "Please select a trip" |
| Amount | numeric | Required; valid amount (> 0) |
| Payment Date | `FleetDateField` | Required — "Payment date is required" |
| Payment Time | `FleetTimeField` | `HH:MM` |
| Payment Mode | dropdown | One of `cash, upi, bank_transfer, cheque, card` |
| Reference No. | text | Optional (UPI ref / cheque no.) |
| Received By | text/picker | Person who received the money |
| Notes | text | Optional |

Trip-level payment status auto-derives: `pending → partial → paid`.

#### SCR-PAY-04 — Edit Payment
| | |
|---|---|
| **Route** | `EditPayment(paymentId)` |
| **Access** | L0–L1 |
| **Purpose** | Correct an existing payment (amount, date, mode, reference). Same validation as Add Payment; trip link is fixed. |

### 5.10 Feat-RPT — Reports & Analytics

> **Gate:** `financials:read` (default: L0/L1 bundles). All reports share the period
> filter: `today, weekly, 15 days, monthly, quarterly, half-yearly, yearly, custom`
> (custom = from/to `DD-MM-YYYY`, from ≤ to). Every report exports to **PDF**
> via `ijs-pdf-report` (Android WebView / iOS WKWebView / browser print).

#### 5.10.1 Reporting Foundations (definitions used by every report)

These definitions come from the actual backend implementation
(`internal/application/report/usecase.go`, `internal/domain/report/entity.go`,
`report_repo.go`) — they are the **single source of truth** for every number shown
in the app.

**(a) Period resolution** — a named period becomes a `[start, end]` date range:

| Period | Start | End |
|--------|-------|-----|
| `today` | today 00:00 | now |
| `weekly` | now − 7 days | now |
| `monthly` (default) | now − 1 month | now |
| `yearly` | now − 1 year | now |
| `custom` | user "from" date | user "to" date |

Only records whose date falls inside `[start, end]` participate in that report.

**(b) Revenue** — money the business *earned*:

```
Revenue (period)  =  Σ trip_price   of trips with state = 'completed'
                                     whose scheduled date ∈ period
```

> Revenue is **recognized on trip completion**, not on payment receipt. A completed
> ₹50,000 trip counts as revenue even if the customer hasn't paid yet — the unpaid
> part shows separately as *pending payments* (cash-flow view, below).

**(c) Expenses** — money the business *spent*, three structured streams:

```
Trip costs         =  Σ amount of trip-cost entries        (groups TC-G-001…006)
Maintenance costs  =  Σ amount of maintenance-cost entries (groups VMC-G-001…007)
Driver costs       =  Σ amount of driver-cost entries      (groups DC-G-001…004)

Total Expenses (operating)  =  Trip costs + Maintenance costs
```

**(d) Profit, margin, and status — the three universal formulas:**

```
Net Profit      =  Revenue − Total Expenses
Profit Margin % =  (Net Profit ÷ Revenue) × 100        (0 when Revenue ≤ 0 —
                                                         division-by-zero safe)
Profit Status   =  "profit"      when Net Profit > 0
                   "loss"        when Net Profit < 0
                   "break_even"  when Net Profit = 0
```

**(e) Cash-flow view (payments)** — independent of profit:

```
Received Payments =  Σ ( trip_price            when payment_status = 'full'
                         partial_payment_amount when payment_status = 'partial' )
Pending Payments  =  Σ pending_amount   of trips with payment_status ∈
                                          ('pending', 'partial')
```

**(f) Worked example** (used throughout this section):

> In May, a fleet completed **8 trips** billing **₹4,00,000** total. Trip costs
> recorded: ₹1,40,000 (₹90,000 fuel, ₹25,000 toll/parking, ₹15,000 driver
> expenses, ₹10,000 misc). Maintenance: ₹35,000. Customers have paid ₹3,10,000.
>
> - Total Expenses = 1,40,000 + 35,000 = **₹1,75,000**
> - Net Profit = 4,00,000 − 1,75,000 = **₹2,25,000** → status `profit`
> - Profit Margin = 2,25,000 ÷ 4,00,000 × 100 = **56.25%**
> - Pending Payments = 4,00,000 − 3,10,000 = **₹90,000**
> - Avg revenue/trip = 4,00,000 ÷ 8 = **₹50,000**; avg profit/trip = **₹28,125**

#### SCR-RPT-01 — Reports Hub
| | |
|---|---|
| **Route** | `Reports` |
| **Purpose** | Summary tiles + navigation into the four detailed reports; the period filter selected here applies across the hub. |

The hub shows the **Financial Summary** computed as:

```
Total Expenses   = Trip costs + Maintenance costs            (period)
Net Profit       = Revenue − Total Expenses
Profit Margin %  = Net Profit ÷ Revenue × 100
Avg Trip Revenue = Revenue    ÷ Completed trips
Avg Trip Cost    = Trip costs ÷ Completed trips
Avg Trip Profit  = Net Profit ÷ Completed trips
+ Received / Pending payments, Completed / Total trip counts
```

#### SCR-RPT-02 — Vehicle Profit & Loss
| | |
|---|---|
| **Route** | `VehicleProfitLoss` |
| **Purpose** | Revenue vs expenses **per vehicle** over the period. The report that answers *"which truck is earning and which one is bleeding?"* |

**Per-vehicle calculation:**

```
Revenue        = Σ trip_price of this vehicle's completed trips in period
Fuel Cost      = Σ trip-cost amounts in group TC-G-001 (Fuel & Energy)
Other Cost     = Σ trip-cost amounts in all other groups (TC-G-002…006)
Maintenance    = Σ maintenance-cost amounts for this vehicle in period
Total Cost     = Fuel Cost + Other Cost + Maintenance

Net Profit          = Revenue − Total Cost
Profit Margin %     = Net Profit ÷ Revenue × 100
Avg Profit per Trip = Net Profit ÷ Total Trips         (0 if no trips)
Avg Profit per Km   = Net Profit ÷ Total Distance (km) (0 if no distance)
```

`Total Distance = Σ actual_distance` of the vehicle's trips — making
**profit-per-km** the single best number for comparing vehicles of different sizes
and routes.

**Fleet roll-up (all vehicles view):** the screen also aggregates every vehicle:

```
Fleet totals    = Σ over vehicles (trips, distance, revenue, cost, net profit)
Active Vehicles = count of vehicles with ≥ 1 trip in the period
Profitable / Loss-making vehicles = counts by Net Profit ≥ 0 / < 0
Average Profit Margin = Σ vehicle margins ÷ vehicle count
```

> **Reading the report:** a vehicle with high revenue but low profit-per-km usually
> has a fuel or maintenance problem; a vehicle with zero trips ("inactive" in the
> period) still accumulates maintenance and EMI — candidates for sale or
> redeployment.

#### SCR-RPT-03 — Trip Profit & Loss
| | |
|---|---|
| **Route** | `TripProfitLoss` |
| **Purpose** | Margin of every individual trip. Identifies loss-making routes, customers, and cargo types. Filterable by date range and vehicle. |

**Per-trip calculation (two-stage margin):**

```
Gross Profit   = Selling Value − Purchase Price
                 (Purchase Price = what the cargo/job cost to acquire, when the
                  business buys-and-sells cargo; 0 for pure transport jobs)
Net Profit     = Gross Profit − Σ trip costs of this trip
Profit Margin% = Net Profit ÷ Selling Value × 100
```

Each trip row shows: date, vehicle registration, driver, route
(start → end), distance, price, total cost, gross/net profit, margin,
payment status and pending amount, plus the **cost breakdown by cost item**
(e.g., Diesel ₹12,000, Toll ₹3,500, Driver Food ₹800…).

**Summary block:**

```
Total Revenue   = Σ selling values
Total Expenses  = Σ (trip costs + purchase prices)
Total P/L       = Σ net profits
Profitable Trips / Loss-making Trips = counts by Net Profit ≥ 0 / < 0
Average Profit Margin = Σ trip margins ÷ trip count
```

**Worked example:** trip Indore → Mumbai, selling value ₹62,000, purchase price
₹40,000 (bought cargo), trip costs ₹9,500 (fuel 6,800 + toll 1,700 + food 1,000).
Gross = 62,000 − 40,000 = ₹22,000. **Net = 22,000 − 9,500 = ₹12,500.**
Margin = 12,500 ÷ 62,000 × 100 = **20.2%**.

#### SCR-RPT-04 — Cost Analysis
| | |
|---|---|
| **Route** | `CostAnalysis` |
| **Purpose** | Where the money actually goes: spend broken down by cost type/group over the period, with statistics. Supports selecting one or many cost types and restricting to specific vehicles. |

**Per-cost-type calculation:**

```
Total Amount        = Σ amounts of entries of this cost type in period
% of Total Expenses = Total Amount ÷ Total of all expenses × 100
Avg per Trip        = Total Amount ÷ number of trips having this cost
Avg per Vehicle     = Total Amount ÷ number of vehicles having this cost
Highest / Lowest single expense = MAX / MIN single entry amount
By-vehicle and by-month breakdowns for drill-down
```

**Multi-type comparison:** each selected type gets its share of the
**selected total** (`type total ÷ Σ selected totals × 100`), and the report
flags the **highest cost type** (name + amount).

**Fuel intelligence (group TC-G-001):** because fuel entries can carry quantity,
rate, and odometer-based efficiency, the analysis additionally computes:

```
Total Fuel Quantity   = Σ fuel_quantity (litres)
Average Fuel Rate     = AVG(fuel_rate)      (₹/litre, zero entries excluded)
Average Efficiency    = AVG(km_per_liter)   (zero entries excluded)
```

> **Reading the report:** fuel is typically 40–55% of trip cost for Indian goods
> carriers. A rising ₹/litre average is market-driven; a falling km/l average on
> one vehicle is a maintenance signal; toll % that jumps suggests route changes.

#### SCR-RPT-05 — Consolidated P&L
| | |
|---|---|
| **Route** | `ConsolidatedPL` |
| **Purpose** | The whole-business statement — the report the owner shares with the accountant. Optional filters: specific vehicles, specific trips, specific cost types (the "filters applied" header shows what was included). |

**Statement structure & calculation:**

```
REVENUE
  Total Revenue           = Σ completed-trip prices (after filters)
  └ by vehicle breakdown

EXPENSES
  Total Expenses          = Σ expenses across every cost type (after filters)
  └ by cost type breakdown  (each type: amount + % of Total Expenses)
  └ by vehicle breakdown

PROFIT / LOSS
  Gross Profit            = Total Revenue − Total Purchase (cargo buy cost)
  Net Profit              = Gross Profit − Total Expenses
  Profit Margin %         = Net Profit ÷ Total Revenue × 100
  Status                  = profit / loss / break_even
  └ by-period breakdown   (per month/week buckets inside the range)
```

**Trend analysis** — the by-period buckets are split into two halves
(first half vs second half of the range) and each of revenue, expenses, and
profit gets a direction:

```
change = (second-half total − first-half total) ÷ first-half total
trend  = "increasing"  when change > +5%
         "decreasing"  when change < −5%
         "stable"      otherwise            (also when < 2 periods of data)
```

So the consolidated report tells the owner not just *"you made ₹2.25 lakh"*
but *"revenue increasing, expenses stable, profit increasing."*

**Executive summary block** (same screen / `profit-loss summary` API):

| Block | Contents & calculation |
|-------|------------------------|
| Overview | Total revenue, total expenses, gross profit, margin %, status |
| Fleet summary | Total / active vehicles; **profitable** vs **loss-making** vehicle counts (by per-vehicle Net Profit sign) |
| Trip summary | Total / completed trips; profitable vs loss-making trips |
| Expense breakdown | Every cost type: `amount` + `% of total expenses` (maintenance shown as its own line) |
| Top performers | **Most profitable vehicle** (max per-vehicle profit) and **most profitable route** (start→end pair with highest summed profit + trip count) |
| Alerts | List of **loss-making vehicles** with the loss amount (`−Net Profit`) — the owner's action list |

#### 5.10.2 Where Each Money Number Lives (cross-feature map)

| Number | Produced by | Consumed by |
|--------|-------------|------------|
| Trip price / selling value | Create Trip (SCR-TRP-03) | Trip P&L, Vehicle P&L, Consolidated, Dashboard |
| Trip costs | Trip Cost Entry (SCR-TRP-04) | All four reports, Cost Overview |
| Maintenance costs | Maintenance Cost Entry (SCR-VEH-04) | Vehicle P&L, Cost Analysis, Consolidated |
| Driver costs | Driver Cost Entry (SCR-DRV-04) | Driver earnings summary (driver detail); roadmap: include in Consolidated P&L (FE-15) |
| Customer payments | Add Payment (SCR-PAY-03) | Received/Pending payments, payment statuses |
| EMI payments | EMI History (SCR-FIN-05) | Vehicle finance summaries; roadmap: include EMI burden in Vehicle P&L (FE-15) |

> **Known accounting gaps (deliberate, tracked):** driver costs and EMI payments
> are **not yet** subtracted inside Vehicle/Consolidated P&L — today's "Net Profit"
> is an *operating* profit (revenue minus trip + maintenance costs). Folding
> driver salaries, EMI, insurance amortization, and depreciation into a "true
> net profit" view is roadmap item **FE-15**.

### 5.11 Feat-FIN — Vehicle Finance

> **Requires `financials:read` (default L0/L1).** Tracks how each vehicle was purchased and the loan behind it.
> Uses the **shared ViewModel pattern** (`rememberSharedViewModel("finance_$vehicleId")`)
> so Detail / EMI History / Edit screens share one state.

#### SCR-FIN-01 — Vehicle Finance List
| **Route** | `VehicleFinance` — all vehicles with their finance status (cash / loan, EMIs remaining). Entry to detail and Add Purchase Info. |
|---|---|

#### SCR-FIN-02 — Vehicle Finance Detail
| **Route** | `VehicleFinanceDetail(vehicleId)` — purchase record, loan summary (financier, principal, interest, tenure, EMIs paid/remaining), links to EMI History and Edit. **Cash-purchased vehicles show an empty loan state, not an error.** |
|---|---|

#### SCR-FIN-03 — Add Purchase Info
| | |
|---|---|
| **Route** | `AddPurchaseInfo` |
| **Access** | L0–L1 |
| **Purpose** | Record how a vehicle was purchased — cash or loan. Loan fields appear only when purchase type = loan. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| Vehicle | picker | Required (submit blocked without it) |
| Purchase Type | toggle | `cash` / `loan` |
| Purchase Date | `FleetDateField` | Required — "Purchase date is required" |
| Purchase Price | numeric | Required; valid number — "Valid purchase price is required" |
| **Loan-only fields:** | | |
| Down Payment | numeric | Required — "Down payment is required" |
| Interest Rate (%) | numeric | Required — "Interest rate is required" |
| Tenure (months) | numeric | Required; valid integer — "Valid tenure is required" |
| Financier Name | text | Required — "Financier name is required" |
| EMI Start Date | `FleetDateField` | Required — "EMI start date is required" |

#### SCR-FIN-04 — Edit Purchase Info
| **Route** | `EditPurchaseInfo(vehicleId)` — same form and validation as SCR-FIN-03, pre-filled. |
|---|---|

#### SCR-FIN-05 — EMI Payment History
| | |
|---|---|
| **Route** | `EmiPaymentHistory(vehicleId)` |
| **Access** | L0–L1 |
| **Purpose** | Chronological EMI payment list with running totals; record a new EMI payment. |

**Record-EMI inputs:**

| Field | Type | Rules |
|-------|------|-------|
| Amount | numeric | Required; valid amount — "Valid amount is required" |
| Payment Date | `FleetDateField` | Required — "Payment date is required" |
| Notes | text | Optional |

### 5.12 Feat-TEM — Team Management

> **Requires the `users:*` permission family (default L0/L1).** Creates real **IAM login accounts** inside the organization's
> tenant and syncs the role's permission set onto the new user.

#### SCR-TEM-01 — Team List
| **Route** | `TeamList` — all team members with role + active status. Local caching (`TeamLocalDataSource`). "+" → Create Team Member. |
|---|---|

#### SCR-TEM-02 — Team Member Detail
| | |
|---|---|
| **Route** | `TeamMemberDetail(memberId)` |
| **Access** | L0–L1 |
| **Purpose** | Member profile + permissions. Admin actions, each gated by its own IAM permission: **Change Role** (`users:change_role`), **Enable/Disable** (`users:toggle_active`), **Reset Password** (`users:reset_password`), edit contact details (validated like Create). |

#### SCR-TEM-03 — Create Team Member
| | |
|---|---|
| **Route** | `CreateTeamMember(excludeGeneralManager)` |
| **Access** | L0–L1; role picker is filtered by what the current user may assign — a user can never assign a bundle at or above their own level (the legacy `excludeGeneralManager` route flag hides the full-access bundle when applicable) |
| **Purpose** | Add a staff member by assigning an IAM role bundle (`owner` / `admin` / `user`). Role list loads live from `GET /team/members/roles` with a static fallback. |

**Inputs & validations:**

| Field | Type | Rules |
|-------|------|-------|
| First Name | text | Required |
| Last Name | text | Required |
| Email | email | Required; valid format (becomes the IAM login) |
| Mobile | numeric | Required; valid 10-digit Indian mobile |
| Password | password | Required; **min 8 characters**; **must not contain the member's name** |
| Confirm Password | password | Required; must match |
| Role | picker (live from IAM) | Required; only roles the current user may assign are listed |

### 5.13 Feat-MAP — Real-Time GPS Tracking

#### SCR-MAP-01 — Maps
| | |
|---|---|
| **Route** | `Maps` |
| **Access** | L0–L3 |
| **Purpose** | Live map of all vehicle positions. Subscribes to MQTT topic `fleet/{ownerId}/vehicle/{vehicleId}/location` published by the Location Tracker APK. *(Current status: mock data; live MQTT wiring in progress.)* |
| **Inputs** | Map pan/zoom; vehicle marker tap → info card. |

#### Companion app — Location Tracker (`locationTracker`, separate APK)
- Driver logs in with their **IAM `driver` account**.
- Foreground service publishes GPS over **MQTT (HiveMQ)**.
- No dependency on the main app; minSdk 24.

### 5.14 Feat-ALR — Alerts

#### SCR-ALR-01 — Alerts List
| | |
|---|---|
| **Route** | `AlertsList` |
| **Access** | L0–L3 |
| **Purpose** | One place for every compliance warning. Alert types: **Document Expiry** (RC, Insurance, Fitness, Permit, PUC), **License Expiry** (drivers), **Maintenance Due**. Tapping an alert deep-links to the relevant vehicle/driver. Alerts clear automatically when a new document/expiry is uploaded. Also previewed on the Dashboard. |
| **Inputs** | Type filter chips. |

### 5.15 Feat-NTF — Push Notifications & Notification Center *(planned — FE-06)*

> **Status:** 🔮 Planned. This section specifies the target design so the feature
> ships with a stable code from day one.
> **Difference from Alerts (Feat-ALR):** Alerts are *pull* — computed compliance
> warnings the user opens. Notifications are *push* — events delivered to the
> user's device the moment they happen, plus a persistent in-app inbox.

**Delivery channels:** FCM (Android), APNs (iOS), Web Push (browser targets).
Device tokens are registered per user + device after login and revoked on logout.

**Notification event types (initial set):**

| Event | Triggered when | Default audience (by permission) |
|-------|----------------|----------------------------------|
| Document expiry | Vehicle document within lead time (links to Feat-ALR) | baseline read |
| License expiry | Driver license within lead time | baseline read |
| Payment received | A customer payment is recorded | `financials:read` |
| Payment overdue | Trip pending amount past due | `financials:read` |
| Trip status change | planned → on_route → completed / cancelled / failed / delayed | baseline read |
| Maintenance due | Scheduled maintenance overdue | baseline read |
| Team changes | Member added / role changed / disabled | `users:read` |
| Subscription | Renewal due, payment failed | tenant owner (L0) |

#### SCR-NTF-01 — Notification Center
| | |
|---|---|
| **Route** | `Notifications` *(to be added to `FleetRoute.kt`)* |
| **Access** | All authenticated users — each user sees only notifications permitted to them (a notification gated by `financials:read` is never delivered to / shown for a user without it) |
| **Purpose** | The in-app inbox: chronological list of received notifications with unread badge (count surfaces on the Dashboard top bar / drawer), read/unread state, and deep links — tapping a notification opens the relevant screen (trip detail, vehicle documents tab, payment detail…). |
| **Inputs** | Type filter chips; "mark all read"; per-item swipe to dismiss. |
| **Validation** | None (read-only list). |

#### SCR-NTF-02 — Notification Settings
| | |
|---|---|
| **Route** | `NotificationSettings` *(to be added)* |
| **Access** | All authenticated users (own preferences only) |
| **Purpose** | Per-user opt-in/out toggles per event type, quiet hours, and channel choice (push / in-app only). Lead-time configuration for expiry events ties into FE-13. |
| **Inputs** | Toggle per event type; quiet-hours time range (`HH:MM`–`HH:MM`). |

### 5.16 Feat-LOC — Localization & Feat-THM — Theming

- **Feat-LOC:** English (default) + **Hindi** via shared string resources.
  Core screens done; list/detail screens for Vehicles, Drivers, Trips, Customers,
  Payments, Reports, Team, Finance are still being migrated. Layouts must never
  clip longer Hindi labels.
- **Feat-THM:** Material 3 with full **light/dark mode**, user-toggleable from any
  top bar; dynamic color via MaterialKolor. Colors always come from
  `MaterialTheme.colorScheme` — never hardcoded.

---

## 6. Key User Journeys (Flows)

### 6.1 New Owner Journey (first run)
```
Install app → Onboarding pages (SCR-ONB-01) → Sign Up (SCR-AUT-02)
→ OTP verification (SCR-AUT-04)
→ Choose subscription plan (SCR-SUB-01) → Pay via Razorpay (SCR-SUB-02)
→ Payment success (SCR-SUB-03) → Create Organization (SCR-SUB-04)
→ Add first team members (SCR-TEM-03) → Dashboard (SCR-DSH-01, fully unlocked)
```

### 6.2 Daily Operations Journey (L2 Operations User)
```
Login → Dashboard → Create Trip (SCR-TRP-03)
  (pick vehicle + driver → route via Places → schedule → cargo → customer)
→ Trip "planned" → driver departs → mark "on_route"
→ Along the way: add trip costs (SCR-TRP-04: fuel, toll…)
→ Trip arrives → mark "completed"
→ A user holding `financials:read` records the customer payment (SCR-PAY-03) — the L2 operations user cannot
```

### 6.3 Money Review Journey (Owner)
```
Login → Dashboard (Financial Summary)
→ Reports (SCR-RPT-01) → Vehicle P&L / Trip P&L / Consolidated P&L
→ Filter by period → Export PDF → share with accountant
```

### 6.4 Compliance Journey
```
Dashboard alert: "Insurance expiring in 7 days"
→ Alerts list (SCR-ALR-01) → open Vehicle → Documents tab
→ renew offline → upload new document with new expiry date
→ alert clears automatically
```

### 6.5 Vehicle Finance Journey (Owner)
```
Buy a truck on loan → Vehicle Finance (SCR-FIN-01) → Add Purchase Info (SCR-FIN-03)
→ each month: EMI Payment History (SCR-FIN-05) → record EMI
→ Vehicle P&L report includes the EMI burden per vehicle
```

### 6.6 Driver Tracking Journey
```
Owner creates driver with password (SCR-DRV-03) → IAM driver account created
→ Location Tracker APK installed on driver's phone → driver logs in
→ tracker publishes GPS over MQTT → owner watches Maps (SCR-MAP-01)
```

---

## 7. Entity State Machines & Data Types

### 7.1 Vehicle States
```
inactive ←→ active → on_route → active
                   → maintenance ←→ damaged → decommissioned (final)
```
A `decommissioned` vehicle can never be assigned to a new trip.

### 7.2 Driver States
```
inactive ←→ active → on_route → active
                   → on_leave / suspended → active
                   → terminated (final)
```
A `terminated` driver can never be assigned to a new trip.

### 7.3 Trip States
```
planned → on_route → completed
planned → cancelled
on_route → failed / delayed
```

### 7.4 Data Type Reference

| Category | Values |
|----------|--------|
| Cargo types | `Gitti, Balu, Bhakshi, Enta, Hazardous, Valuable, Others` |
| Trip cost types | `fuel, toll, driver_allowance, parking, loading_charges, unloading_charges, chalan, permit, insurance, other` |
| Maintenance cost types | `tyre, battery, servicing, engine_repair, body_repair, electrical, ac_repair, other` |
| Driver cost types | `salary, advance, bonus, penalty` |
| Payment modes | `cash, upi, bank_transfer, cheque, card` |
| Payment status (record) | `received, pending, cancelled` |
| Payment status (trip-level) | `pending, partial, paid` |
| Document types | `RC, Insurance, Fitness, Permit, PUC` |
| Report periods | `today, weekly, 15 days, monthly, quarterly, half-yearly, yearly, custom` |

---

## 8. Technical Requirements

### 8.1 Technology Stack
| Category | Technology | Version |
|----------|------------|---------|
| Language | Kotlin Multiplatform | 2.3.0 |
| UI | Compose Multiplatform + Material 3 | 1.10.x |
| Networking | Ktor Client | 3.3.3 |
| DI | Metro (ZacSweers) | 0.9.1 |
| Navigation | Navigation 3 (type-safe routes) | 1.1.0-alpha01 |
| Serialization | kotlinx-serialization | 1.9.0 |
| Local storage | Room + multiplatform-settings | 2.8.4 / 1.3.0 |
| Date/Time | kotlinx-datetime | 0.7.1 |
| Logging | Kermit (via `ijs-logger-lib`) | 2.0.8 |
| Crash reporting | Firebase Crashlytics | BOM |
| Tracking | MQTT (HiveMQ) | — |
| Location services | Google Places + Distance Matrix | — |
| Payments | Razorpay | — |

### 8.2 Architecture (mandatory rules)
- **Clean Architecture** in every module: Domain → Data → Presentation.
- **MVI pattern** for all screens (State / Intent / Effect contract, `MviViewModel` base).
- **Metro DI** with manual wiring through `DefaultViewModelProvider` + `FeatureRepositoryFactory`.
- **Facade pattern**: every `screen-*` module exposes exactly one
  `{Feature}FeatureFacade` of `@Composable` entry points. Navigation is passed in
  as lambdas — feature modules never see `FleetRoute`.
- **Cross-feature adapters** for inter-module data (e.g., `TripProviderForPayment`
  bridged by `TripProviderAdapter` in `sharedUI`).
- **500-line file limit** — any class beyond that must be split by responsibility.
- All async results wrapped in `Result<T>` (`Success` / `Error` / `Loading`).
- Background work only via injected `DispatcherProvider` (never raw `Dispatchers.IO`).

### 8.3 API Conventions
- Base path: `/api/v1` (Cloud Run hosted Go backend).
- **Authorization model:** Bearer JWT issued by **IndusJS-IAM**. The Fleet backend
  enriches each request with the caller's IAM permissions and enforces them per route
  (`RequireIAMPermission`). Unauthorized actions return **403** with a clear message.
  Expired/invalid sessions return **401** → app auto-logout.
- DTOs use `@Serializable` + `@SerialName` snake_case mapping.
- Standard response wrapper: `{ success, message, data }`; errors use
  `{ errorCode, errorMessage, developerMessage, statusCode }`.
- **Date formats (critical):**
  - Trip scheduling → ISO 8601 (`2026-06-11T14:30:00Z`) via `FleetDateTime.toIso8601`
  - Cost entries / document expiry → `DD-MM-YYYY` (+ `HH:MM` where needed)
  - UI always shows `DD-MM-YYYY`, 24-hour time.
- **DTO safety rule (learned the hard way):** every DTO field must have a sensible
  default so a partial backend response never throws `MissingFieldException`
  and silently blanks the screen.
- Repositories must surface backend errors — never swallow failures and return empty data.

### 8.4 Non-Functional Requirements
| Area | Requirement |
|------|-------------|
| Performance | List screens paginate; dashboard caches offline (Room) |
| Security | Bearer token auth; auto-logout on 401; no secrets in code (`local.properties`) |
| Reliability | Graceful fallbacks when backend status calls fail |
| Usability | Loading / error / empty states on every screen; retry buttons; submit-button debounce |
| Accessibility | Material 3 components, content descriptions on icons |
| i18n | English + Hindi; architecture supports more languages |
| Observability | Kermit logging with session IDs; Crashlytics on Android |

---

## 9. Edge Cases & Corner Scenarios

| # | Scenario | Expected behavior |
|---|----------|-------------------|
| 1 | Token expires mid-session (401) | Auto-redirect to Login, clear back stack |
| 2 | Backend returns partial JSON (missing fields) | DTO defaults absorb it; screen still renders |
| 3 | Onboarding-status API fails after signup | User without org → Plan screen (never skipped ahead) |
| 4 | Payment made but app killed before success screen | Gate check on next launch resumes at correct step |
| 5 | Trip cost dated before trip start | Validation blocks it (`getMinDateForTripCost`) |
| 6 | Driver cost dated before joining date or after tomorrow | Date picker bounds block it (min = joining date, max = tomorrow) |
| 7 | User without `financials:read` opens a money screen via deep link | Financial fields hidden in UI; server enforces 403 |
| 8 | Decommissioned vehicle / terminated driver | Cannot be assigned to new trips (terminal states) |
| 9 | Vehicle/driver already on an active trip | Create Trip blocks selection ("currently occupied") |
| 10 | Arrival date entered without arrival time (or vice versa) | Validation requires both or neither |
| 11 | Duplicate cost submission (double-tap) | Save button disabled while submitting |
| 12 | Offline launch | Cached dashboard data shown; clear error + retry elsewhere |
| 13 | Returning from cost entry to detail screen | Shared ViewModel triggers automatic refresh |
| 14 | Long email/name in profile | Text wraps, never truncated with "…" |
| 15 | Hindi locale with long labels | Layouts must not clip translated text |
| 16 | Same customer pays partially across months | Trip payment status = `partial` until fully paid |
| 17 | EMI history for cash-purchased vehicle | Shows empty state, not an error |
| 18 | Two roles edit the same trip | Last-write-wins today; conflict detection is future work |
| 19 | Tenant created but new JWT not delivered (no `tid` claim) | App detects locally and prompts re-login instead of looping on 403 |
| 20 | Role-picker endpoint missing on backend | App falls back to static `owner`/`admin`/`user` roles |
| 21 | IAM permission fetch fails server-side | Backend must not silently 403; app shows actionable "re-login" message |
| 22 | Driver creation fails: "role not found in tenant" | App shows "Driver role is not configured for this organization" (backend must seed `driver` role) |
| 23 | Razorpay order created without `order_id` | Checkout fails fast with explicit developer-facing message; user can retry |
| 24 | Registration number entered lowercase / with spaces | Auto-uppercased; regex blocks non-Indian formats |
| 25 | Vehicle year set to future | Blocked: year ≤ current year + 1, ≥ 1990 |

---

## 10. Future Enhancements & Roadmap

### 10.1 Near Term (next 1–2 releases)
| Code | Enhancement | Detail |
|------|------------|--------|
| FE-01 | **Complete IAM role migration** | Backend ships `GET /team/members/roles` + accepts IAM role names directly; app removes the legacy `general_manager`/`manager`/`supervisor` mapping (`Docs/BACKEND_TEAM_MEMBER_IAM_ROLES_SPEC.md`) |
| FE-02 | **Seed `driver` IAM role per tenant** | So driver login accounts always work out of the box |
| FE-03 | **Finish localization** | Remaining list/detail screens (Vehicles, Drivers, Trips, Customers, Payments, Reports, Team, Finance) |
| FE-04 | **Live MQTT tracking** | Replace mock data on Maps (SCR-MAP-01) with real HiveMQ subscription |
| FE-05 | **Subscription payment hardening** | Webhooks, retry on failed payment, invoice download |
| FE-06 | **Push notifications** | Implement feature **Feat-NTF** (§5.15): FCM/APNs/Web Push delivery + Notification Center (SCR-NTF-01) + Notification Settings (SCR-NTF-02) |

### 10.2 Mid Term
| Code | Enhancement | Detail |
|------|------------|--------|
| FE-07 | **Field cost-capture companion app** | A separate, lightweight mobile app (own codebase/APK, same IAM login + Fleet API) through which a **driver — or any user whose role bundle grants the cost permissions** — uploads **vehicle and trip costs** from the field: fuel receipt, toll, repair, photo proof. Permissions, not titles, decide who may upload. Also: driver sees own trips and updates own trip status. |
| FE-08 | **Photo attachments** | Receipts on costs, proof-of-delivery on trips |
| FE-09 | **Bulk import/export** | CSV/Excel for vehicles, drivers, customers |
| FE-10 | **Advanced filters & search** | Across all list screens (multi-criteria, saved filters) |
| FE-11 | **Offline-first writes** | Queue cost/payment entries created offline; sync later with conflict prompts |
| FE-12 | **Geofencing alerts** | Notify when a vehicle enters/leaves a defined zone |
| FE-13 | **In-app alerts configuration** | Configurable lead time per document type (default ≥ 30 days) |
| FE-14 | **Custom role bundles** | In-app role editor: owner composes a named permission bundle (e.g., "Accountant" = read-only + `financials:read`) and assigns it — the IAM data model already supports this (§2.3) |
| FE-15 | **True net profit** | Fold driver costs, EMI payments, insurance amortization, and depreciation into Vehicle / Consolidated P&L so "Net Profit" becomes a full accounting figure, not just operating profit (§5.10.2) |

### 10.3 Long Term
| Code | Enhancement | Detail |
|------|------------|--------|
| FE-16 | **Desktop target** | Compose for Desktop |
| FE-17 | **Multi-organization support** | One owner, multiple fleets/companies, org switcher |
| FE-18 | **Fuel card / FASTag integrations** | Auto-import fuel and toll costs |
| FE-19 | **Predictive maintenance** | Service reminders based on distance/usage patterns |
| FE-20 | **Customer portal** | Customers see their own trips and pay online |
| FE-21 | **GST-ready invoicing** | Generate tax invoices from trips and payments |
| FE-22 | **More languages** | Marathi, Tamil, Telugu, Gujarati, Punjabi |
| FE-23 | **Audit log** | Who changed what and when (compliance) |
| FE-24 | **Conflict resolution** | Optimistic-locking / merge UI for concurrent edits |
| FE-25 | **Route optimization** | Suggested routing & dispatch automation |
| FE-26 | **Analytics dashboard v2** | Trends, charts, vehicle utilization %, idle-time analysis |

---

## 11. Success Metrics

| Metric | Target |
|--------|--------|
| Owner can create a trip end-to-end | < 2 minutes |
| Crash-free sessions | > 99.5% |
| Alert lead time before document expiry | ≥ 30 days (configurable, FE-13) |
| Payment recording accuracy | 100% (every payment links to a trip) |
| Report generation time | < 5 seconds for monthly P&L |
| Onboarding funnel completion (signup → dashboard) | > 80% |
| Form validation coverage | Every required field has an inline error message (see §5) |
| Role leakage incidents (money data shown to L2/L3) | 0 |

---

## 12. Out of Scope (For Now)

- Hardware GPS device integrations (only phone-based tracking).
- Route optimization / dispatch automation (long-term FE-25).
- Marketplace features (load boards, broker matching).
- Payroll processing (we record driver costs; we don't pay salaries).
- Accounting software replacement (we export PDFs; we are not Tally/QuickBooks).

---

## 13. Glossary

| Term | Meaning |
|------|---------|
| IAM | IndusJS Identity & Access Management — separate service owning users, tenants, roles, permissions, tokens, billing |
| Tenant / Organization | One fleet business account inside the SaaS system (lives in IAM) |
| `tid` claim | The tenant id inside the JWT — required for all tenant-scoped APIs |
| RBAC | Role-Based Access Control — roles bundle fine-grained permissions like `vehicles:create` |
| L0–L4 | The five access tiers: Tenant Owner, Full-Access, Operations, Basic, Driver (§2.2) — documentation shorthand for permission bundles, not job titles |
| Feature code (Feat-XXX) | Stable identifier for a product feature (§4) |
| Screen code (SCR-XXX-NN) | Stable identifier for an individual screen (§5) |
| Trip cost | Money spent during a trip (fuel, toll, etc.) |
| Maintenance cost | Money spent fixing/servicing a vehicle |
| Driver cost | Money paid to a driver (salary, advance, bonus, penalty) |
| Caretaker | A team member assigned responsibility for a driver |
| P&L | Profit & Loss — revenue minus expenses |
| EMI | Monthly loan installment for a financed vehicle |
| Gate | A startup check that decides which screen the user must see next |
| Facade | The single public entry point a feature module exposes |
| MVI | Model–View–Intent UI pattern (State / Intent / Effect) |
| Occupied | A vehicle/driver currently assigned to an active trip (blocks reassignment) |

