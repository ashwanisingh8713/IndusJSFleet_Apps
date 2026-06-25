package com.indusjs.uicomponents.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for all visual dimension values in the library.
 *
 * Every spacing, radius, elevation, icon size, and component height in
 * ijs-ui-components-lib references these tokens. No other file in the
 * library may contain a raw numeric [Dp] literal.
 */
object FleetTokens {

    // =============================================
    // Spacing Scale
    // =============================================

    object Spacing {
        val None: Dp = 0.dp
        val XXS: Dp = 2.dp
        val XS: Dp = 4.dp
        val S: Dp = 8.dp
        val M: Dp = 12.dp
        val L: Dp = 16.dp
        val XL: Dp = 24.dp
        val XXL: Dp = 32.dp
        val XXXL: Dp = 48.dp

        /** Standard horizontal padding for screen-level content. */
        val ScreenHorizontal: Dp = L

        /** Standard vertical padding between sections. */
        val SectionGap: Dp = XL
    }

    // =============================================
    // Corner Radius Scale
    // =============================================

    object Radius {
        val None: Dp = 0.dp
        val XS: Dp = 2.dp
        val S: Dp = 4.dp
        val M: Dp = 8.dp
        val ML: Dp = 10.dp
        val L: Dp = 12.dp
        val XL: Dp = 16.dp
        val XXL: Dp = 20.dp

        /** Full-round pill shape (use with .clip(RoundedCornerShape(Pill))). */
        val Pill: Dp = 999.dp
    }

    // =============================================
    // Elevation Scale
    // =============================================

    object Elevation {
        val None: Dp = 0.dp
        val Card: Dp = 1.dp
        val Raised: Dp = 2.dp
        val Dropdown: Dp = 4.dp
        val Dialog: Dp = 8.dp
        val Modal: Dp = 16.dp
    }

    // =============================================
    // Border / Stroke Widths
    // =============================================

    object Border {
        /** Hairline divider / resting field border. */
        val Hairline: Dp = 1.dp

        /** Default control border (inputs, OTP boxes at rest). */
        val Default: Dp = 1.5.dp

        /** Emphasis border for focused / active / error states. */
        val Emphasis: Dp = 2.dp
    }

    // =============================================
    // Icon Size Scale
    // =============================================

    object IconSize {
        val XS: Dp = 12.dp
        val S: Dp = 16.dp
        val M: Dp = 20.dp
        val Default: Dp = 24.dp
        val L: Dp = 32.dp
        val XL: Dp = 48.dp
        val XXL: Dp = 96.dp
    }

    // =============================================
    // Component Heights
    // =============================================

    object Height {
        /** Small button / chip height. */
        val ButtonSmall: Dp = 36.dp

        /** Default button height. Meets 44dp minimum touch target. */
        val ButtonMedium: Dp = 44.dp

        /** Large / hero button height. */
        val ButtonLarge: Dp = 52.dp

        /** Minimum touch target per accessibility guidelines. */
        val MinTouchTarget: Dp = 44.dp

        /** Standard input field height. */
        val InputField: Dp = 56.dp

        /** Top app bar height. */
        val TopBar: Dp = 64.dp

        /** Tab row height. */
        val TabRow: Dp = 48.dp

        /** Filter chip row height. */
        val FilterChipRow: Dp = 40.dp

        /** Step indicator circle diameter. */
        val StepCircle: Dp = 32.dp

        /** Shimmer list item height. */
        val ShimmerListItem: Dp = 72.dp

        /** Progress indicator stroke width. */
        val ProgressStroke: Dp = 2.dp

        /** Divider thickness. */
        val Divider: Dp = 1.dp

        /** Connector line thickness (step indicator). */
        val Connector: Dp = 2.dp

        /** Progress bar track height. */
        val ProgressBar: Dp = 6.dp
    }

    // =============================================
    // Width Constraints
    // =============================================

    object Width {
        /** Maximum content width on expanded screens. */
        val MaxContent: Dp = 640.dp

        /** Dropdown menu max height before scrolling. */
        val DropdownMaxHeight: Dp = 240.dp
    }
}
