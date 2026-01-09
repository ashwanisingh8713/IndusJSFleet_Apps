# Compose Screen Template

Create a Compose screen for the IndusJS Fleet app following MVI pattern.

## Screen Information
- **Screen Name**: [SCREEN_NAME]
- **Feature**: [FEATURE_NAME]
- **Type**: [LIST / DETAIL / FORM / DASHBOARD]
- **Description**: [BRIEF_DESCRIPTION]

---

## List Screen Template

```kotlin
package com.indusjs.fleet.presentation.{feature}

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.mvi.HandleEffects
import com.indusjs.fleet.core.ui.*
import com.indusjs.fleet.domain.entity.{feature}.{Entity}
import com.indusjs.fleet.presentation.{feature}.{Feature}Contract.*
import indusjs_fleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * {Feature} list screen displaying all {entities}.
 * Uses MVI pattern with unidirectional data flow.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun {Feature}Screen(
    viewModel: {Feature}ViewModel,
    onNavigateBack: () -> Unit,
    onNavigateTo{Entity}Detail: (String) -> Unit,
    onNavigateToAdd{Entity}: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showSnackbar by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time effects
    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.NavigateTo{Entity}Detail -> onNavigateTo{Entity}Detail(effect.{entity}Id)
            is Effect.NavigateToAdd{Entity} -> onNavigateToAdd{Entity}()
            is Effect.ShowSnackbar -> showSnackbar = effect.message
        }
    }

    // Show snackbar
    LaunchedEffect(showSnackbar) {
        showSnackbar?.let {
            snackbarHostState.showSnackbar(it)
            showSnackbar = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("{Feature}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh{Entity}s) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.sendIntent(Intent.Add{Entity}) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = "Add {Entity}"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        ScreenContent(
            modifier = Modifier.padding(paddingValues),
            isLoading = state.isLoading,
            error = state.error,
            screenContext = ErrorHandler.ScreenContext.{FEATURE},
            onRetry = { viewModel.sendIntent(Intent.Load{Entity}s) },
            isEmpty = state.{entities}.isEmpty() && !state.isLoading,
            emptyIcon = Res.drawable.ic_{feature},
            emptyTitle = "No {entities} yet",
            emptySubtitle = "Add your first {entity} to get started",
            emptyActionLabel = "Add {Entity}",
            onEmptyAction = { viewModel.sendIntent(Intent.Add{Entity}) }
        ) {
            {Feature}Content(
                state = state,
                onIntent = { viewModel.sendIntent(it) }
            )
        }
    }
}

@Composable
private fun {Feature}Content(
    state: State,
    onIntent: (Intent) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        FleetSearchField(
            value = state.searchQuery,
            onValueChange = { onIntent(Intent.Search(it)) },
            placeholder = "Search {entities}...",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = state.filtered{Entities},
                key = { it.id }
            ) { {entity} ->
                {Entity}Card(
                    {entity} = {entity},
                    onClick = { onIntent(Intent.Select{Entity}({entity}.id)) }
                )
            }
        }
    }
}

@Composable
private fun {Entity}Card(
    {entity}: {Entity},
    onClick: () -> Unit
) {
    FleetCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = {entity}.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = {entity}.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            FleetStatusBadge(
                status = {entity}.state,
                label = {entity}.stateLabel
            )
        }
    }
}
```

---

## Detail Screen Template

```kotlin
package com.indusjs.fleet.presentation.{feature}

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.mvi.HandleEffects
import com.indusjs.fleet.core.ui.*
import com.indusjs.fleet.presentation.{feature}.{Feature}DetailContract.*
import indusjs_fleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * {Entity} detail screen with view and edit capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun {Feature}DetailScreen(
    viewModel: {Feature}DetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.NavigateBack -> onNavigateBack()
            is Effect.NavigateToEdit -> onNavigateToEdit(effect.{entity}Id)
            is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            is Effect.ShowDeleteConfirmation -> showDeleteDialog = true
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete {Entity}") },
            text = { Text("Are you sure you want to delete this {entity}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.sendIntent(Intent.ConfirmDelete)
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("{Entity} Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(Intent.Edit{Entity}) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_edit),
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = { viewModel.sendIntent(Intent.Delete{Entity}) }) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        ScreenContent(
            modifier = Modifier.padding(paddingValues),
            isLoading = state.isLoading,
            error = state.error,
            onRetry = { viewModel.sendIntent(Intent.Load{Entity}) }
        ) {
            state.{entity}?.let { {entity} ->
                {Entity}DetailContent({entity} = {entity})
            }
        }
    }
}

@Composable
private fun {Entity}DetailContent({entity}: {Entity}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Section
        FleetCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = {entity}.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    FleetStatusBadge(
                        status = {entity}.state,
                        label = {entity}.stateLabel
                    )
                }
            }
        }

        // Details Section
        FleetCard {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Details",
                    style = MaterialTheme.typography.titleMedium
                )
                
                DetailRow(label = "Field 1", value = {entity}.field1)
                DetailRow(label = "Field 2", value = {entity}.field2)
                DetailRow(label = "Created", value = {entity}.createdAt)
            }
        }

        // Additional sections as needed
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
```

---

## Form Screen Template

```kotlin
package com.indusjs.fleet.presentation.{feature}

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.mvi.HandleEffects
import com.indusjs.fleet.core.ui.*
import com.indusjs.fleet.presentation.{feature}.Create{Feature}Contract.*
import indusjs_fleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Create/Edit {Entity} form screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Create{Feature}Screen(
    viewModel: Create{Feature}ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    HandleEffects(viewModel) { effect ->
        when (effect) {
            is Effect.NavigateBack -> onNavigateBack()
            is Effect.{Entity}Created -> onNavigateBack()
            is Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit {Entity}" else "Create {Entity}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Form sections
            FormSection(title = "Basic Information") {
                FleetTextField(
                    value = state.name,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateName(it)) },
                    label = "Name",
                    placeholder = "Enter name",
                    isError = state.nameError != null,
                    errorMessage = state.nameError,
                    isRequired = true
                )

                FleetTextField(
                    value = state.description,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateDescription(it)) },
                    label = "Description",
                    placeholder = "Enter description",
                    singleLine = false,
                    maxLines = 3
                )
            }

            // Date/Time section
            FormSection(title = "Schedule") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FleetDateField(
                        value = state.date,
                        onValueChange = { viewModel.sendIntent(Intent.UpdateDate(it)) },
                        label = "Date",
                        placeholder = "DD-MM-YYYY",
                        isError = state.dateError != null,
                        errorMessage = state.dateError,
                        modifier = Modifier.weight(1f)
                    )
                    
                    FleetTimeField(
                        value = state.time,
                        onValueChange = { viewModel.sendIntent(Intent.UpdateTime(it)) },
                        label = "Time (24hr)",
                        placeholder = "HH:MM",
                        isError = state.timeError != null,
                        errorMessage = state.timeError,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Contact section
            FormSection(title = "Contact") {
                FleetMobileField(
                    value = state.mobile,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateMobile(it)) },
                    label = "Mobile Number",
                    isError = state.mobileError != null,
                    errorMessage = state.mobileError
                )
                
                FleetEmailField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateEmail(it)) },
                    label = "Email",
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            FleetPrimaryButton(
                text = if (state.isEditMode) "Update {Entity}" else "Create {Entity}",
                onClick = { viewModel.sendIntent(Intent.Submit) },
                isLoading = state.isSubmitting,
                enabled = state.isFormValid && !state.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            )

            // Error message
            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}
```

---

## Core UI Components Reference

### Always use these for consistency:

| Component | Usage |
|-----------|-------|
| `FleetTextField` | Standard text input |
| `FleetDateField` | Date input (DD-MM-YYYY auto-format) |
| `FleetTimeField` | Time input (HH:MM auto-format) |
| `FleetMobileField` | Mobile number input (10 digits) |
| `FleetEmailField` | Email input with validation |
| `FleetPasswordField` | Password with visibility toggle |
| `FleetSearchField` | Search input with icon |
| `FleetCard` | Standard card container |
| `FleetPrimaryButton` | Primary action button |
| `FleetSecondaryButton` | Secondary action button |
| `FleetStatusBadge` | Status indicator badge |
| `LoadingContent` | Loading state |
| `ErrorContent` | Error with retry |
| `EmptyContent` | Empty state with action |
| `ScreenContent` | Wrapper for loading/error/empty/content |

---

## Validation Checklist

- [ ] State collected with `collectAsStateWithLifecycle()`
- [ ] Effects handled with `HandleEffects()`
- [ ] Navigation uses callbacks (not direct NavController)
- [ ] Core UI components used (FleetTextField, FleetCard, etc.)
- [ ] Loading, error, empty states handled
- [ ] Back button works correctly
- [ ] Snackbar for feedback messages
- [ ] Theme colors from MaterialTheme.colorScheme
- [ ] Icons from Res.drawable resources

