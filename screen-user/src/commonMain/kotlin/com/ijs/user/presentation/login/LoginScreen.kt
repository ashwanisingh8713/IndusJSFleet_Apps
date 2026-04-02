package com.ijs.user.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme
import com.indusjs.uicomponents.theme.rememberThemeToggle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Login Screen composable.
 * Uses reusable UI components from core/ui for consistent styling.
 *
 * Automatically checks if user is already logged in on startup.
 * If logged in, navigates to dashboard without showing login form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onNavigateToForgotPassword: () -> Unit = {}
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
                is LoginContract.Effect.ShowError -> {
                    pendingSnackbar = effect.message
                }
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
            // Theme toggle button in top-right corner
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

            // Main content centered
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Show splash/loading while checking auth status
                if (state.isCheckingAuth) {
                    SplashContent()
                } else {
                    // Show login form
                    LoginFormContent(
                        state = state,
                        onEmailChange = { viewModel.sendIntent(LoginContract.Intent.UpdateEmail(it)) },
                        onPasswordChange = { viewModel.sendIntent(LoginContract.Intent.UpdatePassword(it)) },
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

/**
 * Splash content shown while checking authentication status.
 */
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

/**
 * Login form content.
 */
@Composable
private fun LoginFormContent(
    state: LoginContract.State,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(FleetTokens.Spacing.XXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_fleet_logo),
            contentDescription = stringResource(Res.string.login_title),
            modifier = Modifier.size(FleetTokens.IconSize.XXL),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(Res.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = stringResource(Res.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

        FleetInputField(
            value = state.email,
            onValueChange = onEmailChange,
            fieldType = FieldType.EMAIL,
            label = stringResource(Res.string.label_email),
            placeholder = stringResource(Res.string.placeholder_email),
            enabled = !state.isLoading,
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        FleetInputField(
            value = state.password,
            onValueChange = onPasswordChange,
            fieldType = FieldType.PASSWORD,
            label = stringResource(Res.string.label_password),
            placeholder = stringResource(Res.string.placeholder_password),
            enabled = !state.isLoading,
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onLogin()
                }
            )
        )

        state.error?.let { error ->
            Text(
                text = error.resolve(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

        FleetButton(
            text = stringResource(Res.string.login_forgot_password),
            onClick = onForgotPassword,
            variant = ButtonVariant.GHOST,
            modifier = Modifier.align(Alignment.End)
        )

        FleetButton(
            text = stringResource(Res.string.login_sign_in),
            onClick = onLogin,
            variant = ButtonVariant.PRIMARY,
            isLoading = state.isLoading,
            enabled = !state.isLoading
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.login_no_account),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FleetButton(
                text = stringResource(Res.string.login_sign_up),
                onClick = onSignUp,
                variant = ButtonVariant.GHOST
            )
        }

        Text(
            text = stringResource(Res.string.login_demo_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

