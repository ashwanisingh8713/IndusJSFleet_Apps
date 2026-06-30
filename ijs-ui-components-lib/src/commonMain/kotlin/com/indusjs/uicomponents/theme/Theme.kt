package com.indusjs.uicomponents.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    scrim = ScrimLight,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    inversePrimary = InversePrimaryLight,
    surfaceDim = SurfaceDimLight,
    surfaceBright = SurfaceBrightLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    scrim = ScrimDark,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    inversePrimary = InversePrimaryDark,
    surfaceDim = SurfaceDimDark,
    surfaceBright = SurfaceBrightDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
)

/**
 * Builds the Fleet typography scale (Calm Fintech — direction §5 + DDD C3) using [fontFamily]
 * (the Noto super-family). Weights: Regular(400) body · Medium(500) labels/titles · SemiBold(600)
 * headlines + KPI numerics (the old Bold/ExtraBold poster scale is retired). `letterSpacing = 0`
 * everywhere — positive tracking detaches Devanagari matras. `LineHeightStyle(trim=None,
 * alignment=Center)` on every role for EN/HI baseline parity. Sentence case only; no ALL-CAPS, no italics.
 */
private fun fleetTypography(fontFamily: FontFamily): Typography {
    val lh = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    )
    fun role(size: Int, line: Int, weight: FontWeight) = TextStyle(
        fontFamily = fontFamily,
        fontWeight = weight,
        fontSize = size.sp,
        lineHeight = line.sp,
        letterSpacing = 0.sp,
        lineHeightStyle = lh,
        // Tabular/lining figures so financial numerics align in columns (digits render Western-Arabic
        // / Latin in both locales, so app-wide tnum is safe and keeps every number column-aligned).
        fontFeatureSettings = "tnum",
    )
    return Typography(
        // display 30/38, scaled down through to headline (no 52/42/34 poster scale)
        displayLarge = role(30, 38, FontWeight.SemiBold),
        displayMedium = role(28, 36, FontWeight.SemiBold),
        displaySmall = role(26, 34, FontWeight.SemiBold),
        headlineLarge = role(24, 32, FontWeight.SemiBold),
        headlineMedium = role(22, 30, FontWeight.SemiBold),
        headlineSmall = role(20, 28, FontWeight.SemiBold),
        titleLarge = role(20, 28, FontWeight.Medium),
        titleMedium = role(16, 24, FontWeight.Medium),
        titleSmall = role(14, 20, FontWeight.Medium),
        bodyLarge = role(16, 24, FontWeight.Normal),
        bodyMedium = role(14, 21, FontWeight.Normal),
        bodySmall = role(12, 18, FontWeight.Normal),
        labelLarge = role(14, 20, FontWeight.Medium),
        labelMedium = role(12, 16, FontWeight.Medium),   // tab / nav / chip / button labels
        labelSmall = role(11, 16, FontWeight.Medium),    // never 10sp/Bold (retired)
    )
}


// Material shape buckets mapped to the consolidated {M, L, XL} set (direction §10).
// medium = Card radius (L=12); large/extraLarge = Sheet radius (XL=16).
private val FleetShapes = Shapes(
    extraSmall = RoundedCornerShape(FleetTokens.Radius.M),
    small = RoundedCornerShape(FleetTokens.Radius.M),
    medium = RoundedCornerShape(FleetTokens.Radius.L),
    large = RoundedCornerShape(FleetTokens.Radius.XL),
    extraLarge = RoundedCornerShape(FleetTokens.Radius.XL)
)

/**
 * Theme state holder for accessing dark mode across the app
 */
val LocalThemeIsDark = compositionLocalOf { mutableStateOf(false) }

/**
 * Toggle theme function accessible throughout the app
 */
@Composable
fun rememberThemeToggle(): () -> Unit {
    val isDarkState = LocalThemeIsDark.current
    return { isDarkState.value = !isDarkState.value }
}

/**
 * Check if current theme is dark
 */
@Composable
fun isAppInDarkTheme(): Boolean {
    return LocalThemeIsDark.current.value
}

/**
 * Main App Theme Composable
 */
@Composable
fun AppTheme(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
    content: @Composable () -> Unit
) {
    val systemIsDark = isSystemInDarkTheme()
    val isDarkState = remember(systemIsDark) { mutableStateOf(systemIsDark) }

    val noto = FleetFonts.notoSansFontFamily()
    val typography = remember(noto) { fleetTypography(noto) }

    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState
    ) {
        val isDark by isDarkState
        onThemeChanged(isDark)

        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography = typography,
            shapes = FleetShapes,
            content = { Surface(content = content) }
        )
    }
}
