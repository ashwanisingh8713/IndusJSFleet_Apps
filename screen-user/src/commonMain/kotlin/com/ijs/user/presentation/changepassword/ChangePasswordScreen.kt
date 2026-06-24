package com.ijs.user.presentation.changepassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.FleetPasswordField
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Change Password Screen composable.
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.change_password_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔐",
                    style = MaterialTheme.typography.displayMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(Res.string.change_password_heading),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(Res.string.change_password_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current Password Field
            FleetPasswordField(
                value = state.currentPassword,
                onValueChange = { viewModel.sendIntent(ChangePasswordContract.Intent.UpdateCurrentPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.change_password_current_label),
                placeholder = stringResource(Res.string.change_password_current_placeholder),
                enabled = !state.isLoading
            )

            // New Password Field (policy is enforced by the backend; no client min-length hint)
            FleetPasswordField(
                value = state.newPassword,
                onValueChange = { viewModel.sendIntent(ChangePasswordContract.Intent.UpdateNewPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.change_password_new_label),
                placeholder = stringResource(Res.string.change_password_new_placeholder),
                enabled = !state.isLoading
            )

            // Confirm Password Field (UI-only match check)
            FleetPasswordField(
                value = state.confirmPassword,
                onValueChange = { viewModel.sendIntent(ChangePasswordContract.Intent.UpdateConfirmPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.change_password_confirm_label),
                placeholder = stringResource(Res.string.change_password_confirm_placeholder),
                isError = state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword,
                errorMessage = if (state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword) {
                    stringResource(Res.string.change_password_mismatch)
                } else null,
                enabled = !state.isLoading,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.sendIntent(ChangePasswordContract.Intent.ChangePassword)
                    }
                )
            )

            // Error Message
            state.error?.let { error ->
                Text(
                    text = error.resolve(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Change Password Button
            Button(
                onClick = { viewModel.sendIntent(ChangePasswordContract.Intent.ChangePassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(Res.string.change_password_button))
                }
            }
        }
    }
}

