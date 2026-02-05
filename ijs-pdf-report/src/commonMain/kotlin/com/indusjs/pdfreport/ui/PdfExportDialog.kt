package com.indusjs.pdfreport.ui

import androidx.compose.runtime.Composable
import com.indusjs.pdfreport.model.PdfExportResult

/**
 * Composable dialog for PDF export result.
 * Shows loading state, success with Open/Share options, or error.
 */
@Composable
expect fun PdfExportDialog(
    result: PdfExportResult?,
    isLoading: Boolean = false,
    loadingMessage: String = "Generating PDF...",
    onOpen: () -> Unit,
    onShare: () -> Unit,
    onDismiss: () -> Unit
)
