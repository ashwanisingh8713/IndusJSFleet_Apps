#!/usr/bin/env python3
"""Add 'private val logger: FleetLogger' constructor parameter to classes that use logger.X() but don't have it."""
import re
import os

BASE = "/Users/ashwani/KotlinMultiplateform/IndusJSFleet"

# Map of files that need constructor 'logger: FleetLogger' added
# Format: (file_path, class_pattern_to_match)
files = [
    # Data layer repos/datasources that need logger in constructor
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/data/repository/TripPaymentRepositoryImpl.kt",
    "screen-report/src/commonMain/kotlin/com/ijs/reports/data/datasource/ReportsRemoteDataSource.kt",
    "screen-report/src/commonMain/kotlin/com/ijs/reports/data/repository/ReportsRepositoryImpl.kt",
    "screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt",
    "screen-finance/src/commonMain/kotlin/com/ijs/finance/data/repository/VehicleFinanceRepositoryImpl.kt",
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/data/datasource/CustomerRemoteDataSource.kt",
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/data/repository/CustomerRepositoryImpl.kt",
    "screen-team/src/commonMain/kotlin/com/ijs/team/data/repository/TeamRepositoryImpl.kt",
    # Presentation layer ViewModels/handlers that need logger
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailViewModel.kt",
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailPdfExporter.kt",
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailTripsHandler.kt",
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailFinancialsHandler.kt",
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailPaymentsHandler.kt",
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/presentation/PaymentDetailViewModel.kt",
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/presentation/PaymentsViewModel.kt",
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/presentation/AddPaymentViewModel.kt",
    "screen-report/src/commonMain/kotlin/com/ijs/reports/presentation/vehicle/VehiclePLViewModel.kt",
    "screen-report/src/commonMain/kotlin/com/ijs/reports/presentation/ReportsViewModel.kt",
    "screen-finance/src/commonMain/kotlin/com/ijs/finance/presentation/VehicleFinanceViewModel.kt",
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/cost/TripCostEntryViewModel.kt",
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/detail/TripDetailDataLoader.kt",
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/detail/TripDetailActionHandler.kt",
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/detail/TripDetailViewModel.kt",
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/create/CreateTripViewModel.kt",
    "screen-driver/src/commonMain/kotlin/com/ijs/driver/data/repository/DriverRepositoryImpl.kt",
]

for rel_path in files:
    full_path = os.path.join(BASE, rel_path)
    if not os.path.exists(full_path):
        print(f"SKIP: {rel_path}")
        continue

    with open(full_path, "r") as f:
        content = f.read()

    # Check if logger is already a constructor param
    if "private val logger: FleetLogger" in content or "val logger: FleetLogger" in content:
        print(f"ALREADY HAS logger: {rel_path}")
        continue

    # Check if it uses logger.X( calls
    if "logger." not in content:
        print(f"NO logger usage: {rel_path}")
        continue

    # Strategy: find the closing ) of the constructor, add logger before it
    # Pattern: look for the last parameter line before ) : SomeInterface { or ) {
    # We look for pattern: "    private val XXXX: YYYY\n) : " or "    private val XXXX: YYYY,\n    ... dispatcherProvider\n) : "
    
    # Find class constructor - add logger param before the closing )
    # Match: something ending with\n) : ClassName { or\n) {
    modified = re.sub(
        r'(class \w+\([^)]*?)(,?\s*\n\)\s*(?::\s*\w+[^{]*)?\{)',
        lambda m: m.group(1) + ',\n    private val logger: FleetLogger' + m.group(2),
        content,
        count=1
    )

    if modified != content:
        with open(full_path, "w") as f:
            f.write(modified)
        print(f"ADDED logger param: {rel_path}")
    else:
        # Try alternative: class Foo(\n    params\n) {
        modified2 = re.sub(
            r'(class \w+\([^)]*?)\n(\) )',
            lambda m: m.group(1) + ',\n    private val logger: FleetLogger\n' + m.group(2),
            content,
            count=1
        )
        if modified2 != content:
            with open(full_path, "w") as f:
                f.write(modified2)
            print(f"ADDED logger param (alt): {rel_path}")
        else:
            print(f"COULD NOT ADD logger: {rel_path} (manual fix needed)")

print("\nDone!")

