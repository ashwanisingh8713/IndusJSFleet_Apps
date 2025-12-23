package com.indusjs.fleet.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.FleetEmailField
import com.indusjs.fleet.core.ui.FleetPasswordField
import com.indusjs.fleet.core.ui.FleetPrimaryButton
import com.indusjs.fleet.core.ui.FleetTextButton
import kotlinx.coroutines.flow.collectLatest

/**
 * Login Screen composable.
 * Uses reusable UI components from core/ui for consistent styling.
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

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is LoginContract.Effect.NavigateToDashboard -> onLoginSuccess()
                is LoginContract.Effect.ShowError -> {
                    snackbarHostState.showSnackbar(effect.message)
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Logo/Title
                Text(
                    text = "🚚",
                    style = MaterialTheme.typography.displayLarge
                )

                Text(
                    text = "Fleet Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Sign in to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field - using reusable component
                FleetEmailField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(LoginContract.Intent.UpdateEmail(it)) },
                    enabled = !state.isLoading,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Password Field - using reusable component
                FleetPasswordField(
                    value = state.password,
                    onValueChange = { viewModel.sendIntent(LoginContract.Intent.UpdatePassword(it)) },
                    enabled = !state.isLoading,
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.sendIntent(LoginContract.Intent.Login)
                        }
                    )
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

                Spacer(modifier = Modifier.height(8.dp))

                // Forgot Password Link - using reusable component
                FleetTextButton(
                    text = "Forgot Password?",
                    onClick = onNavigateToForgotPassword,
                    modifier = Modifier.align(Alignment.End)
                )

                // Login Button - using reusable component
                FleetPrimaryButton(
                    text = "Sign In",
                    onClick = { viewModel.sendIntent(LoginContract.Intent.Login) },
                    isLoading = state.isLoading,
                    enabled = !state.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sign Up Link
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FleetTextButton(
                        text = "Sign Up",
                        onClick = onNavigateToSignUp
                    )
                }

                // Demo hint
                Text(
                    text = "Demo: Enter any email and password",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

