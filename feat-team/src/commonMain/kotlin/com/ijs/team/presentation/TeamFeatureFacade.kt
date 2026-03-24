package com.ijs.team.presentation

import androidx.compose.runtime.Composable
import com.ijs.team.presentation.create.CreateTeamMemberScreen
import com.ijs.team.presentation.create.CreateTeamMemberViewModel
import com.ijs.team.presentation.detail.TeamMemberDetailScreen
import com.ijs.team.presentation.detail.TeamMemberDetailViewModel
import com.ijs.team.presentation.list.TeamListScreen
import com.ijs.team.presentation.list.TeamListViewModel

/**
 * Facade for the Team feature module.
 *
 * Provides @Composable entry points for each team screen.
 * The sharedUI module uses this facade to render team screens
 * without knowing internal implementation details.
 *
 * Navigation is handled via lambda callbacks — this module
 * never imports FleetRoute or any navigation infrastructure.
 *
 * ViewModels are passed from the outside (created by sharedUI's DI layer)
 * to maintain the existing rememberViewModel pattern.
 */
object TeamFeatureFacade {

    /**
     * Entry point for the Team Members List screen.
     */
    @Composable
    fun TeamListEntry(
        viewModel: TeamListViewModel,
        onNavigateBack: () -> Unit,
        onNavigateToCreateMember: () -> Unit,
        onNavigateToMemberDetail: (String) -> Unit
    ) {
        TeamListScreen(
            viewModel = viewModel,
            onNavigateBack = onNavigateBack,
            onNavigateToCreateMember = onNavigateToCreateMember,
            onNavigateToMemberDetail = onNavigateToMemberDetail
        )
    }

    /**
     * Entry point for the Create Team Member screen.
     */
    @Composable
    fun CreateTeamMemberEntry(
        viewModel: CreateTeamMemberViewModel,
        excludeGeneralManager: Boolean = false,
        onNavigateBack: () -> Unit
    ) {
        CreateTeamMemberScreen(
            viewModel = viewModel,
            excludeGeneralManager = excludeGeneralManager,
            onNavigateBack = onNavigateBack
        )
    }

    /**
     * Entry point for the Team Member Detail screen.
     */
    @Composable
    fun TeamMemberDetailEntry(
        viewModel: TeamMemberDetailViewModel,
        memberId: String,
        onNavigateBack: () -> Unit
    ) {
        TeamMemberDetailScreen(
            viewModel = viewModel,
            memberId = memberId,
            onNavigateBack = onNavigateBack
        )
    }
}

