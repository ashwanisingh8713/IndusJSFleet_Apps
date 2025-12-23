package com.indusjs.fleet.feature.team.presentation.create

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
import com.indusjs.fleet.feature.team.domain.entity.TeamMemberRole
import kotlinx.coroutines.flow.collectLatest

/**
 * Create Team Member Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamMemberScreen(
    viewModel: CreateTeamMemberViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateTeamMemberContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is CreateTeamMemberContract.Effect.TeamMemberCreated -> {
                    // Handled by navigation
                }
                is CreateTeamMemberContract.Effect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Create Team Member") },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "👥",
                    style = MaterialTheme.typography.displayMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Add Team Member",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Create a Manager or Supervisor for your organization",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Role Selection
            Text(
                text = "Select Role",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Manager Card
                RoleSelectionCard(
                    title = "Manager",
                    emoji = "👔",
                    description = "Can manage drivers and trips",
                    isSelected = state.selectedRole == TeamMemberRole.MANAGER,
                    onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.SelectRole(TeamMemberRole.MANAGER)) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading
                )

                // Supervisor Card
                RoleSelectionCard(
                    title = "Supervisor",
                    emoji = "👷",
                    description = "Can view and track operations",
                    isSelected = state.selectedRole == TeamMemberRole.SUPERVISOR,
                    onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.SelectRole(TeamMemberRole.SUPERVISOR)) },
                    modifier = Modifier.weight(1f),
                    enabled = !state.isLoading
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Personal Information Section
            Text(
                text = "Personal Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // First Name and Last Name Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateFirstName(it)) },
                    modifier = Modifier.weight(1f),
                    label = { Text("First Name") },
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
                    onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateLastName(it)) },
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

            // Email Field
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateEmail(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                placeholder = { Text("Enter email address") },
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

            // Mobile Field
            OutlinedTextField(
                value = state.mobile,
                onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateMobile(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mobile Number") },
                placeholder = { Text("Enter mobile number") },
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

            // Password Section
            Text(
                text = "Set Password",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            // Password Field
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdatePassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                placeholder = { Text("Create a password") },
                leadingIcon = { Text("🔒") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.TogglePasswordVisibility) }
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

            // Confirm Password Field
            OutlinedTextField(
                value = state.confirmPassword,
                onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateConfirmPassword(it)) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm Password") },
                placeholder = { Text("Re-enter password") },
                leadingIcon = { Text("🔒") },
                trailingIcon = {
                    IconButton(
                        onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.ToggleConfirmPasswordVisibility) }
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
                        viewModel.sendIntent(CreateTeamMemberContract.Intent.CreateTeamMember)
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
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Create Button
            Button(
                onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.CreateTeamMember) },
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
                    val roleText = if (state.selectedRole == TeamMemberRole.MANAGER) "Manager" else "Supervisor"
                    Text("Create $roleText")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleSelectionCard(
    title: String,
    emoji: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                }
            )
        }
    }
}

