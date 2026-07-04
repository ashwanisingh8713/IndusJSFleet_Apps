package com.indusjs.uicomponents.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_chevron_right
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * App-wide "section" building blocks (the canonical replacements for the ~10 per-module
 * SectionCard / EnhancedSectionCard / SectionHeader / EnhancedProfileCard / DashboardSectionCard
 * copies). All dimensions come from [FleetTokens]; all colours from the theme / passed-in accents
 * (no hardcoded values). Light/dark safe.
 */

/**
 * Subtly bordered, lightly elevated surface card with consistent radius + padding.
 *
 * [containerColor] supports tinted hero/alert cards (pass a tint and usually [border]=null);
 * [border]/[elevation]/[contentPadding] are overridable so a single card covers the
 * plain-surface, tinted-fill, and flat variants the modules previously hand-rolled.
 */
@Composable
fun FleetSectionCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    elevation: Dp = FleetTokens.Elevation.Card,
    contentPadding: Dp = FleetTokens.Spacing.L,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(FleetTokens.Radius.XL)
    // Calm Fintech depth: a plain surface card gets a soft shadow (light) / 1px top-highlight (dark).
    // A TINTED highlight/alert card (containerColor != surface) is a FLAT tonal fill — no shadow, border,
    // or highlight — so the tint alone groups it and it doesn't read as a heavy framed box.
    val isDark = isAppInDarkTheme()
    val isTinted = containerColor != MaterialTheme.colorScheme.surface
    val base = modifier
        .fillMaxWidth()
        .clip(shape)
        .then(if (isTinted) Modifier else Modifier.fleetElevatedSurface(shape))
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
    Surface(
        modifier = base,
        shape = shape,
        color = containerColor,
        shadowElevation = if (isTinted || isDark) FleetTokens.Elevation.None else elevation,
        border = if (isTinted || isDark) null else border
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

/** A titled section card = [FleetSectionCard] + [FleetSectionHeader] + content. */
@Composable
fun FleetTitledSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emoji: String? = null,
    iconRes: DrawableResource? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetSectionCard(modifier = modifier, containerColor = containerColor) {
        FleetSectionHeader(
            title = title,
            emoji = emoji,
            iconRes = iconRes,
            accent = accent,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
        if (subtitle != null) {
            Spacer(modifier = Modifier.size(FleetTokens.Spacing.XS))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.size(FleetTokens.Spacing.L))
        content()
    }
}

/** Accent icon chip + bold title + optional trailing action ("View all" + chevron). */
@Composable
fun FleetSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    iconRes: DrawableResource? = null,
    accent: Color = MaterialTheme.colorScheme.primary,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (emoji != null || iconRes != null) {
            FleetAccentIconChip(emoji = emoji, iconRes = iconRes, accent = accent, chipSize = 32.dp, iconSize = 18.dp)
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (actionLabel != null && onActionClick != null) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(FleetTokens.Radius.Pill))
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(FleetTokens.IconSize.S)
                )
            }
        }
    }
}

/** Number-forward metric tile: optional accent icon chip, large value, label, optional sub-label. */
@Composable
fun FleetMetricTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    emoji: String? = null,
    iconRes: DrawableResource? = null,
    subLabel: String? = null,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    // Opt-in override for the value text style. Defaults to headlineSmall (number-forward tiles);
    // pass a smaller style (e.g. titleMedium) for tiles whose value is long text like a license no.
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineSmall,
    showBackground: Boolean = true,
    // §H money-neutral override: when set, the tile fill is this exact colour instead of the
    // accent-derived 8% tint — lets financial tiles read as a neutral surface (no rainbow).
    backgroundColor: Color? = null,
    // Optional rich label slot (e.g. a ▲ Profit / ▼ Loss delta chip). When non-null it replaces
    // the plain muted label Text; the sign/semantic colour lives here, never on the numeral.
    labelContent: (@Composable () -> Unit)? = null,
    centered: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(FleetTokens.Radius.M)
    // Owner call 2026-07-04: stat tiles carry the soft-INDIGO tint (surfaceContainerHigh, now lavender)
    // + a 1dp outlineVariant (indigo) hairline — gives the Home Overview tiles presence, never grey.
    // Callers may still pass an explicit backgroundColor (e.g. a hero). §H money numerals stay onSurface.
    val neutralBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val container = modifier
        .clip(shape)
        .let { if (showBackground) it.background(backgroundColor ?: neutralBg) else it }
        .let { if (showBackground) it.border(FleetTokens.Border.Hairline, MaterialTheme.colorScheme.outlineVariant, shape) else it }
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }
        .padding(if (showBackground) FleetTokens.Spacing.M else FleetTokens.Spacing.XS)
    val align = if (centered) Alignment.CenterHorizontally else Alignment.Start
    val textAlign = if (centered) TextAlign.Center else TextAlign.Start
    Column(
        modifier = container,
        horizontalAlignment = align,
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
    ) {
        if (emoji != null || iconRes != null) {
            FleetAccentIconChip(emoji = emoji, iconRes = iconRes, accent = accent, chipSize = 36.dp, iconSize = 20.dp)
        }
        Text(
            text = value,
            style = valueStyle,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            textAlign = textAlign,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (labelContent != null) {
            labelContent()
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (subLabel != null) {
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                // §3: smallest secondary labels use onSurfaceVariant (sunlight small-glyph legibility),
                // not the primary/accent tint (DDD fidelity nit 2026-06-30).
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = textAlign,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Small rounded accent chip holding an emoji or a vector icon. */
@Composable
fun FleetAccentIconChip(
    accent: Color,
    chipSize: Dp,
    iconSize: Dp,
    emoji: String? = null,
    iconRes: DrawableResource? = null
) {
    Box(
        modifier = Modifier
            .size(chipSize)
            .clip(RoundedCornerShape(FleetTokens.Radius.L))
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        when {
            iconRes != null -> Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(iconSize)
            )
            emoji != null -> Text(emoji, style = MaterialTheme.typography.titleMedium)
        }
    }
}
