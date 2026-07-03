package com.ijs.user.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetPasswordField
import com.indusjs.uicomponents.components.FleetTab
import com.indusjs.uicomponents.components.FleetTabBar
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.i18n.LocalLanguagePickerLauncher
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.uicomponents.theme.rememberThemeToggle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Max width for the centered login form on Medium/Expanded screens (tablet/web). */
private val FORM_MAX_WIDTH = 480.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {},
    onNavigateToOtpVerification: (email: String, mobile: String, needsEmail: Boolean, needsMobile: Boolean) -> Unit = { _, _, _, _ -> }
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

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LoginContract.Effect.NavigateToDashboard -> onLoginSuccess()
                is LoginContract.Effect.ShowError -> pendingSnackbar = effect.message
                is LoginContract.Effect.NavigateToOtpVerification -> onNavigateToOtpVerification(
                    effect.email, effect.mobile, effect.needsEmailVerification, effect.needsMobileVerification
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isDarkTheme = isAppInDarkTheme()
            val toggleTheme = rememberThemeToggle()
            IconButton(
                onClick = toggleTheme,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(FleetTokens.Spacing.L)
            ) {
                Icon(
                    painter = painterResource(
                        if (isDarkTheme) Res.drawable.ic_sun else Res.drawable.ic_moon
                    ),
                    contentDescription = if (isDarkTheme) stringResource(Res.string.cd_switch_to_light_mode) else stringResource(Res.string.cd_switch_to_dark_mode),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                )
            }

            // "भाषा / Language" — re-opens the first-launch language picker. Label is an intentional
            // bilingual literal (both scripts, language-neutral) to match the pre-choice picker copy.
            val openLanguagePicker = LocalLanguagePickerLauncher.current
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(FleetTokens.Spacing.L)
                    .clip(RoundedCornerShape(FleetTokens.Radius.Pill))
                    .clickable { openLanguagePicker() }
                    .padding(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.XS),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_language),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FleetTokens.IconSize.S)
                )
                Text(
                    text = "भाषा / Language",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val breakpoint = rememberFleetBreakpoint()
                // Compact: full-width. Medium/Expanded: centered, constrained column.
                val formWidthModifier = if (breakpoint.isAtLeastMedium) {
                    Modifier.widthIn(max = FORM_MAX_WIDTH)
                } else {
                    Modifier.fillMaxWidth()
                }

                if (state.isCheckingAuth) {
                    SplashContent()
                } else {
                    LoginFormContent(
                        state = state,
                        widthModifier = formWidthModifier,
                        onIdentifierChange = { viewModel.sendIntent(LoginContract.Intent.UpdateIdentifier(it)) },
                        onPasswordChange = { viewModel.sendIntent(LoginContract.Intent.UpdatePassword(it)) },
                        onSwitchMode = { viewModel.sendIntent(LoginContract.Intent.SwitchLoginMode(it)) },
                        onLogin = { viewModel.sendIntent(LoginContract.Intent.Login) },
                        onForgotPassword = onNavigateToForgotPassword,
                        onSignUp = onNavigateToSignUp,
                        focusManager = focusManager
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_fleet_logo),
            contentDescription = stringResource(Res.string.login_title),
            modifier = Modifier.size(FleetTokens.IconSize.XXL),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
        Text(
            text = stringResource(Res.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
        CircularProgressIndicator(
            modifier = Modifier.size(FleetTokens.IconSize.L),
            strokeWidth = FleetTokens.Height.ProgressStroke
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
        Text(
            text = stringResource(Res.string.loading),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LoginFormContent(
    state: LoginContract.State,
    widthModifier: Modifier,
    onIdentifierChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSwitchMode: (LoginContract.LoginMode) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    // §8 rhythm (f3 spec): explicit inter-element spacing, not a uniform spacedBy — the screen should
    // feel composed, not stretched. Top arrangement + per-gap Spacers below.
    Column(
        modifier = Modifier
            .then(widthModifier)
            .verticalScroll(rememberScrollState())
            .padding(FleetTokens.Spacing.XXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Logo — indigo rounded-square placeholder (real mark is a later branding task).
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(FleetTokens.Radius.XL))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_fleet_logo),
                contentDescription = stringResource(Res.string.login_title),
                modifier = Modifier.size(FleetTokens.IconSize.L),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))   // logo → title
        Text(
            text = stringResource(Res.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))   // title → subtitle
        Text(
            text = stringResource(Res.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL)) // subtitle → toggle

        // Email / Mobile toggle — shared segmented pill (FleetTabBar), same inset as the fields.
        val loginModeTabs = listOf(
            FleetTab(LoginContract.LoginMode.EMAIL, stringResource(Res.string.login_mode_email)),
            FleetTab(LoginContract.LoginMode.MOBILE, stringResource(Res.string.login_mode_mobile)),
        )
        FleetTabBar(
            tabs = loginModeTabs,
            selectedTabId = state.loginMode,
            onTabSelected = onSwitchMode,
            modifier = Modifier.fillMaxWidth(),
            scrollable = false,
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))  // toggle → fields

        val identifierErrorText = state.identifierError?.resolve()
        if (state.loginMode == LoginContract.LoginMode.EMAIL) {
            FleetInputField(
                value = state.identifier,
                onValueChange = onIdentifierChange,
                fieldType = FieldType.EMAIL,
                label = stringResource(Res.string.label_email),
                placeholder = stringResource(Res.string.placeholder_email),
                enabled = !state.isLoading,
                isError = identifierErrorText != null,
                errorMessage = identifierErrorText,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
        } else {
            FleetInputField(
                value = state.identifier,
                onValueChange = { onIdentifierChange(filterDigitsOnly(it, 10)) },
                fieldType = FieldType.PHONE,
                label = stringResource(Res.string.login_mobile_label),
                placeholder = stringResource(Res.string.login_mobile_placeholder),
                enabled = !state.isLoading,
                isError = identifierErrorText != null,
                errorMessage = identifierErrorText,
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))   // identifier → password

        val passwordErrorText = state.passwordError?.resolve()
        FleetPasswordField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = stringResource(Res.string.label_password),
            placeholder = stringResource(Res.string.placeholder_password),
            enabled = !state.isLoading,
            isError = passwordErrorText != null,
            errorMessage = passwordErrorText,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLogin()
                }
            )
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))   // password → forgot

        // Forgot password — standard end-aligned form link directly under the password field.
        Text(
            text = stringResource(Res.string.login_forgot_password),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.End)
                .clip(RoundedCornerShape(FleetTokens.Radius.M))
                .clickable(enabled = !state.isLoading, onClick = onForgotPassword)
                .padding(horizontal = FleetTokens.Spacing.XS, vertical = FleetTokens.Spacing.XS)
        )

        state.error?.let { error ->
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = error.resolve(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))  // forgot → CTA

        FleetButton(
            text = stringResource(Res.string.login_sign_in),
            onClick = onLogin,
            variant = ButtonVariant.PRIMARY,
            isLoading = state.isLoading,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL)) // CTA → footer

        // Footer — one centered line; "Sign Up" is a primary, ≥44dp touch target.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.login_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Box(
                modifier = Modifier
                    .heightIn(min = FleetTokens.Height.MinTouchTarget)
                    .clip(RoundedCornerShape(FleetTokens.Radius.M))
                    .clickable(enabled = !state.isLoading, onClick = onSignUp)
                    .padding(horizontal = FleetTokens.Spacing.S),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(Res.string.login_sign_up),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
