package com.ijs.user.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.domain.entity.user.OrganizationStats
import com.indusjs.fleet.domain.entity.user.OwnerInfo
import com.indusjs.fleet.domain.entity.user.User
import com.indusjs.fleet.domain.entity.user.UserRole
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAppInDarkTheme
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.indusjs.uicomponents.theme.rememberThemeToggle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * User Profile Screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    var pendingSnackbar by remember { mutableStateOf<com.indusjs.uicomponents.components.UiText?>(null) }

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
                is ProfileContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is ProfileContract.Effect.NavigateToChangePassword -> {
                    onNavigateToChangePassword()
                }
                is ProfileContract.Effect.NavigateToLogin -> {
                    onLogout()
                }
                is ProfileContract.Effect.ProfileUpdated -> {
                    // Profile updated successfully
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.profile_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    // Theme toggle
                    val isDarkTheme = isAppInDarkTheme()
                    val toggleTheme = rememberThemeToggle()
                    IconButton(onClick = toggleTheme) {
                        Icon(
                            painter = painterResource(
                                if (isDarkTheme) Res.drawable.ic_sun else Res.drawable.ic_moon
                            ),
                            contentDescription = if (isDarkTheme) stringResource(Res.string.cd_light_mode) else stringResource(Res.string.cd_dark_mode),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    if (state.isEditing) {
                        TextButton(onClick = { viewModel.sendIntent(ProfileContract.Intent.CancelEditing) }) {
                            Text(stringResource(Res.string.cancel), color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        IconButton(onClick = { viewModel.sendIntent(ProfileContract.Intent.RefreshProfile) }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_refresh),
                                contentDescription = stringResource(Res.string.refresh),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    com.indusjs.uicomponents.components.ErrorContent(
                        error = state.error!!.resolve(),
                        onRetry = { viewModel.sendIntent(ProfileContract.Intent.LoadProfile) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.profile != null -> {
                    if (state.isEditing) {
                        EditProfileContent(
                            state = state,
                            onFirstNameChange = { viewModel.sendIntent(ProfileContract.Intent.UpdateFirstName(it)) },
                            onLastNameChange = { viewModel.sendIntent(ProfileContract.Intent.UpdateLastName(it)) },
                            onEmailChange = { viewModel.sendIntent(ProfileContract.Intent.UpdateEmail(it)) },
                            onMobileChange = { viewModel.sendIntent(ProfileContract.Intent.UpdateMobile(it)) },
                            onSave = { viewModel.sendIntent(ProfileContract.Intent.SaveProfile) }
                        )
                    } else {
                        ProfileContent(
                            user = state.profile!!.user,
                            organizationStats = state.profile!!.organizationStats,
                            ownerInfo = state.profile!!.ownerInfo,
                            onEditProfile = { viewModel.sendIntent(ProfileContract.Intent.StartEditing) },
                            onChangePassword = { viewModel.sendIntent(ProfileContract.Intent.NavigateToChangePassword) },
                            onLogout = { showLogoutConfirmation = true }
                        )
                    }
                }
            }
        }
    }

    // Logout confirmation dialog
    com.indusjs.uicomponents.components.LogoutConfirmationDialog(
        showDialog = showLogoutConfirmation,
        onConfirmLogout = {
            showLogoutConfirmation = false
            viewModel.sendIntent(ProfileContract.Intent.Logout)
        },
        onDismiss = { showLogoutConfirmation = false }
    )
}

@Composable
private fun ProfileContent(
    user: User,
    organizationStats: OrganizationStats?,
    ownerInfo: OwnerInfo?,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Header
        item {
            ProfileHeader(user = user)
        }

        // Profile Details Card
        item {
            ProfileDetailsCard(user = user)
        }

        // Organization Stats (for Owners)
        if (organizationStats != null) {
            item {
                OrganizationStatsCard(stats = organizationStats)
            }
        }

        // Owner Info (for Managers/Supervisors)
        if (ownerInfo != null) {
            item {
                OwnerInfoCard(ownerInfo = ownerInfo)
            }
        }

        // Action Buttons
        item {
            ActionButtonsCard(
                onEditProfile = onEditProfile,
                onChangePassword = onChangePassword,
                onLogout = onLogout
            )
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
    // Delegates to the shared section card (promoted to ijs-ui-components-lib).
    com.indusjs.uicomponents.components.FleetSectionCard {
        // Compact identity row: avatar + name/email
        Row(verticalAlignment = Alignment.CenterVertically) {
                com.indusjs.uicomponents.components.FleetAvatar(
                    name = user.fullName.ifBlank { user.email },
                    size = 72.dp,
                    textStyle = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.fullName.ifBlank { user.email.ifBlank { "—" } },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (user.email.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Role + Account Status badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoleBadge(role = user.role)
                AccountStatusBadge(isActive = user.isActive)
            }
    }
}

@Composable
private fun AccountStatusBadge(isActive: Boolean) {
    // "Active" is a healthy/positive state -> use a positive teal (FleetAvailable),
    // never the error/warning tint. Only the genuine "Inactive" state stays in error.
    val (containerColor, contentColor, icon, labelRes) = if (isActive) {
        AccountStatusBadgeData(
            containerColor = FleetStatusColors.FleetAvailable.copy(alpha = 0.15f),
            contentColor = FleetStatusColors.FleetAvailable,
            icon = Res.drawable.ic_check_circle,
            labelRes = Res.string.profile_status_active
        )
    } else {
        AccountStatusBadgeData(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.error,
            icon = Res.drawable.ic_warning,
            labelRes = Res.string.profile_status_inactive
        )
    }
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(FleetTokens.IconSize.S)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(labelRes),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}

private data class AccountStatusBadgeData(
    val containerColor: androidx.compose.ui.graphics.Color,
    val contentColor: androidx.compose.ui.graphics.Color,
    val icon: org.jetbrains.compose.resources.DrawableResource,
    val labelRes: org.jetbrains.compose.resources.StringResource
)

@Composable
private fun RoleBadge(role: UserRole) {
    val (containerColor, contentColor, icon) = when (role) {
        UserRole.OWNER -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            Res.drawable.ic_profile
        )
        UserRole.GENERAL_MANAGER -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary,
            Res.drawable.ic_team
        )
        UserRole.MANAGER -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary,
            Res.drawable.ic_team
        )
        UserRole.SUPERVISOR -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Res.drawable.ic_visibility
        )
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(FleetTokens.IconSize.S)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(
                    when (role) {
                        UserRole.OWNER -> Res.string.role_owner
                        UserRole.GENERAL_MANAGER -> Res.string.role_general_manager
                        UserRole.MANAGER -> Res.string.role_manager
                        UserRole.SUPERVISOR -> Res.string.role_supervisor
                    }
                ),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = contentColor
            )
        }
    }
}


@Composable
private fun ProfileDetailsCard(user: User) {
    EnhancedProfileCard(
        title = stringResource(Res.string.profile_contact_info),
        icon = Res.drawable.ic_edit
    ) {
        EnhancedProfileRow(
            icon = Res.drawable.ic_email,
            label = stringResource(Res.string.profile_email),
            value = user.email.ifBlank { "—" }
        )
        EnhancedProfileRow(
            icon = Res.drawable.ic_phone,
            label = stringResource(Res.string.profile_mobile),
            value = formatMobile(user.mobile)
        )
        EnhancedProfileRow(
            icon = Res.drawable.ic_profile,
            label = stringResource(Res.string.profile_user_id),
            value = user.id.ifBlank { "—" }
        )
        EnhancedProfileRow(
            icon = Res.drawable.ic_calendar,
            label = stringResource(Res.string.profile_member_since),
            value = formatDate(user.createdAt)
        )
        EnhancedProfileRow(
            icon = Res.drawable.ic_refresh,
            label = stringResource(Res.string.profile_last_updated),
            value = formatDate(user.updatedAt),
            isLast = true
        )
    }
}

@Composable
private fun EnhancedProfileCard(
    title: String,
    icon: org.jetbrains.compose.resources.DrawableResource,
    content: @Composable ColumnScope.() -> Unit
) {
    // Delegates to the shared section card (promoted to ijs-ui-components-lib).
    com.indusjs.uicomponents.components.FleetTitledSectionCard(
        title = title,
        iconRes = icon,
        content = content
    )
}

@Composable
private fun EnhancedProfileRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    value: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.45f)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(FleetTokens.IconSize.M)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.55f)
        )
    }
    if (!isLast) {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
    }
}


@Composable
private fun OrganizationStatsCard(stats: OrganizationStats) {
    EnhancedProfileCard(
        title = stringResource(Res.string.profile_organization_overview),
        icon = Res.drawable.ic_dashboard
    ) {
        // Team section header
        SectionLabel(
            text = stringResource(Res.string.profile_org_team),
            icon = Res.drawable.ic_team
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedStatItem(
                icon = Res.drawable.ic_team,
                value = stats.totalManagers.toString(),
                label = stringResource(Res.string.profile_stats_managers),
                color = MaterialTheme.colorScheme.secondary
            )
            EnhancedStatItem(
                icon = Res.drawable.ic_visibility,
                value = stats.totalSupervisors.toString(),
                label = stringResource(Res.string.profile_stats_supervisors),
                // Neutral count — use a calm accent (purple), not the theme's orange
                // tertiary which reads as a warning for a plain "0".
                color = FleetStatusColors.AccentPurple
            )
            EnhancedStatItem(
                icon = Res.drawable.ic_profile,
                value = stats.totalTeamMembers.toString(),
                label = stringResource(Res.string.profile_stats_team_members),
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Fleet section header
        SectionLabel(
            text = stringResource(Res.string.profile_org_fleet),
            icon = Res.drawable.ic_truck
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedStatItemWithIcon(
                iconRes = Res.drawable.ic_truck,
                value = stats.totalVehicles.toString(),
                label = stringResource(Res.string.profile_stats_total_vehicles),
                color = MaterialTheme.colorScheme.primary
            )
            // "Active Vehicles" is a healthy/positive metric -> positive teal, not amber/error.
            EnhancedStatItem(
                icon = Res.drawable.ic_check_circle,
                value = stats.activeVehicles.toString(),
                label = stringResource(Res.string.profile_stats_active_vehicles),
                color = FleetStatusColors.FleetAvailable
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Trips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedStatItem(
                icon = Res.drawable.ic_trip,
                value = stats.totalTrips.toString(),
                label = stringResource(Res.string.profile_stats_total_trips),
                color = MaterialTheme.colorScheme.primary
            )
            // "Active Trips" is a healthy/positive metric -> positive green, not amber/error.
            EnhancedStatItem(
                icon = Res.drawable.ic_trip,
                value = stats.activeTrips.toString(),
                label = stringResource(Res.string.profile_stats_active_trips),
                color = FleetStatusColors.FleetOnRoute
            )
            EnhancedStatItem(
                icon = Res.drawable.ic_check_circle,
                value = stats.completedTrips.toString(),
                label = stringResource(Res.string.profile_stats_completed_trips),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, icon: org.jetbrains.compose.resources.DrawableResource) {
    // Delegates to the shared section header (promoted to ijs-ui-components-lib).
    com.indusjs.uicomponents.components.FleetSectionHeader(
        title = text,
        iconRes = icon
    )
}

@Composable
private fun EnhancedStatItem(
    icon: org.jetbrains.compose.resources.DrawableResource,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    // Transparent, centered stat tile (no tinted box) -> showBackground=false, centered=true.
    com.indusjs.uicomponents.components.FleetMetricTile(
        value = value,
        label = label,
        iconRes = icon,
        accent = color,
        valueColor = color,
        showBackground = false,
        centered = true,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun EnhancedStatItemWithIcon(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    // Transparent, centered stat tile (no tinted box) -> showBackground=false, centered=true.
    com.indusjs.uicomponents.components.FleetMetricTile(
        value = value,
        label = label,
        iconRes = iconRes,
        accent = color,
        valueColor = color,
        showBackground = false,
        centered = true,
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
private fun OwnerInfoCard(ownerInfo: OwnerInfo) {
    EnhancedProfileCard(
        title = stringResource(Res.string.profile_org_owner),
        icon = Res.drawable.ic_profile
    ) {
        EnhancedProfileRow(icon = Res.drawable.ic_profile, label = stringResource(Res.string.profile_owner_name), value = ownerInfo.ownerName)
        EnhancedProfileRow(
            icon = Res.drawable.ic_email,
            label = stringResource(Res.string.profile_owner_email),
            value = ownerInfo.ownerEmail,
            isLast = true
        )
    }
}

@Composable
private fun ActionButtonsCard(
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    EnhancedProfileCard(
        title = stringResource(Res.string.profile_account_actions),
        icon = Res.drawable.ic_settings
    ) {
        // Edit Profile Button
        com.indusjs.uicomponents.components.FleetButton(
            text = stringResource(Res.string.profile_edit),
            onClick = onEditProfile,
            variant = com.indusjs.uicomponents.components.ButtonVariant.PRIMARY,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_edit),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.M)
                )
            }
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

        // Change Password Button
        com.indusjs.uicomponents.components.FleetButton(
            text = stringResource(Res.string.profile_change_password),
            onClick = onChangePassword,
            variant = com.indusjs.uicomponents.components.ButtonVariant.SECONDARY,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.M)
                )
            }
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = FleetTokens.Height.Divider
        )
        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        // Logout Button (destructive)
        com.indusjs.uicomponents.components.FleetButton(
            text = stringResource(Res.string.profile_logout),
            onClick = onLogout,
            variant = com.indusjs.uicomponents.components.ButtonVariant.DESTRUCTIVE,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_logout),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.M)
                )
            }
        )
    }
}

@Composable
private fun EditProfileContent(
    state: ProfileContract.State,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val isUpdating = state.isUpdating
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val bp = rememberFleetBreakpoint()
        // Compact = full-width; Medium/Expanded = centered, capped column so the
        // form doesn't stretch edge-to-edge on tablet / web.
        val formWidthModifier = if (bp == FleetBreakpoint.Compact) {
            Modifier.fillMaxWidth()
        } else {
            Modifier.widthIn(max = FORM_MAX_WIDTH)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(FleetTokens.Spacing.L),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
        ) {
            // Header
            item {
                // Solid tonal hero card: full primaryContainer fill (flat, borderless),
                // inner text uses onPrimaryContainer for contrast. The centered hero layout
                // (chip above a centered title/subtitle) can't be expressed by
                // FleetSectionHeader's left-aligned row, so the inner centered content stays
                // bespoke.
                com.indusjs.uicomponents.components.FleetSectionCard(
                    modifier = formWidthModifier,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    border = null,
                    elevation = FleetTokens.Elevation.None,
                    contentPadding = FleetTokens.Spacing.XL
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(FleetTokens.IconSize.XL + FleetTokens.IconSize.S),
                            shape = RoundedCornerShape(FleetTokens.Radius.XL),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_edit),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(FleetTokens.IconSize.Default)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                        Text(
                            text = stringResource(Res.string.profile_edit_heading),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                        Text(
                            text = stringResource(Res.string.profile_edit_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            // Form Card
            item {
                com.indusjs.uicomponents.components.FleetTitledSectionCard(
                    title = stringResource(Res.string.profile_personal_details),
                    iconRes = Res.drawable.ic_edit,
                    modifier = formWidthModifier
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                    ) {
                        com.indusjs.uicomponents.components.FleetInputField(
                            value = state.editFirstName,
                            onValueChange = onFirstNameChange,
                            fieldType = com.indusjs.uicomponents.components.FieldType.DEFAULT,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.profile_first_name),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_profile),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            },
                            isError = state.editFirstNameError != null,
                            errorMessage = state.editFirstNameError?.resolve(),
                            enabled = !isUpdating
                        )

                        com.indusjs.uicomponents.components.FleetInputField(
                            value = state.editLastName,
                            onValueChange = onLastNameChange,
                            fieldType = com.indusjs.uicomponents.components.FieldType.DEFAULT,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.profile_last_name),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_profile),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            },
                            isError = state.editLastNameError != null,
                            errorMessage = state.editLastNameError?.resolve(),
                            enabled = !isUpdating
                        )

                        com.indusjs.uicomponents.components.FleetInputField(
                            value = state.editEmail,
                            onValueChange = onEmailChange,
                            fieldType = com.indusjs.uicomponents.components.FieldType.EMAIL,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.profile_email),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_email),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            },
                            isError = state.editEmailError != null,
                            errorMessage = state.editEmailError?.resolve(),
                            enabled = !isUpdating
                        )

                        com.indusjs.uicomponents.components.FleetInputField(
                            value = state.editMobile,
                            onValueChange = { onMobileChange(com.indusjs.uicomponents.components.filterDigitsOnly(it, 10)) },
                            fieldType = com.indusjs.uicomponents.components.FieldType.PHONE,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.profile_mobile),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_phone),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(FleetTokens.IconSize.M)
                                )
                            },
                            isError = state.editMobileError != null,
                            errorMessage = state.editMobileError?.resolve(),
                            enabled = !isUpdating
                        )

                        state.updateError?.let {
                            com.indusjs.uicomponents.components.FleetInlineErrorBanner(
                                message = it.resolve()
                            )
                        }
                    }
                }
            }

            // Save Button
            item {
                com.indusjs.uicomponents.components.FleetButton(
                    text = stringResource(Res.string.profile_save),
                    onClick = onSave,
                    variant = com.indusjs.uicomponents.components.ButtonVariant.PRIMARY,
                    size = com.indusjs.uicomponents.components.ButtonSize.LARGE,
                    modifier = formWidthModifier,
                    enabled = state.isEditFormValid,
                    isLoading = isUpdating,
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check_circle),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.M)
                        )
                    }
                )
            }
        }
    }
}

/** Centered form cap on Medium/Expanded so the edit form never stretches edge-to-edge. */
private val FORM_MAX_WIDTH = 480.dp

private fun formatDate(isoDate: String): String {
    if (isoDate.isBlank()) return "—"
    // Canonical app-wide date display (ISO/epoch -> "DD-MMM-YYYY").
    return com.indusjs.fleet.core.util.formatDateToHumanReadable(isoDate).ifBlank { "—" }
}

/**
 * Display-friendly mobile number. Keeps a leading + and country code when present,
 * inserting a space after the country code for readability (e.g. "+91 9876543210").
 */
private fun formatMobile(raw: String): String {
    if (raw.isBlank()) return "—"
    val trimmed = raw.trim()
    return when {
        trimmed.startsWith("+") && trimmed.length > 3 -> {
            // Assume up to 3-char country code (covers +91, +1, etc.)
            val ccEnd = (1..3).firstOrNull { trimmed.length > it && !trimmed[it].isDigit() }
                ?: minOf(3, trimmed.length - 1)
            val cc = trimmed.substring(0, ccEnd + 1).trimEnd()
            val rest = trimmed.substring(ccEnd + 1)
            if (rest.isNotEmpty()) "$cc $rest" else trimmed
        }
        else -> trimmed
    }
}

