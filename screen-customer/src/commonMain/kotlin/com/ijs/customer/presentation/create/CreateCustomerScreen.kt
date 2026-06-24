package com.ijs.customer.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.customer.presentation.create.CreateCustomerContract.Effect
import com.ijs.customer.presentation.create.CreateCustomerContract.Intent
import com.ijs.customer.presentation.create.CreateCustomerContract.State
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
    var pendingSnackbar by remember { mutableStateOf<UiText?>(null) }

    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is Effect.NavigateBack -> onNavigateBack()
                is Effect.NavigateToCustomerDetail -> onNavigateToCustomerDetail(effect.customerId)
                is Effect.ShowSnackbar -> pendingSnackbar = effect.message
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
                FleetInlineErrorBanner(message = error.resolve())
            }

            // Company & Contact Card
            CompactSectionCard(
                title = stringResource(Res.string.customer_section_company),
                icon = null
            ) {
                FleetInputField(
                    value = state.companyName,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyName(it)) },
                    label = stringResource(Res.string.customer_label_company_name),
                    placeholder = stringResource(Res.string.customer_placeholder_company_name),
                    isError = state.companyNameError != null,
                    errorMessage = state.companyNameError?.resolve()
                )

                FleetInputField(
                    value = state.personName,
                    onValueChange = { viewModel.sendIntent(Intent.UpdatePersonName(it)) },
                    label = stringResource(Res.string.customer_label_contact_person),
                    placeholder = stringResource(Res.string.customer_placeholder_contact_person),
                    isError = state.personNameError != null,
                    errorMessage = state.personNameError?.resolve()
                )

                // Primary Contact (full width)
                FleetInputField(
                    value = state.primaryContact,
                    onValueChange = { viewModel.sendIntent(Intent.UpdatePrimaryContact(filterDigitsOnly(it, 10))) },
                    fieldType = FieldType.PHONE,
                    label = stringResource(Res.string.customer_label_primary_contact),
                    placeholder = stringResource(Res.string.customer_placeholder_primary_contact),
                    isError = state.primaryContactError != null,
                    errorMessage = state.primaryContactError?.resolve()
                )

                // Secondary Contact (full width)
                FleetInputField(
                    value = state.secondaryContact,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateSecondaryContact(filterDigitsOnly(it, 10))) },
                    fieldType = FieldType.PHONE,
                    label = stringResource(Res.string.customer_label_secondary_contact),
                    placeholder = stringResource(Res.string.customer_placeholder_secondary_contact),
                    isError = state.secondaryContactError != null,
                    errorMessage = state.secondaryContactError?.resolve()
                )

                FleetInputField(
                    value = state.email,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateEmail(it)) },
                    fieldType = FieldType.EMAIL,
                    label = stringResource(Res.string.customer_label_email),
                    placeholder = stringResource(Res.string.customer_placeholder_email),
                    isError = state.emailError != null,
                    errorMessage = state.emailError?.resolve()
                )
            }

            // Business & Address Card
            CompactSectionCard(
                title = stringResource(Res.string.customer_section_business),
                icon = null
            ) {
                FleetInputField(
                    value = state.gstNumber,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateGstNumber(it)) },
                    label = stringResource(Res.string.customer_label_gst),
                    placeholder = stringResource(Res.string.customer_placeholder_gst),
                    isError = state.gstNumberError != null,
                    errorMessage = state.gstNumberError?.resolve()
                )

                FleetInputField(
                    value = state.companyAddress,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyAddress(it)) },
                    fieldType = FieldType.ADDRESS,
                    label = stringResource(Res.string.customer_label_address),
                    placeholder = stringResource(Res.string.customer_placeholder_address)
                )

                FleetInputField(
                    value = state.notes,
                    onValueChange = { viewModel.sendIntent(Intent.UpdateNotes(it)) },
                    fieldType = FieldType.NOTES,
                    label = stringResource(Res.string.customer_label_notes),
                    placeholder = stringResource(Res.string.customer_placeholder_notes)
                )
            }

            // Bottom spacing for button
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

/**
 * Compact section card with reduced padding.
 * Delegates to the shared [FleetTitledSectionCard].
 */
@Composable
private fun CompactSectionCard(
    title: String,
    icon: String?,
    content: @Composable ColumnScope.() -> Unit
) {
    FleetTitledSectionCard(
        title = title,
        emoji = icon,
        content = content
    )
}
