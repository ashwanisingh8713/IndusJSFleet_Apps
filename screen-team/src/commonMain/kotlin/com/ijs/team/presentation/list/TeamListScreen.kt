package com.ijs.team.presentation.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.LoadingContent
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Team Members List Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamListScreen(
    viewModel: TeamListViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCreateMember: () -> Unit = {},
    onNavigateToMemberDetail: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TeamListContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is TeamListContract.Effect.NavigateToCreateMember -> {
                    onNavigateToCreateMember()
                }
                is TeamListContract.Effect.NavigateToMemberDetail -> {
                    onNavigateToMemberDetail(effect.id)
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(Res.string.team_members_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (state.teamMembers.isNotEmpty()) {
                            Text(
                                "${state.teamMembers.size} member${if (state.teamMembers.size != 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.sendIntent(TeamListContract.Intent.RefreshTeamMembers) },
                        enabled = !state.isRefreshing && !state.isLoading
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.refresh),
                                tint = MaterialTheme.colorScheme.onSurface,
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
        },
        floatingActionButton = {
            // Only show FAB if user can create team members (Owner or General Manager)
            if (state.canCreateTeamMember) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.sendIntent(TeamListContract.Intent.NavigateToCreateMember) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.team_add), fontWeight = FontWeight.Medium)
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.sendIntent(TeamListContract.Intent.RefreshTeamMembers) },
            state = pullRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Enhanced Stats Section
                TeamStatsSection(
                    generalManagersCount = state.generalManagersCount,
                    managersCount = state.managersCount,
                    supervisorsCount = state.supervisorsCount,
                    totalCount = state.teamMembers.size
                )

                // Enhanced Filter Tabs
                EnhancedFilterTabs(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = { viewModel.sendIntent(TeamListContract.Intent.SelectFilter(it)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Search Field
                FleetSearchField(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.sendIntent(TeamListContract.Intent.UpdateSearchQuery(it)) },
                    placeholder = stringResource(Res.string.team_search_placeholder),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Content
                when {
                    state.isLoading -> {
                        LoadingContent(message = stringResource(Res.string.team_loading))
                    }

                    state.error != null -> {
                        ErrorContent(
                            error = state.error ?: stringResource(Res.string.error_generic),
                            screenContext = FleetErrorContext.TEAM,
                            onRetry = { viewModel.sendIntent(TeamListContract.Intent.LoadTeamMembers) }
                        )
                    }

                    state.filteredMembers.isEmpty() -> {
                        EmptyTeamContent(
                            searchQuery = state.searchQuery,
                            onAddMember = { viewModel.sendIntent(TeamListContract.Intent.NavigateToCreateMember) }
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.filteredMembers, key = { it.id }) { member ->
                                EnhancedTeamMemberCard(
                                    member = member,
                                    canEdit = state.canEdit(member),
                                    canToggleActive = state.canToggleActive(member),
                                    canResetPassword = state.canResetPassword(member),
                                    canDelete = state.canDelete(member),
                                    isTogglingActive = state.isTogglingActive == member.id,
                                    onClick = { viewModel.sendIntent(TeamListContract.Intent.NavigateToMemberDetail(member.id)) },
                                    onToggleActive = { viewModel.sendIntent(TeamListContract.Intent.ToggleTeamMemberActive(member.id)) },
                                    onResetPassword = { viewModel.sendIntent(TeamListContract.Intent.ShowResetPasswordDialog(member.id)) },
                                    onDelete = { viewModel.sendIntent(TeamListContract.Intent.DeleteTeamMember(member.id)) }
                                )
                            }

                            // Bottom spacing for FAB
                            item {
                                Spacer(modifier = Modifier.height(80.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Reset Password Dialog
    state.showResetPasswordDialogForMemberId?.let { memberId ->
        val member = state.teamMembers.find { it.id == memberId }
        if (member != null) {
            ResetPasswordDialog(
                memberName = member.fullName,
                isLoading = state.isResettingPassword == memberId,
                onDismiss = { viewModel.sendIntent(TeamListContract.Intent.DismissResetPasswordDialog) },
                onConfirm = { newPassword ->
                    viewModel.sendIntent(TeamListContract.Intent.ResetTeamMemberPassword(memberId, newPassword))
                }
            )
        }
    }
}

/**
 * Enhanced stats section with visual cards.
 */
@Composable
private fun TeamStatsSection(
    generalManagersCount: Int,
    managersCount: Int,
    supervisorsCount: Int,
    totalCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                icon = "🎯",
                count = generalManagersCount,
                label = stringResource(Res.string.team_filter_gm),
                color = MaterialTheme.colorScheme.tertiary
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            StatItem(
                icon = "👔",
                count = managersCount,
                label = stringResource(Res.string.team_filter_managers),
                color = MaterialTheme.colorScheme.primary
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            StatItem(
                icon = "👷",
                count = supervisorsCount,
                label = stringResource(Res.string.team_filter_supervisors),
                color = MaterialTheme.colorScheme.secondary
            )

            // Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            StatItem(
                icon = "👥",
                count = totalCount,
                label = stringResource(Res.string.team_label_total),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun StatItem(
    icon: String,
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Enhanced filter tabs with better visual design.
 */
@Composable
private fun EnhancedFilterTabs(
    selectedFilter: TeamListContract.FilterType,
    onFilterSelected: (TeamListContract.FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TeamListContract.FilterType.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        text = when (filter) {
                            TeamListContract.FilterType.ALL -> stringResource(Res.string.team_filter_all)
                            TeamListContract.FilterType.GENERAL_MANAGERS -> stringResource(Res.string.team_filter_gm)
                            TeamListContract.FilterType.MANAGERS -> stringResource(Res.string.team_filter_managers)
                            TeamListContract.FilterType.SUPERVISORS -> stringResource(Res.string.team_filter_supervisors)
                        },
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    Text(
                        text = when (filter) {
                            TeamListContract.FilterType.ALL -> "👥"
                            TeamListContract.FilterType.GENERAL_MANAGERS -> "🎯"
                            TeamListContract.FilterType.MANAGERS -> "👔"
                            TeamListContract.FilterType.SUPERVISORS -> "👷"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Empty state content for team list.
 */
@Composable
private fun EmptyTeamContent(
    searchQuery: String,
    onAddMember: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "🔍" else "👥",
                    style = MaterialTheme.typography.displaySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (searchQuery.isNotEmpty()) {
                stringResource(Res.string.team_no_results)
            } else {
                stringResource(Res.string.team_no_members_yet)
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (searchQuery.isNotEmpty()) {
                stringResource(Res.string.team_no_results_message)
            } else {
                stringResource(Res.string.team_empty_description)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (searchQuery.isEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddMember,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(Res.string.team_add_member))
            }
        }
    }
}

/**
 * Enhanced team member card with better visual design and role-based actions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTeamMemberCard(
    member: TeamMember,
    canEdit: Boolean = true,
    canToggleActive: Boolean = true,
    canResetPassword: Boolean = true,
    canDelete: Boolean = true,
    isTogglingActive: Boolean = false,
    onClick: () -> Unit,
    onToggleActive: () -> Unit = {},
    onResetPassword: () -> Unit = {},
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with role-based color
            Box(
                modifier = Modifier
                    .size(56.dp)
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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = when (member.role) {
                        TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.onTertiary
                        TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.onPrimary
                        TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.onSecondary
                    }
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Member Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name and Role Badge Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Role Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (member.role) {
                            TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                            TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                        }
                    ) {
                        Text(
                            text = member.roleDisplayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = when (member.role) {
                                TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary
                                TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary
                                TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Email
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "✉️",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Mobile
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📱",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = member.mobile,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (member.isActive) {
                        Color(0xFF4CAF50).copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (member.isActive) Color(0xFF4CAF50)
                                    else MaterialTheme.colorScheme.error
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (member.isActive) stringResource(Res.string.team_status_active) else stringResource(Res.string.team_status_inactive),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (member.isActive) {
                                Color(0xFF4CAF50)
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }

            // Actions Menu
            Box {
                IconButton(
                    onClick = { showActionsMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    if (isTogglingActive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_more_vert),
                            contentDescription = stringResource(Res.string.team_action_actions),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showActionsMenu,
                    onDismissRequest = { showActionsMenu = false }
                ) {
                    // Toggle Active
                    if (canToggleActive) {
                        DropdownMenuItem(
                            text = {
                                Text(if (member.isActive) stringResource(Res.string.team_action_disable) else stringResource(Res.string.team_action_enable))
                            },
                            onClick = {
                                showActionsMenu = false
                                onToggleActive()
                            },
                            leadingIcon = {
                                Text(if (member.isActive) "🚫" else "✅")
                            }
                        )
                    }

                    // Reset Password
                    if (canResetPassword) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.team_reset_password_title)) },
                            onClick = {
                                showActionsMenu = false
                                onResetPassword()
                            },
                            leadingIcon = { Text("🔑") }
                        )
                    }

                    // Delete
                    if (canDelete) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(Res.string.delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showActionsMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_delete),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    stringResource(Res.string.team_delete_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    stringResource(Res.string.team_delete_message, member.fullName),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// Legacy components (kept for backward compatibility)
@Composable
private fun StatsCard(
    title: String,
    count: Int,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count.toString(), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: TeamListContract.FilterType,
    onFilterSelected: (TeamListContract.FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    EnhancedFilterTabs(selectedFilter, onFilterSelected, modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamMemberCard(
    member: TeamMember,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    EnhancedTeamMemberCard(
        member = member,
        onClick = onClick,
        onDelete = onDelete
    )
}

/**
 * Dialog for resetting a team member's password.
 */
@Composable
private fun ResetPasswordDialog(
    memberName: String,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                "Reset Password",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    "Set a new password for $memberName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = { Text("New Password") },
                    placeholder = { Text("Enter new password") },
                    singleLine = true,
                    isError = passwordError != null,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordError = null
                    },
                    label = { Text("Confirm Password") },
                    placeholder = { Text("Confirm new password") },
                    singleLine = true,
                    isError = passwordError != null,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                if (passwordError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = passwordError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        password.length < 6 -> {
                            passwordError = "Password must be at least 6 characters"
                        }
                        password != confirmPassword -> {
                            passwordError = "Passwords do not match"
                        }
                        else -> {
                            onConfirm(password)
                        }
                    }
                },
                enabled = !isLoading && password.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Reset Password")
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
