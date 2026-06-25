package com.ijs.user.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetPasswordField
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Sign Up Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateToOtpVerification: (email: String, mobile: String, message: String, isResend: Boolean) -> Unit = { _, _, _, _ -> },
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Pending snackbar UiText — set from LaunchedEffect, resolved in composable scope
    var pendingSnackbar by remember { mutableStateOf<com.indusjs.uicomponents.components.UiText?>(null) }

    // Resolve pending snackbar in composable scope, then show
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
                is SignUpContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is SignUpContract.Effect.NavigateToOtpVerification -> {
                    onNavigateToOtpVerification(effect.email, effect.mobile, effect.message, effect.isResend)
                }
                is SignUpContract.Effect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            val breakpoint = rememberFleetBreakpoint()
            // Compact = full-width phone form; Medium/Expanded = centered, constrained column
            // so the form doesn't stretch edge-to-edge on tablet / web.
            val formWidthModifier = when (breakpoint) {
                FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
                else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = FleetTokens.Spacing.XL),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = formWidthModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                    Text(
                        text = stringResource(Res.string.signup_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                    Text(
                        text = stringResource(Res.string.signup_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL))

                    // Name Fields Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        FleetInputField(
                            value = state.firstName,
                            onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateFirstName(it)) },
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.label_first_name),
                            placeholder = stringResource(Res.string.placeholder_first_name),
                            isError = state.firstNameError != null,
                            errorMessage = state.firstNameError?.resolve(),
                            enabled = !state.isLoading
                        )

                        FleetInputField(
                            value = state.lastName,
                            onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateLastName(it)) },
                            modifier = Modifier.weight(1f),
                            label = stringResource(Res.string.label_last_name),
                            placeholder = stringResource(Res.string.placeholder_last_name),
                            isError = state.lastNameError != null,
                            errorMessage = state.lastNameError?.resolve(),
                            enabled = !state.isLoading
                        )
                    }

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    // Email Field
                    FleetInputField(
                        value = state.email,
                        onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateEmail(it)) },
                        fieldType = FieldType.EMAIL,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.signup_email_label),
                        placeholder = stringResource(Res.string.signup_email_placeholder),
                        isError = state.emailError != null,
                        errorMessage = state.emailError?.resolve(),
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    // Mobile Field
                    FleetInputField(
                        value = state.mobile,
                        onValueChange = {
                            viewModel.sendIntent(
                                SignUpContract.Intent.UpdateMobile(filterDigitsOnly(it, 10))
                            )
                        },
                        fieldType = FieldType.PHONE,
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.signup_mobile_label),
                        placeholder = stringResource(Res.string.signup_mobile_placeholder),
                        isError = state.mobileError != null,
                        errorMessage = state.mobileError?.resolve(),
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    // Password Field
                    FleetPasswordField(
                        value = state.password,
                        onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdatePassword(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.signup_password_label),
                        placeholder = stringResource(Res.string.signup_password_placeholder),
                        enabled = !state.isLoading
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                    // Confirm Password Field
                    FleetPasswordField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateConfirmPassword(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = stringResource(Res.string.signup_confirm_password_label),
                        placeholder = stringResource(Res.string.signup_confirm_password_placeholder),
                        isError = state.isConfirmPasswordMismatch,
                        errorMessage = if (state.isConfirmPasswordMismatch) {
                            stringResource(Res.string.signup_passwords_do_not_match)
                        } else null,
                        enabled = !state.isLoading
                    )

                    // Error Banner
                    state.error?.let { error ->
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                        FleetInlineErrorBanner(
                            message = error.resolve(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                    // Sign Up Button
                    FleetButton(
                        text = stringResource(Res.string.signup_create_account),
                        onClick = {
                            focusManager.clearFocus()
                            viewModel.sendIntent(SignUpContract.Intent.SignUp)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = state.isSubmitEnabled,
                        isLoading = state.isLoading
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                    // Divider with "or"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Text(
                            text = "  ${stringResource(Res.string.or)}  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    // Login Link
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(Res.string.signup_already_have_account),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = { viewModel.sendIntent(SignUpContract.Intent.NavigateToLogin) }
                        ) {
                            Text(
                                text = stringResource(Res.string.login_sign_in),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL))
                }
            }
        }
    }
}
