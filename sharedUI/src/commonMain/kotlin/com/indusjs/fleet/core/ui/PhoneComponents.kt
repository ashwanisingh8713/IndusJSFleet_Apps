package com.indusjs.fleet.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.indusjs.fleet.core.util.rememberPhoneDialer
import org.jetbrains.compose.resources.painterResource
import indusjsfleet.sharedui.generated.resources.*

/**
 * A row displaying a phone number with a call icon.
 * Clicking the call icon opens the phone dialer.
 *
 * @param phoneNumber The phone number to display
 * @param label Optional label text (e.g., "Primary", "Mobile")
 * @param icon Optional emoji icon (e.g., "📱")
 * @param modifier Modifier for the row
 */
@Composable
fun ClickablePhoneRow(
    phoneNumber: String,
    label: String? = null,
    icon: String? = null,
    modifier: Modifier = Modifier
) {
    val dialPhone = rememberPhoneDialer(phoneNumber)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) {
            if (icon != null) {
                Text(icon, modifier = Modifier.width(24.dp))
            }
            Column {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = phoneNumber,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Call button
        IconButton(
            onClick = dialPhone,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_phone),
                contentDescription = "Call $phoneNumber",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * A compact phone chip that shows phone number with call capability.
 * Useful for list items where space is limited.
 *
 * @param phoneNumber The phone number to display
 * @param modifier Modifier for the chip
 */
@Composable
fun PhoneChip(
    phoneNumber: String,
    modifier: Modifier = Modifier
) {
    val dialPhone = rememberPhoneDialer(phoneNumber)

    Surface(
        onClick = dialPhone,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_phone),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = phoneNumber,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Enhanced info row with optional call button for phone numbers.
 *
 * @param label The label text
 * @param value The value text
 * @param isPhone If true, shows a call button
 * @param icon Optional emoji icon
 * @param modifier Modifier for the row
 */
@Composable
fun InfoRowWithCall(
    label: String,
    value: String,
    isPhone: Boolean = false,
    icon: String? = null,
    modifier: Modifier = Modifier
) {
    if (isPhone && value.isNotBlank()) {
        ClickablePhoneRow(
            phoneNumber = value,
            label = label,
            icon = icon,
            modifier = modifier
        )
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            if (icon != null) {
                Text(icon, modifier = Modifier.width(24.dp))
            }
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
