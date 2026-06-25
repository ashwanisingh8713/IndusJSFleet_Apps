package com.ijs.user.presentation.forgotpassword

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetAccentIconChip
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetPasswordField
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Max form width on Medium/Expanded so the form does not stretch edge-to-edge on tablet/web. */
private val FORM_MAX_WIDTH: Dp = 480.dp

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
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Responsive: full-width on Compact, centered ~480dp column on Medium/Expanded.
            val bp = rememberFleetBreakpoint()
            val formModifier = if (bp == FleetBreakpoint.Compact) {
                Modifier.fillMaxWidth()
            } else {
                Modifier.widthIn(max = FORM_MAX_WIDTH)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(FleetTokens.Spacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = formModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.isResetMode) {
                        ResetPasswordContent(
                            resetToken = state.resetToken,
                            newPassword = state.newPassword,
                            confirmPassword = state.confirmPassword,
                            isLoading = state.isLoading,
                            error = state.error,
                            onResetTokenChange = { viewModel.sendIntent(ForgotPasswordContract.Intent.UpdateResetToken(it)) },
                            onNewPasswordChange = { viewModel.sendIntent(ForgotPasswordContract.Intent.UpdateNewPassword(it)) },
                            onConfirmPasswordChange = { viewModel.sendIntent(ForgotPasswordContract.Intent.UpdateConfirmPassword(it)) },
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
    }
}

/** Whether the identifier is a valid email or Indian mobile (email-or-mobile field). */
private fun isValidIdentifier(value: String): Boolean {
    val trimmed = value.trim()
    return ValidationUtils.isValidEmail(trimmed) || ValidationUtils.isValidIndianMobile(trimmed)
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
    // Track interaction so inline errors only appear after the user has typed/blurred.
    var identifierTouched by remember { mutableStateOf(false) }

    val identifierInvalid = identifierTouched && identifier.isNotEmpty() && !isValidIdentifier(identifier)
    val submitEnabled = !isLoading && isValidIdentifier(identifier)

    val submit = {
        identifierTouched = true
        if (isValidIdentifier(identifier)) {
            focusManager.clearFocus()
            onSubmit()
        }
    }

    // Header
    FleetAccentIconChip(
        accent = MaterialTheme.colorScheme.primary,
        chipSize = FleetTokens.IconSize.XXL,
        iconSize = FleetTokens.IconSize.XL,
        iconRes = Res.drawable.ic_lock
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

    Text(
        text = stringResource(Res.string.forgot_password_heading),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

    Text(
        text = stringResource(Res.string.forgot_password_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL))

    // Identifier Field (email or mobile)
    FleetInputField(
        value = identifier,
        onValueChange = {
            identifierTouched = true
            onIdentifierChange(it)
        },
        fieldType = FieldType.EMAIL,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.forgot_password_identifier_label),
        placeholder = stringResource(Res.string.forgot_password_identifier_placeholder),
        isError = identifierInvalid,
        errorMessage = if (identifierInvalid) stringResource(Res.string.error_identifier_required) else null,
        enabled = !isLoading,
        keyboardActions = KeyboardActions(onDone = { submit() })
    )

    // Backend / submit error
    error?.let { errorUiText ->
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
        Text(
            text = errorUiText.resolve(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

    // Submit Button
    FleetButton(
        text = stringResource(Res.string.forgot_password_send_instructions),
        onClick = submit,
        variant = ButtonVariant.PRIMARY,
        size = ButtonSize.LARGE,
        modifier = Modifier.fillMaxWidth(),
        isLoading = isLoading,
        enabled = submitEnabled
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

    // Back to Login Link
    FleetButton(
        text = stringResource(Res.string.forgot_password_back_to_login),
        onClick = onNavigateToLogin,
        variant = ButtonVariant.GHOST,
        enabled = !isLoading
    )
}

@Composable
private fun ResetPasswordContent(
    resetToken: String,
    newPassword: String,
    confirmPassword: String,
    isLoading: Boolean,
    error: com.indusjs.uicomponents.components.UiText?,
    onResetTokenChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetPassword: () -> Unit,
    focusManager: FocusManager
) {
    var resetTokenTouched by remember { mutableStateOf(false) }

    val resetTokenEmpty = resetTokenTouched && resetToken.trim().isEmpty()
    val passwordsMismatch = confirmPassword.isNotEmpty() && confirmPassword != newPassword

    // Submit gate: token present, both passwords present, and they match.
    // Password POLICY (length/complexity) stays backend-only.
    val submitEnabled = !isLoading &&
        resetToken.trim().isNotEmpty() &&
        newPassword.isNotEmpty() &&
        confirmPassword.isNotEmpty() &&
        !passwordsMismatch

    val submit = {
        resetTokenTouched = true
        focusManager.clearFocus()
        onResetPassword()
    }

    // Header
    FleetAccentIconChip(
        accent = MaterialTheme.colorScheme.primary,
        chipSize = FleetTokens.IconSize.XXL,
        iconSize = FleetTokens.IconSize.XL,
        iconRes = Res.drawable.ic_lock
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

    Text(
        text = stringResource(Res.string.reset_password_heading),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

    Text(
        text = stringResource(Res.string.reset_password_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

    // Reset Token Field
    FleetInputField(
        value = resetToken,
        onValueChange = {
            resetTokenTouched = true
            onResetTokenChange(it)
        },
        fieldType = FieldType.DEFAULT,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.reset_token_label),
        placeholder = stringResource(Res.string.reset_token_placeholder),
        isError = resetTokenEmpty,
        errorMessage = if (resetTokenEmpty) {
            stringResource(Res.string.error_reset_token_required)
        } else {
            stringResource(Res.string.reset_token_hint)
        },
        enabled = !isLoading,
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        )
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

    // New Password Field (policy enforced by backend; no client min-length hint)
    FleetPasswordField(
        value = newPassword,
        onValueChange = onNewPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.reset_password_new_label),
        placeholder = stringResource(Res.string.reset_password_new_placeholder),
        enabled = !isLoading
    )

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

    // Confirm Password Field (UI-only match check)
    FleetPasswordField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        modifier = Modifier.fillMaxWidth(),
        label = stringResource(Res.string.reset_password_confirm_label),
        placeholder = stringResource(Res.string.reset_password_confirm_placeholder),
        isError = passwordsMismatch,
        errorMessage = if (passwordsMismatch) {
            stringResource(Res.string.reset_password_mismatch)
        } else null,
        enabled = !isLoading,
        keyboardActions = KeyboardActions(onDone = { submit() })
    )

    // Backend / submit error
    error?.let { errorUiText ->
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
        Text(
            text = errorUiText.resolve(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

    // Reset Password Button
    FleetButton(
        text = stringResource(Res.string.reset_password_button),
        onClick = submit,
        variant = ButtonVariant.PRIMARY,
        size = ButtonSize.LARGE,
        modifier = Modifier.fillMaxWidth(),
        isLoading = isLoading,
        enabled = submitEnabled
    )
}
