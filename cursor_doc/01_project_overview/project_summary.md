# IndusJS Fleet — Project Summary

## What Is It?

**IndusJS Fleet** is a comprehensive **Fleet Management System** built with **Kotlin Multiplatform (KMP)**, targeting **Android, iOS, and Web** (JS + WasmJS). It helps transportation/logistics businesses manage their entire fleet operations from a single application.

## Business Domain

| Area | Purpose |
|------|---------|
| **Fleet Operations** | Track vehicles (trucks, vans) — status, maintenance, documents, real-time GPS |
| **Driver Management** | Profiles, licenses, assignments, costs (salary, advance, bonus, penalty) |
| **Trip Planning** | Route planning (Google Places API), cargo, scheduling, pricing, state machine |
| **Cost Tracking** | Trip costs (fuel, tolls, etc.), maintenance costs (tyre, battery, etc.), driver costs |
| **Payment Collection** | Customer payments for trips — cash, UPI, bank transfer, cheque, card |
| **Financial Reporting** | Profit/Loss by vehicle, by trip, consolidated; cost analysis with date ranges |
| **Vehicle Finance** | Purchase records, loan tracking, EMI payments |
| **Real-time Tracking** | GPS location via MQTT (separate locationTracker APK publishes, fleet app subscribes) |
| **Customer Management** | Company profiles, contact info, GST, trip history, financial summaries |
| **Team Management** | Role-based team hierarchy with granular permissions |

## User Roles (Hierarchical Access)

```
Owner (full access)
  └── General Manager (financial access, manage Managers/Supervisors)
       └── Manager (operational access, NO financial data)
            └── Supervisor (view-only, can update trip status only)
```

| Role | Financials | Team Mgmt | CRUD Operations | Trip Status |
|------|-----------|-----------|-----------------|-------------|
| **Owner** | Full | Full | All | All |
| **General Manager** | Full | Managers + Supervisors | All | All |
| **Manager** | Hidden (no trip_price, no P&L) | None | Create trips/costs | All |
| **Supervisor** | Hidden | None | View-only | Update only |

## Technology Stack

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 2.3.0 |
| UI Framework | Compose Multiplatform | 1.10.0-rc01 |
| Design System | Material 3 | 1.10.0-alpha05 |
| Networking | Ktor Client | 3.3.3 |
| DI Framework | Metro (ZacSweers) | 0.9.1 |
| Navigation | Navigation 3 | 1.1.0-alpha01 |
| Serialization | kotlinx-serialization | 1.9.0 |
| Local Storage | Room + multiplatform-settings | 2.8.4 / 1.3.0 |
| Date/Time | kotlinx-datetime | 0.7.1 |
| Logging | Kermit | 2.0.8 |
| Theming | MaterialKolor | 4.0.5 |
| PDF | ijs-pdf-report (internal) | — |
| Location | Google Places + Distance Matrix API | — |
| Tracking | MQTT (HiveMQ) via locationTracker | — |
| Crash Reporting | Firebase Crashlytics | BOM 33.7.0 |

## Build Targets

| Target | Build Command |
|--------|--------------|
| Android Debug | `./gradlew :androidApp:assembleDebug` |
| Android Release | `./gradlew :androidApp:assembleRelease` |
| Web JS Dev | `./gradlew :webApp:jsBrowserDevelopmentRun` |
| Web WASM Dev | `./gradlew :webApp:wasmJsBrowserDevelopmentRun` |
| Location Tracker | `./gradlew :locationTracker:assembleDebug` |
| iOS | Via Xcode (links SharedUI framework + CocoaPods for Firebase) |

## Centralized Build Config

All Android modules share `gradle/fleet-android-conventions.gradle`:
- `compileSdk = 36`, `minSdk = 23` (locationTracker overrides to 24), `targetSdk = 36`
- `jvmTarget = JVM_17`
- Individual modules must NOT override these values.

## API Base URL

```
Production: https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2
```
