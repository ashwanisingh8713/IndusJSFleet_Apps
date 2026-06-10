# IndusJS Fleet 🚛

> A complete **Fleet Management System** for transport & logistics businesses —
> built once in **Kotlin Multiplatform**, running on **Android, iOS, and Web**.

Manage vehicles, drivers, trips, costs, customer payments, team members,
vehicle loans/EMIs, profit-loss reports, document-expiry alerts, and
real-time GPS tracking — all from one app.

---

## 📚 Documentation Map

| Document | What's inside |
|----------|---------------|
| **[IndusJS_Fleet_Software_Development_PRD.md](IndusJS_Fleet_Software_Development_PRD.md)** | Product requirements: users, features, flows, edge cases, roadmap |
| **[project_context.md](project_context.md)** | Engineering context: architecture, conventions, key files, lessons learned |
| `AGENTS.md` | Root module guide (each module also has its own `AGENTS.md`) |
| `.github/copilot-instructions.md` | Coding rules for AI assistants |
| `Docs/` | API specs, postman collections, DB schemas, design plans |

---

## ✨ What It Does

| Area | Highlights |
|------|------------|
| 🚚 **Vehicles** | CRUD, documents (RC/Insurance/Permit) with expiry alerts, maintenance costs, lifecycle states |
| 👨‍✈️ **Drivers** | Profiles, license tracking, costs (salary/advance/bonus/penalty) |
| 🗺️ **Trips** | Route planning (Google Places), cargo, scheduling, trip costs, state machine |
| 💰 **Payments** | Customer payments (cash/UPI/bank/cheque/card), pending tracking |
| 📊 **Reports** | Vehicle P&L, Trip P&L, cost analysis, consolidated P&L — with PDF export |
| 🏦 **Finance** | Vehicle purchase records, loan tracking, EMI history |
| 👥 **Team** | Permission-based access via IAM role bundles (`owner` / `admin` / `user` / `driver`) |
| 📍 **Tracking** | Live GPS via MQTT (separate Location Tracker APK for drivers) |
| 🔔 **Alerts** | Document/license expiry, maintenance due |
| 📣 **Notifications** *(planned)* | Push notifications (FCM/APNs/Web Push) + in-app Notification Center & settings screens (PRD Feat-NTF / FE-06) |
| 💳 **SaaS Billing** | Subscription plans + Razorpay checkout + organization setup funnel |
| 🌐 **Localization** | English + Hindi |
| 🌙 **Theming** | Material 3, full light/dark mode |

---

## 🏗️ Tech Stack

| Category | Technology |
|----------|------------|
| Language | Kotlin Multiplatform 2.3.0 |
| UI | Compose Multiplatform 1.10.x + Material 3 |
| Networking | Ktor Client 3.3.3 |
| DI | Metro (ZacSweers) 0.9.1 |
| Navigation | Navigation 3 (type-safe `@Serializable` routes) |
| Storage | Room + multiplatform-settings |
| Logging / Crash | Kermit + Firebase Crashlytics |
| Maps & Location | Google Places, Distance Matrix, MQTT (HiveMQ) |
| Payments | Razorpay |
| Backend | Go (Gin) REST API on Google Cloud Run *(separate repo)* |
| Identity / RBAC | IndusJS-IAM service — tenants, roles, permissions, JWT *(separate repo)* |

**Architecture:** Clean Architecture + MVI + Facade pattern per feature module.
28 Gradle modules — see [project_context.md](project_context.md) for the full map.

```
app shells (android/ios/web) → sharedUI → screen-* features → ijs-network-lib → ijs-core-lib
```

---

## 🚀 Getting Started

### Prerequisites
- JDK 17+
- Android Studio (latest stable) with KMP plugin
- Xcode (for iOS)
- A `local.properties` file with:
  ```properties
  sdk.dir=/path/to/Android/sdk
  GOOGLE_PLACES_API_KEY=your_key_here
  ```

### Android
```bash
./gradlew :androidApp:assembleDebug
# APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk
```
Or open the project in Android Studio and run the `androidApp` configuration.

### iOS
Open `iosApp/iosApp.xcodeproj` in Xcode and run the standard configuration
(the `SharedUI` framework is built from `sharedUI` automatically).

### Web (JS)
```bash
./gradlew :webApp:jsBrowserDevelopmentRun
```

### Web (WasmJS)
```bash
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

### Web production build
```bash
./gradlew :webApp:composeCompatibilityBrowserDistribution
# Deploy: webApp/build/dist/composeWebCompatibility/productionExecutable
```

### Driver GPS Tracker (separate APK)
```bash
./gradlew :locationTracker:assembleDebug
```

### Quick compile check (fastest validation while developing)
```bash
./gradlew :screen-vehicle:compileCommonMainKotlinMetadata
```

---

## 🧭 Project Structure (short version)

```
├── sharedUI/              Navigation + DI wiring + theme (NO screens here)
├── androidApp/ iosApp/ webApp/   Thin platform shells
├── locationTracker/       Standalone driver GPS app (MQTT publisher)
├── screen-*/              Feature modules (vehicle, driver, trip, customer,
│                          payment, team, report, finance, user, onboarding,
│                          dashboard, alerts, map) — each is Clean Architecture + MVI
├── ijs-*-lib/             Foundation: core, network, error, dispatcher,
│                          datetime, pdf, ui-components, logger
└── Docs/                  API & database documentation
```

---

## 👤 Access Control (IAM, permission-first)

Identity, organizations (tenants), roles, and permissions live in the separate
**IndusJS-IAM** service. Access is **permission-first**: a role is just a named
bundle of permissions with a hierarchy level — any user with the right
permissions can use any feature. Each tenant is seeded with default bundles:

| IAM bundle | Level | Access |
|------------|:-----:|--------|
| `owner` | 100 | Everything, incl. money, reports, team, billing |
| `admin` | 50 | Operations only; financial data hidden (no `financials:read`) |
| `user` | 10 | View + trip status updates + own cost entries |
| `driver` | app-scoped | Location Tracker login; (future) field cost-upload app |

Authorization is **permission-based** (`vehicles:create`, `financials:read`,
`users:change_role`, …) — enforced server-side per route, mirrored client-side
in `PermissionUtils` for UI gating. See the
[PRD §2](IndusJS_Fleet_Software_Development_PRD.md#2-target-users-roles--access-hierarchy)
for the full capability matrix.

---

## 🤝 Contributing

1. Read [project_context.md](project_context.md) first — it contains all conventions.
2. Follow Clean Architecture + MVI + Facade rules strictly.
3. Keep every class file under **500 lines**.
4. All DTO fields need `@SerialName` **and default values**.
5. Add strings in **both English and Hindi**.
6. Verify with `compileCommonMainKotlinMetadata` for your module, then
   `:androidApp:assembleDebug` before submitting.

---

## 📌 Status

Active development. See the roadmap in the
[PRD §10](IndusJS_Fleet_Software_Development_PRD.md#10-future-enhancements--roadmap)
and current work items in `TODO/Todo.txt`.

