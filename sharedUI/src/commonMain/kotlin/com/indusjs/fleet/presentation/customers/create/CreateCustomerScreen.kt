package com.indusjs.fleet.presentation.customers.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.FleetEmailField
import com.indusjs.fleet.core.ui.FleetMobileField
import com.indusjs.fleet.navigation.FleetRoute
import com.indusjs.fleet.presentation.customers.create.CreateCustomerContract.Effect
import com.indusjs.fleet.presentation.customers.create.CreateCustomerContract.Intent
import com.indusjs.fleet.presentation.customers.create.CreateCustomerContract.State
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import indusjsfleet.sharedui.generated.resources.*

/**
 * Create Customer Screen - Compact and efficient UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomerScreen(
    viewModel: CreateCustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigate: (FleetRoute) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.NavigateTo -> onNavigate(effect.route)
                is Effect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Add Customer")
                        // Progress indicator
                        if (state.formCompletionPercentage > 0) {
                            LinearProgressIndicator(
                                progress = { state.formCompletionPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .padding(top = 2.dp),
                                color = when {
                                    state.formCompletionPercentage == 100 -> MaterialTheme.colorScheme.primary
                                    state.formCompletionPercentage >= 66 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.outline
                                },
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.sendIntent(Intent.CreateCustomer) },
                    enabled = state.isValid && !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating...")
                    } else {
                        Text("✅ Create Customer", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Error message
            state.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Company & Contact Card (consolidated)
            CompactSectionCard(
                title = "📋 Company & Contact",
                icon = null
            ) {
                // Company Name
                OutlinedTextField(
                    value = state.companyName,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyName(it)) },
                    label = { Text("Company Name *") },
                    placeholder = { Text("Company Or Firm Name") },
                    isError = state.companyNameError != null,
                    supportingText = state.companyNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Contact Person
                OutlinedTextField(
                    value = state.personName,
                    onValueChange = { viewModel.sendIntent(Intent.UpdatePersonName(it)) },
                    label = { Text("Contact Person *") },
                    placeholder = { Text("Company Person Name") },
                    isError = state.personNameError != null,
                    supportingText = state.personNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Primary & Secondary Contact in Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FleetMobileField(
                        rawValue = state.primaryContact,
                        onRawValueChange = { viewModel.sendIntent(Intent.UpdatePrimaryContact(it)) },
                        label = "Primary *",
                        placeholder = "Mobile",
                        isError = state.primaryContactError != null,
                        errorMessage = state.primaryContactError,
                        modifier = Modifier.weight(1f)
                    )
                    FleetMobileField(
                        rawValue = state.secondaryContact,
                        onRawValueChange = { viewModel.sendIntent(Intent.UpdateSecondaryContact(it)) },
                        label = "Secondary",
                        placeholder = "Optional",
                        isError = state.secondaryContactError != null,
                        errorMessage = state.secondaryContactError,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Email
                FleetEmailField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateEmail(it)) },
                    label = "Email",
                    placeholder = "contact@company.com",
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )
            }

            // Business & Address Card (consolidated)
            CompactSectionCard(
                title = "🏢 Business Details",
                icon = null
            ) {
                // GST Number with validation hint
                OutlinedTextField(
                    value = state.gstNumber,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateGstNumber(it)) },
                    label = { Text("GST Number") },
                    placeholder = { Text("22AAAAA0000A1Z5") },
                    isError = state.gstNumberError != null,
                    supportingText = if (state.gstNumberError != null) {
                        { Text(state.gstNumberError!!, color = MaterialTheme.colorScheme.error) }
                    } else if (state.gstNumber.isNotBlank() && state.gstNumber.length < 15) {
                        { Text("${state.gstNumber.length}/15 characters", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Address (compact)
                OutlinedTextField(
                    value = state.companyAddress,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyAddress(it)) },
                    label = { Text("Address") },
                    placeholder = { Text("Full address with city, pincode") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Notes (compact)
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateNotes(it)) },
                    label = { Text("Notes") },
                    placeholder = { Text("Additional information") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }

            // Bottom spacing for button
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

/**
 * Compact section card with reduced padding.
 */
@Composable
private fun CompactSectionCard(
    title: String,
    icon: String?,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                thickness = 1.dp
            )
            content()
        }
    }
}
