# Cursor Prompt — `ijs-ui-components-lib` UI Component Library

## Purpose

This prompt instructs Cursor to build, consolidate, and enforce a single shared UI component library for the IndusJsFleet Kotlin Multiplatform application. The library resolves three root problems: broken adaptive layouts across device screen resolutions, inconsistent component behaviour across modules, and duplicate Compose definitions scattered throughout feature modules.

---

## Project Identity

- **App Name:** IndusJsFleet
- **Type:** Kotlin Multiplatform (KMP)
- **Targets:** Android, iOS, JS, WasmJS
- **UI Framework:** Compose Multiplatform
- **Architecture:** MVI + Clean Architecture + Metro DI + `Flow<Result<T>>`
- **Component Library Module:** `ijs-ui-components-lib`

---

## Problem Statement — Deep Analysis

Before writing a single line of code, Cursor must fully understand the existing failure modes.

### Problem 1 — Screen Resolution Is Not Fully Supported

Components are built with hardcoded dimensions. They do not respond to the device's actual screen width or pixel density. This causes clipping, overflow, incorrect touch targets, and broken layouts on phones, tablets, and desktop JS targets. The fix is not cosmetic — it requires a foundational adaptive layout system built into every component from the ground up.

### Problem 2 — InputFields Allow Multiline Incorrectly

Across the project, InputField composables are either inconsistently configured or allow the user to press Enter and expand the field into multiple lines. This breaks form layouts and violates platform UI conventions. The rule is absolute: every input is single-line by default. The only two exceptions are the Address field and the Notes field, and those exceptions must be enforced by the component's internal logic, not left to the caller's discretion.

### Problem 3 — Duplicate Compose Definitions

Feature modules (`feat-vehicle`, `feat-driver`, `feat-trip`, `feat-dashboard`, and others) each contain their own local versions of buttons, input fields, search bars, tab rows, and filter chips. These duplicates diverge over time. Fixing a bug in one does not fix it elsewhere. This must end. Every UI primitive must have exactly one definition in `ijs-ui-components-lib` and zero definitions anywhere else.

### Problem 4 — Filter Custom Button Behaviour Is Inconsistent

The "Custom" filter chip, which appears in ReportsScreen and any date-filtered list screen, currently opens different implementations of date selection in different modules — some use dialogs, some use bottom sheets, some have no validation. The behaviour must be unified into a single dialog-based date range picker that is triggered identically everywhere.

---

## Architectural Rules — Non-Negotiable

These rules apply to every file generated in this task. Cursor must not proceed past any file that violates them.

**Module boundaries.** All shared UI components live exclusively in `ijs-ui-components-lib/src/commonMain/`. No feature module may define its own composable that duplicates a component available in this library.

**No hardcoded dimensions.** No `dp` or `sp` value may appear in any component file. All sizing, spacing, radius, and typography values must reference the design token system defined in `FleetTokens`.

**No platform imports in commonMain.** Compose Multiplatform commonMain must not import `android.*`, `UIKit`, or browser APIs directly. Where platform behaviour differs, use `expect`/`actual` with clear justification documented in the file header.

**No direct logging.** `android.util.Log`, `println`, and `System.out.println` are banned. Use `FleetLogger` from `ijs-logger-lib` for all internal component logging.

**No coroutine dispatchers.** Any asynchronous behaviour inside components (such as debounce in search) must delegate through `ijs-dispatcher-lib`. Do not use `Dispatchers.IO` or `Dispatchers.Main` directly.

**Dark mode mandatory.** Every colour reference must go through `MaterialTheme.colorScheme`. No hardcoded hex or ARGB values anywhere.

**Accessibility mandatory.** Every interactive component must have `contentDescription` and appropriate `semantics` blocks. Minimum touch target height is 44dp, enforced by the token system.

**Dependency direction.** `ijs-ui-components-lib` may depend on `ijs-logger-lib` and `ijs-dispatcher-lib`. It must never depend on any `feat-*` module. Feature modules depend on `ijs-ui-components-lib`, never the reverse.

---

## Step 1 — Design Token System

Before any component is written, establish the design token foundation. This is the single source of truth for all visual values in the library.

### FleetTokens

Define spacing tokens as a small named scale (extra-small through extra-extra-large). Define corner radius tokens as a named scale (small through pill). Define elevation tokens for cards, dialogs, and dropdowns. Define icon size tokens for small, medium, and large icon usage.

These tokens are Kotlin objects in `commonMain`. They are the only place where numeric `dp` values appear in the entire library.

### FleetTypography

Map all text styles to `MaterialTheme.typography` roles. Do not create raw `sp` values. Document which typography role maps to which component part (label, body, caption, headline).

### FleetBreakpoints

Define three breakpoints that mirror Material 3's window size classes: Compact (phones in portrait, under 600dp), Medium (phones landscape and small tablets, 600–840dp), and Expanded (tablets and desktop, above 840dp).

Provide a composable function that reads the current container width using `BoxWithConstraints` and returns the active breakpoint as a sealed type. Every component that needs adaptive behaviour calls this function rather than querying screen dimensions directly.

---

## Step 2 — Component Specifications

Each component below is the canonical, sole definition of that UI element in the entire project.

### FleetInputField

This is the foundation of all text input in the application.

**Single-line enforcement.** The component exposes a `FieldType` parameter. Internally, the component maps `FieldType` to `singleLine` and `maxLines`. The mapping is: all field types are single-line with `maxLines = 1`, except `ADDRESS` and `NOTES`, which set `singleLine = false` and `maxLines = 5`. The caller has no parameter to override this mapping. If a caller passes a field type of `DEFAULT` but needs multiline, the answer is: they must use `NOTES` or `ADDRESS`. This is intentional.

**Keyboard type.** The component derives the correct `KeyboardType` and `ImeAction` from `FieldType` automatically. Email fields get email keyboard. Phone fields get phone keyboard. Search fields get search IME action. The caller does not set keyboard options manually.

**States.** The component must visually distinguish: default, focused, error (with error message below), disabled, and read-only states. Error message renders below the field in the error colour from `MaterialTheme.colorScheme`.

**Structure.** Support a label above the field, a placeholder inside, an optional leading icon, and an optional trailing icon slot (used for password reveal, clear button, etc.).

**Adaptive width.** On Compact breakpoints, the field fills maximum available width. On Medium and Expanded, it respects its modifier but defaults to filling its column.

**IME padding.** The component applies `imePadding()` so the keyboard never covers the field on any target.

### FleetButton

**Variants.** Define a sealed type or enum covering: Primary (filled, brand colour), Secondary (outlined, brand colour border), Ghost (text only, no border), Destructive (filled, error colour), and Loading (spinner replaces label, interaction blocked).

**Sizes.** Define Small (36dp height), Medium (44dp height, default), and Large (52dp height). Heights come from FleetTokens, not inline values.

**Adaptive width.** On Compact breakpoints, buttons fill maximum width by default. On Medium and Expanded, they wrap their content. Callers can override this with an explicit modifier.

**Loading state.** When loading is true, the button shows a circular progress indicator in place of the text and leading icon. Click events are silently ignored. The button dimensions do not change.

**Minimum touch target.** Enforced by token height values. 44dp is the floor for Medium size and above.

### FleetSearchField

This is a specialised composition built on top of `FleetInputField` with `FieldType.SEARCH`. It is not a separate OutlinedTextField from scratch.

**Search icon.** Always present as the leading icon. Not configurable by the caller.

**Clear button.** A trailing icon (X) appears only when the query string is non-empty. Tapping it clears the field and calls `onQueryChange` with an empty string.

**Debounce.** The component internally debounces emissions to `onQueryChange` by 300 milliseconds using a `LaunchedEffect` and `snapshotFlow`. The caller receives debounced values. The caller does not implement debounce themselves.

**Single-line.** Always enforced. No exception. Search is never multiline.

### FleetTabBar

Used in TripScreen, VehicleScreen, DriverScreen, and any future screen that requires top-level navigation between content sections.

**Adaptive scroll.** On Compact breakpoints, the tab row is scrollable. On Medium and Expanded, it is fixed and distributes tabs evenly.

**Active indicator.** A bottom border in the brand colour indicates the active tab. Background fill on tabs is not used.

**Badge support.** Each tab definition carries an optional badge count. When greater than zero, a badge renders on the tab label. When zero, no badge is shown.

**Tab definition.** Tabs are passed as a list of data objects, each carrying an identifier, a display label, and an optional badge count. The component is fully data-driven; no tab labels are hardcoded inside it.

**Selection.** The caller owns selected state. The component calls back with the identifier of the tapped tab. The caller updates state. The component re-renders.

### FleetFilterBar and FleetFilterChip

Used in ReportsScreen and any list screen that supports filtering by category or date.

**Structure.** A horizontally scrollable row of chips. The bar is data-driven via a list of filter definitions.

**All chip.** Always the first chip. When selected, it deselects all other chips. When any other chip is selected, All is automatically deselected.

**Custom chip.** A filter definition may be marked as a date range trigger. When a chip with this flag is tapped, the component opens `FleetDateRangePickerDialog`. This is the only way the date range dialog is opened anywhere in the application.

**Chip appearance.** Selected chip: filled with brand colour, white label. Unselected chip: outlined, brand colour border and label.

**Callback separation.** The bar exposes two callbacks: one for regular filter selection (returns filter identifier), and one for date range confirmation (returns start and end milliseconds). The caller handles both independently.

### FleetDateRangePickerDialog

This is the single, authoritative implementation of date range selection for the entire application. It must not be reimplemented anywhere else.

**Trigger.** Only opened by `FleetFilterBar` when a Custom-flagged chip is tapped. No other component opens a date picker independently.

**UI pattern.** Material 3 `DateRangePicker` inside an `AlertDialog`. Not a bottom sheet. Not a popover. A dialog, consistently across all targets.

**Validation.** The Confirm button is disabled until both a start and end date are selected and the start is not after the end. Optionally, a maximum range in days is enforceable (default 365 days, configurable by the caller).

**State ownership.** Dialog open/close state lives in the caller's ViewModel, not inside this component. The component receives an `onDismiss` and an `onConfirm` callback. It does not manage its own visibility.

**Initial values.** The caller may pass initial start and end milliseconds to pre-populate the picker when reopening after a prior selection.

### FleetTextField (Display Only)

A read-only display component used in detail, profile, and summary screens. It is not an input. It shows a labelled value.

**Distinction from FleetInputField.** This component cannot receive focus, cannot be typed into, and has no keyboard interaction. It renders a label and a value as styled text. It is not built on `OutlinedTextField`.

**Use case.** Vehicle detail screen showing "Plate Number: KA-01-AB-1234". Driver profile showing "Licence Type: HMV". These are display fields, not editable fields.

### FleetDropdown

A selection component for choosing one item from a predefined list.

**Foundation.** Built on Material 3's `ExposedDropdownMenuBox`. Not a custom popover.

**Single-line trigger.** The trigger field that the user taps to open the dropdown is always single-line. The selected option text truncates with ellipsis if it exceeds the field width.

**Options list.** Passed as a list of data objects, each with an identifier and a display label. The component is fully data-driven.

**Max height.** The dropdown menu is capped at 240dp height. When the list of options exceeds this height, the menu scrolls internally.

**Adaptive width.** Follows the same rule as FleetInputField: full width on Compact, content-driven on Medium and Expanded.

**States.** Supports default, disabled, and error (with error message below) states.

---

## Step 3 — Adaptive Layout System

### The Core Rule

No component may use a hardcoded width or height value. Every size decision flows from either FleetTokens (for fixed sizes like button heights and icon sizes) or FleetBreakpoints (for layout decisions like column count and component width).

### BoxWithConstraints Usage

Components that need to respond to their container width use `BoxWithConstraints` to read `maxWidth` and derive the active breakpoint. This is preferred over reading the global window size, because components may be placed in multi-column layouts on larger screens where their container is narrower than the full window.

### Keyboard and Inset Handling

All input components apply `WindowInsets` and `imePadding()` so that the software keyboard never obscures a focused field on any target. This is mandatory, not optional. It applies to Android, iOS, and JS targets where keyboard behaviour differs.

### Target-Specific Window Size

On Android, derive window size from `WindowSizeClass` via the Compose adaptive library. On iOS, derive it from the root view's bounds via an `actual` implementation. On JS and WasmJS, derive it from `window.innerWidth` via an `actual` implementation. The `expect`/`actual` pair lives in `FleetBreakpoints` and the rest of the library consumes only the common interface.

### Tested Screen Configurations

Every component must be verifiable at these three configurations: 360×800dp (compact Android phone), 600×1024dp (medium tablet or landscape phone), and 1280×800dp (expanded desktop or JS browser window).

---

## Step 4 — Duplicate Removal Audit

After implementing all components in `ijs-ui-components-lib`, Cursor must perform a full project audit.

### What to Search For

Search every `feat-*` module, every `app-*` module, and the `shared` module for any composable function that wraps or reimplements: text input fields, buttons, search bars, tab rows, filter chips, date pickers, read-only text displays, or dropdown menus.

### What to Do With Findings

For each duplicate found: replace every call site with the corresponding `Fleet*` component, then delete the local definition entirely. Do not leave the local definition with a deprecation annotation. Delete it.

### What Not to Touch

Composables that are genuinely screen-level (not reusable components) — such as `TripListScreen`, `VehicleDetailScreen` — are not duplicates and must not be touched.

### Verification

After removal, the project must compile cleanly. No feature module may import a locally defined composable that duplicates a component in `ijs-ui-components-lib`.

---

## Step 5 — Module Dependency Map

```
feat-vehicle          ──► ijs-ui-components-lib
feat-driver           ──► ijs-ui-components-lib
feat-trip             ──► ijs-ui-components-lib
feat-dashboard        ──► ijs-ui-components-lib
feat-reports          ──► ijs-ui-components-lib

ijs-ui-components-lib ──► ijs-logger-lib
ijs-ui-components-lib ──► ijs-dispatcher-lib

ijs-ui-components-lib ──✗── feat-* (FORBIDDEN)
feat-*                ──✗── feat-* (FORBIDDEN, no cross-feature imports)
```

Any dependency that violates this map must be flagged as a build error. If Cursor detects such a violation while generating code, it must stop and report it before continuing.

---

## Step 6 — File Deliverables

Cursor must generate the following files with complete, production-ready implementation. No placeholders. No `TODO` comments. No stub functions.

```
ijs-ui-components-lib/
├── build.gradle.kts
└── src/
    └── commonMain/
        └── kotlin/com/indusjsfleet/ui/
            ├── theme/
            │   ├── FleetTokens.kt
            │   ├── FleetBreakpoints.kt
            │   └── FleetTheme.kt
            └── components/
                ├── FleetInputField.kt
                ├── FleetButton.kt
                ├── FleetSearchField.kt
                ├── FleetTabBar.kt
                ├── FleetFilterBar.kt
                ├── FleetDateRangePickerDialog.kt
                ├── FleetTextField.kt
                └── FleetDropdown.kt
```

Generate files in this order: `FleetTokens.kt` → `FleetBreakpoints.kt` → `FleetTheme.kt` → `FleetInputField.kt` → `FleetButton.kt` → `FleetSearchField.kt` → `FleetTabBar.kt` → `FleetFilterBar.kt` → `FleetDateRangePickerDialog.kt` → `FleetTextField.kt` → `FleetDropdown.kt` → `build.gradle.kts`.

After each file, confirm that no compilation error exists before proceeding to the next file.

---

## Enforcement Summary

| Concern | Rule |
|---|---|
| InputField multiline | Internal to component via FieldType; ADDRESS and NOTES only |
| Custom filter | Opens FleetDateRangePickerDialog only; no other path |
| Date range dialog | One implementation; dialog pattern; not bottom sheet |
| Dimensions | FleetTokens only; no inline dp/sp values |
| Colours | MaterialTheme.colorScheme only; no hardcoded values |
| Logging | FleetLogger only; no android.util.Log or println |
| Async/coroutines | ijs-dispatcher-lib only; no Dispatchers.* directly |
| Duplicates | Zero tolerance; delete, do not deprecate |
| Accessibility | contentDescription + semantics on all interactive components |
| Dark mode | Fully supported via MaterialTheme; no light-mode-only code |
| Touch targets | Minimum 44dp height on all interactive components |
| Adaptive layout | BoxWithConstraints + FleetBreakpoints; no hardcoded widths |
| Platform code | expect/actual only; commonMain stays clean |
| Dependency direction | ijs-ui-components-lib never imports feat-* |
