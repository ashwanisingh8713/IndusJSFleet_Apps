package com.indusjs.uicomponents.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

/**
 * Data-driven tab definition.
 *
 * @property id Unique identifier returned in [FleetTabBar]'s callback (also the geometry key, so it
 *   must be stable across recompositions).
 * @property label Display label (sentence case; EN or HI).
 * @property badgeCount Optional neutral count badge. Renders when > 0.
 * @property iconRes Optional 16dp leading icon; active-tinted `primary`.
 */
data class FleetTab<T>(
    val id: T,
    val label: String,
    val badgeCount: Int = 0,
    val iconRes: DrawableResource? = null,
)

/**
 * Canonical segmented tab bar — "Calm Fintech" (direction §6 + DDD D1). Rebuilt on a custom layout
 * (NOT Material3 `TabRow`): a rounded track holding segments with a raised, sliding **pill** marking
 * the selection.
 *
 * - **Track:** `surfaceContainerHighest` (light, B1 glare fallback) / `surfaceContainerHigh` (dark),
 *   `Radius.Pill`, [FleetTokens.Spacing.XXS] inner padding.
 * - **Active pill:** `surface` (light) / `surfaceContainerHighest` (dark), `Radius.Pill`, with a 1px
 *   `outlineVariant` hairline in light (B1). Slides x **and** width together on selection change via a
 *   token-capped tween ([FleetTokens.Motion.FastSpringMillis], no overshoot, ≤ [FleetTokens.Motion.PillCapMillis]).
 *   Scales 0.98 while its own segment is pressed. The dark pill carries the C2 1px top-highlight via
 *   `fleetElevatedSurface(pillShape, FleetElevation.Pill)` (light mode: no-op).
 * - **Labels:** constant `FontWeight.Medium`, `labelMedium` (12sp) — no weight toggle (no reflow).
 *   Rest `onSurfaceVariant`, active `onSurface`; crossfades on the same duration. Optional 16dp icon, active-tinted.
 * - **Badge:** neutral (`surfaceContainerHighest` fill + `onSurfaceVariant`), trails the label.
 * - **Scroll fallback:** > 4 segments ⇒ a horizontally-scrollable content-sized pill row, with the
 *   active segment auto-scrolled into view (centered against the measured viewport) + a right-edge fade
 *   (B2). ≤ 4 fill the width equally. The pill sits inside the scroll layer so it tracks segments as
 *   they scroll. (TODO: also flip to scroll on MEASURED overflow — long-Hindi/large-font at ≤4 tabs —
 *   per §6; `> 4` is the documented fast-path heuristic for now and covers the §9.8 acceptance case.)
 *
 * `FleetTab<T>` API + badge slot + per-segment selected/`contentDescription` semantics are preserved.
 */
@Composable
fun <T> FleetTabBar(
    tabs: List<FleetTab<T>>,
    selectedTabId: T,
    onTabSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    // Auto-scroll when there are many tabs; callers with a few short labels (e.g. a 5-option period
    // selector) can pass `false` to keep all segments visible at equal width.
    scrollable: Boolean = tabs.size > 4,
) {
    if (tabs.isEmpty()) return
    val selectedIndex = tabs.indexOfFirst { it.id == selectedTabId }.coerceAtLeast(0)

    val isDark = isAppInDarkTheme()
    val cs = MaterialTheme.colorScheme
    val trackColor = if (isDark) cs.surfaceContainerHigh else cs.surfaceContainerHighest
    val pillColor = if (isDark) cs.surfaceContainerHighest else cs.surface
    val pillBorder = if (isDark) null else BorderStroke(FleetTokens.Border.Hairline, cs.outlineVariant)
    val pillShape = RoundedCornerShape(FleetTokens.Radius.Pill)
    val gap = FleetTokens.Spacing.XXS
    val trackPad = FleetTokens.Spacing.XXS

    // Geometry is keyed by stable tab id (NOT list index) so a runtime tab-list change can't leave the
    // pill pointing at a different segment's bounds.
    val tabsKey = tabs.map { it.id }
    val segLeftPx = remember(tabsKey) { mutableStateMapOf<T, Float>() }
    val segWidthPx = remember(tabsKey) { mutableStateMapOf<T, Float>() }

    val pillLeft = remember { Animatable(0f) }
    val pillWidth = remember { Animatable(0f) }
    var pillReady by remember { mutableStateOf(false) }
    var pressedIndex by remember { mutableStateOf(-1) }
    // Row height drives the pill height. fillMaxHeight would inflate the pill to the parent's (unbounded)
    // height — the segment row is the only thing that should define how tall the track is.
    var rowHeightPx by remember { mutableStateOf(0) }
    var viewportWidthPx by remember { mutableStateOf(0) }

    // Reset transient geometry state when the tab SET changes (ids), so the pill re-snaps cleanly.
    LaunchedEffect(tabsKey) { pillReady = false; pressedIndex = -1 }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    val targetLeft = segLeftPx[selectedTabId]
    val targetWidth = segWidthPx[selectedTabId]
    // Token-driven, duration-capped: no overshoot, hard ≤ PillCapMillis (180 < 200).
    val slideSpec = tween<Float>(durationMillis = FleetTokens.Motion.FastSpringMillis, easing = FastOutSlowInEasing)

    LaunchedEffect(targetLeft, targetWidth, selectedTabId) {
        val l = targetLeft ?: return@LaunchedEffect
        val w = targetWidth ?: return@LaunchedEffect
        if (!pillReady) {
            pillLeft.snapTo(l); pillWidth.snapTo(w); pillReady = true
        } else {
            pillLeft.animateTo(l, slideSpec)
        }
    }
    LaunchedEffect(targetWidth, selectedTabId) {
        val w = targetWidth ?: return@LaunchedEffect
        if (pillReady) pillWidth.animateTo(w, slideSpec)
    }

    // Auto-scroll the active segment into view, centered against the MEASURED viewport width (B2).
    LaunchedEffect(selectedTabId, targetLeft, targetWidth, viewportWidthPx) {
        if (scrollable && targetLeft != null && targetWidth != null && viewportWidthPx > 0) {
            val center = (targetLeft + targetWidth / 2f).roundToInt()
            scrollState.animateScrollTo((center - viewportWidthPx / 2).coerceIn(0, scrollState.maxValue))
        }
    }

    val showEdgeFade = scrollable && scrollState.canScrollForward
    val fadeWidthPx = with(density) { FleetTokens.Spacing.L.toPx() }

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(trackColor)
            .padding(trackPad)
    ) {
        val scrollMod = (if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier.fillMaxWidth())
            .onGloballyPositioned { viewportWidthPx = it.size.width }
            .let { base ->
                if (showEdgeFade) base.drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Transparent,
                            1f to trackColor,
                            startX = size.width - fadeWidthPx,
                            endX = size.width,
                        )
                    )
                } else base
            }

        Box(modifier = scrollMod) {
            // Pill first → drawn behind the segment row; both live inside the scroll layer so the pill
            // tracks segments as they scroll. Gated on measured target so it never flashes at stale geometry.
            if (pillReady && rowHeightPx > 0 && targetLeft != null && targetWidth != null) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset(pillLeft.value.roundToInt(), 0) }
                        .width(with(density) { pillWidth.value.toDp() })
                        .height(with(density) { rowHeightPx.toDp() })
                        .scale(if (selectedIndex == pressedIndex) 0.98f else 1f)
                        .clip(pillShape)
                        .background(pillColor)
                        .then(if (pillBorder != null) Modifier.border(pillBorder, pillShape) else Modifier)
                        .fleetElevatedSurface(pillShape, FleetElevation.Pill)
                )
            }
            Row(
                modifier = Modifier.onGloballyPositioned { rowHeightPx = it.size.height },
                horizontalArrangement = Arrangement.spacedBy(gap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    val segMod = (if (scrollable) Modifier.widthIn(min = 72.dp) else Modifier.weight(1f))
                        .onGloballyPositioned { coords ->
                            segLeftPx[tab.id] = coords.positionInParent().x
                            segWidthPx[tab.id] = coords.size.width.toFloat()
                        }
                    TabSegment(
                        tab = tab,
                        selected = index == selectedIndex,
                        onPressedChange = { pressed ->
                            if (pressed) pressedIndex = index
                            else if (pressedIndex == index) pressedIndex = -1
                        },
                        onClick = { onTabSelected(tab.id) },
                        modifier = segMod,
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> TabSegment(
    tab: FleetTab<T>,
    selected: Boolean,
    onPressedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cs = MaterialTheme.colorScheme
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    LaunchedEffect(pressed) { onPressedChange(pressed) }

    val contentColor by animateColorAsState(
        targetValue = if (selected) cs.onSurface else cs.onSurfaceVariant,
        animationSpec = tween(FleetTokens.Motion.FastSpringMillis),
        label = "tabContentColor",
    )

    Box(
        modifier = modifier
            .heightIn(min = FleetTokens.Height.MinTouchTarget)
            .clip(RoundedCornerShape(FleetTokens.Radius.Pill))
            .then(
                if (pressed) Modifier.background(cs.onSurface.copy(alpha = FleetTokens.StateLayer.Pressed))
                else Modifier
            )
            // selectable announces role=Tab + selected state to screen readers (parity with Material Tab).
            .selectable(
                selected = selected,
                interactionSource = interaction,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.S)
            .semantics { contentDescription = tab.label },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS),
        ) {
            if (tab.iconRes != null) {
                Icon(
                    painter = painterResource(tab.iconRes),
                    contentDescription = null,
                    tint = if (selected) cs.primary else cs.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.S),
                )
            }
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = contentColor,
                maxLines = 2,
                // Spec: wrap, never truncate. Clip (not Ellipsis) so the Hindi-length snapshot catches any overflow.
                overflow = TextOverflow.Clip,
            )
            if (tab.badgeCount > 0) {
                Spacer(Modifier.width(FleetTokens.Spacing.XXS))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(FleetTokens.Radius.Pill))
                        .background(cs.surfaceContainerHighest)
                        .padding(horizontal = FleetTokens.Spacing.XS, vertical = FleetTokens.Spacing.XXS),
                ) {
                    Text(
                        text = tab.badgeCount.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = cs.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
