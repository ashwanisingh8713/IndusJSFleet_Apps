# Plan — Auth bugs: duplicate-mobile message + missing OTP "Verify" button

Reported by user (on-device, session B / KMP apps):
1. **Sign Up**: using an already-used mobile number then "Create Account" shows
   the unhelpful generic message **"This record already exists"**.
2. **"Verify Your Account" (OTP) screen**: the **"Verify" button is missing**
   (only "Resend" shows, full-width).

## Root causes (verified)

### Bug 1 — generic duplicate message
Chain: App → Fleet Go backend → IAM.
- IAM `Signup` returns `ErrMobileAlreadyExists` for a duplicate mobile
  (`auth_signup.go:24`), mapped to **409 CONFLICT** with the envelope field
  `errorMessage` set to the i18n **key** (`mobile_already_exists`).
- IAM locale files have `user_already_exists` translated but are **missing
  `mobile_already_exists`** (`i18n/messages/en.json` / `hi.json`), so IAM returns
  the raw underscored key.
- Fleet backend forwards `errorMessage` verbatim (`RespondDomainError`).
- App `ApiErrorHandler.tryExtractJsonMessage` deliberately **skips** an
  `errorMessage` that contains `_` (treats it as a code), finds no other usable
  field, and falls back to `getHttpStatusMessage(Conflict)` = "This record
  already exists."

We are the client (session B) → fix app-side; do not edit the backend. (Will
flag the missing `mobile_already_exists` translation to backend as a courtesy.)

### Bug 2 — missing "Verify" button (the real one)
`FleetButton` wraps its button in `BoxWithConstraints { ... }` and applies the
caller's `modifier` to the **inner** `Button`, not the root. A
`RowScope.weight(1f)` passed by the caller therefore never lands on the Row's
direct child (the `BoxWithConstraints`). On Compact each button also calls
`fillMaxWidth()`. In the OTP `Row { FleetButton(Resend, weight); FleetButton(Verify, weight) }`
the first button eats the entire row width and the second collapses to zero width
→ invisible. Confirmed on device (emulator-5556, dark mode): lone full-width
"Resend", no "Verify". The `BoxWithConstraints` scope's `maxWidth` is never used
(breakpoint comes from `rememberFleetBreakpoint()`), so the wrapper is pure dead
weight that also breaks weight-forwarding. Affects **all** side-by-side
FleetButton pairs (~33 files use FleetButton + weight).

## Fix

1. **FleetButton.kt** — remove the `BoxWithConstraints` wrapper; apply the
   caller's `modifier` (which may carry `weight`) directly to the button root,
   then the Compact `fillMaxWidth()` width modifier, height, min-touch, semantics.
   Behavior unchanged for single buttons; `weight` now works for side-by-side.
2. **ApiErrorHandler.kt** — recognize duplicate-account conflict in
   `errorMessage` (key or sentence form) and map mobile/email/user duplicates to
   clear messages before the underscore-skip. Benefits all callers (English,
   consistent with the existing `has_active_trip` key mapping).
3. **SignUpViewModel.kt** — classify the failure message (keyword match, same
   pattern as LoginViewModel "not verified") into localized `StringRes`
   (mobile / email / generic), and also highlight the offending field inline.
4. **strings.xml (EN + HI)** — add `error_signup_mobile_exists`,
   `error_signup_email_exists`, `error_account_exists_generic`.

## Verify
- `:ijs-ui-components-lib`, `:ijs-network-lib`, `:screen-user` compile.
- `:androidApp:assembleDebug` green; `adb install -r -d` (never uninstall).
- On emulator-5556: sign up with a **new email + the already-used mobile
  8390098886** → expect mobile-specific message (Bug 1); sign up with the
  existing pending email → OTP screen shows both **Resend + Verify** (Bug 2).
