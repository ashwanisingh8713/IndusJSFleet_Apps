# ijs-ui-components-lib — IndusJS Fleet

## Purpose

Reusable Compose UI components, theme, icons, fonts. Shared across all feature modules
via `fleet-compose-conventions.gradle`.

## Package: `com.indusjs.fleet.ui`

## Core Components (Always Use These)

| Component | Usage |
|-----------|-------|
| `FleetTextField` | Standard text input |
| `FleetDateField` | Date input (DD-MM-YYYY with auto-delimiters) |
| `FleetTimeField` | Time input (HH:MM 24hr with auto-colon) |
| `FleetMobileField` | Mobile input (10 digits) |
| `FleetEmailField` | Email input with validation |
| `FleetPasswordField` | Password with visibility toggle |
| `LoadingContent` | Centered loading spinner |
| `ErrorContent` | Error display with retry button |
| `EmptyContent` | Empty state with icon and action |
| `ScreenContent` | Wrapper handling loading/error/content states |
| `FleetCard` | Standard card with consistent styling |
| `FleetPrimaryButton` | Primary action button |
| `FleetSecondaryButton` | Secondary action button |

## Theme

```kotlin
MaterialTheme.colorScheme.primary       // Always use theme colors
MaterialTheme.colorScheme.onSurface     // NEVER hardcode colors
MaterialTheme.colorScheme.surfaceContainerLow
```

## Icons (SVG in composeResources)

```kotlin
import indusjs_fleet.ijs_ui_components_lib.generated.resources.*
Icon(painterResource(Res.drawable.ic_arrow_back), contentDescription = "Back")
```

## Module Path

`ijs-ui-components-lib/src/commonMain/kotlin/com/indusjs/fleet/ui/`

## Depends On: Compose Multiplatform, Material 3, MaterialKolor
## Depended On By: All `screen-*` modules (via `fleet-compose-conventions.gradle`)

## Common Mistakes

- ❌ Creating custom TextField variants — use `FleetTextField`, `FleetDateField`, etc.
- ❌ Hardcoding colors — always use `MaterialTheme.colorScheme.*`
- ❌ Duplicating component code — import from this library

