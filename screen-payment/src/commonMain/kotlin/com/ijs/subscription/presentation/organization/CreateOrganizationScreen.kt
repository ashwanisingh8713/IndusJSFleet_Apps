package com.ijs.subscription.presentation.organization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetStepIndicator
import com.indusjs.uicomponents.components.StepInfo
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.subscription.presentation.organization.CreateOrganizationContract.Effect
import com.ijs.subscription.presentation.organization.CreateOrganizationContract.Intent
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.create_org_button
import indusjsfleet.ijs_ui_components_lib.generated.resources.create_org_cta_hint
import indusjsfleet.ijs_ui_components_lib.generated.resources.create_org_description
import indusjsfleet.ijs_ui_components_lib.generated.resources.create_org_name_label
import indusjsfleet.ijs_ui_components_lib.generated.resources.create_org_name_placeholder
import indusjsfleet.ijs_ui_components_lib.generated.resources.create_org_name_supporting
import indusjsfleet.ijs_ui_components_lib.generated.resources.create_org_title
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_chevron_right
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_fleet_logo
import indusjsfleet.ijs_ui_components_lib.generated.resources.step_dashboard
import indusjsfleet.ijs_ui_components_lib.generated.resources.step_organization
import indusjsfleet.ijs_ui_components_lib.generated.resources.step_payment
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateOrganizationScreen(
    viewModel: CreateOrganizationViewModel,
    onOrganizationCreated: () -> Unit,
    onSkipToTeamMember: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val scheme = MaterialTheme.colorScheme

    var showContent by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }

    val iconScale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        iconScale.animateTo(1f, tween(500, easing = EaseOutBack))
        showContent = true
        delay(200)
        showForm = true
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToAddTeamMember -> onOrganizationCreated()
                is Effect.ShowError -> { /* error shown via state */ }
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        scheme.primaryContainer.copy(alpha = 0.12f),
                        scheme.surface
                    ),
                    startY = 0f,
                    endY = 500f
                )
            )
    ) {
        val bp = rememberFleetBreakpoint()
        // Compact: full width. Medium/Expanded: center the form to a comfortable max width.
        val contentWidthModifier = if (bp == FleetBreakpoint.Compact) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.widthIn(max = ContentMaxWidth)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = contentWidthModifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = FleetTokens.Spacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.size(FleetTokens.Spacing.XXL))

                FleetStepIndicator(
                    steps = listOf(
                        StepInfo(stringResource(Res.string.step_payment)),
                        StepInfo(stringResource(Res.string.step_organization)),
                        StepInfo(stringResource(Res.string.step_dashboard))
                    ),
                    currentStep = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.size(FleetTokens.Spacing.XL))

                Box(
                    modifier = Modifier.size(FleetTokens.IconSize.XXL).scale(iconScale.value),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = scheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(FleetTokens.IconSize.XXL)
                    ) {}
                    Surface(
                        shape = CircleShape,
                        color = scheme.primary.copy(alpha = 0.2f),
                        modifier = Modifier.size(76.dp)
                    ) {}
                    Surface(
                        shape = CircleShape,
                        color = scheme.primary,
                        modifier = Modifier.size(58.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_fleet_logo),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(FleetTokens.Spacing.M),
                            tint = scheme.onPrimary
                        )
                    }
                }

                Spacer(Modifier.size(FleetTokens.Spacing.L))

                AnimatedVisibility(
                    visible = showContent,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(Res.string.create_org_title),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = scheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(Modifier.size(FleetTokens.Spacing.S))

                        Text(
                            text = stringResource(Res.string.create_org_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = FleetTokens.Spacing.S)
                        )

                        Spacer(Modifier.size(FleetTokens.Spacing.XS))

                        Text(
                            text = stringResource(Res.string.create_org_cta_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.size(FleetTokens.Spacing.XL))

                AnimatedVisibility(
                    visible = showForm,
                    enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FleetInputField(
                            value = state.organizationName,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateOrganizationName(it)) },
                            fieldType = FieldType.DEFAULT,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.create_org_name_label),
                            placeholder = stringResource(Res.string.create_org_name_placeholder),
                            isError = state.organizationNameError != null,
                            errorMessage = state.organizationNameError?.resolve(),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    viewModel.sendIntent(Intent.Submit)
                                }
                            )
                        )

                        // Field hint (only when there is no inline error to show).
                        if (state.organizationNameError == null) {
                            Spacer(Modifier.size(FleetTokens.Spacing.XS))
                            Text(
                                text = stringResource(Res.string.create_org_name_supporting),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        AnimatedVisibility(visible = state.error != null) {
                            state.error?.let { error ->
                                Column {
                                    Spacer(Modifier.size(FleetTokens.Spacing.S))
                                    FleetInlineErrorBanner(message = error.resolve())
                                }
                            }
                        }

                        Spacer(Modifier.size(FleetTokens.Spacing.XL))

                        FleetButton(
                            text = stringResource(Res.string.create_org_button),
                            onClick = { viewModel.sendIntent(Intent.Submit) },
                            size = ButtonSize.LARGE,
                            modifier = Modifier.fillMaxWidth(),
                            enabled = state.isValid,
                            isLoading = state.isCreating,
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_chevron_right),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            }
                        )

                        Spacer(Modifier.size(FleetTokens.Spacing.XXL))
                    }
                }
            }
        }
    }
}

/** Max width for the centered form on Medium / Expanded breakpoints. */
private val ContentMaxWidth = 480.dp

