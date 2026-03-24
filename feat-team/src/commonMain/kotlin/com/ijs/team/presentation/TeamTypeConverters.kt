package com.ijs.team.presentation

import com.indusjs.fleet.core.model.shared.CaretakerInfo
import com.ijs.team.data.model.TeamMemberDto

/**
 * Cross-feature type conversion extensions for Team entities.
 *
 * These converters allow other feature modules (vehicle, driver, trip)
 * to convert TeamMemberDto into shared contracts without depending
 * on sharedUI's presentation layer.
 */

/**
 * Convert a [TeamMemberDto] to a [CaretakerInfo] shared contract.
 */
fun TeamMemberDto.toCaretakerInfo() = CaretakerInfo(
    id = id.toString(),
    name = "$firstName $lastName",
    role = role,
    mobile = mobile,
    email = email
)

/**
 * Convert a list of [TeamMemberDto] to a list of [CaretakerInfo].
 */
fun List<TeamMemberDto>.toCaretakerInfoList() = map { it.toCaretakerInfo() }

