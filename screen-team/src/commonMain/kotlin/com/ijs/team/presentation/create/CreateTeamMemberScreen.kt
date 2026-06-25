package com.ijs.team.presentation.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.util.ValidationUtils
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetPasswordField
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.team.domain.entity.AssignableTeamRole
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

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
    onNavigateBack: () -> Unit = {},
    onTeamMemberCreated: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    // Apply excludeGeneralManager filter on first composition
    LaunchedEffect(excludeGeneralManager) {
        if (excludeGeneralManager) {
            viewModel.sendIntent(CreateTeamMemberContract.Intent.SetExcludeGeneralManager(exclude = true))
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CreateTeamMemberContract.Effect.ShowSnackbar -> {
                    pendingSnackbar = effect.message
                }
                is CreateTeamMemberContract.Effect.TeamMemberCreated -> {
                    onTeamMemberCreated()
                }
                is CreateTeamMemberContract.Effect.NavigateBack -> {
                    onNavigateBack()
                }
            }
        }
    }

    // ---- Inline field validation (single source of truth: ValidationUtils) ----
    // Errors surface only after the user has typed something; "required" gating is
    // handled by [allValid] below so the submit button stays disabled until clean.
    val firstNameError = state.firstName.takeIf { it.isNotEmpty() }
        ?.let { ValidationUtils.getNameError(it, "First name", required = false) }
    val lastNameError = state.lastName.takeIf { it.isNotEmpty() }
        ?.let { ValidationUtils.getNameError(it, "Last name", required = false) }
    val emailError = state.email.takeIf { it.isNotEmpty() }
        ?.let { ValidationUtils.getEmailError(it, required = false) }
    val mobileError = state.mobile.takeIf { it.isNotEmpty() }
        ?.let { ValidationUtils.getMobileError(it, required = false) }
    val confirmMismatch = state.confirmPassword.isNotEmpty() && state.confirmPassword != state.password

    val allValid = ValidationUtils.isValidName(state.firstName) &&
        ValidationUtils.isValidName(state.lastName) &&
        ValidationUtils.isValidEmail(state.email.trim()) &&
        ValidationUtils.isValidIndianMobile(state.mobile) &&
        state.password.isNotEmpty() &&
        state.confirmPassword.isNotEmpty() &&
        state.password == state.confirmPassword &&
        state.selectedIamRoleName.isNotBlank()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.team_add)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = FleetTokens.Spacing.ScreenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = formWidthModifier,
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.L)
                ) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                    // Role Selection — IAM roles from API (admin, user)
                    SectionCard(
                        title = stringResource(Res.string.team_select_role_title),
                        subtitle = stringResource(Res.string.team_select_role_subtitle)
                    ) {
                        if (state.rolesLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = FleetTokens.Spacing.XL),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(FleetTokens.IconSize.L))
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)) {
                                state.availableIamRoles.forEach { role ->
                                    val (title, iconRes, description) = iamRoleCardContent(role)
                                    RoleSelectionCard(
                                        title = title,
                                        iconRes = iconRes,
                                        description = description,
                                        isSelected = state.selectedIamRoleName == role.name,
                                        onClick = {
                                            viewModel.sendIntent(CreateTeamMemberContract.Intent.SelectIamRole(role.name))
                                        },
                                        enabled = !state.isLoading
                                    )
                                }
                            }
                        }
                    }

                    // Personal Information Section
                    SectionCard(
                        title = stringResource(Res.string.team_section_personal),
                        subtitle = stringResource(Res.string.team_section_member_details_subtitle)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                        ) {
                            // First Name and Last Name Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                            ) {
                                FleetInputField(
                                    value = state.firstName,
                                    onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateFirstName(it)) },
                                    fieldType = FieldType.DEFAULT,
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(Res.string.team_label_first_name),
                                    placeholder = stringResource(Res.string.team_placeholder_first_name),
                                    isError = firstNameError != null,
                                    errorMessage = firstNameError,
                                    enabled = !state.isLoading
                                )

                                FleetInputField(
                                    value = state.lastName,
                                    onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateLastName(it)) },
                                    fieldType = FieldType.DEFAULT,
                                    modifier = Modifier.weight(1f),
                                    label = stringResource(Res.string.team_label_last_name),
                                    placeholder = stringResource(Res.string.team_placeholder_last_name),
                                    isError = lastNameError != null,
                                    errorMessage = lastNameError,
                                    enabled = !state.isLoading
                                )
                            }

                            // Email Field
                            FleetInputField(
                                value = state.email,
                                onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateEmail(it)) },
                                fieldType = FieldType.EMAIL,
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.team_label_email),
                                placeholder = stringResource(Res.string.team_placeholder_email),
                                isError = emailError != null,
                                errorMessage = emailError,
                                enabled = !state.isLoading
                            )

                            // Mobile Field
                            FleetInputField(
                                value = state.mobile,
                                onValueChange = {
                                    viewModel.sendIntent(
                                        CreateTeamMemberContract.Intent.UpdateMobile(filterDigitsOnly(it, 10))
                                    )
                                },
                                fieldType = FieldType.PHONE,
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.team_label_mobile),
                                placeholder = stringResource(Res.string.team_placeholder_mobile),
                                isError = mobileError != null,
                                errorMessage = mobileError,
                                enabled = !state.isLoading
                            )
                        }
                    }

                    // Password Section
                    SectionCard(
                        title = stringResource(Res.string.team_section_password),
                        subtitle = stringResource(Res.string.team_section_password_hint)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                        ) {
                            // Password Field (policy enforced by the backend; no client min-length hint)
                            FleetPasswordField(
                                value = state.password,
                                onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdatePassword(it)) },
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.team_label_password),
                                placeholder = stringResource(Res.string.team_placeholder_password),
                                enabled = !state.isLoading
                            )

                            // Confirm Password Field (UI-only match check)
                            FleetPasswordField(
                                value = state.confirmPassword,
                                onValueChange = { viewModel.sendIntent(CreateTeamMemberContract.Intent.UpdateConfirmPassword(it)) },
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(Res.string.team_label_confirm_password),
                                placeholder = stringResource(Res.string.team_placeholder_confirm_password),
                                isError = confirmMismatch,
                                errorMessage = if (confirmMismatch) {
                                    stringResource(Res.string.error_passwords_mismatch)
                                } else null,
                                enabled = !state.isLoading,
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (allValid && !state.isLoading) {
                                            viewModel.sendIntent(CreateTeamMemberContract.Intent.CreateTeamMember)
                                        }
                                    }
                                )
                            )
                        }
                    }

                    // Error Message
                    state.error?.let { error ->
                        FleetSectionCard(
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            border = null
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                            ) {
                                Text(
                                    text = error.resolve(),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // Create Button
                    val roleTitle = iamRoleTitle(state.selectedIamRoleName)
                    FleetButton(
                        text = stringResource(Res.string.team_create_button, roleTitle),
                        onClick = { viewModel.sendIntent(CreateTeamMemberContract.Intent.CreateTeamMember) },
                        size = ButtonSize.LARGE,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = allValid && !state.isLoading,
                        isLoading = state.isLoading
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
                }
            }
        }
    }
}

@Composable
private fun iamRoleTitle(roleName: String): String = when (roleName.lowercase()) {
    "admin" -> stringResource(Res.string.team_iam_role_admin_title)
    "user" -> stringResource(Res.string.team_iam_role_user_title)
    else -> roleName.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

@Composable
private fun iamRoleDescription(roleName: String, apiDescription: String): String =
    apiDescription.ifBlank {
        when (roleName.lowercase()) {
            "admin" -> stringResource(Res.string.team_iam_role_admin_desc)
            "user" -> stringResource(Res.string.team_iam_role_user_desc)
            else -> ""
        }
    }

private fun iamRoleIcon(roleName: String): DrawableResource = when (roleName.lowercase()) {
    "admin" -> Res.drawable.ic_visibility
    "user" -> Res.drawable.ic_profile
    else -> Res.drawable.ic_profile
}

@Composable
private fun iamRoleCardContent(role: AssignableTeamRole): Triple<String, DrawableResource, String> = Triple(
    iamRoleTitle(role.name),
    iamRoleIcon(role.name),
    iamRoleDescription(role.name, role.description)
)

/**
 * Section card wrapper with title and subtitle.
 */
@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetTitledSectionCard(
        title = title,
        subtitle = subtitle,
        content = content
    )
}

/**
 * Enhanced role selection card with icon on left and title/description on right.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RoleSelectionCard(
    title: String,
    iconRes: DrawableResource,
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
        shape = RoundedCornerShape(FleetTokens.Radius.ML),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            }
        ),
        border = if (isSelected) {
            BorderStroke(FleetTokens.Height.Connector, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(FleetTokens.Height.Divider, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FleetTokens.Spacing.M),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
        ) {
            // Icon on left
            Surface(
                modifier = Modifier.size(FleetTokens.IconSize.XL),
                shape = RoundedCornerShape(FleetTokens.Radius.ML),
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
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            }

            // Title and description on right
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.XXS)
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
                    modifier = Modifier.size(FleetTokens.IconSize.Default),
                    shape = RoundedCornerShape(FleetTokens.Radius.L),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(FleetTokens.IconSize.S)
                        )
                    }
                }
            }
        }
    }
}
