package com.indusjs.uicomponents.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shimmer / skeleton placeholder components for perceived performance.
 *
 * UX Finding #9: "No shimmer/skeleton loading anywhere."
 * UX Finding #86: "No skeleton/shimmer loading exists in the library."
 *
 * Provides card, list-item, and detail layout presets for a polished
 * loading experience that replaces basic CircularProgressIndicator.
 */

// =============================================
// Core Shimmer Effect
// =============================================

/**
 * Creates a shimmer brush that animates from left to right.
 * Call from a composable scope.
 *
 * @param shimmerColors The colors used in the gradient.
 */
@Composable
fun rememberShimmerBrush(
    shimmerColors: List<Color>? = null
): Brush {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val highlightColor = MaterialTheme.colorScheme.surface
    val colors = shimmerColors ?: listOf(
        baseColor,
        highlightColor,
        baseColor
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = colors,
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * A basic shimmer box — the building block for all presets.
 *
 * @param modifier Modifier controlling size and shape.
 * @param brush The shimmer brush (obtain via [rememberShimmerBrush]).
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    brush: Brush = rememberShimmerBrush()
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
    )
}

// =============================================
// Preset: List Item Skeleton
// =============================================

/**
 * Shimmer placeholder matching a standard list item card layout.
 * Mimics FleetItemCard with avatar + two text lines + trailing widget.
 *
 * @param modifier Modifier for the container.
 * @param showAvatar Whether to show a circular avatar placeholder.
 * @param showTrailing Whether to show a trailing placeholder.
 */
@Composable
fun ShimmerListItem(
    modifier: Modifier = Modifier,
    showAvatar: Boolean = true,
    showTrailing: Boolean = true
) {
    val brush = rememberShimmerBrush()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showAvatar) {
            ShimmerBox(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                brush = brush
            )
            Spacer(modifier = Modifier.width(12.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp),
                brush = brush
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp),
                brush = brush
            )
        }

        if (showTrailing) {
            Spacer(modifier = Modifier.width(12.dp))
            ShimmerBox(
                modifier = Modifier
                    .width(48.dp)
                    .height(16.dp),
                brush = brush
            )
        }
    }
}

// =============================================
// Preset: Card Skeleton
// =============================================

/**
 * Shimmer placeholder matching a standard card layout.
 * Includes title line, subtitle line, and a content area.
 *
 * @param modifier Modifier for the card.
 * @param contentHeight Height of the placeholder content area.
 */
@Composable
fun ShimmerCard(
    modifier: Modifier = Modifier,
    contentHeight: Dp = 80.dp
) {
    val brush = rememberShimmerBrush()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title line
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(18.dp),
                brush = brush
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle line
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(14.dp),
                brush = brush
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Content area
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentHeight),
                brush = brush
            )
        }
    }
}

// =============================================
// Preset: Detail Screen Skeleton
// =============================================

/**
 * Shimmer placeholder for a detail screen hero + info sections.
 * Mimics a header card with key-value rows below.
 *
 * @param modifier Modifier for the container.
 * @param sectionCount Number of info sections to show.
 */
@Composable
fun ShimmerDetailScreen(
    modifier: Modifier = Modifier,
    sectionCount: Int = 3
) {
    val brush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(28.dp),
                    brush = brush
                )
                Spacer(modifier = Modifier.height(12.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp),
                    brush = brush
                )
                Spacer(modifier = Modifier.height(8.dp))
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(0.3f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    brush = brush
                )
            }
        }

        // Info sections
        repeat(sectionCount) {
            ShimmerInfoSection(brush = brush)
        }
    }
}

/**
 * A single info section skeleton — title + key-value rows.
 */
@Composable
private fun ShimmerInfoSection(brush: Brush) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Section title
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.35f)
                    .height(14.dp),
                brush = brush
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Key-value rows
            repeat(3) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp),
                        brush = brush
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(120.dp)
                            .height(12.dp),
                        brush = brush
                    )
                }
            }
        }
    }
}

// =============================================
// Preset: List Screen Skeleton
// =============================================

/**
 * Full-screen shimmer for list screens.
 * Shows filter chips placeholder + multiple list item skeletons.
 *
 * @param itemCount Number of list item skeletons to display.
 * @param showFilterChips Whether to show filter chip placeholders.
 * @param modifier Modifier for the container.
 */
@Composable
fun ShimmerListScreen(
    itemCount: Int = 6,
    showFilterChips: Boolean = true,
    modifier: Modifier = Modifier
) {
    val brush = rememberShimmerBrush()

    Column(modifier = modifier.fillMaxWidth()) {
        // Filter chips placeholder
        if (showFilterChips) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(4) { i ->
                    ShimmerBox(
                        modifier = Modifier
                            .width((60 + i * 10).dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        brush = brush
                    )
                }
            }
        }

        // List items
        repeat(itemCount) {
            ShimmerListItem()
        }
    }
}

// =============================================
// Preset: Dashboard Skeleton
// =============================================

/**
 * Shimmer placeholder for the dashboard screen.
 * Shows summary cards + action buttons placeholders.
 *
 * @param modifier Modifier for the container.
 */
@Composable
fun ShimmerDashboard(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Greeting placeholder
        ShimmerBox(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(24.dp),
            brush = brush
        )

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    brush = brush
                )
            }
        }

        // Summary card
        ShimmerCard(contentHeight = 100.dp)

        // Quick actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ShimmerBox(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        brush = brush
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ShimmerBox(
                        modifier = Modifier
                            .width(48.dp)
                            .height(10.dp),
                        brush = brush
                    )
                }
            }
        }
    }
}

