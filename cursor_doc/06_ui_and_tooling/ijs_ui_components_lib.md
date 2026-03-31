# ijs-ui-components-lib — Reusable UI Components & Theme

**Namespace:** `com.indusjs.uicomponents`
**Depends on:** `ijs-core-lib` (api)

## File Tree (25 Kotlin files)

```
ijs-ui-components-lib/src/commonMain/kotlin/com/indusjs/uicomponents/
├── theme/
│   ├── Color.kt                        # Light/dark palettes + FleetColors (status colors)
│   ├── Font.kt                         # FleetFonts.poppinsFontFamily() (Poppins via resources)
│   └── Theme.kt                        # AppTheme, FleetTypography, FleetShapes, dark mode toggle
├── components/
│   ├── ButtonComponents.kt             # FleetPrimary/Secondary/Destructive/Text/IconTextButton
│   ├── CardComponents.kt              # FleetCard, FleetItemCard, FleetStatusBadge, FleetIconAvatar, etc.
│   ├── CaretakerComponents.kt         # CaretakerDropdownField, CaretakerSectionCard
│   ├── CommonComponents.kt            # LoadingContent, ErrorContent, EmptyContent, ScreenContent
│   ├── CostBreakdownComponents.kt     # CostBreakdownSection, FinancialKpiCard, ProfitLossBadge
│   ├── CostTypeChipSelector.kt        # CostTypeTwoLevelSelector (category → group → item)
│   ├── DateRangePickerDialog.kt       # Start/end date range picker
│   ├── FinanceComponents.kt           # LoanProgressCircle, EmiCountdownCard, PaymentTimelineItem
│   ├── HistoryComponents.kt           # HistoryTimeline, HistoryItemCard, HistorySummaryCard
│   ├── InputComponents.kt             # FleetTextField, FleetEmailField, FleetPasswordField, etc.
│   ├── InputFields.kt                 # DateInputField, TimeInputField
│   ├── PhoneComponents.kt             # ClickablePhoneRow, PhoneChip
│   ├── PieChart.kt                    # Canvas-based pie chart with legend
│   ├── StateComponents.kt             # StateChangeDialog, StateChipWithAction
│   ├── TripCostComponents.kt          # TotalCostHeader, CostListItem, FuelDetailsCard
│   └── UiText.kt                      # UiText sealed class (StringResource / DynamicString)
├── customer/                           # Customer UI using SelectableCustomer from core
│   ├── CustomerDetailsSection.kt
│   ├── CustomerSelectionBottomSheet.kt
│   └── SelectedCustomerCard.kt
└── components/customer/                # Customer UI using string-only API
    ├── CustomerDetailsSection.kt
    ├── CustomerSelectionBottomSheet.kt
    └── SelectedCustomerCard.kt
```

## Theme

### AppTheme

```kotlin
@Composable
fun AppTheme(onThemeChanged: (Boolean) -> Unit = {}, content: @Composable () -> Unit)
```

- Follows system dark mode initially, then allows manual toggle
- `LocalThemeIsDark` — CompositionLocal for dark mode state
- `rememberThemeToggle()` — returns `() -> Unit` toggle function
- `isAppInDarkTheme()` — returns current dark mode Boolean

### Color System
- **LightColorScheme / DarkColorScheme** — Full Material 3 color roles
- **FleetColors** — Domain-specific colors:
  - Success/Warning/Info (light + dark variants)
  - Vehicle status: `vehicleActive`, `vehicleOnRoute`, `vehicleMaintenance`, etc.
  - Driver status: `driverActive`, `driverOnRoute`, `driverOnLeave`, etc.
  - Trip status: `tripPlanned`, `tripOnRoute`, `tripCompleted`, etc.

### Typography
- `FleetTypography` — Uses `FontFamily.SansSerif` (not Poppins)
- `FleetFonts.poppinsFontFamily()` — Poppins available via Compose resources
- Sizes tuned for fleet app density

### Shapes
- `FleetShapes` — Rounded corners: 2dp (extra small) to 12dp (large)

## Key Components

### State Management Components
| Component | Purpose |
|-----------|---------|
| `LoadingContent` | Centered spinner with optional message |
| `ErrorContent` | Error display with icon, title, message, retry button |
| `EmptyContent` | Empty state with icon/emoji and optional action |
| `ScreenContent<T>` | Wrapper handling loading/error/empty/content states |

### Input Components
| Component | Purpose |
|-----------|---------|
| `FleetTextField` | Standard text input |
| `FleetDateField` | DD-MM-YYYY with auto-delimiters |
| `FleetTimeField` | HH:MM 24hr with auto-colon |
| `FleetMobileField` | 10-digit mobile number |
| `FleetEmailField` | Email with validation |
| `FleetPasswordField` | Password with visibility toggle |
| `FleetSearchField` | Search input |
| `FleetDropdownField` | Dropdown selection |
| `FleetIsoDateField` | ISO date format input |

### Button Components
| Component | Style |
|-----------|-------|
| `FleetPrimaryButton` | Filled primary |
| `FleetSecondaryButton` | Outlined |
| `FleetDestructiveButton` | Error/red |
| `FleetTextButton` | Text only |
| `FleetIconTextButton` | Icon + text |

### Card Components
| Component | Purpose |
|-----------|---------|
| `FleetCard` | Standard card |
| `FleetItemCard` | List item card |
| `FleetSectionCard` | Section grouping |
| `FleetStatusBadge` | Status indicator |
| `FleetIconAvatar` | Icon in circle |
| `FleetSectionHeader` | Section title |

### Domain-Specific Components
| Component | Purpose |
|-----------|---------|
| `CostTypeTwoLevelSelector` | 3-level cost type picker |
| `CostBreakdownSection` | Cost summary with items |
| `FinancialKpiCard` | Financial metric display |
| `ProfitLossBadge` | P&L indicator |
| `LoanProgressCircle` | EMI progress arc |
| `EmiCountdownCard` | Next EMI due |
| `HistoryTimeline` | Activity history |
| `PieChart` | Canvas-based with legend |
| `DateRangePickerDialog` | Date range selection |

## Icons (46 SVG XMLs)

Located in `src/commonMain/composeResources/drawable/`:

`ic_add`, `ic_arrow_back`, `ic_bus`, `ic_calendar`, `ic_car`, `ic_check`, `ic_chevron_right`, `ic_close`, `ic_cost`, `ic_cyclone`, `ic_dark_mode`, `ic_dashboard`, `ic_delete`, `ic_download`, `ic_driver`, `ic_edit`, `ic_email`, `ic_filter`, `ic_fleet_logo`, `ic_help`, `ic_history`, `ic_info`, `ic_light_mode`, `ic_lock`, `ic_logout`, `ic_map`, `ic_menu`, `ic_moon`, `ic_more_vert`, `ic_motorcycle`, `ic_notifications`, `ic_phone`, `ic_profile`, `ic_refresh`, `ic_rotate_right`, `ic_search`, `ic_settings`, `ic_sun`, `ic_team`, `ic_time`, `ic_trailer`, `ic_trip`, `ic_truck`, `ic_van`, `ic_vehicle`, `ic_warning`

## Fonts

Poppins family: Regular, Medium, SemiBold, Bold (TTF in composeResources/font/)

## String Resources

- `values/strings.xml` — English
- `values-hi/strings.xml` — Hindi
