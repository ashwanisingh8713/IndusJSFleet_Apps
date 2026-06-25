package com.ijs.user.presentation.otp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetAccentIconChip
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetOtpInput
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.components.FleetTopAppBar
import com.indusjs.uicomponents.theme.FleetStatusColors
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint

/** Max width of the OTP form on Medium / Expanded so it doesn't stretch edge-to-edge. */
private val FormMaxWidth: Dp = 480.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    viewModel: OtpVerificationViewModel,
    onVerificationComplete: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var pendingSnackbar by remember { mutableStateOf<com.indusjs.uicomponents.components.UiText?>(null) }
    pendingSnackbar?.let { uiText ->
        val message = uiText.resolve()
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            pendingSnackbar = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is OtpVerificationContract.Effect.ShowSnackbar -> pendingSnackbar = effect.message
                is OtpVerificationContract.Effect.VerificationComplete -> onVerificationComplete()
                is OtpVerificationContract.Effect.NavigateToLogin -> onNavigateToLogin()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FleetTopAppBar(
                title = stringResource(Res.string.otp_title),
                onNavigateBack = onNavigateToLogin
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            val widthModifier = if (rememberFleetBreakpoint().isAtLeastMedium) {
                Modifier.widthIn(max = FormMaxWidth)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = Modifier
                    .then(widthModifier)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = FleetTokens.Spacing.L),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

                val subtitleRes = when {
                    state.needsEmailVerification && state.needsMobileVerification -> Res.string.otp_subtitle
                    state.needsEmailVerification -> Res.string.otp_subtitle_email_only
                    state.needsMobileVerification -> Res.string.otp_subtitle_mobile_only
                    else -> Res.string.otp_subtitle
                }
                Text(
                    text = stringResource(subtitleRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))

                // ── Email OTP Section (only if email needs verification) ──
                if (state.needsEmailVerification) {
                    OtpSection(
                        iconRes = Res.drawable.ic_email,
                        title = stringResource(Res.string.otp_email_section_title),
                        subtitle = state.email,
                        otpValue = state.emailOtp,
                        onOtpChange = { viewModel.sendIntent(OtpVerificationContract.Intent.UpdateEmailOtp(it)) },
                        isVerified = state.isEmailVerified,
                        isVerifying = state.isEmailVerifying,
                        isResending = state.isResendingEmail,
                        error = state.emailError?.resolve(),
                        onVerify = { viewModel.sendIntent(OtpVerificationContract.Intent.VerifyEmail) },
                        onResend = { viewModel.sendIntent(OtpVerificationContract.Intent.ResendEmailOtp) },
                        verifyButtonText = stringResource(Res.string.otp_verify_email),
                        resendButtonText = stringResource(Res.string.otp_resend_email),
                        verifiedLabel = stringResource(Res.string.otp_email_verified),
                        otpLength = 6
                    )
                }

                if (state.needsEmailVerification && state.needsMobileVerification) {
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
                }

                // ── Mobile OTP Section (only if mobile needs verification) ──
                if (state.needsMobileVerification) {
                    OtpSection(
                        iconRes = Res.drawable.ic_phone,
                        title = stringResource(Res.string.otp_mobile_section_title),
                        subtitle = state.mobile,
                        otpValue = state.mobileOtp,
                        onOtpChange = { viewModel.sendIntent(OtpVerificationContract.Intent.UpdateMobileOtp(it)) },
                        isVerified = state.isMobileVerified,
                        isVerifying = state.isMobileVerifying,
                        isResending = state.isResendingMobile,
                        error = state.mobileError?.resolve(),
                        onVerify = { viewModel.sendIntent(OtpVerificationContract.Intent.VerifyMobile) },
                        onResend = { viewModel.sendIntent(OtpVerificationContract.Intent.ResendMobileOtp) },
                        verifyButtonText = stringResource(Res.string.otp_verify_mobile),
                        resendButtonText = stringResource(Res.string.otp_resend_mobile),
                        verifiedLabel = stringResource(Res.string.otp_mobile_verified),
                        otpLength = 4
                    )
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))
                    Text(
                        text = error.resolve(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL))
            }
        }
    }
}

@Composable
private fun OtpSection(
    iconRes: DrawableResource,
    title: String,
    subtitle: String,
    otpValue: String,
    onOtpChange: (String) -> Unit,
    isVerified: Boolean,
    isVerifying: Boolean,
    isResending: Boolean,
    error: String?,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    verifyButtonText: String,
    resendButtonText: String,
    verifiedLabel: String,
    otpLength: Int
) {
    FleetSectionCard(modifier = Modifier.fillMaxWidth()) {
        // Header: icon anchor + title/subtitle, with a "Verified" pill once done.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FleetAccentIconChip(
                accent = MaterialTheme.colorScheme.primary,
                chipSize = 40.dp,
                iconSize = FleetTokens.IconSize.M,
                iconRes = iconRes
            )
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (isVerified) {
                VerifiedPill()
            }
        }

        if (isVerified) {
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.S))
            Text(
                text = verifiedLabel,
                style = MaterialTheme.typography.bodySmall,
                color = FleetStatusColors.ProfitGreen
            )
        } else {
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            FleetOtpInput(
                value = otpValue,
                onValueChange = onOtpChange,
                length = otpLength,
                enabled = !isVerifying,
                isError = error != null
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.M)
            ) {
                FleetButton(
                    text = resendButtonText,
                    onClick = onResend,
                    variant = ButtonVariant.SECONDARY,
                    isLoading = isResending,
                    enabled = !isResending && !isVerifying,
                    modifier = Modifier.weight(1f)
                )
                FleetButton(
                    text = verifyButtonText,
                    onClick = onVerify,
                    variant = ButtonVariant.PRIMARY,
                    isLoading = isVerifying,
                    enabled = otpValue.length == otpLength && !isVerifying,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VerifiedPill() {
    Surface(
        color = FleetStatusColors.ProfitGreen.copy(alpha = 0.14f),
        shape = RoundedCornerShape(FleetTokens.Radius.Pill)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = FleetTokens.Spacing.M,
                vertical = FleetTokens.Spacing.XS
            )
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_check_circle),
                contentDescription = null,
                tint = FleetStatusColors.ProfitGreen,
                modifier = Modifier.size(FleetTokens.IconSize.S)
            )
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.XXS))
            Text(
                text = stringResource(Res.string.otp_verified_badge),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = FleetStatusColors.ProfitGreen
            )
        }
    }
}
