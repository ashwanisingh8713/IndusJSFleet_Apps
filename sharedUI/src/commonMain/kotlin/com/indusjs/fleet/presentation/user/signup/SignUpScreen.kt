package com.indusjs.fleet.presentation.user.signup

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
 * Sign Up Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onSignUpSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SignUpContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is SignUpContract.Effect.NavigateToDashboard -> {
                    onSignUpSuccess()
                }
                is SignUpContract.Effect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Header
            Text(
                text = "🚚",
                style = MaterialTheme.typography.displayLarge
            )

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Sign up to get started with Fleet Management",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // First Name and Last Name Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateFirstName(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("First Name") },
                    leadingIcon = { Text("👤") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Right) }
                    ),
                    singleLine = true,
                    enabled = !state.isLoading
                )

                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateLastName(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("Last Name") },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    enabled = !state.isLoading
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Email Field
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateEmail(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                placeholder = { Text("Enter your email") },
                leadingIcon = { Text("📧") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mobile Field
            OutlinedTextField(
                value = state.mobile,
                onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateMobile(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mobile Number") },
                placeholder = { Text("Enter your mobile number") },
                leadingIcon = { Text("📱") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdatePassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                placeholder = { Text("Create a password") },
                leadingIcon = { Text("🔒") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(SignUpContract.Intent.TogglePasswordVisibility) }
                    ) {
                        Text(if (state.isPasswordVisible) "👁️" else "👁️‍🗨️")
                    }
                },
                visualTransformation = if (state.isPasswordVisible) {
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
                    Text("Minimum 6 characters")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateConfirmPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter your password") },
                leadingIcon = { Text("🔒") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(SignUpContract.Intent.ToggleConfirmPasswordVisibility) }
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
                        viewModel.sendIntent(SignUpContract.Intent.SignUp)
                    }
                ),
                singleLine = true,
                enabled = !state.isLoading,
                isError = state.confirmPassword.isNotEmpty() && state.confirmPassword != state.password,
                supportingText = {
                    if (state.confirmPassword.isNotEmpty() && state.confirmPassword != state.password) {
                        Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // Error Message
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign Up Button
            Button(
                onClick = { viewModel.sendIntent(SignUpContract.Intent.SignUp) },
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
                    Text("Create Account")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.sendIntent(SignUpContract.Intent.NavigateToLogin) }
                ) {
                    Text("Sign In")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

