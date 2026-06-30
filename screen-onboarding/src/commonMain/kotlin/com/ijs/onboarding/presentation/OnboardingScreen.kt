package com.ijs.onboarding.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.indusjs.uicomponents.components.ButtonSize
import com.indusjs.uicomponents.components.ButtonVariant
import com.indusjs.uicomponents.components.FleetButton
import com.indusjs.uicomponents.theme.FleetTokens
import com.indusjs.uicomponents.theme.isAtLeastMedium
import com.indusjs.uicomponents.theme.rememberFleetBreakpoint
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Max width for the centered onboarding content on Medium/Expanded screens
 * (tablet/web). Matches the auth-flow form width used by LoginScreen so the
 * intro carousel stays visually consistent with sign-in.
 */
private val CONTENT_MAX_WIDTH = 480.dp

/**
 * Data class representing a single onboarding page.
 */
private data class OnboardingPage(
    val titleRes: StringResource,
    val descriptionRes: StringResource,
    val iconRes: DrawableResource
)

/**
 * Onboarding Screen shown on first app launch.
 *
 * Features:
 * - 4-page horizontal pager with fleet management highlights
 * - Skip button to bypass, Get Started button on last page
 * - Animated page indicator dots
 * - Persists completion status to prevent re-showing
 */
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { state.totalPages })
    val scope = rememberCoroutineScope()

    // Define onboarding pages
    val pages = remember {
        listOf(
            OnboardingPage(
                titleRes = Res.string.onboarding_page1_title,
                descriptionRes = Res.string.onboarding_page1_description,
                iconRes = Res.drawable.ic_vehicle
            ),
            OnboardingPage(
                titleRes = Res.string.onboarding_page2_title,
                descriptionRes = Res.string.onboarding_page2_description,
                iconRes = Res.drawable.ic_driver
            ),
            OnboardingPage(
                titleRes = Res.string.onboarding_page3_title,
                descriptionRes = Res.string.onboarding_page3_description,
                iconRes = Res.drawable.ic_trip
            ),
            OnboardingPage(
                titleRes = Res.string.onboarding_page4_title,
                descriptionRes = Res.string.onboarding_page4_description,
                iconRes = Res.drawable.ic_cost
            )
        )
    }

    // Sync pager state with ViewModel state
    LaunchedEffect(pagerState.currentPage) {
        viewModel.sendIntent(OnboardingContract.Intent.GoToPage(pagerState.currentPage))
    }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnboardingContract.Effect.NavigateToLogin -> onComplete()
                is OnboardingContract.Effect.AnimateToPage -> {
                    scope.launch {
                        pagerState.animateScrollToPage(effect.page)
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            val breakpoint = rememberFleetBreakpoint()
            // Compact: full-width. Medium/Expanded: centered, constrained column.
            val contentWidthModifier = if (breakpoint.isAtLeastMedium) {
                Modifier.widthIn(max = CONTENT_MAX_WIDTH)
            } else {
                Modifier.fillMaxWidth()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .then(contentWidthModifier)
            ) {
                // Top bar with Skip button
                OnboardingTopBar(
                    isLastPage = state.isLastPage,
                    onSkip = { viewModel.sendIntent(OnboardingContract.Intent.Skip) }
                )

                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { pageIndex ->
                    OnboardingPageContent(
                        page = pages[pageIndex],
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Bottom section with dots and buttons
                OnboardingBottomSection(
                    currentPage = state.currentPage,
                    totalPages = state.totalPages,
                    isLastPage = state.isLastPage,
                    onNext = { viewModel.sendIntent(OnboardingContract.Intent.NextPage) },
                    onGetStarted = { viewModel.sendIntent(OnboardingContract.Intent.GetStarted) },
                    modifier = Modifier.padding(FleetTokens.Spacing.XL)
                )
            }
        }
    }
}

/**
 * Top bar with Skip button.
 */
@Composable
private fun OnboardingTopBar(
    isLastPage: Boolean,
    onSkip: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = FleetTokens.Spacing.L,
                vertical = FleetTokens.Spacing.M
            ),
        horizontalArrangement = Arrangement.End
    ) {
        if (!isLastPage) {
            // Wrap so the ghost button measures its content width instead of
            // stretching full-width on Compact breakpoints.
            Box(modifier = Modifier.wrapContentWidth()) {
                FleetButton(
                    text = stringResource(Res.string.skip),
                    onClick = onSkip,
                    variant = ButtonVariant.GHOST
                )
            }
        }
    }
}

/**
 * Individual onboarding page content with icon, title, and description.
 */
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = FleetTokens.Spacing.XXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration: icon inside a colored circle. Sizes come from
        // FleetTokens.IconSize so the illustration scales with the design system.
        Box(
            modifier = Modifier
                .size(FleetTokens.IconSize.XXL)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(page.iconRes),
                contentDescription = stringResource(page.titleRes),
                modifier = Modifier.size(FleetTokens.IconSize.XL),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.XXXL))

        // Title
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(FleetTokens.Spacing.L))

        // Description
        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Bottom section with page indicator dots and navigation buttons.
 */
@Composable
private fun OnboardingBottomSection(
    currentPage: Int,
    totalPages: Int,
    isLastPage: Boolean,
    onNext: () -> Unit,
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Page indicator dots
        PageIndicator(
            currentPage = currentPage,
            totalPages = totalPages,
            modifier = Modifier.padding(bottom = FleetTokens.Spacing.XXL)
        )

        // Action button (Next on intermediate pages, Get Started on last page)
        FleetButton(
            text = if (isLastPage) {
                stringResource(Res.string.onboarding_get_started)
            } else {
                stringResource(Res.string.next)
            },
            onClick = if (isLastPage) onGetStarted else onNext,
            variant = ButtonVariant.PRIMARY,
            size = ButtonSize.LARGE,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Animated page indicator dots.
 */
@Composable
private fun PageIndicator(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(FleetTokens.Spacing.S),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) FleetTokens.Spacing.XL else FleetTokens.Spacing.S,
                animationSpec = tween(300)
            )
            val color by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                animationSpec = tween(300)
            )

            Box(
                modifier = Modifier
                    .width(width)
                    .height(FleetTokens.Spacing.S)
                    .clip(RoundedCornerShape(FleetTokens.Radius.M))
                    .background(color)
            )
        }
    }
}
