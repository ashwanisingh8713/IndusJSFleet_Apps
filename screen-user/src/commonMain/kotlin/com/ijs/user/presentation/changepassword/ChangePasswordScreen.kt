package com.ijs.user.presentation.changepassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.FleetAccentIconChip
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetPasswordField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTopAppBar
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

/**
 * Change Password Screen composable.
 *
 * Design-system compliant: FleetTopAppBar + FleetSectionCard + FleetPasswordField
 * + FleetButton, FleetTokens spacing, theme colours only, and a centred max-width
 * form on Medium/Expanded breakpoints. Password policy (length/complexity) stays
 * backend-only; the only client checks are confirm-matches and new!=current, which
 * are surfaced as inline field errors and gate the submit button.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    var pendingSnackbar by remember { mutableStateOf<com.indusjs.uicomponents.components.UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ChangePasswordContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is ChangePasswordContract.Effect.PasswordChanged -> {
                    // Password changed successfully
                }
                is ChangePasswordContract.Effect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    // UI-only inline checks (policy is backend-only).
    val confirmMismatch = state.confirmPassword.isNotEmpty() &&
        state.confirmPassword != state.newPassword
    val newSameAsCurrent = state.newPassword.isNotEmpty() &&
        state.currentPassword.isNotEmpty() &&
        state.newPassword == state.currentPassword

    val canSubmit = !state.isLoading &&
        state.currentPassword.isNotEmpty() &&
        state.newPassword.isNotEmpty() &&
        state.confirmPassword.isNotEmpty() &&
        !confirmMismatch &&
        !newSameAsCurrent

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FleetTopAppBar(
                title = stringResource(Res.string.change_password_title),
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val bp = rememberFleetBreakpoint()

            // Compact = full width; Medium/Expanded = centred, constrained form column.
            val columnAlignment = if (bp == FleetBreakpoint.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.fillMaxWidth().widthIn(max = FORM_MAX_WIDTH)
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = columnAlignment
                        .verticalScroll(rememberScrollState())
                        .padding(FleetTokens.Spacing.L),
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                ) {
                    // Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FleetAccentIconChip(
                            accent = MaterialTheme.colorScheme.primary,
                            chipSize = FleetTokens.IconSize.XL,
                            iconSize = FleetTokens.IconSize.L,
                            iconRes = Res.drawable.ic_lock
                        )

                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                        Text(
                            text = stringResource(Res.string.change_password_heading),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                        Text(
                            text = stringResource(Res.string.change_password_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Form card
                    FleetSectionCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                        ) {
                            // Current Password
                            FleetPasswordField(
                                value = state.currentPassword,
                                onValueChange = {
                                    viewModel.sendIntent(
                                        ChangePasswordContract.Intent.UpdateCurrentPassword(it)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.change_password_current_label),
                                placeholder = stringResource(Res.string.change_password_current_placeholder),
                                enabled = !state.isLoading
                            )

                            // New Password (policy enforced by backend; only the
                            // "must differ from current" check is client-side).
                            FleetPasswordField(
                                value = state.newPassword,
                                onValueChange = {
                                    viewModel.sendIntent(
                                        ChangePasswordContract.Intent.UpdateNewPassword(it)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.change_password_new_label),
                                placeholder = stringResource(Res.string.change_password_new_placeholder),
                                isError = newSameAsCurrent,
                                errorMessage = if (newSameAsCurrent) {
                                    stringResource(Res.string.error_password_same_as_current)
                                } else null,
                                enabled = !state.isLoading
                            )

                            // Confirm Password (UI-only match check)
                            FleetPasswordField(
                                value = state.confirmPassword,
                                onValueChange = {
                                    viewModel.sendIntent(
                                        ChangePasswordContract.Intent.UpdateConfirmPassword(it)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.change_password_confirm_label),
                                placeholder = stringResource(Res.string.change_password_confirm_placeholder),
                                isError = confirmMismatch,
                                errorMessage = if (confirmMismatch) {
                                    stringResource(Res.string.change_password_mismatch)
                                } else null,
                                enabled = !state.isLoading,
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (canSubmit) {
                                            viewModel.sendIntent(ChangePasswordContract.Intent.ChangePassword)
                                        }
                                    }
                                )
                            )

                            // Server / submit-level error
                            state.error?.let { error ->
                                Text(
                                    text = error.resolve(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Submit
                    FleetButton(
                        text = stringResource(Res.string.change_password_button),
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.sendIntent(ChangePasswordContract.Intent.ChangePassword)
                        },
                        size = ButtonSize.LARGE,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = canSubmit,
                        isLoading = state.isLoading
                    )
                }
            }
        }
    }
}

/** Centred form max-width on Medium/Expanded so it doesn't stretch edge-to-edge on tablet/web. */
private val FORM_MAX_WIDTH = 480.dp
