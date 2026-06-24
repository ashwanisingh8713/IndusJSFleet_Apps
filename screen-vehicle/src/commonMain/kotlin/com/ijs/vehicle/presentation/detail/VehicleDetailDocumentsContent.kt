package com.ijs.vehicle.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.error.FleetErrorContext
import com.indusjs.fleet.core.network.ApiConfig
import com.indusjs.uicomponents.components.ErrorContent
import com.indusjs.uicomponents.components.FleetMetricTile
import com.indusjs.uicomponents.components.LoadingContent
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
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Documents Summary
                if (summary != null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                DocumentStatItem(
                                    count = summary.uploaded.toString(),
                                    label = stringResource(Res.string.vehicle_docs_stat_uploaded),
                                    icon = "📄"
                                )
                                DocumentStatItem(
                                    count = summary.notUploaded.toString(),
                                    label = stringResource(Res.string.vehicle_docs_stat_missing),
                                    icon = "⚠️"
                                )
                                DocumentStatItem(
                                    count = summary.expiringSoon.toString(),
                                    label = stringResource(Res.string.vehicle_docs_stat_expiring),
                                    icon = "⏰"
                                )
                            }
                        }
                    }
                }

                // Alert Documents
                if (alertDocs.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "⚠️ ${stringResource(Res.string.vehicle_docs_attention)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
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
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
internal fun DocumentStatItem(count: String, label: String, icon: String) {
    FleetMetricTile(value = count, label = label, emoji = icon, showBackground = false, centered = true)
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
            icon = { Text("📤", style = MaterialTheme.typography.headlineMedium) },
            title = { Text(stringResource(Res.string.vehicle_docs_upload_title, doc.typeName)) },
            text = {
                Column {
                    Text(stringResource(Res.string.vehicle_docs_upload_message))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = doc.typeName,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (doc.isRequired) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(Res.string.vehicle_docs_required),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.vehicle_docs_ensure_readable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
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
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isExpired -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
                    isExpiringSoon -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with gradient accent
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
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
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Document icon with check badge
                    Box(modifier = Modifier.padding(top = 4.dp)) {
                        Surface(
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = when {
                                isExpired -> MaterialTheme.colorScheme.errorContainer
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            tonalElevation = 2.dp
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
                                .offset(x = 4.dp, y = 4.dp),
                            shape = CircleShape,
                            color = when {
                                isExpired -> MaterialTheme.colorScheme.error
                                isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = when {
                                        isExpired -> "!"
                                        isExpiringSoon -> "⏰"
                                        else -> "✓"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Document details
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = doc.typeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status row with icon
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = when {
                                    isExpired -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                    isExpiringSoon -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)
                                    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                }
                            ) {
                                Text(
                                    text = when {
                                        isExpired -> "⚠️ ${stringResource(Res.string.vehicle_docs_expired)}"
                                        isExpiringSoon -> "⏰ ${stringResource(Res.string.vehicle_docs_expires_in, daysLeft)}"
                                        docInfo?.expiryDate != null -> "✓ ${stringResource(Res.string.vehicle_docs_valid_till, formatIsoDateToDisplay(docInfo.expiryDate))}"
                                        else -> "✓ ${docInfo?.statusLabel ?: stringResource(Res.string.vehicle_docs_uploaded)}"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        isExpired -> MaterialTheme.colorScheme.error
                                        isExpiringSoon -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.primary
                                    },
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Document number if available
                        if (!doc.document?.documentNumber.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🔢",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(Res.string.vehicle_docs_doc_number, doc.document?.documentNumber.orEmpty()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Upload date if available
                        doc.document?.uploadedAt?.takeIf { it > 0L }?.let { uploadedAt ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "📅",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.width(6.dp))
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
                                        Text("👁️", modifier = Modifier.padding(end = 8.dp))
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
                                        Text("⬇️", modifier = Modifier.padding(end = 8.dp))
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
                                        Text("🔄", modifier = Modifier.padding(end = 8.dp))
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
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilledTonalButton(
                        onClick = onPreviewClick,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_search),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(Res.string.vehicle_docs_preview),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    FilledTonalButton(
                        onClick = onDownloadClick,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp).rotate(270f),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(Res.string.vehicle_docs_download),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    FilledTonalButton(
                        onClick = onReplaceClick,
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_refresh),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            stringResource(Res.string.vehicle_docs_replace),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    } else {
        // ==================== NOT UPLOADED DOCUMENT CARD ====================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Document icon
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "📁",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Document info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.typeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(Res.string.vehicle_docs_not_uploaded),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Upload button - streamlined without icon
                Button(
                    onClick = { showUploadConfirmDialog = true },
                    modifier = Modifier
                        .height(38.dp)
                        .widthIn(min = 80.dp),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        stringResource(Res.string.vehicle_docs_upload),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}



