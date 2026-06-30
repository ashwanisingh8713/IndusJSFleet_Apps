package com.indusjs.uicomponents.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.Font

/**
 * Calm Fintech typography font (2026-06) — **Noto Sans (Latin) + Noto Sans Devanagari**, one
 * metric-compatible super-family so EN and HI rows share metrics and never misalign. Bundled in
 * `composeResources/font/` so Android/iOS/Web render identical metrics (no reliance on system fonts).
 * Poppins retired — its wide geometry hurt 12–14sp body and number-dense tables.
 *
 * Weights: Regular(400) body · Medium(500) labels/titles · SemiBold(600) headlines + KPI numerics
 * (the old Bold/ExtraBold shouty scale is gone). Each weight lists the Latin font first and the
 * Devanagari font as the same-weight fallback so the text engine resolves Devanagari codepoints the
 * Latin font lacks.
 */
object FleetFonts {

    @Composable
    fun notoSansFontFamily(): FontFamily = FontFamily(
        Font(Res.font.noto_sans_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.noto_sans_devanagari_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.noto_sans_medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.noto_sans_devanagari_medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.noto_sans_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.noto_sans_devanagari_semibold, FontWeight.SemiBold, FontStyle.Normal),
    )
}
