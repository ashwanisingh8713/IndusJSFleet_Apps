package com.ijs.team.presentation

import androidx.compose.runtime.Composable
import com.ijs.team.domain.entity.TeamMemberRole
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.team_iam_role_admin_title
import indusjsfleet.ijs_ui_components_lib.generated.resources.team_iam_role_user_title
import indusjsfleet.ijs_ui_components_lib.generated.resources.team_role_owner
import org.jetbrains.compose.resources.stringResource

@Composable
fun TeamMemberRole.localizedDisplayName(): String = when (this) {
    TeamMemberRole.OWNER -> stringResource(Res.string.team_role_owner)
    TeamMemberRole.ADMIN -> stringResource(Res.string.team_iam_role_admin_title)
    TeamMemberRole.USER -> stringResource(Res.string.team_iam_role_user_title)
}
