package com.ijs.customer.presentation.create

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
import com.indusjs.uicomponents.components.FleetEmailField
import com.indusjs.uicomponents.components.FleetMobileField
import com.ijs.customer.presentation.create.CreateCustomerContract.Effect
import com.ijs.customer.presentation.create.CreateCustomerContract.Intent
import com.ijs.customer.presentation.create.CreateCustomerContract.State
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * Create Customer Screen - Compact and efficient UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomerScreen(
    viewModel: CreateCustomerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCustomerDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.NavigateToCustomerDetail -> onNavigateToCustomerDetail(effect.customerId)
                is Effect.ShowSnackbar -> scope.launch { snackbarHostState.showSnackbar(effect.message) }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(Res.string.customers_add))
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
                            contentDescription = stringResource(Res.string.back),
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
                        Text(stringResource(Res.string.action_creating))
                    } else {
                        Text("✅ ${stringResource(Res.string.customers_add)}", fontWeight = FontWeight.SemiBold)
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
                title = stringResource(Res.string.customer_section_company),
                icon = null
            ) {
                // Company Name
                OutlinedTextField(
                    value = state.companyName,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyName(it)) },
                    label = { Text(stringResource(Res.string.customer_label_company_name)) },
                    placeholder = { Text(stringResource(Res.string.customer_placeholder_company_name)) },
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
                    label = { Text(stringResource(Res.string.customer_label_contact_person)) },
                    placeholder = { Text(stringResource(Res.string.customer_placeholder_contact_person)) },
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
                        label = stringResource(Res.string.customer_label_primary_contact),
                        placeholder = stringResource(Res.string.customer_placeholder_primary_contact),
                        isError = state.primaryContactError != null,
                        errorMessage = state.primaryContactError,
                        modifier = Modifier.weight(1f)
                    )
                    FleetMobileField(
                        rawValue = state.secondaryContact,
                        onRawValueChange = { viewModel.sendIntent(Intent.UpdateSecondaryContact(it)) },
                        label = stringResource(Res.string.customer_label_secondary_contact),
                        placeholder = stringResource(Res.string.customer_placeholder_secondary_contact),
                        isError = state.secondaryContactError != null,
                        errorMessage = state.secondaryContactError,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Email
                FleetEmailField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateEmail(it)) },
                    label = stringResource(Res.string.customer_label_email),
                    placeholder = stringResource(Res.string.customer_placeholder_email),
                    isError = state.emailError != null,
                    errorMessage = state.emailError
                )
            }

            // Business & Address Card (consolidated)
            CompactSectionCard(
                title = stringResource(Res.string.customer_section_business),
                icon = null
            ) {
                // GST Number with validation hint
                OutlinedTextField(
                    value = state.gstNumber,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateGstNumber(it)) },
                    label = { Text(stringResource(Res.string.customer_label_gst)) },
                    placeholder = { Text(stringResource(Res.string.customer_placeholder_gst)) },
                    isError = state.gstNumberError != null,
                    supportingText = if (state.gstNumberError != null) {
                        { Text(state.gstNumberError!!, color = MaterialTheme.colorScheme.error) }
                    } else if (state.gstNumber.isNotBlank() && state.gstNumber.length < 15) {
                        { Text(stringResource(Res.string.customer_gst_chars, state.gstNumber.length), color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Address (compact)
                OutlinedTextField(
                    value = state.companyAddress,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyAddress(it)) },
                    label = { Text(stringResource(Res.string.customer_label_address)) },
                    placeholder = { Text(stringResource(Res.string.customer_placeholder_address)) },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Notes (compact)
                OutlinedTextField(
                    value = state.notes,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateNotes(it)) },
                    label = { Text(stringResource(Res.string.customer_label_notes)) },
                    placeholder = { Text(stringResource(Res.string.customer_placeholder_notes)) },
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
