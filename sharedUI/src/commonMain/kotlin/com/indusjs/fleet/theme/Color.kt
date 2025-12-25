package com.indusjs.fleet.theme

import androidx.compose.ui.graphics.Color

/**
 * Fleet Management App Color Palette
 *
 * IMPACTFUL & VIBRANT color scheme:
 * - Primary: Bold Blue (authority, trust)
 * - Secondary: Electric Teal (modern, tech)
 * - Tertiary: Vivid Orange (action, energy)
 * - High contrast text for readability
 * - Rich backgrounds for depth
 */

// ============== LIGHT THEME COLORS ==============

// Primary - Bold Electric Blue
internal val PrimaryLight = Color(0xFF1976D2)  // Strong Blue
internal val OnPrimaryLight = Color(0xFFFFFFFF)
internal val PrimaryContainerLight = Color(0xFFBBDEFB)  // Light blue container
internal val OnPrimaryContainerLight = Color(0xFF0D47A1)  // Dark blue text

// Secondary - Electric Teal
internal val SecondaryLight = Color(0xFF0097A7)  // Bold Teal
internal val OnSecondaryLight = Color(0xFFFFFFFF)
internal val SecondaryContainerLight = Color(0xFFB2EBF2)
internal val OnSecondaryContainerLight = Color(0xFF004D40)

// Tertiary - Vivid Orange
internal val TertiaryLight = Color(0xFFFF5722)  // Bold Orange
internal val OnTertiaryLight = Color(0xFFFFFFFF)
internal val TertiaryContainerLight = Color(0xFFFFCCBC)
internal val OnTertiaryContainerLight = Color(0xFFBF360C)

// Error - Strong Red
internal val ErrorLight = Color(0xFFD32F2F)
internal val OnErrorLight = Color(0xFFFFFFFF)
internal val ErrorContainerLight = Color(0xFFFFCDD2)
internal val OnErrorContainerLight = Color(0xFFB71C1C)

// Backgrounds & Surfaces - Light (Clean with subtle warmth)
internal val BackgroundLight = Color(0xFFF8F9FA)  // Clean light gray
internal val OnBackgroundLight = Color(0xFF1A1A1A)  // Near black for max contrast
internal val SurfaceLight = Color(0xFFFFFFFF)  // Pure white
internal val OnSurfaceLight = Color(0xFF1A1A1A)  // Near black
internal val SurfaceVariantLight = Color(0xFFECEFF1)  // Cool gray
internal val OnSurfaceVariantLight = Color(0xFF455A64)  // Blue-gray for secondary text

// Outline & Dividers
internal val OutlineLight = Color(0xFF90A4AE)  // Medium gray-blue
internal val OutlineVariantLight = Color(0xFFCFD8DC)  // Light gray-blue

// Other
internal val ScrimLight = Color(0xFF000000)
internal val InverseSurfaceLight = Color(0xFF263238)  // Dark blue-gray
internal val InverseOnSurfaceLight = Color(0xFFECEFF1)
internal val InversePrimaryLight = Color(0xFF64B5F6)

// Surface Tones - Light
internal val SurfaceDimLight = Color(0xFFE0E0E0)
internal val SurfaceBrightLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowestLight = Color(0xFFFFFFFF)
internal val SurfaceContainerLowLight = Color(0xFFF5F5F5)
internal val SurfaceContainerLight = Color(0xFFEEEEEE)
internal val SurfaceContainerHighLight = Color(0xFFE0E0E0)
internal val SurfaceContainerHighestLight = Color(0xFFBDBDBD)

// ============== DARK THEME COLORS ==============

// Primary - Vibrant Blue for Dark Mode
internal val PrimaryDark = Color(0xFF64B5F6)  // Bright Blue
internal val OnPrimaryDark = Color(0xFF002952)
internal val PrimaryContainerDark = Color(0xFF1565C0)  // Rich blue container
internal val OnPrimaryContainerDark = Color(0xFFE3F2FD)

// Secondary - Bright Cyan for Dark Mode
internal val SecondaryDark = Color(0xFF4DD0E1)  // Bright Cyan
internal val OnSecondaryDark = Color(0xFF00363D)
internal val SecondaryContainerDark = Color(0xFF00838F)
internal val OnSecondaryContainerDark = Color(0xFFE0F7FA)

// Tertiary - Bright Orange for Dark Mode
internal val TertiaryDark = Color(0xFFFFAB40)  // Bright Amber
internal val OnTertiaryDark = Color(0xFF3E2600)
internal val TertiaryContainerDark = Color(0xFFE65100)
internal val OnTertiaryContainerDark = Color(0xFFFFE0B2)

// Error - Bright Red for Dark Mode
internal val ErrorDark = Color(0xFFFF8A80)
internal val OnErrorDark = Color(0xFF5F0000)
internal val ErrorContainerDark = Color(0xFFD32F2F)
internal val OnErrorContainerDark = Color(0xFFFFCDD2)

// Backgrounds & Surfaces - Dark (Deep, rich blacks with blue tint)
internal val BackgroundDark = Color(0xFF0D1117)  // GitHub-style dark
internal val OnBackgroundDark = Color(0xFFF0F6FC)  // High contrast white
internal val SurfaceDark = Color(0xFF161B22)  // Elevated surface
internal val OnSurfaceDark = Color(0xFFF0F6FC)  // High contrast white
internal val SurfaceVariantDark = Color(0xFF21262D)  // Card variant
internal val OnSurfaceVariantDark = Color(0xFF8B949E)  // Muted text

// Outline & Dividers - Dark
internal val OutlineDark = Color(0xFF30363D)  // Subtle border
internal val OutlineVariantDark = Color(0xFF21262D)

// Other - Dark
internal val ScrimDark = Color(0xFF000000)
internal val InverseSurfaceDark = Color(0xFFF0F6FC)
internal val InverseOnSurfaceDark = Color(0xFF0D1117)
internal val InversePrimaryDark = Color(0xFF1976D2)

// Surface Tones - Dark (Rich depth hierarchy)
internal val SurfaceDimDark = Color(0xFF0D1117)
internal val SurfaceBrightDark = Color(0xFF30363D)
internal val SurfaceContainerLowestDark = Color(0xFF010409)  // Deepest black
internal val SurfaceContainerLowDark = Color(0xFF0D1117)
internal val SurfaceContainerDark = Color(0xFF161B22)
internal val SurfaceContainerHighDark = Color(0xFF21262D)
internal val SurfaceContainerHighestDark = Color(0xFF30363D)

// ============== EXTENDED COLORS ==============
// These can be used for custom status colors throughout the app

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
