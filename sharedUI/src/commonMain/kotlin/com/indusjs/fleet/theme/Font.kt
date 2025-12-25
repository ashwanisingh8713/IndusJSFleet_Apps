package com.indusjs.fleet.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.Font

/**
 * Custom Font Family for Fleet Management App
 * Using Poppins - a modern, geometric sans-serif font that's
 * clean, professional, and highly readable.
 */
object FleetFonts {

    /**
     * Poppins Font Family
     * - Modern geometric sans-serif
     * - Excellent readability
     * - Professional appearance
     * - Great for both headings and body text
     */
    @Composable
    fun poppinsFontFamily(): FontFamily = FontFamily(
        Font(Res.font.poppins_regular, FontWeight.Normal, FontStyle.Normal),
        Font(Res.font.poppins_medium, FontWeight.Medium, FontStyle.Normal),
        Font(Res.font.poppins_semibold, FontWeight.SemiBold, FontStyle.Normal),
        Font(Res.font.poppins_bold, FontWeight.Bold, FontStyle.Normal),
    )
}

