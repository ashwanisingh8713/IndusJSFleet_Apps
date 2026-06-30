# Step 5 — Navigation migration (drawer → bottom bar + rail + More sheet)

> Calm Fintech build, step 5 (the largest). Replace the dashboard-scoped hamburger drawer with an
> app-root persistent bottom nav (Compact) / `FleetNavRail` (Medium/Expanded) + a More sheet, gated on
> explicit permission slugs. Atomic change — implement fully, then build (half-migrated nav = broken app).

## Current architecture (from 3 scout passes)
- **Host:** `App.kt` renders `NavDisplay(backStack, fleetEntryProvider(...), onBack)` inside a `Scaffold`
  (snackbarHost only) → Box(padding). This is where the bar/rail scaffold wraps.
- **Routes:** `FleetRoute` (sealed interface : NavKey). Top-level: Dashboard, Vehicles, Drivers, Trips,
  Customers, Payments, Maps, TeamList, Reports, VehicleFinance, Profile. Leaf: *Detail/Create/*Entry/auth/sub.
- **Nav helpers** (NavigationExtensions.kt): `add` (push), `navigateAndClear`, `navigateSingleTop`,
  `popAndNavigate`, `removeLastOrNull` (back). Single backstack.
- **Breakpoint:** `BoxWithConstraintsScope.rememberFleetBreakpoint()` → Compact<600 / Medium<840 / Expanded.
  `isCompact`, `isAtLeastMedium`, `isExpanded`.
- **Permissions:** `LocalViewModelProvider.current.permissionChecker.has(slug)` (no LocalPermissionChecker).
  Slugs in `Permissions.kt` — *:view slugs MISSING client-side (backend has them; A enforcement live).
  Owner-expansion: `effectivePermissions(base, roles)` adds `OWNER` set for owners → OWNER must contain new slugs.
- **Drawer:** dashboard-scoped `ModalNavigationDrawer` in DashboardScreen + `NavigationDrawerContent.kt`.
  Items: Vehicles/Drivers/Trips/Customers/Payments/Maps · Team · [hasFinancialAccess] VehicleFinance+Reports ·
  Profile · Dark-Mode toggle · profile header. To REMOVE.

## Decided IA ([[design-refresh-calm-fintech]])
Bottom bar: **Home(Dashboard) · Trips · Live Map · Payments · More**. More sheet: Vehicles, Drivers,
Customers, Team, Vehicle Finance, Reports, Profile (+ Dark-Mode toggle + profile header).

## Gating (nav-gating-slugs; flip is UNBLOCKED — A enforcement live)
Add client slug consts (match backend, note hyphen): `trips:view vehicles:view drivers:view
live-map:view customers:view payments:view` + ensure they're in `Permissions.OWNER`.
- Home (Dashboard) = always (authed). More = always.
- Trips→`trips:view`; Live Map→`live-map:view`; Payments→`payments:view` (admin/owner).
- More items: Vehicles→vehicles:view, Drivers→drivers:view, Customers→customers:view,
  VehicleFinance/Reports→`financials:read`, Team→`canManageTeam()`/users:read, Profile=always.
- Bottom-bar middle slots = first 3 permission-visible of [Trips, Maps, Payments]; base user (no payments)
  → Home/Trips/Maps/More. (Vehicles-into-bar backfill = later refinement; not v1.)

## Build steps (atomic)
1. **Slugs — ✅ DONE.** Added TRIPS/VEHICLES/DRIVERS/LIVE_MAP/CUSTOMERS/PAYMENTS `_VIEW` consts to
   `Permissions.kt` (LIVE_MAP_VIEW = "live-map:view", hyphen) + into the OWNER expansion set. Compiles.
2. **Components — ✅ DONE (additive, compiles).** `FleetNavItem` + `FleetBottomNavBar` + `FleetNavRail`
   (ijs-ui-components-lib/FleetNavBar.kt) — Material3 NavigationBar/Rail themed to Calm Fintech
   (primaryContainer selected indicator + onPrimaryContainer icon, always-visible labels, badge support).
   v1 uses Material3 (not the custom SubcomposeLayout the DDD spec names) — FLAG to DDD.
   `FleetMoreSheet` (ModalBottomSheet w/ profile header + items + dark-mode) → build with the wiring (step 4).
   --- CHECKPOINT: 1-2 are safe + additive; app still runs on the existing drawer. 3-6 = the ATOMIC swap. ---
3. **Nav model + helper** (sharedUI): a `FleetNavDestination` list (route + label + icon + slug); a
   `navigateTopLevel(route)` helper = pop-to-Dashboard then push (keeps stack rooted at Dashboard, back → Home).
4. **App.kt wrap** — BoxWithConstraints → bottomBar(Compact)/rail(Medium+) when `shouldShowNav` (backStack.last
   is a top-level destination); bar/rail items gated; More opens the sheet. Hide on leaf/auth/sub.
5. **Remove dashboard drawer** — DashboardScreen drops ModalNavigationDrawer + hamburger; profile header +
   dark-mode move to the More sheet; keep notifications bell on the dashboard top bar.
6. **Build + verify** on device: bottom bar on phone, current-tab highlight, More sheet, gating (base vs
   owner), detail screens hide the bar, back returns to Home; light + dark.

3-6. **ATOMIC SWAP — ✅ DONE + device-verified.** `navigateTopLevel` helper; `FleetNavScaffold` (App.kt now
   wraps NavDisplay in it inside ProvideViewModels); dashboard `ModalNavigationDrawer` + hamburger removed
   (DashboardTopBar drops onMenuClick/navigationIcon); nav_home/nav_more strings added (EN+HI). Build green.
   Verified on emulator (owner): bottom bar Home/Trips/Live Map/Payments/More (Home selected, no hamburger);
   More sheet shows all gated overflow (Vehicles/Drivers/Customers/Team/Finance/Reports/Profile + dark-mode);
   tab switch highlights correctly; bar persists on top-level (Vehicles → "More" highlighted), HIDES on detail
   (Vehicle Detail); back from a tab returns Home. Light + dark both clean (nav-bottombar-en-{light,dark}.png).

## STEP 5 ✅ COMPLETE. NEXT = step 6 (status-colour refactor) then step 7 (acceptance).

## Notes / risks
- **Rail (Medium/Expanded) not device-verified** — no wide/tablet emulator handy; code path + breakpoint are in,
  visual unverified. Verify on a tablet/web target.
- **Base-user gating verified by LOGIC only** (current account is owner → sees all, as expected via OWNER
  expansion). A non-owner login would confirm Payments/Customers/Finance hide. Flag for D's E2E.
- Profile header (avatar/name/role) from the old drawer is NOT in the More sheet (no dashboard state at app
  root) — replaced by the "Profile & Settings" item. Acceptable; revisit if a header is wanted.
- Material3 NavigationBar/Rail themed (not the custom SubcomposeLayout the DDD spec names) — FLAG to DDD.
- Material3 NavigationBar/Rail themed (v1) instead of the custom SubcomposeLayout the DDD spec names — flag.
- Tab-switch model: `navigateTopLevel` resets to [.., Dashboard, tab]; not per-tab stacks (Nav3 single stack).
- Onboarding/subscription/auth never show the bar.
