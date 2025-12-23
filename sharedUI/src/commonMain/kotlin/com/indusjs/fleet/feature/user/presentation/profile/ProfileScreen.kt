package com.indusjs.fleet.feature.user.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.feature.user.domain.entity.OrganizationStats
import com.indusjs.fleet.feature.user.domain.entity.OwnerInfo
import com.indusjs.fleet.feature.user.domain.entity.User
import com.indusjs.fleet.feature.user.domain.entity.UserRole
import kotlinx.coroutines.flow.collectLatest

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

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ProfileContract.Effect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(effect.message)
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
                title = { Text("My Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                actions = {
                    if (state.isEditing) {
                        TextButton(onClick = { viewModel.sendIntent(ProfileContract.Intent.CancelEditing) }) {
                            Text("Cancel")
                        }
                    } else {
                        IconButton(onClick = { viewModel.sendIntent(ProfileContract.Intent.RefreshProfile) }) {
                            Text("↻", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
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
                    ErrorContent(
                        error = state.error!!,
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
                            onLogout = { viewModel.sendIntent(ProfileContract.Intent.Logout) }
                        )
                    }
                }
            }
        }
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${user.firstName.firstOrNull() ?: ""}${user.lastName.firstOrNull() ?: ""}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user.fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Role Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = when (user.role) {
                UserRole.OWNER -> MaterialTheme.colorScheme.primaryContainer
                UserRole.MANAGER -> MaterialTheme.colorScheme.secondaryContainer
                UserRole.SUPERVISOR -> MaterialTheme.colorScheme.tertiaryContainer
            }
        ) {
            Text(
                text = user.role.name.lowercase().replaceFirstChar { it.uppercase() },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = when (user.role) {
                    UserRole.OWNER -> MaterialTheme.colorScheme.onPrimaryContainer
                    UserRole.MANAGER -> MaterialTheme.colorScheme.onSecondaryContainer
                    UserRole.SUPERVISOR -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }
    }
}

@Composable
private fun ProfileDetailsCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Contact Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            ProfileDetailRow(
                icon = "📧",
                label = "Email",
                value = user.email
            )

            ProfileDetailRow(
                icon = "📱",
                label = "Mobile",
                value = user.mobile
            )

            ProfileDetailRow(
                icon = "📅",
                label = "Member Since",
                value = formatDate(user.createdAt)
            )
        }
    }
}

@Composable
private fun ProfileDetailRow(
    icon: String,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun OrganizationStatsCard(stats: OrganizationStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Organization Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = stats.totalManagers.toString(), label = "Managers")
                StatItem(value = stats.totalSupervisors.toString(), label = "Supervisors")
                StatItem(value = stats.totalDrivers.toString(), label = "Drivers")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(value = stats.totalVehicles.toString(), label = "Vehicles")
                StatItem(value = stats.activeTrips.toString(), label = "Active Trips")
            }
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun OwnerInfoCard(ownerInfo: OwnerInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Organization Owner",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            ProfileDetailRow(
                icon = "👤",
                label = "Owner Name",
                value = ownerInfo.ownerName
            )

            ProfileDetailRow(
                icon = "📧",
                label = "Owner Email",
                value = ownerInfo.ownerEmail
            )
        }
    }
}

@Composable
private fun ActionButtonsCard(
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Account Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            Button(
                onClick = onEditProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("✏️  Edit Profile")
            }

            OutlinedButton(
                onClick = onChangePassword,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🔑  Change Password")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("🚪  Logout")
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
    error: String?,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onMobileChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Edit Profile",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = onFirstNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("First Name") },
            singleLine = true,
            enabled = !isUpdating
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = onLastNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Last Name") },
            singleLine = true,
            enabled = !isUpdating
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            enabled = !isUpdating
        )

        OutlinedTextField(
            value = mobile,
            onValueChange = onMobileChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mobile") },
            singleLine = true,
            enabled = !isUpdating
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUpdating
        ) {
            if (isUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Save Changes")
            }
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "😕",
            style = MaterialTheme.typography.displayMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun formatDate(isoDate: String): String {
    // Simple date formatting - in production use kotlinx-datetime
    return try {
        val datePart = isoDate.split("T").firstOrNull() ?: isoDate
        datePart
    } catch (_: Exception) {
        isoDate
    }
}

