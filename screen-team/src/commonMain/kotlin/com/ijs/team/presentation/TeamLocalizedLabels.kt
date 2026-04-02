package com.ijs.team.presentation

import androidx.compose.runtime.Composable
import com.ijs.team.domain.entity.TeamMemberRole
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.team_role_general_manager
import indusjsfleet.ijs_ui_components_lib.generated.resources.team_role_manager
import indusjsfleet.ijs_ui_components_lib.generated.resources.team_role_supervisor
import org.jetbrains.compose.resources.stringResource

@Composable
fun TeamMemberRole.localizedDisplayName(): String = when (this) {
    TeamMemberRole.GENERAL_MANAGER -> stringResource(Res.string.team_role_general_manager)
    TeamMemberRole.MANAGER -> stringResource(Res.string.team_role_manager)
    TeamMemberRole.SUPERVISOR -> stringResource(Res.string.team_role_supervisor)
}
