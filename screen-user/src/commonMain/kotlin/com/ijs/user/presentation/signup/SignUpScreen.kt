package com.ijs.user.presentation.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.filterDigitsOnly
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
    onSignUpSuccess: () -> Unit = {},
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(Res.string.signup_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.signup_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Name Fields Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FleetInputField(
                    value = state.firstName,
                    onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateFirstName(it)) },
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.label_first_name),
                    placeholder = stringResource(Res.string.placeholder_first_name),
                    enabled = !state.isLoading
                )

                FleetInputField(
                    value = state.lastName,
                    onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateLastName(it)) },
                    modifier = Modifier.weight(1f),
                    label = stringResource(Res.string.label_last_name),
                    placeholder = stringResource(Res.string.placeholder_last_name),
                    enabled = !state.isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Email Field
            FleetInputField(
                value = state.email,
                onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateEmail(it)) },
                fieldType = FieldType.EMAIL,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.signup_email_label),
                placeholder = stringResource(Res.string.signup_email_placeholder),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field
            FleetInputField(
                value = state.password,
                onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdatePassword(it)) },
                fieldType = FieldType.PASSWORD,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.signup_password_label),
                placeholder = stringResource(Res.string.signup_password_placeholder),
                enabled = !state.isLoading
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirm Password Field
            FleetInputField(
                value = state.confirmPassword,
                onValueChange = { viewModel.sendIntent(SignUpContract.Intent.UpdateConfirmPassword(it)) },
                fieldType = FieldType.PASSWORD,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.signup_confirm_password_label),
                placeholder = stringResource(Res.string.signup_confirm_password_placeholder),
                isError = state.confirmPassword.isNotEmpty() && state.confirmPassword != state.password,
                errorMessage = if (state.confirmPassword.isNotEmpty() && state.confirmPassword != state.password) {
                    stringResource(Res.string.signup_passwords_do_not_match)
                } else null,
                enabled = !state.isLoading
            )

            // Error Message
            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "⚠️ ${error.resolve()}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sign Up Button
            Button(
                onClick = { viewModel.sendIntent(SignUpContract.Intent.SignUp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.signup_create_account),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

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

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

