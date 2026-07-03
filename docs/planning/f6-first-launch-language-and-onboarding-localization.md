# f6 — First-launch language picker (f6a) + onboarding localization (f6b)

PO SPEC 1783051288. Two parts.

## Scope investigation (DONE — corrects PO's hypothesis)

- **`ProvideAppLanguage` is NOT the bug.** App.kt:170 wraps `ProvideAppLanguage(appLanguage) { … NavDisplay(fleetEntryProvider(...)) }` — the ENTIRE nav graph (Login, Sign-Up, `Onboarding`, `SubscriptionPlans`, `CreateOrganization`, Payments — all in fleetEntryProvider). `AppLocaleController.apply` = `Locale.setDefault` + `LocaleList.setDefault` (android), which compose-resources reads on API24+. So the chosen language already applies to auth + onboarding.
- **Real cause of un-localized onboarding = hardcoded strings** in `screen-payment` (subscription): ~5 `UiText.Raw` + ~17 literal `Text("…")` across ~7 files.
- **Bug #42** (reset on activity-recreate) is separate: `appLanguage by remember { mutableStateOf(languageManager.saved()) }` (App.kt:70) re-inits from `saved()` on recreate. Re-verify `saved()` persistence during build; HI survived logout in testing, so likely fine.

## f6a — first-launch picker

**Gate:** `LanguageManager` persists to `Settings[KEY="app_language"]`; `saved()` defaults to EN when unset. Add:
```kotlin
fun hasChosen(): Boolean = settings.getStringOrNull(KEY) != null   // AppLocale.kt LanguageManager
```
App.kt (~line 20): `initialRoute = if (!languageManager.hasChosen()) FleetRoute.LanguagePicker else FleetRoute.Login`. Keep the auth-check LaunchedEffect; only change the pre-auth landing.

**Route + screen:** new `FleetRoute.LanguagePicker`; register in fleetEntryProvider (before Login). Screen lives in **screen-user** (auth-adjacent; sharedUI must not hold screens per CLAUDE.md). It is pure UI — no ViewModel/facade needed:
- logo tile (reuse the f3 72dp indigo `Radius.XL` square), both-script title `Choose your language / अपनी भाषा चुनें` (a NEW string that hardcodes both scripts, shown pre-choice), two large `FleetSectionCard` selectable cards `English` / `हिंदी` (each label literal in its own script — NOT stringResource, so both always show), preselect from system locale (`Locale.current`), full-width Continue CTA (bilingual literal `Continue · जारी रखें` or two-line).
- On Continue: call `LocalAppLanguageController.current(selected)` (App.kt:172 — persists via languageManager + sets `appLanguage` live) then `backStack.navigateAndClear(FleetRoute.Login)`.
- OPTIONAL (small, include unless disproportionate): a compact `भाषा / Language` text button on the Sign-In top bar (LoginScreen top row, next to the theme toggle) that navigates back to the picker. Needs a nav callback wired through LoginScreen.

**Persistence + "once":** `hasChosen()` becomes true after Continue → picker never re-shows; Profile LanguageCard still changes it (same store).

## f6b — onboarding/subscription localization

Replace hardcoded strings with `stringResource` + EN+HI. Files (counts approx):
- `presentation/components/PlanCard.kt` (14; also DROP the `🎁` emoji → text or FleetTokens icon)
- `presentation/checkout/PaymentCheckoutScreen.kt` (8)
- `presentation/organization/CreateOrganizationViewModel.kt` (3 org-name validation `UiText.Raw` → `UiText.StringRes`)
- `presentation/plans/PlansScreen.kt` ("Log out", "Continue with …" — keep the dynamic plan name, localize the frame)
- `presentation/plans/PlansViewModel.kt`, `components/BillingToggle.kt`, `checkout/PaymentCheckoutViewModel.kt` (1 each)
- Skip pure dynamic/proper-noun interpolations (plan.name) and network-error fallbacks that already route through ApiErrorHandler.

**Org-setup screen (`CreateOrganizationScreen.kt`):** trim the long marketing prose to 1-2 lines, calm register, EN+HI same length discipline:
`Your organization brings vehicles, drivers, trips and payments under one roof.` / HI equivalent. Localize the "Set Up Your Organization" title + step indicator (Payment / Organization / Dashboard).

**Coherence:** the Organization Name captured in CreateOrganization == the tenant/business name the f5 Home header shows (`business_name`). Verify same source (the org create writes the tenant name that IAM/backend returns as `business_name`).

## Sequencing / evidence
1. f6b string sweep first (mechanical, unblocks HI onboarding) — or f6a first (owner's stated #1). PO listed f6a as (1); do **f6a** first, then f6b. Both need EN+HI.
2. Evidence: picker EN+HI (light+dark); org-setup + one payment screen in HI (light+dark). HI capture works now (use annotated coords — [[android-mcp-tap-annotated-coords]]).
3. Reuse: FleetSectionCard, f3 logo tile, existing language infra. No new module.
