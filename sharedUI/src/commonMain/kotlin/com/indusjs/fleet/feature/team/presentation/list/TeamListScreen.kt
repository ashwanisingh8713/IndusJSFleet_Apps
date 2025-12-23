package com.indusjs.fleet.feature.team.presentation.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.feature.team.domain.entity.TeamMember
import com.indusjs.fleet.feature.team.domain.entity.TeamMemberRole
import kotlinx.coroutines.flow.collectLatest

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
                title = { Text("Team Members") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendIntent(TeamListContract.Intent.NavigateToCreateMember) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("+", style = MaterialTheme.typography.headlineMedium)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    title = "Managers",
                    count = state.managersCount,
                    emoji = "👔",
                    modifier = Modifier.weight(1f)
                )

                StatsCard(
                    title = "Supervisors",
                    count = state.supervisorsCount,
                    emoji = "👷",
                    modifier = Modifier.weight(1f)
                )
            }

            // Filter Tabs
            FilterTabs(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { viewModel.sendIntent(TeamListContract.Intent.SelectFilter(it)) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Search Field
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.sendIntent(TeamListContract.Intent.UpdateSearchQuery(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search team members...") },
                leadingIcon = { Text("🔍") },
                singleLine = true
            )

            // Content
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "😕",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = state.error ?: "Something went wrong",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.sendIntent(TeamListContract.Intent.LoadTeamMembers) }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                state.filteredMembers.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "👥",
                                style = MaterialTheme.typography.displayMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (state.searchQuery.isNotEmpty()) {
                                    "No team members found matching \"${state.searchQuery}\""
                                } else {
                                    "No team members yet"
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.sendIntent(TeamListContract.Intent.NavigateToCreateMember) }
                            ) {
                                Text("Add Team Member")
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.filteredMembers, key = { it.id }) { member ->
                            TeamMemberCard(
                                member = member,
                                onClick = { viewModel.sendIntent(TeamListContract.Intent.NavigateToMemberDetail(member.id)) },
                                onDelete = { viewModel.sendIntent(TeamListContract.Intent.DeleteTeamMember(member.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

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
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: TeamListContract.FilterType,
    onFilterSelected: (TeamListContract.FilterType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TeamListContract.FilterType.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = {
                    Text(
                        when (filter) {
                            TeamListContract.FilterType.ALL -> "All"
                            TeamListContract.FilterType.MANAGERS -> "Managers"
                            TeamListContract.FilterType.SUPERVISORS -> "Supervisors"
                        }
                    )
                },
                leadingIcon = if (selectedFilter == filter) {
                    { Text("✓") }
                } else null
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TeamMemberCard(
    member: TeamMember,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = when (member.role) {
                    TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primaryContainer
                    TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondaryContainer
                },
                modifier = Modifier.size(56.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = member.initials,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = when (member.role) {
                            TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.onPrimaryContainer
                            TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.onSecondaryContainer
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Role Badge
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = when (member.role) {
                            TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.primary
                            TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.secondary
                        }
                    ) {
                        Text(
                            text = member.roleDisplayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = when (member.role) {
                                TeamMemberRole.MANAGER -> MaterialTheme.colorScheme.onPrimary
                                TeamMemberRole.SUPERVISOR -> MaterialTheme.colorScheme.onSecondary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = member.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = member.mobile,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Status
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (member.isActive) "●" else "○",
                        color = if (member.isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (member.isActive) "Active" else "Inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Delete Button
            IconButton(onClick = { showDeleteDialog = true }) {
                Text("🗑️")
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Team Member") },
            text = { Text("Are you sure you want to delete ${member.fullName}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

