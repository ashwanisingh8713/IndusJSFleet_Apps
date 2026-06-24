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
import com.indusjs.uicomponents.theme.isAppInDarkTheme
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
                            firstName = state.editFirstName,
                            lastName = state.editLastName,
                            email = state.editEmail,
                            mobile = state.editMobile,
                            isUpdating = state.isUpdating,
                            error = state.updateError,
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
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text(stringResource(Res.string.logout_confirmation_title)) },
            text = { Text(stringResource(Res.string.logout_confirmation_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmation = false
                        viewModel.sendIntent(ProfileContract.Intent.Logout)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(Res.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
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
    val (containerColor, contentColor, icon, labelRes) = if (isActive) {
        AccountStatusBadgeData(
            containerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.tertiary,
            icon = "✅",
            labelRes = Res.string.profile_status_active
        )
    } else {
        AccountStatusBadgeData(
            containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            contentColor = MaterialTheme.colorScheme.error,
            icon = "⛔",
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
            Text(text = icon, style = MaterialTheme.typography.labelLarge)
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
    val icon: String,
    val labelRes: org.jetbrains.compose.resources.StringResource
)

@Composable
private fun RoleBadge(role: UserRole) {
    val (containerColor, contentColor, icon) = when (role) {
        UserRole.OWNER -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.primary,
            "👑"
        )
        UserRole.GENERAL_MANAGER -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.tertiary,
            "👨‍💼"
        )
        UserRole.MANAGER -> Triple(
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.secondary,
            "💼"
        )
        UserRole.SUPERVISOR -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "👁️"
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
            Text(text = icon, style = MaterialTheme.typography.labelLarge)
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
        icon = "📋"
    ) {
        EnhancedProfileRow(
            icon = "📧",
            label = stringResource(Res.string.profile_email),
            value = user.email.ifBlank { "—" }
        )
        EnhancedProfileRow(
            icon = "📱",
            label = stringResource(Res.string.profile_mobile),
            value = formatMobile(user.mobile)
        )
        EnhancedProfileRow(
            icon = "🆔",
            label = stringResource(Res.string.profile_user_id),
            value = user.id.ifBlank { "—" }
        )
        EnhancedProfileRow(
            icon = "📅",
            label = stringResource(Res.string.profile_member_since),
            value = formatDate(user.createdAt)
        )
        EnhancedProfileRow(
            icon = "🔄",
            label = stringResource(Res.string.profile_last_updated),
            value = formatDate(user.updatedAt),
            isLast = true
        )
    }
}

@Composable
private fun EnhancedProfileCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit
) {
    // Delegates to the shared section card (promoted to ijs-ui-components-lib).
    com.indusjs.uicomponents.components.FleetTitledSectionCard(
        title = title,
        emoji = icon,
        content = content
    )
}

@Composable
private fun EnhancedProfileRow(
    icon: String,
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
            Text(
                text = icon,
                style = MaterialTheme.typography.bodyMedium
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
        icon = "📊"
    ) {
        // Team section header
        SectionLabel(
            text = stringResource(Res.string.profile_org_team),
            icon = "👥"
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnhancedStatItem(
                icon = "💼",
                value = stats.totalManagers.toString(),
                label = stringResource(Res.string.profile_stats_managers),
                color = MaterialTheme.colorScheme.secondary
            )
            EnhancedStatItem(
                icon = "👁️",
                value = stats.totalSupervisors.toString(),
                label = stringResource(Res.string.profile_stats_supervisors),
                color = MaterialTheme.colorScheme.tertiary
            )
            EnhancedStatItem(
                icon = "👨‍✈️",
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
            icon = "🚚"
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
            EnhancedStatItem(
                icon = "🟢",
                value = stats.activeVehicles.toString(),
                label = stringResource(Res.string.profile_stats_active_vehicles),
                color = MaterialTheme.colorScheme.tertiary
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
                icon = "🧭",
                value = stats.totalTrips.toString(),
                label = stringResource(Res.string.profile_stats_total_trips),
                color = MaterialTheme.colorScheme.primary
            )
            EnhancedStatItem(
                icon = "🚀",
                value = stats.activeTrips.toString(),
                label = stringResource(Res.string.profile_stats_active_trips),
                color = MaterialTheme.colorScheme.tertiary
            )
            EnhancedStatItem(
                icon = "✅",
                value = stats.completedTrips.toString(),
                label = stringResource(Res.string.profile_stats_completed_trips),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, icon: String) {
    // Delegates to the shared section header (promoted to ijs-ui-components-lib).
    com.indusjs.uicomponents.components.FleetSectionHeader(
        title = text,
        emoji = icon
    )
}

@Composable
private fun EnhancedStatItem(
    icon: String,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    // Transparent, centered stat tile (no tinted box) -> showBackground=false, centered=true.
    com.indusjs.uicomponents.components.FleetMetricTile(
        value = value,
        label = label,
        emoji = icon,
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
        icon = "👑"
    ) {
        EnhancedProfileRow(icon = "👤", label = stringResource(Res.string.profile_owner_name), value = ownerInfo.ownerName)
        EnhancedProfileRow(
            icon = "📧",
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
        icon = "⚙️"
    ) {
        // Edit Profile Button
        Button(
            onClick = onEditProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "✏️", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.profile_edit),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Change Password Button
        OutlinedButton(
            onClick = onChangePassword,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "🔑", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.profile_change_password),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "🚪", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.profile_logout),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun EditProfileContent(
    firstName: String,
    lastName: String,
    email: String,
    mobile: String,
    isUpdating: Boolean,
    error: com.indusjs.uicomponents.components.UiText?,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onSave: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            // Tinted hero card -> FleetSectionCard with containerColor = the original
            // primaryContainer .3 tint, border = null. The centered hero layout (64dp chip
            // above a centered title/subtitle) can't be expressed by FleetSectionHeader's
            // left-aligned row, so the inner centered content stays bespoke.
            com.indusjs.uicomponents.components.FleetSectionCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = null,
                elevation = 0.dp,
                contentPadding = 20.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(64.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✏️",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.profile_edit_heading),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.profile_edit_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Form Card
        item {
            com.indusjs.uicomponents.components.FleetTitledSectionCard(
                title = stringResource(Res.string.profile_personal_details),
                emoji = "📋",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = onFirstNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.profile_first_name)) },
                        leadingIcon = { Text("👤") },
                        singleLine = true,
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = onLastNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.profile_last_name)) },
                        leadingIcon = { Text("👤") },
                        singleLine = true,
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.profile_email)) },
                        leadingIcon = { Text("📧") },
                        singleLine = true,
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = onMobileChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(Res.string.profile_mobile)) },
                        leadingIcon = { Text("📱") },
                        singleLine = true,
                        enabled = !isUpdating,
                        shape = RoundedCornerShape(12.dp)
                    )

                    error?.let {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it.resolve(),
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        // Save Button
        item {
            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isUpdating,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("💾", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.profile_save),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

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

