package com.indusjs.uicomponents.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Centralized dialog components for consistent UX across all screens.
 *
 * UX Finding #2:  "No unsaved changes warning on ANY form"
 * UX Finding #4:  "No delete confirmation on list screens"
 * UX Finding #19: "No logout confirmation"
 */

// =============================================
// Discard Changes Dialog
// =============================================

/**
 * Dialog shown when user attempts to leave a form with unsaved changes.
 *
 * Usage:
 * ```
 * var hasChanges by remember { mutableStateOf(false) }
 * DiscardChangesDialog(
 *     showDialog = showDiscardDialog,
 *     onDiscard = { onNavigateBack() },
 *     onKeepEditing = { showDiscardDialog = false }
 * )
 * ```
 *
 * @param showDialog Whether the dialog is visible.
 * @param onDiscard Callback when user confirms discarding changes.
 * @param onKeepEditing Callback when user chooses to keep editing.
 */
@Composable
fun DiscardChangesDialog(
    showDialog: Boolean,
    onDiscard: () -> Unit,
    onKeepEditing: () -> Unit
) {
    if (!showDialog) return

    val dialogShape = MaterialTheme.shapes.extraLarge
    AlertDialog(
        onDismissRequest = onKeepEditing,
        // §9.3 depth: 1px top-highlight so the dialog reads as raised on a dark surface (no-op in light).
        modifier = Modifier.fleetElevatedSurface(dialogShape, FleetElevation.Dialog),
        shape = dialogShape,
        icon = {
            Icon(
                painter = painterResource(Res.drawable.ic_warning),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(Res.string.discard_changes_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.discard_changes_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(
                    text = stringResource(Res.string.discard),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing) {
                Text(
                    text = stringResource(Res.string.keep_editing),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

// =============================================
// Generic Confirmation Dialog
// =============================================

/**
 * Reusable confirmation dialog for destructive or important actions.
 * Supports loading state while action is in progress.
 *
 * Usage:
 * ```
 * FleetConfirmationDialog(
 *     showDialog = state.showDeleteConfirmation,
 *     title = "Delete Vehicle",
 *     message = "Are you sure you want to delete this vehicle?",
 *     confirmText = "Delete",
 *     isDestructive = true,
 *     isLoading = state.isDeleting,
 *     onConfirm = { viewModel.sendIntent(Intent.ConfirmDelete) },
 *     onDismiss = { viewModel.sendIntent(Intent.HideDeleteConfirmation) }
 * )
 * ```
 *
 * @param showDialog Whether the dialog is visible.
 * @param title The dialog title.
 * @param message The dialog message/description.
 * @param confirmText Text for the confirm button.
 * @param dismissText Text for the dismiss button.
 * @param isDestructive If true, confirm button uses error color.
 * @param isLoading If true, shows loading indicator on confirm button.
 * @param onConfirm Callback when user confirms the action.
 * @param onDismiss Callback when user dismisses the dialog.
 */
@Composable
fun FleetConfirmationDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    confirmText: String = stringResource(Res.string.confirm),
    dismissText: String = stringResource(Res.string.cancel),
    isDestructive: Boolean = false,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!showDialog) return

    val dialogShape = MaterialTheme.shapes.extraLarge
    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        modifier = Modifier.fleetElevatedSurface(dialogShape, FleetElevation.Dialog),
        shape = dialogShape,
        icon = if (isDestructive) {
            {
                Icon(
                    painter = painterResource(Res.drawable.ic_warning),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            }
        } else null,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = confirmText,
                    color = if (isDestructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text(
                    text = dismissText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

// =============================================
// Logout Confirmation Dialog
// =============================================

/**
 * Pre-configured logout confirmation dialog.
 *
 * @param showDialog Whether the dialog is visible.
 * @param onConfirmLogout Callback when user confirms logout.
 * @param onDismiss Callback when user cancels.
 */
@Composable
fun LogoutConfirmationDialog(
    showDialog: Boolean,
    onConfirmLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    FleetConfirmationDialog(
        showDialog = showDialog,
        title = stringResource(Res.string.logout_confirmation_title),
        message = stringResource(Res.string.logout_confirmation_message),
        confirmText = stringResource(Res.string.profile_logout),
        isDestructive = true,
        onConfirm = onConfirmLogout,
        onDismiss = onDismiss
    )
}

// =============================================
// Delete Confirmation Dialog
// =============================================

/**
 * Pre-configured delete confirmation dialog with loading support.
 *
 * @param showDialog Whether the dialog is visible.
 * @param entityName Descriptive name of the item being deleted (e.g., "vehicle", "payment").
 * @param entityDetail Optional detail for identification (e.g., "MH-12-AB-1234").
 * @param isLoading Whether the delete operation is in progress.
 * @param onConfirmDelete Callback when user confirms deletion.
 * @param onDismiss Callback when user cancels.
 */
@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    entityName: String,
    entityDetail: String? = null,
    isLoading: Boolean = false,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = if (entityDetail != null) {
        stringResource(Res.string.delete_entity_confirmation_message, entityName, entityDetail)
    } else {
        stringResource(Res.string.delete_generic_confirmation_message, entityName)
    }

    FleetConfirmationDialog(
        showDialog = showDialog,
        title = stringResource(Res.string.delete_confirmation_title),
        message = message,
        confirmText = stringResource(Res.string.delete),
        isDestructive = true,
        isLoading = isLoading,
        onConfirm = onConfirmDelete,
        onDismiss = onDismiss
    )
}

// =============================================
// Status Toggle Confirmation Dialog
// =============================================

/**
 * Confirmation dialog for toggling an entity's active status.
 *
 * UX Finding #49: "Status toggle has no confirmation"
 *
 * @param showDialog Whether the dialog is visible.
 * @param entityName Name of the entity (e.g., "customer", "driver").
 * @param currentlyActive Current status — true means we're deactivating.
 * @param isLoading Whether the toggle operation is in progress.
 * @param onConfirm Callback when user confirms the toggle.
 * @param onDismiss Callback when user cancels.
 */
@Composable
fun StatusToggleConfirmationDialog(
    showDialog: Boolean,
    entityName: String,
    currentlyActive: Boolean,
    isLoading: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val action = if (currentlyActive) {
        stringResource(Res.string.deactivate)
    } else {
        stringResource(Res.string.activate)
    }

    FleetConfirmationDialog(
        showDialog = showDialog,
        title = "$action ${entityName.replaceFirstChar { it.uppercase() }}",
        message = stringResource(Res.string.status_toggle_confirmation_message, action, entityName),
        confirmText = action,
        isDestructive = currentlyActive,
        isLoading = isLoading,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
