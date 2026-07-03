package com.ijs.user.presentation.language

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.components.FleetSectionCard
import com.indusjs.uicomponents.i18n.AppLanguage
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.ic_fleet_logo
import org.jetbrains.compose.resources.painterResource

/**
 * f6a — first-launch language picker, shown ONCE before Sign-In when no language is chosen yet.
 *
 * All copy here is INTENTIONALLY bilingual literal (both scripts visible pre-choice) rather than
 * `stringResource`, since the user hasn't picked a language yet. Preselects from the system locale.
 * [onConfirm] persists the choice (via the app language controller) and routes on to Login.
 */
@Composable
fun LanguagePickerScreen(
    onConfirm: (AppLanguage) -> Unit,
    initial: AppLanguage? = null,
) {
    val systemDefault = remember(initial) {
        initial ?: if (Locale.current.language.lowercase().startsWith("hi")) AppLanguage.HI else AppLanguage.EN
    }
    var selected by remember { mutableStateOf(systemDefault) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FleetTokens.Spacing.XXL),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Logo — indigo rounded-square placeholder (matches the Sign-In screen).
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(FleetTokens.Radius.XL))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_fleet_logo),
                    contentDescription = null,
                    modifier = Modifier.size(FleetTokens.IconSize.L),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XL))
            // Both scripts, pre-choice.
            Text(
                text = "Choose your language",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XS))
            Text(
                text = "अपनी भाषा चुनें",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL))

            LanguageOptionCard(
                label = "English",
                selected = selected == AppLanguage.EN,
                onClick = { selected = AppLanguage.EN }
            )
            Spacer(modifier = Modifier.height(FleetTokens.Spacing.M))
            LanguageOptionCard(
                label = "हिंदी",
                selected = selected == AppLanguage.HI,
                onClick = { selected = AppLanguage.HI }
            )

            Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXL))

            FleetButton(
                text = "Continue · जारी रखें",
                onClick = { onConfirm(selected) },
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Large selectable card for one language, styled on the app's section-card + a leading radio. */
@Composable
private fun LanguageOptionCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    FleetSectionCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        // Selected fill = the primaryContainer token (#E1E4FF) exactly — not a primary tint — to match
        // the plan-card selection treatment (DDD token-precision).
        containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            FleetTokens.Border.Hairline,
            if (selected) accent else MaterialTheme.colorScheme.outlineVariant
        ),
        contentPadding = FleetTokens.Spacing.L
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = FleetTokens.Height.MinTouchTarget),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(FleetTokens.Spacing.S))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
