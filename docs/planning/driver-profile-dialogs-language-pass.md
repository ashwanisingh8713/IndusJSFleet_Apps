# Add-Driver polish, status dialogs, Profile overview, list font, + in-app Language switch

Based on the `driver-profile-i18n-investigation` workflow map. Order = quick wins → Add Driver → dialogs → language (riskiest, test on device).

## 1. Drivers list — license font too big (shared component, opt-in)
- `FleetMetricTile` (ijs-ui-components-lib/.../FleetSectionComponents.kt) hardcodes value Text to `headlineSmall` + Bold. Add an **opt-in `valueStyle: TextStyle = headlineSmall`** param (default unchanged → no app-wide impact). Already has maxLines=1+ellipsis.
- `DriversScreen.kt` DriverInfoItem (~L420): pass a compact style (titleMedium/SemiBold) so rating/trips/license shrink; relax `.take(10)` → `.take(14)`.

## 2. Profile → Organization Overview (compact; ProfileScreen.kt only)
- `OrganizationStatsCard` (L489-592): tiles aren't weighted (`padding(horizontal=8.dp)` + `SpaceEvenly`) and Fleet has 2 vs 3 → columns don't align; 32dp divider blocks waste space.
- Fix: give each `EnhancedStatItem` `Modifier.weight(1f)` (make it a `RowScope` ext or add a Modifier param), `Arrangement.spacedBy(Spacing.S)`, pad the 2-tile Fleet row to 3 weighted slots (3rd = `Spacer(Modifier.weight(1f))`), drop/halve dividers, post-label spacer 12→S. Bind `OrganizationStats` unchanged. Don't invent a 3rd Fleet metric.

## 3. Add Driver redesign (CreateDriverScreen.kt; contract value mapping)
- Gaps come from flat Column `spacedBy(L=16)` + a leading `Spacer(L)` + per-section `SectionHeader()` (emits its own Spacers) + trailing `Spacer(XXXL=48)`.
- Redesign to the CreateTeamMemberScreen idiom: outer Column `spacedBy(L)` of `FleetTitledSectionCard`s; inside each card inner Column `spacedBy(M=12)`; dual rows (First/Last already; **DOB + Blood Group** as `Row { weight 1f each }`).
- **Blood Group → `FleetDropdown<String>`** with options **N/A + A+,A-,B+,B-,AB+,AB-,O+,O-** (prepend "N/A" via `not_applicable_short` string). DOB stays `FleetDatePicker` with `Modifier.weight(1f)`.
- ViewModel `submitDriver`: map blood group `takeIf { it.isNotBlank() && it != "N/A" }` so the wire isn't polluted. **DTO/mapper unchanged** (`blood_group: String?`, `date_of_birth: Long?`).

## 4. Status dialogs — Driver/Vehicle/Trip (ONE shared component)
- `StateChangeDialog`/`StateOptionRow` in ijs-ui-components-lib/.../StateComponents.kt is used by all 3 (DriverDetailScreen:152, VehicleDetailScreen:279, TripDetailScreen:142). Current row = RadioIndicator + a separate emoji circle (double-circle); emoji from `StatusConstants.getIcon()` in ijs-core-lib.
- Keep `ijs-core-lib` untouched (no compose-resources there). Change `StateOption.icon: String(emoji)` → `iconRes: DrawableResource`; the 3 builders (`DriverStateOptions.kt`/`VehicleStateOptions.kt`/`TripStateOptions.kt`, which DO have compose-resources) map state→drawable via `when`.
- Rework `StateOptionRow` to a **single clean row**: one tinted icon chip (state color) + label + trailing selection (radio→filled check). Enhance `CurrentStateBanner`. Replace raw dp with FleetTokens.
- New drawables to add (ijs-ui-components-lib/.../drawable): `ic_wrench` (maintenance), `ic_pause` (suspended), `ic_block` (decommissioned), `ic_flag` (completed), `ic_power` (inactive), `ic_beach`/leave. Reuse: active→ic_check_circle, on_trip/on_route→ic_car, damaged→ic_warning, planned→ic_trip, cancelled→ic_close, delayed/failed→ic_time/ic_warning.
- Wire status values MUST NOT change (only the icon representation + layout). Preserve `StateChangeDialog` signature + DB stateLabels override.

## 5. In-app Language switch (EN/HI), persisted all platforms, runtime re-localize
Compose MP 1.10.0-rc01; EN+HI strings already shipped (2052 keys each); shared `Settings` singleton at `DefaultViewModelProvider:189`; single root `sharedUI/.../App.kt`.
- **Persistence**: `AppLanguage{EN("en"),HI("hi")}` + `LanguageManager(settings)` in `sharedUI/commonMain` (key `app_language`), reusing the SAME `Settings` (do NOT new up a second). Expose via `DefaultViewModelProvider`.
- **Apply at runtime**: `expect object AppLocaleController { fun apply(tag: String) }` + `staticCompositionLocalOf { AppLanguage.EN } LocalAppLanguage` + `@Composable ProvideAppLanguage(lang){ AppLocaleController.apply(code); key(lang){ CompositionLocalProvider(LocalAppLanguage provides lang, content) } }`. Wrap App body in `App.kt` keyed on language so the tree recomposes and compose-resources re-resolves.
  - androidMain actual: `LocaleList.setDefault(LocaleList(Locale(tag)))` + `Locale.setDefault(...)` (API24+ `Locale.current` reads LocaleList). The `key()` recompose re-resolves stringResource without Activity recreate — verify on device; recreate() fallback if needed (saved value re-read at startup).
  - iosMain: set NSUserDefaults `AppleLanguages`; live update via recompose.
  - jsMain + wasmJsMain (separate source sets): store; live via recompose.
- **Startup**: read `LanguageManager.saved()` in `App.kt` (alongside the onboarding check), seed before first composition.
- **Profile UI**: a "Language" row (ActionButtonsCard) showing current language → confirm dialog (mirror LogoutConfirmationDialog) → on Yes: persist + call `onLanguageChange` (threaded from App). New strings: language, language_english, language_hindi, language_change_title/message/confirm/cancel (EN+HI).
- **Consistency**: route existing `Locale.current.language == "hi"` checks (CreateTripScreen:821, AddVehicleScreen:627, VehicleDetailEditContent:107) through `LocalAppLanguage` so they agree with displayed strings.
- Risks: non-composable string reads (suspend getString / ViewModels) use system locale until restart — acceptable; nav backstack recreated by `key()` — ensure language change doesn't drop to Login (preserve route / accept reset to current root).

## Verify
Per area: module `compileCommonMainKotlinMetadata`, then `:androidApp:assembleDebug`, install `-r -d -t`, device-check. Language: toggle EN↔HI on device, kill+relaunch to confirm persistence.
