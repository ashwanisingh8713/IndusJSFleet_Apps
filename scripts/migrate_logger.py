#!/usr/bin/env python3
"""Migrate remaining files from Kermit Logger to FleetLogger."""
import re
import os

BASE = "/Users/ashwani/KotlinMultiplateform/IndusJSFleet"

files_to_migrate = {
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/data/repository/TripPaymentRepositoryImpl.kt": ("TAG_PAYMENT_REPO", "com.ijs.payment"),
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/data/mapper/TripPaymentMapper.kt": ("TAG_PAYMENT_MAPPER", "com.ijs.payment"),
    "screen-report/src/commonMain/kotlin/com/ijs/reports/data/datasource/ReportsRemoteDataSource.kt": ("TAG_REPORTS_REMOTE_DS", "com.ijs.reports"),
    "screen-report/src/commonMain/kotlin/com/ijs/reports/data/repository/ReportsRepositoryImpl.kt": ("TAG_REPORTS_REPO", "com.ijs.reports"),
    "screen-finance/src/commonMain/kotlin/com/ijs/finance/data/datasource/VehicleFinanceRemoteDataSource.kt": ("TAG_FINANCE_REMOTE_DS", "com.ijs.finance"),
    "screen-finance/src/commonMain/kotlin/com/ijs/finance/data/repository/VehicleFinanceRepositoryImpl.kt": ("TAG_FINANCE_REPO", "com.ijs.finance"),
    "screen-driver/src/commonMain/kotlin/com/ijs/driver/data/repository/DriverRepositoryImpl.kt": ("TAG_DRIVER_REMOTE_DS", "com.ijs.driver"),
    "ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet/core/network/HttpClientProvider.kt": ("TAG_NETWORK_GRAPH", "com.indusjs.fleet.network"),
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailViewModel.kt": ("TAG_CUSTOMER_DETAIL_VM", "com.ijs.customer"),
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailPdfExporter.kt": ("TAG_CUSTOMER_PDF", "com.ijs.customer"),
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailTripsHandler.kt": ("TAG_CUSTOMER_TRIPS", "com.ijs.customer"),
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailFinancialsHandler.kt": ("TAG_CUSTOMER_FINANCIALS", "com.ijs.customer"),
    "screen-customer/src/commonMain/kotlin/com/ijs/customer/presentation/detail/CustomerDetailPaymentsHandler.kt": ("TAG_CUSTOMER_PAYMENTS", "com.ijs.customer"),
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/presentation/PaymentDetailViewModel.kt": ("TAG_PAYMENT_DETAIL_VM", "com.ijs.payment"),
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/presentation/PaymentsViewModel.kt": ("TAG_PAYMENTS_VM", "com.ijs.payment"),
    "screen-payment/src/commonMain/kotlin/com/ijs/payment/presentation/AddPaymentViewModel.kt": ("TAG_ADD_PAYMENT_VM", "com.ijs.payment"),
    "screen-report/src/commonMain/kotlin/com/ijs/reports/presentation/vehicle/VehiclePLViewModel.kt": ("TAG_VEHICLE_PL_VM", "com.ijs.reports"),
    "screen-report/src/commonMain/kotlin/com/ijs/reports/presentation/ReportsViewModel.kt": ("TAG_REPORTS_VM", "com.ijs.reports"),
    "screen-finance/src/commonMain/kotlin/com/ijs/finance/presentation/VehicleFinanceViewModel.kt": ("TAG_FINANCE_VM", "com.ijs.finance"),
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/cost/TripCostEntryViewModel.kt": ("TAG_TRIP_COST_VM", "com.ijs.trip"),
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/detail/TripDetailDataLoader.kt": ("TAG_TRIP_DETAIL_LOADER", "com.ijs.trip"),
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/detail/TripDetailActionHandler.kt": ("TAG_TRIP_DETAIL_ACTION", "com.ijs.trip"),
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/detail/TripDetailViewModel.kt": ("TAG_TRIP_DETAIL_VM", "com.ijs.trip"),
    "screen-trip/src/commonMain/kotlin/com/ijs/trip/presentation/create/CreateTripViewModel.kt": ("TAG_CREATE_TRIP_VM", "com.ijs.trip"),
}

for rel_path, (tag_const, tag_pkg) in files_to_migrate.items():
    full_path = os.path.join(BASE, rel_path)
    if not os.path.exists(full_path):
        print(f"SKIP (not found): {rel_path}")
        continue

    with open(full_path, "r") as f:
        content = f.read()

    original = content

    # 1. Replace Kermit import
    content = content.replace(
        "import co.touchlab.kermit.Logger",
        f"import com.indusjs.fleet.core.logger.FleetLogger\nimport {tag_pkg}.{tag_const}",
    )

    # 2. Remove private val log = Logger.withTag("...")
    content = re.sub(
        r"\n\s*private val log = Logger\.withTag\(\"[^\"]*\"\)\s*", "\n", content
    )

    # 3. log.X(exception) { "message" } -> logger.X(TAG, "message", exception)
    content = re.sub(
        r"log\.([dewiv])\((\w+)\)\s*\{\s*(.*?)\s*\}",
        lambda m: f"logger.{m.group(1)}({tag_const}, {m.group(3)}, {m.group(2)})",
        content,
    )

    # 4. log.X { "message" } -> logger.X(TAG, "message")
    content = re.sub(
        r"log\.([dewiv])\s*\{\s*(.*?)\s*\}",
        lambda m: f"logger.{m.group(1)}({tag_const}, {m.group(2)})",
        content,
    )

    if content != original:
        with open(full_path, "w") as f:
            f.write(content)
        print(f"MIGRATED: {rel_path}")
    else:
        print(f"NO CHANGE: {rel_path}")

print("\nDone!")

