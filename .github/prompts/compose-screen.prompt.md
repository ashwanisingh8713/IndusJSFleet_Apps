# Compose Screen Templates

> Use this prompt when building a new Compose screen in the Fleet app.

## Screen Types

### 1. List Screen

```kotlin
@Composable
fun {Feature}Screen(
    viewModel: {Feature}ViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Handle one-time effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateToDetail -> onNavigateToDetail(effect.id)
                is Effect.NavigateToAdd -> onNavigateToAdd()
                is Effect.ShowSnackbar -> { /* snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("{Feature}s") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(Intent.Refresh) }) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.sendIntent(Intent.AddNew) }) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        ScreenContent(
            isLoading = state.isLoading,
            error = state.error,
            onRetry = { viewModel.sendIntent(Intent.LoadData) },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn {
                items(state.filteredItems, key = { it.id }) { item ->
                    {Feature}Card(
                        item = item,
                        onClick = { viewModel.sendIntent(Intent.SelectItem(item.id)) }
                    )
                }
            }
        }
    }
}
```

### 2. Detail Screen (with Tabs)

```kotlin
@Composable
fun {Feature}DetailScreen(
    viewModel: {Feature}DetailViewModel,
    {feature}Id: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect({feature}Id) {
        viewModel.sendIntent(Intent.LoadDetail({feature}Id))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.ShowSnackbar -> { /* snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.entity?.name ?: "{Feature} Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), "Back")
                    }
                }
            )
        }
    ) { padding ->
        ScreenContent(
            isLoading = state.isLoading,
            error = state.error,
            onRetry = { viewModel.sendIntent(Intent.LoadDetail({feature}Id)) },
            modifier = Modifier.padding(padding)
        ) {
            val entity = state.entity ?: return@ScreenContent

            Column {
                // Hero section
                {Feature}Header(entity)

                // Tabs
                TabRow(selectedTabIndex = state.selectedTab) {
                    Tab(selected = state.selectedTab == 0, onClick = { viewModel.sendIntent(Intent.ChangeTab(0)) }) {
                        Text("Overview")
                    }
                    Tab(selected = state.selectedTab == 1, onClick = { viewModel.sendIntent(Intent.ChangeTab(1)) }) {
                        Text("History")
                    }
                }

                when (state.selectedTab) {
                    0 -> OverviewTab(entity)
                    1 -> HistoryTab(entity)
                }
            }
        }
    }
}
```

### 3. Form/Create Screen

```kotlin
@Composable
fun Create{Feature}Screen(
    viewModel: Create{Feature}ViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.CreateSuccess -> onNavigateBack()
                is Effect.ShowSnackbar -> { /* snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create {Feature}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painterResource(Res.drawable.ic_arrow_back), "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Use reusable Fleet input components
            FleetTextField(
                value = state.name,
                onValueChange = { viewModel.sendIntent(Intent.UpdateName(it)) },
                label = "Name",
                isError = state.nameError != null,
                errorMessage = state.nameError
            )

            FleetDateField(
                value = state.date,
                onValueChange = { viewModel.sendIntent(Intent.UpdateDate(it)) },
                label = "Date",
                placeholder = "DD-MM-YYYY"
            )

            FleetTimeField(
                value = state.time,
                onValueChange = { viewModel.sendIntent(Intent.UpdateTime(it)) },
                label = "Time",
                placeholder = "HH:MM"
            )

            Spacer(Modifier.height(24.dp))

            FleetPrimaryButton(
                text = "Create",
                onClick = { viewModel.sendIntent(Intent.Submit) },
                isLoading = state.isSubmitting,
                enabled = !state.isSubmitting
            )
        }
    }
}
```

## Reusable UI Components (Always Use These)

| Component | Import | Usage |
|-----------|--------|-------|
| `FleetTextField` | `core.ui.InputComponents` | Standard text input |
| `FleetDateField` | `core.ui.InputComponents` | DD-MM-YYYY auto-format |
| `FleetTimeField` | `core.ui.InputComponents` | HH:MM auto-format |
| `FleetMobileField` | `core.ui.PhoneComponents` | 10-digit mobile |
| `FleetEmailField` | `core.ui.InputFields` | Email with validation |
| `FleetPasswordField` | `core.ui.InputFields` | Password with toggle |
| `ScreenContent` | `core.ui.CommonComponents` | Handles loading/error/content |
| `LoadingContent` | `core.ui.CommonComponents` | Centered spinner |
| `ErrorContent` | `core.ui.CommonComponents` | Error + retry button |
| `EmptyContent` | `core.ui.CommonComponents` | Empty state + action |
| `FleetCard` | `core.ui.CardComponents` | Standard card |
| `FleetPrimaryButton` | `core.ui.ButtonComponents` | Primary action |
| `FleetSecondaryButton` | `core.ui.ButtonComponents` | Secondary action |
| `FleetDateTimePicker` | `com.indusjs.datetimepicker` | Calendar/time picker dialog |
| `CostTypeChipSelector` | `core.ui.CostTypeChipSelector` | Cost type chips |

## Rules

- Always use `MaterialTheme.colorScheme.*` — never hardcode colors
- Always handle 3 states: loading, error, content (via `ScreenContent`)
- Use `collectAsStateWithLifecycle()` for state observation
- Collect effects in `LaunchedEffect(Unit)` block
- Use SVG icons from `composeResources/drawable/` via `painterResource(Res.drawable.ic_*)`

