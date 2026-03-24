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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import indusjsfleet.ijs_ui_components_lib.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

/**
 * Data class representing a single onboarding page.
 */
private data class OnboardingPage(
    val title: String,
    val description: String,
    val iconRes: Any // Res.drawable reference
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
                title = "Fleet Management",
                description = "Manage your entire fleet of vehicles from a single dashboard. Track vehicle status, maintenance schedules, and documents — all in one place.",
                iconRes = Res.drawable.ic_vehicle
            ),
            OnboardingPage(
                title = "Driver Management",
                description = "Keep track of your drivers, their licenses, assignments, and costs. Ensure your fleet always has qualified drivers on the road.",
                iconRes = Res.drawable.ic_driver
            ),
            OnboardingPage(
                title = "Trip Planning",
                description = "Plan and manage trips with route optimization, cargo tracking, and real-time status updates. From planning to delivery — stay in control.",
                iconRes = Res.drawable.ic_trip
            ),
            OnboardingPage(
                title = "Cost & Revenue Tracking",
                description = "Track fuel, tolls, maintenance, and all operational costs. Get detailed profit & loss reports to maximize your fleet's profitability.",
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
        Column(
            modifier = Modifier.fillMaxSize()
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
                modifier = Modifier.padding(24.dp)
            )
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.End
    ) {
        if (!isLastPage) {
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon with colored background circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            @Suppress("UNCHECKED_CAST")
            Icon(
                painter = painterResource(page.iconRes as org.jetbrains.compose.resources.DrawableResource),
                contentDescription = page.title,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
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
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Action button
        Button(
            onClick = if (isLastPage) onGetStarted else onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (isLastPage) "Get Started" else "Next",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
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
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalPages) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(
                targetValue = if (isSelected) 24.dp else 8.dp,
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
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

