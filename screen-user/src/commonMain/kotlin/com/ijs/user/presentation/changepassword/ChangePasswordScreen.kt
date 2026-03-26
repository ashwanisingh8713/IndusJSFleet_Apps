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
import kotlinx.coroutines.flow.collectLatest

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

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ChangePasswordContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
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
                title = { Text("Change Password") },
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
                    text = "Update Your Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Enter your current password and choose a new secure password",
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
                label = { Text("Current Password") },
                placeholder = { Text("Enter your current password") },
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
                label = { Text("New Password") },
                placeholder = { Text("Enter your new password") },
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
                    Text("Password must be at least 6 characters")
                }
            )

            // Confirm Password Field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.sendIntent(ChangePasswordContract.Intent.UpdateConfirmPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm New Password") },
                placeholder = { Text("Re-enter your new password") },
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
                        Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // Error Message
            state.error?.let { error ->
                Text(
                    text = error,
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
                    Text("Change Password")
                }
            }
        }
    }
}

