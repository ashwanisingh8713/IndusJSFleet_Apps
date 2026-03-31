# cursor_doc — IndusJS Fleet Project Understanding

This folder contains a deep analysis and documentation of the IndusJS Fleet project, organized into 10 sections covering every aspect of the codebase.

## Folder Structure

```
cursor_doc/
│
├── README.md                                    ← You are here
│
├── 01_project_overview/
│   ├── project_summary.md                       # Business domain, tech stack, build targets
│   ├── module_catalog.md                        # All 22 modules with dependency graph
│   └── screens_and_routes.md                    # 41 routes, navigation flow, entity state machines
│
├── 02_architecture/
│   ├── clean_architecture.md                    # Layer structure, folder layout, module splits
│   ├── mvi_pattern.md                           # MviViewModel, Contract, Screen patterns
│   └── data_flow.md                             # Request lifecycle, auth flow, error handling, caching
│
├── 03_foundation_modules/
│   ├── ijs_error_lib.md                         # Result<T>, IjsException hierarchy, ErrorHandler
│   ├── ijs_core_lib.md                          # MVI base, StatusConstants, utilities, shared DTOs
│   ├── ijs_dispatcher_lib.md                    # DispatcherProvider, platform implementations
│   ├── ijs_datetime_utils.md                    # FleetDateTime (1800+ lines), conversion rules
│   └── ijs_logger_lib.md                        # IjsLogger, KermitFleetLogger, crash handling
│
├── 04_infrastructure/
│   └── ijs_network_lib.md                       # HTTP client, auth, endpoints, data sources, DI
│
├── 05_feature_modules/
│   └── feature_modules_overview.md              # All 13 screen-* modules: files, patterns, facades
│
├── 06_ui_and_tooling/
│   ├── ijs_ui_components_lib.md                 # 50+ composables, theme, icons, fonts
│   ├── ijs_pdf_report.md                        # HTML→PDF generation, platform rendering
│   └── ijs_datetime_picker.md                   # Date/time picker components
│
├── 07_app_shells/
│   ├── android_app.md                           # FleetApplication, AppActivity, Firebase
│   ├── web_app.md                               # ComposeViewport, index.html, PWA
│   ├── ios_app.md                               # SwiftUI host, CocoaPods, MainViewController
│   └── location_tracker.md                      # Standalone GPS tracker, MQTT, foreground service
│
├── 08_navigation_and_di/
│   ├── navigation.md                            # Navigation 3, FleetRoute, fleetEntryProvider
│   └── dependency_injection.md                  # ViewModelProvider, DefaultViewModelProvider, DI flow
│
├── 09_patterns_and_conventions/
│   ├── coding_conventions.md                    # DO/DON'T rules, DTO conventions, templates
│   └── adding_new_feature.md                    # Step-by-step checklist with code examples
│
└── 10_api_and_data_flow/
    ├── api_endpoints.md                         # Complete API reference (50+ endpoints)
    └── data_types_reference.md                  # All enums, states, formats, cost types
```

## Key Numbers

| Metric | Count |
|--------|-------|
| Gradle modules | 22 (+ iosApp via Xcode) |
| Routes/Screens | 41 |
| Feature modules (data+presentation) | 8 |
| Presentation-only modules | 5 |
| Foundation/utility libraries | 7 |
| Reusable UI components | 50+ |
| SVG icons | 46 |
| API endpoints | 50+ |
| Kotlin source files | ~300+ |
| Platforms | 4 (Android, iOS, JS, WasmJS) |

## Quick Reference

- **Tech:** Kotlin 2.3.0, Compose Multiplatform 1.10.0-rc01, Ktor 3.3.3, Metro 0.9.1, Navigation 3
- **Architecture:** Clean Architecture + MVI + Manual DI (Metro annotated)
- **Pattern:** Screen → ViewModel → UseCase → Repository → DataSource → HTTP/Room
- **Base URL:** `https://indusjs-fleet-docker-960880113496.asia-south2.run.app/api/v2`
- **Key entry:** `sharedUI/.../App.kt` → `FleetNavigation.kt` → Feature Facades
