package com.indusjs.uicomponents.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Data class representing a single step in the wizard.
 *
 * @property label Short label displayed below the step circle.
 */
data class StepInfo(
    val label: String
)

/**
 * Horizontal step/wizard progress indicator.
 *
 * Shows numbered circles connected by lines with completed/active/pending states.
 * Used for multi-step forms like CreateTrip, AddVehicle, report wizards.
 *
 * UX Finding #39: "Convert CreateTrip to a multi-step wizard."
 * Reports Deep-Dive #22: "No step progress indicator."
 *
 * Usage:
 * ```
 * FleetStepIndicator(
 *     steps = listOf(
 *         StepInfo("Schedule"),
 *         StepInfo("Route"),
 *         StepInfo("Cargo"),
 *         StepInfo("Review")
 *     ),
 *     currentStep = state.currentStep  // 0-based index
 * )
 * ```
 *
 * @param steps List of step definitions.
 * @param currentStep Zero-based index of the current active step.
 * @param modifier Modifier for the container.
 */
@Composable
fun FleetStepIndicator(
    steps: List<StepInfo>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    if (steps.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        steps.forEachIndexed { index, step ->
            val state = when {
                index < currentStep -> StepState.COMPLETED
                index == currentStep -> StepState.ACTIVE
                else -> StepState.PENDING
            }

            // Step circle + label
            StepItem(
                stepNumber = index + 1,
                label = step.label,
                state = state,
                modifier = Modifier.weight(1f)
            )

            // Connector line between steps
            if (index < steps.lastIndex) {
                StepConnector(
                    isCompleted = index < currentStep,
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(top = 16.dp) // Align with center of circle
                )
            }
        }
    }
}

/**
 * A single step in the indicator: circle + optional label.
 */
@Composable
private fun StepItem(
    stepNumber: Int,
    label: String,
    state: StepState,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = when (state) {
            StepState.COMPLETED -> MaterialTheme.colorScheme.primary
            StepState.ACTIVE -> MaterialTheme.colorScheme.primary
            StepState.PENDING -> MaterialTheme.colorScheme.surfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "step_bg"
    )

    val contentColor by animateColorAsState(
        targetValue = when (state) {
            StepState.COMPLETED -> MaterialTheme.colorScheme.onPrimary
            StepState.ACTIVE -> MaterialTheme.colorScheme.onPrimary
            StepState.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "step_content"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circle
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                StepState.COMPLETED -> {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                else -> {
                    Text(
                        text = stepNumber.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (state == StepState.ACTIVE) FontWeight.SemiBold else FontWeight.Normal,
            color = if (state == StepState.PENDING) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Connector line between two steps.
 */
@Composable
private fun StepConnector(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (isCompleted) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(durationMillis = 300),
        label = "connector_color"
    )

    Box(
        modifier = modifier
            .height(2.dp)
            .background(color)
    )
}

/**
 * Internal enum for step visual state.
 */
private enum class StepState {
    COMPLETED,
    ACTIVE,
    PENDING
}

/**
 * A simpler step indicator variant showing "Step X of Y" text with a progress bar.
 * Useful when the full circle-and-line UI isn't needed.
 *
 * @param currentStep The current step (1-based for display).
 * @param totalSteps The total number of steps.
 * @param modifier Modifier for the container.
 */
@Composable
fun FleetStepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.step_indicator, currentStep, totalSteps),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "${((currentStep.toFloat() / totalSteps) * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        val progress = currentStep.toFloat() / totalSteps

        androidx.compose.material3.LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

