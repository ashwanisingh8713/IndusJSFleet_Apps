package com.ijs.team.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.uicomponents.components.DeleteConfirmationDialog
import com.indusjs.uicomponents.components.EmptyContent
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetAvatar
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.StatusToggleConfirmationDialog
import com.indusjs.uicomponents.components.FleetStatusBadge
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.FleetPasswordField
import com.indusjs.uicomponents.components.FleetSearchField
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.isExpanded
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import com.ijs.team.presentation.localizedDisplayName
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
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

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
                is TeamListContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
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
                                stringResource(
                                    Res.string.team_members_count,
                                    state.teamMembers.size,
                                    if (state.teamMembers.size != 1) "s" else ""
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    if (state.canCreateTeamMember) {
                        IconButton(
                            onClick = { viewModel.sendIntent(TeamListContract.Intent.NavigateToCreateMember) },
                            enabled = !state.isLoading
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_add),
                                contentDescription = stringResource(Res.string.team_add_member),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.sendIntent(TeamListContract.Intent.RefreshTeamMembers) },
                        enabled = !state.isRefreshing && !state.isLoading
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(FleetTokens.IconSize.M),
                                strokeWidth = FleetTokens.Height.ProgressStroke
                            )
                        } else {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.refresh),
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                // Enhanced Filter Tabs
                EnhancedFilterTabs(
                    selectedFilter = state.selectedFilter,
                    onFilterSelected = { viewModel.sendIntent(TeamListContract.Intent.SelectFilter(it)) },
                    modifier = Modifier.padding(
                        horizontal = FleetTokens.Spacing.ScreenHorizontal,
                        vertical = FleetTokens.Spacing.M
                    )
                )

                // Search Field
                FleetSearchField(
                    query = state.searchQuery,
                    onQueryChange = { viewModel.sendIntent(TeamListContract.Intent.UpdateSearchQuery(it)) },
                    placeholder = stringResource(Res.string.team_search_placeholder),
                    modifier = Modifier.padding(
                        horizontal = FleetTokens.Spacing.ScreenHorizontal,
                        vertical = FleetTokens.Spacing.S
                    )
                )

                // Content
                when {
                    state.isLoading -> {
                        LoadingContent(message = stringResource(Res.string.team_loading))
                    }

                    state.error != null -> {
                        ErrorContent(
                            error = state.error?.resolve() ?: stringResource(Res.string.error_generic),
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
                        TeamMemberList(
                            members = state.filteredMembers,
                            canEdit = { state.canEdit(it) },
                            canToggleActive = { state.canToggleActive(it) },
                            canResetPassword = { state.canResetPassword(it) },
                            canDelete = { state.canDelete(it) },
                            isTogglingActive = { state.isTogglingActive == it.id },
                            onClick = { viewModel.sendIntent(TeamListContract.Intent.NavigateToMemberDetail(it.id)) },
                            onToggleActive = { viewModel.sendIntent(TeamListContract.Intent.ToggleTeamMemberActive(it.id)) },
                            onResetPassword = { viewModel.sendIntent(TeamListContract.Intent.ShowResetPasswordDialog(it.id)) },
                            onDelete = { viewModel.sendIntent(TeamListContract.Intent.DeleteTeamMember(it.id)) }
                        )
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
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
    ) {
        TeamListContract.FilterType.entries.forEach { filter ->
            val isSelected = selectedFilter == filter
            FleetFilterChip(
                selected = isSelected,
                label = when (filter) {
                    TeamListContract.FilterType.ALL -> stringResource(Res.string.team_filter_all)
                    TeamListContract.FilterType.ADMINS -> stringResource(Res.string.team_filter_admins)
                    TeamListContract.FilterType.USERS -> stringResource(Res.string.team_filter_users)
                },
                onClick = { onFilterSelected(filter) },
                leadingIcon = when (filter) {
                    TeamListContract.FilterType.ALL -> Res.drawable.ic_team
                    TeamListContract.FilterType.ADMINS -> Res.drawable.ic_visibility
                    TeamListContract.FilterType.USERS -> Res.drawable.ic_profile
                }
            )
        }
    }
}

/**
 * Responsive team member list.
 *
 * Compact stays 1-up; Medium/Expanded show two cards per row. On Expanded the
 * grid is capped to a readable width and centered instead of stretching.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamMemberList(
    members: List<TeamMember>,
    canEdit: (TeamMember) -> Boolean,
    canToggleActive: (TeamMember) -> Boolean,
    canResetPassword: (TeamMember) -> Boolean,
    canDelete: (TeamMember) -> Boolean,
    isTogglingActive: (TeamMember) -> Boolean,
    onClick: (TeamMember) -> Unit,
    onToggleActive: (TeamMember) -> Unit,
    onResetPassword: (TeamMember) -> Unit,
    onDelete: (TeamMember) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val breakpoint = rememberFleetBreakpoint()
        val columns = if (breakpoint.isAtLeastMedium) 2 else 1
        val contentWidthModifier = if (breakpoint.isExpanded) {
            Modifier.fillMaxWidth().widthIn(max = FleetTokens.Width.MaxContent)
        } else {
            Modifier.fillMaxWidth()
        }

        LazyColumn(
            modifier = contentWidthModifier.fillMaxHeight().align(Alignment.TopCenter),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            if (columns == 1) {
                items(members, key = { it.id }) { member ->
                    EnhancedTeamMemberCard(
                        member = member,
                        canEdit = canEdit(member),
                        canToggleActive = canToggleActive(member),
                        canResetPassword = canResetPassword(member),
                        canDelete = canDelete(member),
                        isTogglingActive = isTogglingActive(member),
                        onClick = { onClick(member) },
                        onToggleActive = { onToggleActive(member) },
                        onResetPassword = { onResetPassword(member) },
                        onDelete = { onDelete(member) }
                    )
                }
            } else {
                val rows = members.chunked(columns)
                items(rows, key = { row -> row.first().id }) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                    ) {
                        row.forEach { member ->
                            EnhancedTeamMemberCard(
                                member = member,
                                canEdit = canEdit(member),
                                canToggleActive = canToggleActive(member),
                                canResetPassword = canResetPassword(member),
                                canDelete = canDelete(member),
                                isTogglingActive = isTogglingActive(member),
                                onClick = { onClick(member) },
                                onToggleActive = { onToggleActive(member) },
                                onResetPassword = { onResetPassword(member) },
                                onDelete = { onDelete(member) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // Keep the last odd card aligned to a single column width.
                        if (row.size < columns) {
                            repeat(columns - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
            }
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
    val isSearching = searchQuery.isNotEmpty()
    EmptyContent(
        iconRes = if (isSearching) Res.drawable.ic_search else Res.drawable.ic_team,
        title = if (isSearching) {
            stringResource(Res.string.team_no_results)
        } else {
            stringResource(Res.string.team_no_members_yet)
        },
        message = if (isSearching) {
            stringResource(Res.string.team_no_results_message)
        } else {
            stringResource(Res.string.team_empty_description)
        },
        actionLabel = if (isSearching) null else stringResource(Res.string.team_add_member),
        onAction = if (isSearching) null else onAddMember
    )
}

/**
 * Enhanced team member card with better visual design and role-based actions.
 */
@Composable
private fun MemberStatusBadge(isActive: Boolean) {
    val color = if (isActive) FleetStatusColors.FleetOnRoute else MaterialTheme.colorScheme.error
    FleetStatusBadge(
        status = if (isActive) {
            stringResource(Res.string.team_status_active)
        } else {
            stringResource(Res.string.team_status_inactive)
        },
        color = color
    )
}

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
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDisableDialog by remember { mutableStateOf(false) }
    var showActionsMenu by remember { mutableStateOf(false) }

    FleetSectionCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with role-based color
            val roleColor = when (member.role) {
                TeamMemberRole.OWNER -> MaterialTheme.colorScheme.tertiary
                TeamMemberRole.ADMIN -> MaterialTheme.colorScheme.primary
                TeamMemberRole.USER -> MaterialTheme.colorScheme.secondary
            }
            val onRoleColor = when (member.role) {
                TeamMemberRole.OWNER -> MaterialTheme.colorScheme.onTertiary
                TeamMemberRole.ADMIN -> MaterialTheme.colorScheme.onPrimary
                TeamMemberRole.USER -> MaterialTheme.colorScheme.onSecondary
            }
            FleetAvatar(
                name = member.fullName,
                size = FleetTokens.IconSize.XL,
                background = roleColor,
                contentColor = onRoleColor,
                textStyle = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.width(FleetTokens.Spacing.L))

            // Member Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name on its own line so it is never truncated by the pills.
                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                // Role + status pills reflow to a second row beneath the name.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Role Badge
                    val roleBadgeColor = when (member.role) {
                        TeamMemberRole.OWNER -> MaterialTheme.colorScheme.tertiary
                        TeamMemberRole.ADMIN -> MaterialTheme.colorScheme.primary
                        TeamMemberRole.USER -> MaterialTheme.colorScheme.secondary
                    }
                    FleetStatusBadge(
                        status = member.role.localizedDisplayName(),
                        color = roleBadgeColor
                    )

                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))

                    MemberStatusBadge(isActive = member.isActive)
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                // Email
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_email),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                    Text(
                        text = member.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                // Mobile
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_phone),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.S)
                    )
                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                    Text(
                        text = member.mobile,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

            }

            // Actions Menu
            Box {
                IconButton(
                    onClick = { showActionsMenu = true },
                    modifier = Modifier.size(FleetTokens.Height.FilterChipRow)
                ) {
                    if (isTogglingActive) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(FleetTokens.IconSize.M),
                            strokeWidth = FleetTokens.Height.ProgressStroke
                        )
                    } else {
                        Icon(
                            painter = painterResource(Res.drawable.ic_more_vert),
                            contentDescription = stringResource(Res.string.team_action_actions),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
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
                                // Disabling access is consequential — confirm first.
                                // Enabling is non-destructive and applied immediately.
                                if (member.isActive) {
                                    showDisableDialog = true
                                } else {
                                    onToggleActive()
                                }
                            },
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(
                                        if (member.isActive) Res.drawable.ic_visibility_off
                                        else Res.drawable.ic_check_circle
                                    ),
                                    contentDescription = null,
                                    tint = if (member.isActive) {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    } else {
                                        FleetStatusColors.FleetOnRoute
                                    },
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
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
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_lock),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                            }
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
                                    modifier = Modifier.size(FleetTokens.IconSize.S)
                                )
                            }
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    DeleteConfirmationDialog(
        showDialog = showDeleteDialog,
        entityName = member.fullName,
        onConfirmDelete = {
            showDeleteDialog = false
            onDelete()
        },
        onDismiss = { showDeleteDialog = false }
    )

    // Disable Access Confirmation Dialog
    // Only shown when disabling (member currently active) — see the toggle menu item.
    StatusToggleConfirmationDialog(
        showDialog = showDisableDialog,
        entityName = member.fullName,
        currentlyActive = true,
        onConfirm = {
            showDisableDialog = false
            onToggleActive()
        },
        onDismiss = { showDisableDialog = false }
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
    var passwordError by remember { mutableStateOf<UiText?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        title = {
            Text(
                stringResource(Res.string.team_reset_password_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    stringResource(Res.string.team_reset_password_subtitle, memberName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                FleetPasswordField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    label = stringResource(Res.string.team_label_new_password),
                    placeholder = stringResource(Res.string.team_placeholder_new_password),
                    isError = passwordError != null,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                FleetPasswordField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        passwordError = null
                    },
                    label = stringResource(Res.string.team_label_confirm_new_password),
                    placeholder = stringResource(Res.string.team_placeholder_confirm_new_password),
                    isError = passwordError != null,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                passwordError?.let { err ->
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    Text(
                        text = err.resolve(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Password policy is enforced by the backend; only the UI-level
                    // confirm-match is checked here before submitting.
                    if (password != confirmPassword) {
                        passwordError = UiText.StringRes(Res.string.error_passwords_mismatch)
                    } else {
                        onConfirm(password)
                    }
                },
                enabled = !isLoading && password.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(FleetTokens.IconSize.M),
                        strokeWidth = FleetTokens.Height.ProgressStroke,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(Res.string.team_reset_password_title))
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(stringResource(Res.string.cancel))
            }
        },
        shape = RoundedCornerShape(FleetTokens.Radius.XL)
    )
}
