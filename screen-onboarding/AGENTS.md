# AGENTS.md — screen-onboarding

## Purpose

**Onboarding** feature module. Displays a first-launch onboarding flow introducing key app features to new users. This is the simplest `screen-*` module — presentation-only with no network calls.

**Package:** `com.ijs.onboarding`
**Targets:** Android, iOS, JS, WasmJS

---

## Source Tree

```
src/commonMain/kotlin/com/ijs/onboarding/
├── LogTags.kt
└── presentation/
    ├── OnboardingContract.kt              # MVI contract (State/Intent/Effect)
    ├── OnboardingFeatureFacade.kt         # DI entry point
    ├── OnboardingScreen.kt                # Paged onboarding slides with skip/next/done
    └── OnboardingViewModel.kt             # Tracks current page, handles completion
```

---

## Module Dependencies

| Kind | Dependency |
|------|------------|
| api | `:ijs-core-lib` |
| libs | kotlinx-coroutines, kotlinx-serialization, kermit, multiplatform-settings |

**Notable:** This module depends on `ijs-core-lib` only (not `ijs-network-lib`), since it makes no API calls. Uses `multiplatform-settings` to persist "onboarding completed" flag.

---

## Screen

| Screen | Route | Description |
|--------|-------|-------------|
| OnboardingScreen | `Onboarding` | Paged slides → Login |

---

## Key Patterns

- **First-launch only** — The ViewModel checks `multiplatform-settings` for a "hasSeenOnboarding" flag. Once completed, the screen is never shown again.
- **Minimal dependencies** — No network lib, no feature module dependencies. Only `ijs-core-lib` for MVI base classes.
