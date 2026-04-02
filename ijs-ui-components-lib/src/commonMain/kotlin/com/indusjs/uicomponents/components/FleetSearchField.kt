package com.indusjs.uicomponents.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import com.indusjs.uicomponents.theme.FleetTokens
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * The canonical search field for the entire application.
 *
 * Built on [FleetInputField] with [FieldType.SEARCH], inheriting its
 * adaptive width, [imePadding], and single-line enforcement.
 *
 * ### Search icon
 * Always present as the leading icon. Not configurable.
 *
 * ### Clear button
 * A trailing icon (X) appears only when [query] is non-empty.
 * Tapping it clears the field and calls [onQueryChange] with "".
 *
 * ### Debounce
 * The component internally debounces emissions to [onQueryChange] by
 * 300ms using [LaunchedEffect] + [snapshotFlow]. Callers receive
 * debounced values and should not implement debounce themselves.
 *
 * ### Single-line
 * Always enforced via [FieldType.SEARCH].
 *
 * @param query Current search query (external state).
 * @param onQueryChange Debounced callback when the query changes.
 * @param modifier Modifier for the text field.
 * @param placeholder Placeholder text.
 * @param onSearch Optional callback for keyboard search action.
 * @param accessibilityLabel Content description for screen readers.
 */
@OptIn(FlowPreview::class)
@Composable
fun FleetSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(Res.string.search_hint),
    onSearch: (() -> Unit)? = null,
    accessibilityLabel: String = ""
) {
    val resolvedAccessibilityLabel = accessibilityLabel.ifBlank { stringResource(Res.string.search) }
    val focusManager = LocalFocusManager.current

    var localQuery by remember(query) { mutableStateOf(query) }

    LaunchedEffect(Unit) {
        snapshotFlow { localQuery }
            .debounce(300L)
            .distinctUntilChanged()
            .collect { debounced ->
                onQueryChange(debounced)
            }
    }

    FleetInputField(
        value = localQuery,
        onValueChange = { localQuery = it },
        fieldType = FieldType.SEARCH,
        modifier = modifier,
        placeholder = placeholder,
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(FleetTokens.IconSize.M)
            )
        },
        trailingIcon = {
            AnimatedVisibility(
                visible = localQuery.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                IconButton(
                    onClick = {
                        localQuery = ""
                        onQueryChange("")
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_close),
                        contentDescription = stringResource(Res.string.clear_search),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FleetTokens.IconSize.M)
                    )
                }
            }
        },
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch?.invoke()
                focusManager.clearFocus()
            }
        ),
        accessibilityLabel = resolvedAccessibilityLabel
    )
}
