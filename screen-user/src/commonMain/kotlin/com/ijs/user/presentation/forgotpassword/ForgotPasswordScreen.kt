package com.ijs.user.presentation.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource

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
                is ForgotPasswordContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
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
                title = { Text(if (state.isResetMode) stringResource(Res.string.reset_password_title) else stringResource(Res.string.forgot_password_title)) },
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
                .verticalScroll(rememberScrollState())
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
    error: com.indusjs.uicomponents.components.UiText?,
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
        text = stringResource(Res.string.forgot_password_heading),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.forgot_password_description),
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
        label = { Text(stringResource(Res.string.forgot_password_identifier_label)) },
        placeholder = { Text(stringResource(Res.string.forgot_password_identifier_placeholder)) },
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
    error?.let { errorUiText ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorUiText.resolve(),
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
            Text(stringResource(Res.string.forgot_password_send_instructions))
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Back to Login Link
    TextButton(onClick = onNavigateToLogin) {
        Text(stringResource(Res.string.forgot_password_back_to_login))
    }
}

@Composable
private fun ResetPasswordContent(
    newPassword: String,
    confirmPassword: String,
    isPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    isLoading: Boolean,
    error: com.indusjs.uicomponents.components.UiText?,
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
        text = stringResource(Res.string.reset_password_heading),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(Res.string.reset_password_description),
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
        label = { Text(stringResource(Res.string.reset_password_new_label)) },
        placeholder = { Text(stringResource(Res.string.reset_password_new_placeholder)) },
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
            Text(stringResource(Res.string.reset_password_min_chars))
        }
    )

    Spacer(modifier = Modifier.height(12.dp))

    // Confirm Password Field
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(Res.string.reset_password_confirm_label)) },
        placeholder = { Text(stringResource(Res.string.reset_password_confirm_placeholder)) },
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
                Text(stringResource(Res.string.reset_password_mismatch), color = MaterialTheme.colorScheme.error)
            }
        }
    )

    // Error Message
    error?.let { errorUiText ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorUiText.resolve(),
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
            Text(stringResource(Res.string.reset_password_button))
        }
    }
}

