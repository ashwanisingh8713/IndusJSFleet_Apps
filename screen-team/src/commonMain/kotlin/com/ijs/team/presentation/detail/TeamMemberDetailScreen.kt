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
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.fleet.core.util.formatDateToHumanReadable
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetFilterChip
import com.indusjs.uicomponents.components.FleetAvatar
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.components.ClickablePhoneRow
import com.ijs.team.presentation.localizedDisplayName
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.team.domain.entity.TeamMember
import com.ijs.team.domain.entity.TeamMemberRole
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Load member on first composition
    LaunchedEffect(memberId) {
        viewModel.sendIntent(TeamMemberDetailContract.Intent.LoadMember(memberId))
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is TeamMemberDetailContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
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
                        if (state.isEditMode) {
                            stringResource(Res.string.team_edit_member)
                        } else {
                            stringResource(Res.string.team_detail)
                        },
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
                            contentDescription = if (state.isEditMode) {
                                stringResource(Res.string.cancel)
                            } else {
                                stringResource(Res.string.back)
                            },
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
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
                                contentDescription = stringResource(Res.string.edit),
                                tint = MaterialTheme.colorScheme.primary,
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
        when {
            state.isLoading -> {
                LoadingContent(message = stringResource(Res.string.team_loading_member))
            }
            state.error != null -> {
                ErrorContent(
                    error = state.error?.resolve() ?: stringResource(Res.string.error_generic),
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

@Composable
private fun ViewMemberContent(
    member: TeamMember,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val breakpoint = rememberFleetBreakpoint()
        // Compact = full-width phone content; Medium/Expanded = centered, constrained
        // column so the content doesn't stretch edge-to-edge on tablet / web.
        val contentWidthModifier = when (breakpoint) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FleetTokens.Spacing.L),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = contentWidthModifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Avatar
                val roleColor = when (member.role) {
                    TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary
                    TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary
                    TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary
                }
                val onRoleColor = when (member.role) {
                    TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.onTertiary
                    TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.onPrimary
                    TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.onSecondary
                }
                FleetAvatar(
                    name = member.fullName,
                    size = FleetTokens.IconSize.XXL,
                    background = roleColor,
                    contentColor = onRoleColor,
                    textStyle = MaterialTheme.typography.headlineLarge
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                Text(
                    text = member.fullName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                // Role Badge
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = when (member.role) {
                        TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                        TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                    }
                ) {
                    Text(
                        text = member.role.localizedDisplayName(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium,
                        color = when (member.role) {
                            TeamMemberRole.GENERAL_MANAGER -> MaterialTheme.colorScheme.tertiary
                            TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary
                            TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary
                        },
                        modifier = Modifier.padding(
                            horizontal = FleetTokens.Spacing.L,
                            vertical = FleetTokens.Spacing.S
                        )
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(FleetTokens.Radius.M),
                    color = if (member.isActive) {
                        FleetStatusColors.FleetOnRoute.copy(alpha = 0.15f)
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(
                            horizontal = FleetTokens.Spacing.M,
                            vertical = FleetTokens.Spacing.XS
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .size(FleetTokens.Spacing.S)
                                .clip(CircleShape)
                                .background(
                                    if (member.isActive) FleetStatusColors.FleetOnRoute
                                    else MaterialTheme.colorScheme.error
                                )
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
                        Text(
                            text = if (member.isActive) {
                                stringResource(Res.string.team_status_active)
                            } else {
                                stringResource(Res.string.team_status_inactive)
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (member.isActive) FleetStatusColors.FleetOnRoute else MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL))

                // Contact Information Card
                FleetTitledSectionCard(
                    title = stringResource(Res.string.team_section_contact_information)
                ) {
                    DetailRow(
                        iconRes = Res.drawable.ic_email,
                        label = stringResource(Res.string.team_label_email),
                        value = member.email
                    )

                    // Mobile with call icon
                    ClickablePhoneRow(
                        phoneNumber = member.mobile,
                        label = stringResource(Res.string.team_label_mobile)
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                // Additional Information Card
                FleetTitledSectionCard(
                    title = stringResource(Res.string.team_section_additional_info)
                ) {
                    DetailRow(
                        iconRes = Res.drawable.ic_profile,
                        label = stringResource(Res.string.team_label_member_id),
                        value = member.id
                    )
                    DetailRow(
                        iconRes = Res.drawable.ic_calendar,
                        label = stringResource(Res.string.team_label_created_on),
                        value = formatDateToHumanReadable(member.createdAt).ifBlank { "—" }
                    )
                }
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
    // ---- Inline field validation (single source of truth: ValidationUtils) ----
    // Errors surface only after the user has typed something; "required" gating is
    // handled by [allValid] below so the Save button stays disabled until clean.
    // Prefer any server-side error already on state; fall back to live inline validation.
    val firstNameError = state.firstNameError?.resolve()
        ?: state.editFirstName.takeIf { it.isNotEmpty() }
            ?.let { ValidationUtils.getNameError(it, "First name", required = false) }
    val lastNameError = state.lastNameError?.resolve()
        ?: state.editLastName.takeIf { it.isNotEmpty() }
            ?.let { ValidationUtils.getNameError(it, "Last name", required = false) }
    val emailError = state.emailError?.resolve()
        ?: state.editEmail.takeIf { it.isNotEmpty() }
            ?.let { ValidationUtils.getEmailError(it, required = false) }
    val mobileError = state.mobileError?.resolve()
        ?: state.editMobile.takeIf { it.isNotEmpty() }
            ?.let { ValidationUtils.getMobileError(it, required = false) }

    val allValid = ValidationUtils.isValidName(state.editFirstName) &&
        ValidationUtils.isValidName(state.editLastName) &&
        ValidationUtils.isValidEmail(state.editEmail.trim()) &&
        ValidationUtils.isValidIndianMobile(state.editMobile)

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val breakpoint = rememberFleetBreakpoint()
        // Compact = full-width phone form; Medium/Expanded = centered, constrained
        // column so the form doesn't stretch edge-to-edge on tablet / web.
        val formWidthModifier = when (breakpoint) {
            FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
            else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FleetTokens.Spacing.L),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = formWidthModifier,
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
            ) {
                // Personal Information Section
                FleetTitledSectionCard(
                    title = stringResource(Res.string.team_section_personal_information)
                ) {
                    FleetInputField(
                        value = state.editFirstName,
                        onValueChange = { viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateFirstName(it)) },
                        fieldType = FieldType.DEFAULT,
                        label = stringResource(Res.string.team_label_first_name),
                        placeholder = stringResource(Res.string.team_placeholder_first_name),
                        isError = firstNameError != null,
                        errorMessage = firstNameError,
                        enabled = !state.isSaving
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                    FleetInputField(
                        value = state.editLastName,
                        onValueChange = { viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateLastName(it)) },
                        fieldType = FieldType.DEFAULT,
                        label = stringResource(Res.string.team_label_last_name),
                        placeholder = stringResource(Res.string.team_placeholder_last_name),
                        isError = lastNameError != null,
                        errorMessage = lastNameError,
                        enabled = !state.isSaving
                    )
                }

                // Contact Information Section
                FleetTitledSectionCard(
                    title = stringResource(Res.string.team_section_contact_information)
                ) {
                    FleetInputField(
                        value = state.editEmail,
                        onValueChange = { viewModel.sendIntent(TeamMemberDetailContract.Intent.UpdateEmail(it)) },
                        fieldType = FieldType.EMAIL,
                        label = stringResource(Res.string.team_label_email),
                        placeholder = stringResource(Res.string.team_placeholder_email),
                        isError = emailError != null,
                        errorMessage = emailError,
                        enabled = !state.isSaving
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))

                    FleetInputField(
                        value = state.editMobile,
                        onValueChange = {
                            viewModel.sendIntent(
                                TeamMemberDetailContract.Intent.UpdateMobile(filterDigitsOnly(it, 10))
                            )
                        },
                        fieldType = FieldType.PHONE,
                        label = stringResource(Res.string.team_label_mobile),
                        placeholder = stringResource(Res.string.team_placeholder_mobile),
                        isError = mobileError != null,
                        errorMessage = mobileError,
                        enabled = !state.isSaving
                    )
                }

                // Role & Status Section
                FleetTitledSectionCard(
                    title = stringResource(Res.string.team_section_role_status)
                ) {
                    // Role Selection (based on permissions and not editing self)
                    if (state.canChangeRole && state.availableRoles.isNotEmpty() && !state.isSelf) {
                        Text(
                            text = stringResource(Res.string.team_label_role),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                        // Use Column for 3+ roles, Row for 2
                        if (state.availableRoles.size >= 3) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
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
                                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
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
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                    }

                    RolePermissionPreview(role = state.editRole)
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    // Active Status Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(Res.string.team_label_active_status),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (state.editIsActive) {
                                    stringResource(Res.string.team_access_enabled_hint)
                                } else {
                                    stringResource(Res.string.team_access_disabled_hint)
                                },
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

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                // Save Button
                FleetButton(
                    text = stringResource(Res.string.team_save_changes),
                    onClick = { viewModel.sendIntent(TeamMemberDetailContract.Intent.SaveChanges) },
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = allValid && !state.isSaving,
                    isLoading = state.isSaving
                )

                // Cancel Button
                FleetButton(
                    text = stringResource(Res.string.cancel),
                    onClick = { viewModel.sendIntent(TeamMemberDetailContract.Intent.ExitEditMode) },
                    variant = ButtonVariant.SECONDARY,
                    size = ButtonSize.LARGE,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSaving
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
            }
        }
    }
}

@Composable
private fun DetailRow(
    iconRes: DrawableResource,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FleetTokens.Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(FleetTokens.IconSize.M)
        )
        Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
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
    val iconRes = when (role) {
        TeamMemberRole.GENERAL_MANAGER -> Res.drawable.ic_profile
        TeamMemberRole.MANAGER -> Res.drawable.ic_team
        TeamMemberRole.SUPERVISOR -> Res.drawable.ic_visibility
    }
    val label = role.localizedDisplayName()
    FleetFilterChip(
        selected = isSelected,
        label = label,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        leadingIcon = iconRes,
    )
}

@Composable
private fun RolePermissionPreview(role: TeamMemberRole) {
    val title = when (role) {
        TeamMemberRole.GENERAL_MANAGER -> stringResource(Res.string.team_role_owner)
        TeamMemberRole.MANAGER -> stringResource(Res.string.team_iam_role_admin_title)
        TeamMemberRole.SUPERVISOR -> stringResource(Res.string.team_iam_role_user_title)
    }
    val description = when (role) {
        TeamMemberRole.GENERAL_MANAGER -> stringResource(Res.string.team_iam_role_owner_desc)
        TeamMemberRole.MANAGER -> stringResource(Res.string.team_iam_role_admin_desc)
        TeamMemberRole.SUPERVISOR -> stringResource(Res.string.team_iam_role_user_desc)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(FleetTokens.Radius.L),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    ) {
        Column(
            modifier = Modifier.padding(FleetTokens.Spacing.M),
            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XS)
        ) {
            Text(
                text = stringResource(Res.string.team_role_permissions_title, title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

