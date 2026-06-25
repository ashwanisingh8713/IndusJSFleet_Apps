package com.ijs.customer.presentation.create

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetInlineErrorBanner
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.components.UiText
import com.indusjs.uicomponents.components.filterDigitsOnly
import com.indusjs.uicomponents.theme.FleetBreakpoint
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import com.ijs.customer.presentation.create.CreateCustomerContract.Effect
import com.ijs.customer.presentation.create.CreateCustomerContract.Intent
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import indusjsfleet.ijs_ui_components_lib.generated.resources.*

/**
 * Create Customer Screen.
 *
 * Compact = full-width phone form; Medium / Expanded = centered, width-capped column so the
 * form does not stretch edge-to-edge on tablet / web. Submit is gated on [State.isValid].
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
                        // Form completion progress.
                        if (state.formCompletionPercentage > 0) {
                            LinearProgressIndicator(
                                progress = { state.formCompletionPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(FleetTokens.Height.ProgressStroke)
                                    .padding(top = FleetTokens.Spacing.XXS),
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
                            modifier = Modifier.size(FleetTokens.IconSize.Default)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = FleetTokens.Elevation.Dialog
            ) {
                FleetButton(
                    text = if (state.isSaving) stringResource(Res.string.action_creating)
                    else stringResource(Res.string.customers_add),
                    onClick = { viewModel.sendIntent(Intent.CreateCustomer) },
                    variant = ButtonVariant.PRIMARY,
                    enabled = state.isValid && !state.isSaving,
                    isLoading = state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FleetTokens.Spacing.L, vertical = FleetTokens.Spacing.M)
                )
            }
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val breakpoint = rememberFleetBreakpoint()
            val formWidthModifier = when (breakpoint) {
                FleetBreakpoint.Compact -> Modifier.fillMaxWidth()
                else -> Modifier.widthIn(max = FleetTokens.Width.MaxContent)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = FleetTokens.Spacing.ScreenHorizontal),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = formWidthModifier,
                    verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
                ) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))

                    // Error banner
                    state.error?.let { error ->
                        FleetInlineErrorBanner(message = error.resolve())
                    }

                    // Company & Contact Card
                    FleetTitledSectionCard(
                        title = stringResource(Res.string.customer_section_company)
                    ) {
                        FleetInputField(
                            value = state.companyName,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyName(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_company_name),
                            placeholder = stringResource(Res.string.customer_placeholder_company_name),
                            isError = state.companyNameError != null,
                            errorMessage = state.companyNameError?.resolve()
                        )

                        FleetInputField(
                            value = state.personName,
                            onValueChange = { viewModel.sendIntent(Intent.UpdatePersonName(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_contact_person),
                            placeholder = stringResource(Res.string.customer_placeholder_contact_person),
                            isError = state.personNameError != null,
                            errorMessage = state.personNameError?.resolve()
                        )

                        FleetInputField(
                            value = state.primaryContact,
                            onValueChange = { viewModel.sendIntent(Intent.UpdatePrimaryContact(filterDigitsOnly(it, 10))) },
                            fieldType = FieldType.PHONE,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_primary_contact),
                            placeholder = stringResource(Res.string.customer_placeholder_primary_contact),
                            isError = state.primaryContactError != null,
                            errorMessage = state.primaryContactError?.resolve()
                        )

                        FleetInputField(
                            value = state.secondaryContact,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateSecondaryContact(filterDigitsOnly(it, 10))) },
                            fieldType = FieldType.PHONE,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_secondary_contact),
                            placeholder = stringResource(Res.string.customer_placeholder_secondary_contact),
                            isError = state.secondaryContactError != null,
                            errorMessage = state.secondaryContactError?.resolve()
                        )

                        FleetInputField(
                            value = state.email,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateEmail(it)) },
                            fieldType = FieldType.EMAIL,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_email),
                            placeholder = stringResource(Res.string.customer_placeholder_email),
                            isError = state.emailError != null,
                            errorMessage = state.emailError?.resolve()
                        )
                    }

                    // Business & Address Card
                    FleetTitledSectionCard(
                        title = stringResource(Res.string.customer_section_business)
                    ) {
                        FleetInputField(
                            value = state.gstNumber,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateGstNumber(it)) },
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_gst),
                            placeholder = stringResource(Res.string.customer_placeholder_gst),
                            isError = state.gstNumberError != null,
                            errorMessage = state.gstNumberError?.resolve()
                        )

                        FleetInputField(
                            value = state.companyAddress,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateCompanyAddress(it)) },
                            fieldType = FieldType.ADDRESS,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_address),
                            placeholder = stringResource(Res.string.customer_placeholder_address)
                        )

                        FleetInputField(
                            value = state.notes,
                            onValueChange = { viewModel.sendIntent(Intent.UpdateNotes(it)) },
                            fieldType = FieldType.NOTES,
                            modifier = Modifier.fillMaxWidth(),
                            label = stringResource(Res.string.customer_label_notes),
                            placeholder = stringResource(Res.string.customer_placeholder_notes)
                        )
                    }

                    // Bottom spacing so the last field clears the bottom action bar.
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL))
                }
            }
        }
    }
}
