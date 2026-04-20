package com.ijs.team.domain.entity

/**
 * IAM tenant role returned by GET /team/members/roles (e.g. admin, user).
 */
data class AssignableTeamRole(
    val id: String,
    val name: String,
    val description: String
)
