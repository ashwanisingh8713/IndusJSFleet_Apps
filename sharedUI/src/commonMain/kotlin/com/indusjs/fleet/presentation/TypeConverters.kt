package com.indusjs.fleet.presentation

import com.indusjs.fleet.core.model.shared.CaretakerInfo
import com.indusjs.fleet.core.model.shared.SelectableCustomer
import com.ijs.customer.domain.entity.Customer
import com.ijs.team.data.model.TeamMemberDto

/**
 * Cross-feature type conversion extensions.
 *
 * TEMPORARY: These converters live here until each feature's presentation
 * layer is migrated into its respective feat-* module, at which point
 * the presentation code will use shared contracts directly and these
 * converters move to sharedUI/di/adapters/.
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

/**
 * Convert a [Customer] domain entity to a [SelectableCustomer] shared contract.
 */
fun Customer.toSelectableCustomer() = SelectableCustomer(
    id = id,
    companyName = companyName,
    personName = personName,
    primaryContact = primaryContact,
    email = email,
    gstNumber = gstNumber
)

/**
 * Convert a list of [Customer] to a list of [SelectableCustomer].
 */
fun List<Customer>.toSelectableCustomerList() = map { it.toSelectableCustomer() }

