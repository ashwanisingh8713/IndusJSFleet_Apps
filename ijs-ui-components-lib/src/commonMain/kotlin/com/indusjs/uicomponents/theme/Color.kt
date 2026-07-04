package com.indusjs.uicomponents.theme

import androidx.compose.ui.graphics.Color

/**
 * Fleet Management — "Calm Fintech" palette (direction v2 + DDD spec, 2026-06).
 *
 * Single confident brand **indigo** for primary actions / active nav / selected pills / key numerics;
 * a warm-neutral grayscale canvas; semantic colour (success/warning/error/info) spent only in chips,
 * dots and left-accent bars — never tinting a whole card. Sunlight-first contrast floor (see §3/§4/§9):
 * light mode is pushed past AA for bright-Indian-sunlight legibility on cheap in-vehicle LCDs.
 *
 * Values map 1:1 to `MaterialTheme.colorScheme.*` via [Theme.kt]; the val NAMES are stable so the
 * colorScheme wiring does not change when the palette is re-tuned. Exact ratios are authoritative in the
 * generated WCAG report; inline hexes are CI-gated against it.
 */

// ============== LIGHT THEME ==============

// Primary — Indigo Trust
internal val PrimaryLight = Color(0xFF3D4EDB)
internal val OnPrimaryLight = Color(0xFFFFFFFF)
internal val PrimaryContainerLight = Color(0xFFE1E4FF)
internal val OnPrimaryContainerLight = Color(0xFF161C73)

// Secondary — neutral slate (supporting controls)
internal val SecondaryLight = Color(0xFF475569)
internal val OnSecondaryLight = Color(0xFFFFFFFF)
internal val SecondaryContainerLight = Color(0xFFD4DAF2)  // neutral chip/badge — indigo tint, not grey
internal val OnSecondaryContainerLight = Color(0xFF262B5A)

// Tertiary — teal = semantic INFO only (not a second brand)
internal val TertiaryLight = Color(0xFF0E7C86)
internal val OnTertiaryLight = Color(0xFFFFFFFF)
internal val TertiaryContainerLight = Color(0xFFCDEAEC)
internal val OnTertiaryContainerLight = Color(0xFF044B50)

// Error
internal val ErrorLight = Color(0xFFC2362E)
internal val OnErrorLight = Color(0xFFFFFFFF)
internal val ErrorContainerLight = Color(0xFFF7DEDC)
internal val OnErrorContainerLight = Color(0xFFB0302A)  // DDD R1: error chip fg (≥5.6:1 on #F7DEDC)

// Backgrounds & surfaces — warm-neutral page below white cards
internal val BackgroundLight = Color(0xFFECEEFB)  // page canvas — lavender, not grey
internal val OnBackgroundLight = Color(0xFF16181D)
internal val SurfaceLight = Color(0xFFFFFFFF)
internal val OnSurfaceLight = Color(0xFF16181D)
internal val SurfaceVariantLight = Color(0xFFDEE2F6)
internal val OnSurfaceVariantLight = Color(0xFF535D6B)  // secondary/label text; 6.68:1 on #FFFFFF

// Outline (control border, ≥3:1) + divider
internal val OutlineLight = Color(0xFF8A909B)
internal val OutlineVariantLight = Color(0xFFC3C9EA)  // hairline/divider — indigo, not grey

// Other
internal val ScrimLight = Color(0xFF000000)
internal val InverseSurfaceLight = Color(0xFF2B3038)
internal val InverseOnSurfaceLight = Color(0xFFF2F2F0)
internal val InversePrimaryLight = Color(0xFF9AA6FF)

// Surface container ladder — light. Owner call 2026-07-04: NO grey — the neutral surfaces carry a
// soft INDIGO tint (single-accent lavender, same hue as primary #3D4EDB), not grayscale. Lightness of
// each step preserved so the on-surface contrast floors still hold.
internal val SurfaceDimLight = Color(0xFFCFD5EB)
internal val SurfaceBrightLight = Color(0xFFF4F6FD)
internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFF4F6FD)
internal val SurfaceContainerLight = Color(0xFFE4E7F7)        // nav bar / rail surface
internal val SurfaceContainerHighLight = Color(0xFFDCE0F5)    // menus; tinted tiles/cards
internal val SurfaceContainerHighestLight = Color(0xFFD2D8F1) // seg-tab track; pressed; dialogs

// ============== DARK THEME ==============

// Primary — Indigo Trust (brightened); never white-on-#9AA6FF
internal val PrimaryDark = Color(0xFF9AA6FF)
internal val OnPrimaryDark = Color(0xFF10165A)
internal val PrimaryContainerDark = Color(0xFF2C36A6)
internal val OnPrimaryContainerDark = Color(0xFFE1E4FF)

// Secondary — neutral slate
internal val SecondaryDark = Color(0xFF9AA7B8)
internal val OnSecondaryDark = Color(0xFF10151C)
internal val SecondaryContainerDark = Color(0xFF3A4266)  // neutral chip/badge — indigo-tinted, not grey
internal val OnSecondaryContainerDark = Color(0xFFDDE1F6)

// Tertiary — info teal
internal val TertiaryDark = Color(0xFF46C7D0)
internal val OnTertiaryDark = Color(0xFF06363B)
internal val TertiaryContainerDark = Color(0xFF10353A)
internal val OnTertiaryContainerDark = Color(0xFFB0EBEF)

// Error
internal val ErrorDark = Color(0xFFFF8A7E)
internal val OnErrorDark = Color(0xFF5A0A05)
internal val ErrorContainerDark = Color(0xFF3A1512)
internal val OnErrorContainerDark = Color(0xFFF7DEDC)

// Backgrounds & surfaces — near-black, never pure #000
internal val BackgroundDark = Color(0xFF0D1020)
internal val OnBackgroundDark = Color(0xFFEDEFF3)
internal val SurfaceDark = Color(0xFF191D2E)
internal val OnSurfaceDark = Color(0xFFEDEFF3)               // ~14:1 on #191D2E
internal val SurfaceVariantDark = Color(0xFF282E45)
internal val OnSurfaceVariantDark = Color(0xFFA6AFBC)

// Outline (control border, darkened to ≥3:1 on #161A20) + divider
internal val OutlineDark = Color(0xFF707A86)
internal val OutlineVariantDark = Color(0xFF303A55)  // hairline/divider — indigo-tinted, not grey

// Other
internal val ScrimDark = Color(0xFF000000)
internal val InverseSurfaceDark = Color(0xFFE4E4E2)
internal val InverseOnSurfaceDark = Color(0xFF1A1E24)
internal val InversePrimaryDark = Color(0xFF3D4EDB)

// Surface container ladder — dark. Owner call 2026-07-04: NO grey — indigo-tinted darks (same hue as
// primary), every step still ≥~5 luminance points apart; the #161B22 collapse stays gone.
internal val SurfaceDimDark = Color(0xFF0E1122)
internal val SurfaceBrightDark = Color(0xFF3A4058)
internal val SurfaceContainerLowestDark = Color(0xFF0B0E1A)   // recessed wells
internal val SurfaceContainerLowDark = Color(0xFF161A2E)
internal val SurfaceContainerDark = Color(0xFF1C2136)         // nav bar / rail surface
internal val SurfaceContainerHighDark = Color(0xFF282E45)     // seg-tab track; menus
internal val SurfaceContainerHighestDark = Color(0xFF333A58)  // pressed; dialogs; active dark tab pill

// ============== EXTENDED COLORS ==============
// NOTE: FleetColors below is the LEGACY status palette. It is retired in build step 6
// (status-colour refactor → themed semantic chips). Left intact here so existing
// FleetStatusColors / status-chip call sites keep compiling during the staged migration.

object FleetColors {
    // Status Colors - Light Theme
    val successLight = Color(0xFF2E7D32)  // Green
    val onSuccessLight = Color(0xFFFFFFFF)
    val successContainerLight = Color(0xFFC8E6C9)

    val warningLight = Color(0xFFF57C00)  // Orange
    val onWarningLight = Color(0xFFFFFFFF)
    val warningContainerLight = Color(0xFFFFE0B2)

    val infoLight = Color(0xFF0288D1)  // Light Blue
    val onInfoLight = Color(0xFFFFFFFF)
    val infoContainerLight = Color(0xFFB3E5FC)

    // Status Colors - Dark Theme
    val successDark = Color(0xFF81C784)
    val onSuccessDark = Color(0xFF003909)
    val successContainerDark = Color(0xFF1B5E20)

    val warningDark = Color(0xFFFFB74D)
    val onWarningDark = Color(0xFF3E2723)
    val warningContainerDark = Color(0xFFE65100)

    val infoDark = Color(0xFF4FC3F7)
    val onInfoDark = Color(0xFF01579B)
    val infoContainerDark = Color(0xFF0277BD)

    // Vehicle Status Colors
    val vehicleActive = Color(0xFF4CAF50)
    val vehicleInactive = Color(0xFF9E9E9E)
    val vehicleMaintenance = Color(0xFFFF9800)
    val vehicleRetired = Color(0xFF607D8B)

    // Driver Status Colors
    val driverAvailable = Color(0xFF4CAF50)
    val driverOnTrip = Color(0xFF2196F3)
    val driverOffDuty = Color(0xFF9E9E9E)
    val driverOnLeave = Color(0xFFFF9800)

    // Trip Status Colors
    val tripPlanned = Color(0xFF9E9E9E)
    val tripInProgress = Color(0xFF2196F3)
    val tripCompleted = Color(0xFF4CAF50)
    val tripCancelled = Color(0xFFF44336)
}
