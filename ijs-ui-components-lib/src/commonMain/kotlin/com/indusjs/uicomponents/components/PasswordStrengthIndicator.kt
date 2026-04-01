package com.indusjs.uicomponents.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/**
 * Password strength levels.
 */
enum class PasswordStrength {
    NONE,
    WEAK,
    FAIR,
    GOOD,
    STRONG
}

/**
 * Password strength indicator with animated color bar.
 *
 * UX Finding #17: "No password strength indicator. SignUp and ChangePassword
 * show no strength meter."
 *
 * Features:
 * - Animated progress bar showing strength level
 * - Color transitions: red (weak) → orange (fair) → yellow-green (good) → green (strong)
 * - Criteria-based scoring: length, uppercase, lowercase, digits, special chars
 *
 * Usage:
 * ```
 * PasswordStrengthIndicator(
 *     password = state.password,
 *     modifier = Modifier.padding(horizontal = 16.dp)
 * )
 * ```
 *
 * @param password The current password text.
 * @param modifier Modifier for the container.
 * @param showLabel Whether to show the "Password Strength" label.
 */
@Composable
fun PasswordStrengthIndicator(
    password: String,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val strength by remember(password) {
        derivedStateOf { calculatePasswordStrength(password) }
    }

    if (password.isEmpty()) return

    val progress by animateFloatAsState(
        targetValue = when (strength) {
            PasswordStrength.NONE -> 0f
            PasswordStrength.WEAK -> 0.25f
            PasswordStrength.FAIR -> 0.50f
            PasswordStrength.GOOD -> 0.75f
            PasswordStrength.STRONG -> 1.0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "strength_progress"
    )

    val color by animateColorAsState(
        targetValue = when (strength) {
            PasswordStrength.NONE -> MaterialTheme.colorScheme.outlineVariant
            PasswordStrength.WEAK -> Color(0xFFEF4444)    // Red
            PasswordStrength.FAIR -> Color(0xFFF59E0B)    // Amber
            PasswordStrength.GOOD -> Color(0xFF84CC16)    // Lime
            PasswordStrength.STRONG -> Color(0xFF22C55E)  // Green
        },
        animationSpec = tween(durationMillis = 300),
        label = "strength_color"
    )

    val strengthLabel = when (strength) {
        PasswordStrength.NONE -> ""
        PasswordStrength.WEAK -> stringResource(Res.string.password_strength_weak)
        PasswordStrength.FAIR -> stringResource(Res.string.password_strength_fair)
        PasswordStrength.GOOD -> stringResource(Res.string.password_strength_good)
        PasswordStrength.STRONG -> stringResource(Res.string.password_strength_strong)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Progress bar
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Label row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showLabel) {
                Text(
                    text = stringResource(Res.string.password_strength_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = strengthLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

/**
 * Calculates password strength based on multiple criteria.
 *
 * Scoring:
 * - Length ≥ 6: +1
 * - Length ≥ 10: +1
 * - Has uppercase: +1
 * - Has lowercase: +1
 * - Has digit: +1
 * - Has special character: +1
 *
 * Strength mapping:
 * - 0–1: WEAK
 * - 2–3: FAIR
 * - 4–5: GOOD
 * - 6:   STRONG
 */
fun calculatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.NONE
    if (password.length < 4) return PasswordStrength.WEAK

    var score = 0

    if (password.length >= 6) score++
    if (password.length >= 10) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when (score) {
        in 0..1 -> PasswordStrength.WEAK
        in 2..3 -> PasswordStrength.FAIR
        in 4..5 -> PasswordStrength.GOOD
        else -> PasswordStrength.STRONG
    }
}

