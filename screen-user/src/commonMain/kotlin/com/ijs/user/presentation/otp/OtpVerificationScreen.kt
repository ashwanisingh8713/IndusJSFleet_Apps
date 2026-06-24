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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.stringResource
import com.indusjs.uicomponents.components.FieldType
import com.indusjs.uicomponents.components.FleetInputField
import com.indusjs.uicomponents.components.FleetTopAppBar

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

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

            Spacer(modifier = Modifier.height(32.dp))

            // ── Email OTP Section (only if email needs verification) ──
            if (state.needsEmailVerification) {
                OtpSection(
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
                    otpLength = 6,
                    placeholder = stringResource(Res.string.otp_placeholder)
                )
            }

            if (state.needsEmailVerification && state.needsMobileVerification) {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── Mobile OTP Section (only if mobile needs verification) ──
            if (state.needsMobileVerification) {
                OtpSection(
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
                    otpLength = 4,
                    placeholder = stringResource(Res.string.otp_placeholder_mobile)
                )
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error.resolve(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun OtpSection(
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
    otpLength: Int = 6,
    placeholder: String = ""
) {
    // Tinted card -> FleetSectionCard with the original conditional tint as containerColor,
    // border = null. The bespoke header Row (title/subtitle + solid "Verified" pill) and the
    // OTP input/buttons stay custom because FleetSectionHeader can't express the trailing pill.
    com.indusjs.uicomponents.components.FleetSectionCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (isVerified)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = null,
        contentPadding = 20.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.otp_verified_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }

        if (!isVerified) {
            Spacer(modifier = Modifier.height(16.dp))

            FleetInputField(
                value = otpValue,
                onValueChange = onOtpChange,
                fieldType = FieldType.NUMBER,
                modifier = Modifier.fillMaxWidth(),
                label = stringResource(Res.string.otp_enter_code),
                placeholder = placeholder,
                isError = error != null,
                errorMessage = error,
                enabled = !isVerifying
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onResend,
                    enabled = !isResending && !isVerifying,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isResending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = resendButtonText, style = MaterialTheme.typography.labelMedium)
                    }
                }

                Button(
                    onClick = onVerify,
                    enabled = otpValue.length == otpLength && !isVerifying,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isVerifying) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = verifyButtonText, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
