package com.ijs.team.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetEmailField
import com.indusjs.uicomponents.components.FleetMobileField
import com.indusjs.uicomponents.components.FleetTextField
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource

/**
 * Team Member Detail Screen - displays detailed information about a team member with edit capability.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMemberDetailScreen(
    viewModel: TeamMemberDetailViewModel,
    memberId: String,
    onNavigateBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load member on first composition
    LaunchedEffect(memberId) {
        viewModel.sendIntent(TeamMemberDetailContract.Intent.LoadMember(memberId))
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TeamMemberDetailContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TeamMemberDetailContract.Effect.NavigateBack -> {
                    onNavigateBack()
                }
                is TeamMemberDetailContract.Effect.MemberUpdated -> {
                    // Refresh handled internally
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.isEditMode) "Edit Member" else "Member Details",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isEditMode) {
                            viewModel.sendIntent(TeamMemberDetailContract.Intent.ExitEditMode)
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = if (state.isEditMode) "Cancel" else "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (!state.isLoading && state.member != null && !state.isEditMode && state.canEdit) {
                        IconButton(onClick = {
                            viewModel.sendIntent(TeamMemberDetailContract.Intent.EnterEditMode)
                        }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingContent(message = "Loading team member...")
            }
            state.error != null -> {
                ErrorContent(
                    error = state.error ?: "Something went wrong",
                    screenContext = FleetErrorContext.TEAM,
                    onRetry = { viewModel.sendIntent(TeamMemberDetailContract.Intent.RefreshMember) }
                )
            }
            state.member != null -> {
                if (state.isEditMode) {
                    EditMemberContent(
                        state = state,
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                } else {
                    ViewMemberContent(
                        member = state.member!!,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

/**
 * Simplified overload for use without ViewModel (backward compatibility).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamMemberDetailScreen(
    memberId: String,
    onNavigateBack: () -> Unit = {}
) {
    // Placeholder for when no ViewModel is provided
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Member Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Member ID: $memberId\n\nViewModel not provided.\nPlease use the overload with ViewModel.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ViewMemberContent(
    member: TeamMember,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    when (member.role) {
                        TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary
                        TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary
                        TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.initials.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = when (member.role) {
                    TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.onTertiary
                    TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.onPrimary
                    TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.onSecondary
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = member.fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Role Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = when (member.role) {
                TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            }
        ) {
            Text(
                text = member.roleDisplayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = when (member.role) {
                    TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary
                    TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary
                    TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Status Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (member.isActive) {
                Color(0xFF4CAF50).copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (member.isActive) Color(0xFF4CAF50)
                            else MaterialTheme.colorScheme.error
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (member.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (member.isActive) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Contact Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Contact Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailRow(icon = "✉️", label = "Email", value = member.email)

                // Mobile with call icon
                ClickablePhoneRow(
                    phoneNumber = member.mobile,
                    label = "Mobile",
                    icon = "📱"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Additional Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Additional Info",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                DetailRow(icon = "🆔", label = "Member ID", value = member.id)
                DetailRow(icon = "📅", label = "Created on", value = formatDate(member.createdAt))
            }
        }
    }
}

@Composable
private fun EditMemberContent(
    state: TeamMemberDetailContract.State,
    viewModel: TeamMemberDetailViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Personal Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                FleetTextField(
                    value = state.editFirstName,
                    onValueChange = { viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateFirstName(it)) },
                    label = "First Name",
                    placeholder = "Enter first name",
                    isError = state.firstNameError != null,
                    errorMessage = state.firstNameError,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                FleetTextField(
                    value = state.editLastName,
                    onValueChange = { viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateLastName(it)) },
                    label = "Last Name",
                    placeholder = "Enter last name",
                    isError = state.lastNameError != null,
                    errorMessage = state.lastNameError,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Information Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Contact Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                FleetEmailField(
                    value = state.editEmail,
                    onValueChange = { viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateEmail(it)) },
                    label = "Email",
                    placeholder = "Enter email address",
                    isError = state.emailError != null,
                    errorMessage = state.emailError,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                FleetMobileField(
                    rawValue = state.editMobile,
                    onRawValueChange = { viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateMobile(it)) },
                    label = "Mobile",
                    placeholder = "Enter mobile number",
                    isError = state.mobileError != null,
                    errorMessage = state.mobileError,
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Role & Status Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Role & Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Role Selection (based on permissions and not editing self)
                if (state.canChangeRole && state.availableRoles.isNotEmpty() && !state.isSelf) {
                    Text(
                        text = "Role",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Use Column for 3+ roles, Row for 2
                    if (state.availableRoles.size >= 3) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableRoles.forEach { role ->
                                RoleFilterChip(
                                    role = role,
                                    isSelected = state.editRole == role,
                                    onClick = {
                                        viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateRole(role))
                                    },
                                    enabled = !state.isSaving,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.availableRoles.forEach { role ->
                                RoleFilterChip(
                                    role = role,
                                    isSelected = state.editRole == role,
                                    onClick = {
                                        viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateRole(role))
                                    },
                                    enabled = !state.isSaving
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Active Status Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Status",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (state.editIsActive) "Member can access the app" else "Member access is disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.editIsActive,
                        onCheckedChange = {
                            viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateIsActive(it))
                        },
                        enabled = !state.isSaving && state.canToggleActive
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = { viewModel.sendIntent(TeamMemberDetailContract.Intent.SaveChanges) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isSaving
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Save Changes", fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Cancel Button
        OutlinedButton(
            onClick = { viewModel.sendIntent(TeamMemberDetailContract.Intent.ExitEditMode) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !state.isSaving
        ) {
            Text("Cancel", fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DetailRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Role filter chip for dynamic role selection.
 */
@Composable
private fun RoleFilterChip(
    role: TeamMemberRole,
    isSelected: Boolean,
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val (icon, label) = when (role) {
        TeamMemberRole.GENERAL_MANAGER -> "👨‍💼" to "General Manager"
        TeamMemberRole.MANAGER -> "👔" to "Manager"
        TeamMemberRole.SUPERVISOR -> "👷" to "Supervisor"
    }
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = { Text(icon) },
        enabled = enabled,
        modifier = modifier
    )
}

private fun formatDate(dateString: String): String {
    return try {
        // Parse ISO date and format to DD-MM-YYYY
        if (dateString.contains("T")) {
            val datePart = dateString.substringBefore("T")
            val parts = datePart.split("-")
            if (parts.size == 3) {
                "${parts[2]}-${parts[1]}-${parts[0]}"
            } else dateString
        } else dateString
    } catch (e: Exception) {
        dateString
    }
}

