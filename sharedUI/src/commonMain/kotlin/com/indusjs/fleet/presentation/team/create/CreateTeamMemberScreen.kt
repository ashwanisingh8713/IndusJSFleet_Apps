package com.indusjs.fleet.presentation.team.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.FleetEmailField
import com.indusjs.fleet.core.ui.FleetMobileField
import com.indusjs.fleet.domain.entity.team.TeamMemberRole
import indusjsfleet.sharedui.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Create Team Member Screen composable.
 *
 * @param viewModel The ViewModel for this screen
 * @param excludeGeneralManager If true, General Manager role will not be shown (e.g., when coming from Caretaker assignment)
 * @param onNavigateBack Callback to navigate back
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamMemberScreen(
    viewModel: CreateTeamMemberViewModel,
    excludeGeneralManager: Boolean = false,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Apply excludeGeneralManager filter on first composition
    LaunchedEffect(excludeGeneralManager) {
        if (excludeGeneralManager) {
            viewModel.sendIntent(CreateTeamMemberContract.Intent.SetExcludeGeneralManager(true))
        }
    }

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
                title = { Text("Add Team Member") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Role Selection Section
            SectionCard(
                title = "Select Role",
                subtitle = "Choose the team member's role and permissions"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.availableRoles.forEach { role ->
                        val (title, emoji, description) = when (role) {
                            TeamMemberRole.GENERAL_MANAGER -> Triple("General Manager", "👨‍💼", "Full operational & financial access")
                            TeamMemberRole.MANAGER -> Triple("Manager", "👔", "Can manage drivers and trips")
                            TeamMemberRole.SUPERVISOR -> Triple("Supervisor", "👷", "Can view and track operations")
                        }
                        RoleSelectionCard(
                            title = title,
                            emoji = emoji,
                            description = description,
                            isSelected = state.selectedRole == role,
                            onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.SelectRole(role)) },
                            enabled = !state.isLoading
                        )
                    }
                }
            }

            // Personal Information Section
            SectionCard(
                title = "Personal Information",
                subtitle = "Enter the team member's details"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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
                            placeholder = { Text("Enter first name") },
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
                            placeholder = { Text("Enter last name") },
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
                    FleetEmailField(
                        value = state.email,
                        onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateEmail(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Email Address",
                        placeholder = "Enter email address",
                        enabled = !state.isLoading
                    )

                    // Mobile Field
                    FleetMobileField(
                        rawValue = state.mobile,
                        onRawValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateMobile(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = "Mobile Number",
                        placeholder = "Enter mobile number",
                        enabled = !state.isLoading
                    )
                }
            }

            // Password Section
            SectionCard(
                title = "Set Password",
                subtitle = "Create a secure password for the team member"
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Password Field
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdatePassword(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Password") },
                        placeholder = { Text("Enter password") },
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
                            Text("Minimum 6 characters", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    )

                    // Confirm Password Field
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateConfirmPassword(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Confirm Password") },
                        placeholder = { Text("Enter password again") },
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
                }
            }

            // Error Message
            state.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚠️")
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Create Button
            Button(
                onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.CreateTeamMember) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                enabled = !state.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    val roleText = when (state.selectedRole) {
                        TeamMemberRole.GENERAL_MANAGER -> "General Manager"
                        TeamMemberRole.MANAGER -> "Manager"
                        TeamMemberRole.SUPERVISOR -> "Supervisor"
                    }
                    Text("Create $roleText", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * Section card wrapper with title and subtitle.
 */
@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            content()
        }
    }
}

/**
 * Enhanced role selection card with icon on left and title/description on right.
 */
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
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon on left
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = emoji,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            // Title and description on right
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Selection indicator
            if (isSelected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}
