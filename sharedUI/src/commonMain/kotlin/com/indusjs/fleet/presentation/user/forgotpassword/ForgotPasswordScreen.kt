package com.indusjs.fleet.presentation.user.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
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
 * Forgot Password Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ForgotPasswordContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is ForgotPasswordContract.Effect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
                is ForgotPasswordContract.Effect.ShowResetPassword -> {
                    // Already handled by state change
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (state.isResetMode) "Reset Password" else "Forgot Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.isResetMode) {
                ResetPasswordContent(
                    newPassword = state.newPassword,
                    confirmPassword = state.confirmPassword,
                    isPasswordVisible = state.isPasswordVisible,
                    isConfirmPasswordVisible = state.isConfirmPasswordVisible,
                    isLoading = state.isLoading,
                    error = state.error,
                    onNewPasswordChange = { viewModel.sendIntent(ForgotPasswordContract.Intent.UpdateNewPassword(it)) },
                    onConfirmPasswordChange = { viewModel.sendIntent(ForgotPasswordContract.Intent.UpdateConfirmPassword(it)) },
                    onTogglePasswordVisibility = { viewModel.sendIntent(ForgotPasswordContract.Intent.TogglePasswordVisibility) },
                    onToggleConfirmPasswordVisibility = { viewModel.sendIntent(ForgotPasswordContract.Intent.ToggleConfirmPasswordVisibility) },
                    onResetPassword = { viewModel.sendIntent(ForgotPasswordContract.Intent.ResetPassword) },
                    focusManager = focusManager
                )
            } else {
                ForgotPasswordContent(
                    identifier = state.identifier,
                    isLoading = state.isLoading,
                    error = state.error,
                    onIdentifierChange = { viewModel.sendIntent(ForgotPasswordContract.Intent.UpdateIdentifier(it)) },
                    onSubmit = { viewModel.sendIntent(ForgotPasswordContract.Intent.SubmitForgotPassword) },
                    onNavigateToLogin = { viewModel.sendIntent(ForgotPasswordContract.Intent.NavigateToLogin) },
                    focusManager = focusManager
                )
            }
        }
    }
}

@Composable
private fun ForgotPasswordContent(
    identifier: String,
    isLoading: Boolean,
    error: String?,
    onIdentifierChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToLogin: () -> Unit,
    focusManager: FocusManager
) {
    // Header
    Text(
        text = "🔓",
        style = MaterialTheme.typography.displayLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Forgot Your Password?",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Enter your email or mobile number and we'll help you reset your password.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    // Identifier Field
    OutlinedTextField(
        value = identifier,
        onValueChange = onIdentifierChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Email or Mobile") },
        placeholder = { Text("Enter your email or mobile number") },
        leadingIcon = { Text("📧") },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                onSubmit()
            }
        ),
        singleLine = true,
        enabled = !isLoading
    )

    // Error Message
    error?.let { errorMessage ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Submit Button
    Button(
        onClick = onSubmit,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Send Reset Instructions")
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Back to Login Link
    TextButton(onClick = onNavigateToLogin) {
        Text("Back to Sign In")
    }
}

@Composable
private fun ResetPasswordContent(
    newPassword: String,
    confirmPassword: String,
    isPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    isLoading: Boolean,
    error: String?,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onResetPassword: () -> Unit,
    focusManager: FocusManager
) {
    // Header
    Text(
        text = "🔐",
        style = MaterialTheme.typography.displayLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Create New Password",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Enter your new password below.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    // New Password Field
    OutlinedTextField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("New Password") },
        placeholder = { Text("Enter new password") },
        leadingIcon = { Text("🔑") },
        trailingIcon = {
            IconButton(onClick = onTogglePasswordVisibility) {
                Text(if (isPasswordVisible) "👁️" else "👁️‍🗨️")
            }
        },
        visualTransformation = if (isPasswordVisible) {
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
        enabled = !isLoading,
        supportingText = {
            Text("Minimum 6 characters")
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Confirm Password Field
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Confirm Password") },
        placeholder = { Text("Re-enter new password") },
        leadingIcon = { Text("🔑") },
        trailingIcon = {
            IconButton(onClick = onToggleConfirmPasswordVisibility) {
                Text(if (isConfirmPasswordVisible) "👁️" else "👁️‍🗨️")
            }
        },
        visualTransformation = if (isConfirmPasswordVisible) {
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
                onResetPassword()
            }
        ),
        singleLine = true,
        enabled = !isLoading,
        isError = confirmPassword.isNotEmpty() && confirmPassword != newPassword,
        supportingText = {
            if (confirmPassword.isNotEmpty() && confirmPassword != newPassword) {
                Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
            }
        }
    )

    // Error Message
    error?.let { errorMessage ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Reset Password Button
    Button(
        onClick = onResetPassword,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text("Reset Password")
        }
    }
}

