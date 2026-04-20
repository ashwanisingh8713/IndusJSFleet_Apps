package com.ijs.subscription.presentation.organization

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.FleetStepIndicator
import com.indusjs.uicomponents.components.StepInfo
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

    Column(
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        FleetStepIndicator(
            steps = listOf(
                StepInfo(stringResource(Res.string.step_payment)),
                StepInfo(stringResource(Res.string.step_organization)),
                StepInfo(stringResource(Res.string.step_dashboard))
            ),
            currentStep = 1,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier.size(96.dp).scale(iconScale.value),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = scheme.primary.copy(alpha = 0.1f),
                modifier = Modifier.size(96.dp)
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
                        .padding(14.dp),
                    tint = scheme.onPrimary
                )
            }
        }

        Spacer(Modifier.height(20.dp))

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

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.create_org_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = stringResource(Res.string.create_org_cta_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        AnimatedVisibility(
            visible = showForm,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = state.organizationName,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateOrganizationName(it)) },
                    label = { Text(stringResource(Res.string.create_org_name_label)) },
                    placeholder = { Text(stringResource(Res.string.create_org_name_placeholder)) },
                    supportingText = {
                        Text(
                            stringResource(Res.string.create_org_name_supporting),
                            color = scheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.sendIntent(Intent.Submit)
                        }
                    )
                )

                AnimatedVisibility(visible = state.error != null) {
                    state.error?.let { error ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = scheme.errorContainer.copy(alpha = 0.6f)
                            )
                        ) {
                            Text(
                                text = error.resolve(),
                                color = scheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(28.dp))

                Button(
                    onClick = { viewModel.sendIntent(Intent.Submit) },
                    enabled = !state.isCreating && state.organizationName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 1.dp
                    )
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = scheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.create_org_button),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(Res.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

