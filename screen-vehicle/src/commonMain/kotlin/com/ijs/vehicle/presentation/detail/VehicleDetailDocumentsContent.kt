package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.LoadingContent
import com.indusjs.uicomponents.theme.FleetTokens
import com.ijs.vehicle.domain.entity.DocumentTypeDetail
import com.ijs.vehicle.domain.entity.VehicleDocumentsData
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DocumentsTabContent(
    documentsData: VehicleDocumentsData?,
    isLoading: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onUploadClick: (documentType: String, documentTypeName: String) -> Unit,
    onPreviewClick: (documentId: String, documentName: String, fileUrl: String?) -> Unit = { _, _, _ -> },
    onDownloadClick: (documentId: String, documentName: String, fileUrl: String?) -> Unit = { _, _, _ -> },
    onReplaceClick: (documentType: String, documentTypeName: String) -> Unit = { _, _ -> }
) {
    when {
        isLoading && documentsData == null -> {
            LoadingContent(message = stringResource(Res.string.vehicle_docs_loading))
        }
        error != null && documentsData == null -> {
            ErrorContent(
                error = error,
                screenContext = FleetErrorContext.DOCUMENTS,
                onRetry = onRefresh
            )
        }
        else -> {
            val summary = documentsData?.summary
            val documentTypes = documentsData?.documentTypes ?: emptyList()
            val alertDocs = documentsData?.alertDocs ?: emptyList()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(FleetTokens.Spacing.L),
                verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                // Documents Summary
                if (summary != null) {
                    item {
                        FleetSectionCard(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            border = null,
                            elevation = FleetTokens.Elevation.None
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                DocumentStatItem(
                                    count = summary.uploaded.toString(),
                                    label = stringResource(Res.string.vehicle_docs_stat_uploaded),
                                    iconRes = Res.drawable.ic_folder
                                )
                                DocumentStatItem(
                                    count = summary.notUploaded.toString(),
                                    label = stringResource(Res.string.vehicle_docs_stat_missing),
                                    iconRes = Res.drawable.ic_warning
                                )
                                DocumentStatItem(
                                    count = summary.expiringSoon.toString(),
                                    label = stringResource(Res.string.vehicle_docs_stat_expiring),
                                    iconRes = Res.drawable.ic_time
                                )
                            }
                        }
                    }
                }

                // Alert Documents
                if (alertDocs.isNotEmpty()) {
                    item {
                        FleetSectionCard(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            border = null,
                            elevation = FleetTokens.Elevation.None
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_warning),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.S),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                                Text(
                                    text = stringResource(Res.string.vehicle_docs_attention),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                            alertDocs.forEach { alert ->
                                Text(
                                    text = "• ${alert.typeName} - ${alert.message}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // All Documents Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.vehicle_docs_all),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Document List
                if (documentTypes.isNotEmpty()) {
                    items(documentTypes.size) { index ->
                        val doc = documentTypes[index]
                        DocumentTypeCard(
                            doc = doc,
                            onUploadClick = { onUploadClick(doc.type, doc.typeName) },
                            onPreviewClick = {
                                val documentId = doc.document?.id ?: ""
                                // Backend download_url is relative and already includes /api/v1,
                                // so prefix with BASE_ORIGIN (NOT BASE_URL) to build the full URL.
                                val fileUrl = doc.document?.fileUrl?.let { ApiConfig.BASE_ORIGIN + it }
                                onPreviewClick(documentId, doc.typeName, fileUrl)
                            },
                            onDownloadClick = {
                                val documentId = doc.document?.id ?: ""
                                val fileUrl = doc.document?.fileUrl?.let { ApiConfig.BASE_ORIGIN + it }
                                onDownloadClick(documentId, doc.typeName, fileUrl)
                            },
                            onReplaceClick = { onReplaceClick(doc.type, doc.typeName) }
                        )
                    }
                } else {
                    item {
                        Text(
                            text = stringResource(Res.string.vehicle_docs_no_docs),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(FleetTokens.Spacing.L)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL)) }
            }
        }
    }
}

@Composable
internal fun DocumentStatItem(count: String, label: String, iconRes: org.jetbrains.compose.resources.DrawableResource) {
    FleetMetricTile(
        value = count,
        label = label,
        iconRes = iconRes,
        valueColor = MaterialTheme.colorScheme.onPrimaryContainer,
        showBackground = false,
        centered = true
    )
}

@Composable
internal fun DocumentTypeCard(
    doc: DocumentTypeDetail,
    onUploadClick: () -> Unit,
    onPreviewClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onReplaceClick: () -> Unit = {}
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    var showUploadConfirmDialog by remember { mutableStateOf(false) }

    val docInfo = doc.document
    val daysLeft = docInfo?.daysRemaining
    val isExpiringSoon = daysLeft != null && daysLeft <= 30
    val isExpired = daysLeft != null && daysLeft <= 0

    // Upload confirmation dialog
    if (showUploadConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUploadConfirmDialog = false },
            shape = RoundedCornerShape(FleetTokens.Radius.XL),
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_folder),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.L)
                )
            },
            title = { Text(stringResource(Res.string.vehicle_docs_upload_title, doc.typeName)) },
            text = {
                Column {
                    Text(stringResource(Res.string.vehicle_docs_upload_message))
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                    Text(
                        text = doc.typeName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (doc.isRequired) {
                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                        Text(
                            text = stringResource(Res.string.vehicle_docs_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                    Text(
                        text = stringResource(Res.string.vehicle_docs_ensure_readable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showUploadConfirmDialog = false
                    onUploadClick()
                }) {
                    Text(stringResource(Res.string.vehicle_docs_select_file))
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadConfirmDialog = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }

    if (doc.isUploaded) {
        // ==================== UPLOADED DOCUMENT CARD ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(FleetTokens.Radius.XL),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    isExpiringSoon -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = FleetTokens.Elevation.Raised)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with gradient accent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(FleetTokens.Spacing.XS)
                        .background(
                            when {
                                isExpired -> MaterialTheme.colorScheme.error
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                )

                // Main content
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FleetTokens.Spacing.L),
                    verticalAlignment = Alignment.Top
                ) {
                    // Document icon with check badge
                    Box(modifier = Modifier.padding(top = FleetTokens.Spacing.XS)) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(FleetTokens.Radius.XL),
                            color = when {
                                isExpired -> MaterialTheme.colorScheme.errorContainer
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            tonalElevation = FleetTokens.Elevation.Raised
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_check),
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp),
                                    tint = when {
                                        isExpired -> MaterialTheme.colorScheme.error
                                        isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            }
                        }

                        // Status indicator
                        Surface(
                            modifier = Modifier
                                .size(22.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = FleetTokens.Spacing.XS, y = FleetTokens.Spacing.XS),
                            shape = CircleShape,
                            color = when {
                                isExpired -> MaterialTheme.colorScheme.error
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shadowElevation = FleetTokens.Elevation.Raised
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(
                                        when {
                                            isExpired -> Res.drawable.ic_warning
                                            isExpiringSoon -> Res.drawable.ic_time
                                            else -> Res.drawable.ic_check
                                        }
                                    ),
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.L))

                    // Document details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.typeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                        // Status row with icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(FleetTokens.Radius.M),
                                color = when {
                                    isExpired -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    isExpiringSoon -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            ) {
                                val statusContentColor = when {
                                    isExpired -> MaterialTheme.colorScheme.error
                                    isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.primary
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.M, vertical = FleetTokens.Spacing.XS)
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            when {
                                                isExpired -> Res.drawable.ic_warning
                                                isExpiringSoon -> Res.drawable.ic_time
                                                else -> Res.drawable.ic_check
                                            }
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(FleetTokens.IconSize.S),
                                        tint = statusContentColor
                                    )
                                    Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                                    Text(
                                        text = when {
                                            isExpired -> stringResource(Res.string.vehicle_docs_expired)
                                            isExpiringSoon -> stringResource(Res.string.vehicle_docs_expires_in, daysLeft)
                                            docInfo?.expiryDate != null -> stringResource(Res.string.vehicle_docs_valid_till, formatIsoDateToDisplay(docInfo.expiryDate))
                                            else -> docInfo?.statusLabel ?: stringResource(Res.string.vehicle_docs_uploaded)
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = statusContentColor
                                    )
                                }
                            }
                        }

                        // Document number if available
                        if (!doc.document?.documentNumber.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_folder),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.S),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                                Text(
                                    text = stringResource(Res.string.vehicle_docs_doc_number, doc.document?.documentNumber.orEmpty()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Upload date if available
                        doc.document?.uploadedAt?.takeIf { it > 0L }?.let { uploadedAt ->
                            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_calendar),
                                    contentDescription = null,
                                    modifier = Modifier.size(FleetTokens.IconSize.S),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                                Text(
                                    text = stringResource(
                                        Res.string.vehicle_docs_uploaded_at,
                                        com.indusjs.fleet.core.util.formatDateTimeForDisplay(uploadedAt)
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // More options button
                    Box {
                        IconButton(onClick = { showOptionsMenu = true }) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_more_vert),
                                contentDescription = stringResource(Res.string.cd_more_options),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showOptionsMenu,
                            onDismissRequest = { showOptionsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_visibility),
                                            contentDescription = null,
                                            modifier = Modifier.size(FleetTokens.IconSize.S).padding(end = FleetTokens.Spacing.XS),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(stringResource(Res.string.vehicle_docs_preview))
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onPreviewClick()
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_download),
                                            contentDescription = null,
                                            modifier = Modifier.size(FleetTokens.IconSize.S).padding(end = FleetTokens.Spacing.XS),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(stringResource(Res.string.vehicle_docs_download))
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onDownloadClick()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            painter = painterResource(Res.drawable.ic_refresh),
                                            contentDescription = null,
                                            modifier = Modifier.size(FleetTokens.IconSize.S).padding(end = FleetTokens.Spacing.XS),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(stringResource(Res.string.vehicle_docs_replace))
                                    }
                                },
                                onClick = {
                                    showOptionsMenu = false
                                    onReplaceClick()
                                }
                            )
                        }
                    }
                }

                // Action buttons row
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = FleetTokens.Spacing.L),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(FleetTokens.Spacing.S),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(
                        onClick = onPreviewClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = FleetTokens.Spacing.XS)
                            .heightIn(min = FleetTokens.Height.MinTouchTarget),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.S)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.S),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                        Text(
                            stringResource(Res.string.vehicle_docs_preview),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    FilledTonalButton(
                        onClick = onDownloadClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = FleetTokens.Spacing.XS)
                            .heightIn(min = FleetTokens.Height.MinTouchTarget),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.S)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.S).rotate(270f),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                        Text(
                            stringResource(Res.string.vehicle_docs_download),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    FilledTonalButton(
                        onClick = onReplaceClick,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = FleetTokens.Spacing.XS)
                            .heightIn(min = FleetTokens.Height.MinTouchTarget),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        contentPadding = PaddingValues(horizontal = FleetTokens.Spacing.S, vertical = FleetTokens.Spacing.S)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.S),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(FleetTokens.Spacing.XS))
                        Text(
                            stringResource(Res.string.vehicle_docs_replace),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    } else {
        // ==================== NOT UPLOADED DOCUMENT CARD ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(FleetTokens.Radius.XL),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = FleetTokens.Height.Divider,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FleetTokens.Spacing.L),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document icon
                Surface(
                    modifier = Modifier.size(FleetTokens.IconSize.XL),
                    shape = RoundedCornerShape(FleetTokens.Radius.ML),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_folder),
                            contentDescription = null,
                            modifier = Modifier.size(FleetTokens.IconSize.M),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))

                // Document info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.typeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))

                    Text(
                        text = stringResource(Res.string.vehicle_docs_not_uploaded),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))

                // Upload button
                FleetButton(
                    text = stringResource(Res.string.vehicle_docs_upload),
                    onClick = { showUploadConfirmDialog = true },
                    variant = ButtonVariant.PRIMARY,
                    size = ButtonSize.SMALL,
                    modifier = Modifier.widthIn(min = 80.dp)
                )
            }
        }
    }
}



