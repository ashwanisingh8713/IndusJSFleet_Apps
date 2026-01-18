package com.indusjs.fleet.presentation.payments

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.fleet.core.ui.ErrorContent
import com.indusjs.fleet.core.ui.LoadingContent
import com.indusjs.fleet.domain.entity.payment.PaymentStatus
import com.indusjs.fleet.domain.entity.payment.TripPayment
import indusjsfleet.sharedui.generated.resources.*
import org.jetbrains.compose.resources.painterResource

/**
 * Payment Detail Screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentDetailScreen(
    viewModel: PaymentDetailViewModel,
    paymentId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    var showMenu by remember { mutableStateOf(false) }

    // Initialize
    LaunchedEffect(paymentId) {
        viewModel.sendIntent(PaymentDetailContract.Intent.LoadPayment(paymentId))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PaymentDetailContract.Effect.NavigateBack -> onNavigateBack()
                is PaymentDetailContract.Effect.NavigateToEdit -> onNavigateToEdit(effect.paymentId)
                is PaymentDetailContract.Effect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
                is PaymentDetailContract.Effect.ShowError -> snackbarHostState.showSnackbar(effect.message)
                is PaymentDetailContract.Effect.PaymentDeleted -> { /* Handled in NavigateBack */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (state.canEdit || state.canDelete) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_more_vert),
                                    contentDescription = "More options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                if (state.canEdit) {
                                    DropdownMenuItem(
                                        text = { Text("Edit") },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_edit),
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            viewModel.sendIntent(PaymentDetailContract.Intent.NavigateToEdit)
                                        }
                                    )
                                }
                                if (state.canDelete) {
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(Res.drawable.ic_delete),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            showMenu = false
                                            viewModel.sendIntent(PaymentDetailContract.Intent.ShowDeleteConfirmation)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    LoadingContent()
                }
                state.error != null -> {
                    ErrorContent(
                        error = state.error ?: "Something went wrong",
                        onRetry = { viewModel.sendIntent(PaymentDetailContract.Intent.Refresh) }
                    )
                }
                state.payment != null -> {
                    PaymentDetailContent(
                        payment = state.payment!!,
                        scrollState = scrollState
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(PaymentDetailContract.Intent.HideDeleteConfirmation) },
            title = { Text("Delete Payment") },
            text = {
                Text("Are you sure you want to delete this payment of ${state.payment?.amountDisplay}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(PaymentDetailContract.Intent.ConfirmDelete) },
                    enabled = !state.isDeleting
                ) {
                    if (state.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(PaymentDetailContract.Intent.HideDeleteConfirmation) }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PaymentDetailContent(
    payment: TripPayment,
    scrollState: androidx.compose.foundation.ScrollState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Section - Amount and Status
        HeroSection(payment = payment)

        // Trip Info Card
        TripInfoCard(payment = payment)

        // Payment Details Card
        PaymentDetailsCard(payment = payment)

        // Customer Details Card
        CustomerDetailsCard(payment = payment)

        // Financial Info Card
        FinancialInfoCard(payment = payment)

        // Additional Info Card
        AdditionalInfoCard(payment = payment)

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun HeroSection(payment: TripPayment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mode icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = payment.modeIcon,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount
            Text(
                text = payment.amountDisplay,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Net amount if different
            if (payment.hasTds || payment.hasDiscount) {
                Text(
                    text = "Net: ${payment.netAmountDisplay}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status badge
            PaymentStatusBadge(status = payment.paymentStatus)

            Spacer(modifier = Modifier.height(8.dp))

            // Receipt number
            payment.receiptNumber?.let { receipt ->
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Receipt: $receipt",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Date
            payment.paymentDate?.let { date ->
                Text(
                    text = formatDisplayDate(date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TripInfoCard(payment: TripPayment) {
    val tripInfo = payment.tripInfo ?: return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "🚛 Trip Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicle
            tripInfo.vehicleRegistration?.let { vehicle ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Vehicle",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = vehicle,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Driver
            tripInfo.driverName?.let { driver ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Driver",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = driver,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Route
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "From",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tripInfo.startLocation ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
                Text(
                    text = "→",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "To",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = tripInfo.endLocation ?: "N/A",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2
                    )
                }
            }

            // Trip price if available
            tripInfo.tripPrice?.let { price ->
                if (price > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Trip Price",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = tripInfo.tripPriceDisplay,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentDetailsCard(payment: TripPayment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "💳 Payment Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            DetailRow(label = "Amount", value = payment.amountDisplay)

            if (payment.hasTds) {
                DetailRow(label = "TDS Deducted", value = payment.tdsDisplay)
            }

            if (payment.hasDiscount) {
                DetailRow(label = "Discount", value = payment.discountDisplay)
            }

            DetailRow(
                label = "Net Amount",
                value = payment.netAmountDisplay,
                isHighlighted = true
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            DetailRow(label = "Payment Type", value = "${payment.paymentType.icon} ${payment.typeDisplay}")
            DetailRow(label = "Payment Mode", value = "${payment.modeIcon} ${payment.modeDisplay}")

            payment.transactionId?.let {
                DetailRow(label = "Transaction ID", value = it)
            }

            payment.bankName?.let {
                DetailRow(label = "Bank Name", value = it)
            }
        }
    }
}

@Composable
private fun CustomerDetailsCard(payment: TripPayment) {
    if (payment.customerName.isNullOrBlank() &&
        payment.customerContact.isNullOrBlank() &&
        payment.customerCompany.isNullOrBlank()) {
        return
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "👤 Customer Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            payment.customerName?.let {
                DetailRow(label = "Name", value = it)
            }

            payment.customerCompany?.let {
                DetailRow(label = "Company", value = it)
            }

            payment.customerContact?.let {
                DetailRow(label = "Contact", value = it)
            }

            payment.customerGst?.let {
                DetailRow(label = "GST Number", value = it)
            }
        }
    }
}

@Composable
private fun FinancialInfoCard(payment: TripPayment) {
    val hasFinancialInfo = !payment.financialYear.isNullOrBlank() ||
            !payment.financialMonth.isNullOrBlank()

    if (!hasFinancialInfo) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📊 Financial Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            payment.financialYear?.let {
                DetailRow(label = "Financial Year", value = "FY $it")
            }

            payment.financialMonth?.let {
                DetailRow(label = "Financial Month", value = it)
            }
        }
    }
}

@Composable
private fun AdditionalInfoCard(payment: TripPayment) {
    val hasAdditionalInfo = !payment.notes.isNullOrBlank() ||
            !payment.receivedBy.isNullOrBlank() ||
            !payment.receivedAtLocation.isNullOrBlank() ||
            !payment.createdByName.isNullOrBlank()

    if (!hasAdditionalInfo) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "📝 Additional Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            payment.notes?.let {
                DetailRow(label = "Notes", value = it)
            }

            payment.receivedBy?.let {
                DetailRow(label = "Received By", value = it)
            }

            payment.receivedAtLocation?.let {
                DetailRow(label = "Received At", value = it)
            }

            payment.createdByName?.let {
                DetailRow(label = "Created By", value = it)
            }

            payment.createdAt?.let {
                DetailRow(label = "Created At", value = formatDisplayDate(it))
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isHighlighted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
            fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun PaymentStatusBadge(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (status) {
        PaymentStatus.RECEIVED -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        PaymentStatus.PENDING -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        PaymentStatus.CANCELLED -> Color(0xFFFFEBEE) to Color(0xFFC62828)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = status.displayName,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

private fun formatDisplayDate(isoDate: String): String {
    return try {
        val parts = isoDate.take(10).split("-")
        if (parts.size == 3) {
            "${parts[2]} ${getMonthName(parts[1].toInt())} ${parts[0]}"
        } else {
            isoDate.take(10)
        }
    } catch (e: Exception) {
        isoDate.take(10)
    }
}

private fun getMonthName(month: Int): String {
    return when (month) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""
    }
}
