package com.indusjs.uicomponents.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Canonical initials derivation (replaces the per-screen `computeInitials` / inline copies in the
 * nav drawer, profile, customer, etc.).
 *
 * - "" -> "?"; one token -> first char; multiple -> first + last token initials. Always uppercase.
 */
fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts.first().take(1) + parts.last().take(1)).uppercase()
    }
}

/**
 * Circular initials avatar in the primary-container palette. One shared implementation for the
 * drawer header, profile header, customer rows, etc.
 */
@Composable
fun FleetAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    background: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    textStyle: TextStyle = MaterialTheme.typography.titleMedium,
    border: BorderStroke? = null
) {
    val initials = remember(name) { initialsOf(name) }
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(background)
            .let { if (border != null) it.border(border, CircleShape) else it },
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, style = textStyle, fontWeight = FontWeight.Bold, color = contentColor)
    }
}
