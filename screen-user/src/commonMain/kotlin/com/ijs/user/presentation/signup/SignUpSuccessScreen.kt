package com.ijs.user.presentation.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.FleetAccentIconChip
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTitledSectionCard
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import org.jetbrains.compose.resources.stringResource

/** Max width of the centered content column on Medium / Expanded breakpoints. */
private val ContentMaxWidth = 480.dp

@Composable
fun SignUpSuccessScreen(
    message: String,
    isResend: Boolean,
    onNavigateToLogin: () -> Unit = {}
) {
    Scaffold { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val widthConstraint = if (rememberFleetBreakpoint().isAtLeastMedium) {
                Modifier.widthIn(max = ContentMaxWidth)
            } else {
                Modifier.fillMaxWidth()
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = widthConstraint
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(FleetTokens.Spacing.XXL),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL))

                    FleetAccentIconChip(
                        accent = if (isResend) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                        chipSize = FleetTokens.IconSize.XXL,
                        iconSize = FleetTokens.IconSize.XL,
                        iconRes = if (isResend) Res.drawable.ic_email else Res.drawable.ic_check_circle
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                    Text(
                        text = if (isResend)
                            stringResource(Res.string.signup_success_resend_title)
                        else
                            stringResource(Res.string.signup_success_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    FleetSectionCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        border = null,
                        contentPadding = FleetTokens.Spacing.XL
                    ) {
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                    Text(
                        text = if (isResend)
                            stringResource(Res.string.signup_success_resend_description)
                        else
                            stringResource(Res.string.signup_success_description),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                    FleetTitledSectionCard(
                        title = stringResource(Res.string.signup_success_next_steps),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S)
                        ) {
                            Text(
                                text = stringResource(Res.string.signup_success_step_1),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(Res.string.signup_success_step_2),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(Res.string.signup_success_step_3),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL))

                    FleetButton(
                        text = stringResource(Res.string.signup_success_go_to_login),
                        onClick = onNavigateToLogin,
                        size = ButtonSize.LARGE,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                }
            }
        }
    }
}
