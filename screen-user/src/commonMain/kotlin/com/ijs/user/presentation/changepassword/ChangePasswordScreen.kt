package com.ijs.user.presentation.changepassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
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
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
            OutlinedTextField(
                value = state.currentPassword,
                onValueChange = { viewModel.sendIntent(ChangePasswordContract.Intent.UpdateCurrentPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.change_password_current_label)) },
                placeholder = { Text(stringResource(Res.string.change_password_current_placeholder)) },
                leadingIcon = { Text("🔒") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(ChangePasswordContract.Intent.ToggleCurrentPasswordVisibility) }
                    ) {
                        Text(if (state.isCurrentPasswordVisible) "👁️" else "👁️‍🗨️")
                    }
                },
                visualTransformation = if (state.isCurrentPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !state.isLoading
            )

            // New Password Field
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = { viewModel.sendIntent(ChangePasswordContract.Intent.UpdateNewPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.change_password_new_label)) },
                placeholder = { Text(stringResource(Res.string.change_password_new_placeholder)) },
                leadingIcon = { Text("🔑") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(ChangePasswordContract.Intent.ToggleNewPasswordVisibility) }
                    ) {
                        Text(if (state.isNewPasswordVisible) "👁️" else "👁️‍🗨️")
                    }
                },
                visualTransformation = if (state.isNewPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !state.isLoading,
                supportingText = {
                    Text(stringResource(Res.string.change_password_min_chars))
                }
            )

            // Confirm Password Field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.sendIntent(ChangePasswordContract.Intent.UpdateConfirmPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(Res.string.change_password_confirm_label)) },
                placeholder = { Text(stringResource(Res.string.change_password_confirm_placeholder)) },
                leadingIcon = { Text("🔑") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(ChangePasswordContract.Intent.ToggleConfirmPasswordVisibility) }
                    ) {
                        Text(if (state.isConfirmPasswordVisible) "👁️" else "👁️‍🗨️")
                    }
                },
                visualTransformation = if (state.isConfirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.sendIntent(ChangePasswordContract.Intent.ChangePassword)
                    }
                ),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword,
                supportingText = {
                    if (state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword) {
                        Text(stringResource(Res.string.change_password_mismatch), color = MaterialTheme.colorScheme.error)
                    }
                }
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

