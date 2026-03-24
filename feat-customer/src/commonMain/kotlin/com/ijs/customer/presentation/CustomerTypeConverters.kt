package com.ijs.customer.presentation

import com.indusjs.fleet.core.model.shared.SelectableCustomer
import com.ijs.customer.domain.entity.Customer

/**
 * Cross-feature type conversion extensions for Customer entities.
 */

/**
 * Convert a [Customer] domain entity to a [SelectableCustomer] shared contract.
 */
fun Customer.toSelectableCustomer() = SelectableCustomer(
    id = id,
    companyName = companyName,
    personName = personName,
    primaryContact = primaryContact,
    email = email ?: "",
    gstNumber = gstNumber ?: ""
)

/**
 * Convert a list of [Customer] to a list of [SelectableCustomer].
 */
fun List<Customer>.toSelectableCustomerList() = map { it.toSelectableCustomer() }

