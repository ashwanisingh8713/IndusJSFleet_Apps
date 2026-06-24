package com.ijs.vehicle.presentation

import androidx.compose.runtime.Composable
import com.ijs.vehicle.domain.entity.DocumentType
import indusjsfleet.ijs_ui_components_lib.generated.resources.Res
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_driver_license
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_fitness
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_insurance
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_other
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_permit
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_puc
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_rc
import indusjsfleet.ijs_ui_components_lib.generated.resources.vehicle_doc_type_road_tax
import org.jetbrains.compose.resources.stringResource

/**
 * Composable resolvers for vehicle domain enums whose display names are
 * rendered in @Composable scope. Mirrors the TeamLocalizedLabels pattern.
 */
@Composable
fun DocumentType.localizedDisplayName(): String = when (this) {
    DocumentType.REGISTRATION_CERTIFICATE -> stringResource(Res.string.vehicle_doc_type_rc)
    DocumentType.INSURANCE -> stringResource(Res.string.vehicle_doc_type_insurance)
    DocumentType.PUC_CERTIFICATE -> stringResource(Res.string.vehicle_doc_type_puc)
    DocumentType.FITNESS_CERTIFICATE -> stringResource(Res.string.vehicle_doc_type_fitness)
    DocumentType.ROAD_TAX -> stringResource(Res.string.vehicle_doc_type_road_tax)
    DocumentType.PERMIT -> stringResource(Res.string.vehicle_doc_type_permit)
    DocumentType.DRIVER_LICENSE -> stringResource(Res.string.vehicle_doc_type_driver_license)
    DocumentType.OTHER -> stringResource(Res.string.vehicle_doc_type_other)
}
