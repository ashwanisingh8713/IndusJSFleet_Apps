#!/bin/bash
# Delete files from sharedUI that have been moved to ijs-network-lib
# Also delete files from ijs-reports-lib and ijs-finance-lib that have been folded in
set -e

ROOT="/Users/ashwani/KotlinMultiplateform/IndusJSFleet"
SHARED="$ROOT/sharedUI/src/commonMain/kotlin/com/indusjs/fleet"

echo "=== Removing moved domain entities from sharedUI ==="
rm -v "$SHARED/domain/entity/customer/Customer.kt"
rm -v "$SHARED/domain/entity/dashboard/DashboardStats.kt"
rm -v "$SHARED/domain/entity/driver/Driver.kt"
rm -v "$SHARED/domain/entity/maps/MapEntities.kt"
rm -v "$SHARED/domain/entity/payment/PaymentEnums.kt"
rm -v "$SHARED/domain/entity/payment/TripPayment.kt"
rm -v "$SHARED/domain/entity/team/TeamMember.kt"
rm -v "$SHARED/domain/entity/trip/Trip.kt"
rm -v "$SHARED/domain/entity/vehicle/Vehicle.kt"
rm -v "$SHARED/domain/entity/vehicle/VehicleDetail.kt"

echo ""
echo "=== Removing moved DTOs/Models from sharedUI ==="
rm -v "$SHARED/data/model/caretaker/CaretakerDto.kt"
rm -v "$SHARED/data/model/costs/CostModels.kt"
rm -v "$SHARED/data/model/customer/CustomerDto.kt"
rm -v "$SHARED/data/model/dashboard/DashboardModels.kt"
rm -v "$SHARED/data/model/driver/DriverCostModels.kt"
rm -v "$SHARED/data/model/driver/DriverDto.kt"
rm -v "$SHARED/data/model/history/HistoryDto.kt"
rm -v "$SHARED/data/model/payment/TripPaymentDto.kt"
rm -v "$SHARED/data/model/payment/TripPaymentRequest.kt"
rm -v "$SHARED/data/model/state/StateHistoryDto.kt"
rm -v "$SHARED/data/model/team/TeamDto.kt"
rm -v "$SHARED/data/model/trip/TripDto.kt"
rm -v "$SHARED/data/model/vehicle/VehicleDto.kt"

echo ""
echo "=== Removing moved Mappers from sharedUI ==="
rm -v "$SHARED/data/mapper/customer/CustomerMapper.kt"
rm -v "$SHARED/data/mapper/dashboard/DashboardMapper.kt"
rm -v "$SHARED/data/mapper/driver/DriverMapper.kt"
rm -v "$SHARED/data/mapper/payment/TripPaymentMapper.kt"
rm -v "$SHARED/data/mapper/team/TeamMapper.kt"
rm -v "$SHARED/data/mapper/trip/TripMapper.kt"
rm -v "$SHARED/data/mapper/vehicle/VehicleMapper.kt"
# NOTE: DashboardCacheMapper.kt stays in sharedUI (depends on Room entities)

echo ""
echo "=== Removing moved Remote Data Sources from sharedUI ==="
rm -v "$SHARED/data/datasource/costs/CostsRemoteDataSource.kt"
rm -v "$SHARED/data/datasource/customer/CustomerRemoteDataSource.kt"
rm -v "$SHARED/data/datasource/dashboard/DashboardRemoteDataSource.kt"
rm -v "$SHARED/data/datasource/driver/DriverRemoteDataSource.kt"
rm -v "$SHARED/data/datasource/payment/TripPaymentRemoteDataSource.kt"
rm -v "$SHARED/data/datasource/team/TeamRemoteDataSource.kt"
rm -v "$SHARED/data/datasource/trip/TripRemoteDataSource.kt"
rm -v "$SHARED/data/datasource/vehicle/VehicleRemoteDataSource.kt"

echo ""
echo "=== Removing moved Repository Interfaces from sharedUI ==="
rm -v "$SHARED/domain/repository/costs/CostsRepository.kt"
rm -v "$SHARED/domain/repository/costs/CostTypesRepository.kt"
rm -v "$SHARED/domain/repository/customer/CustomerRepository.kt"
rm -v "$SHARED/domain/repository/dashboard/DashboardRepository.kt"
rm -v "$SHARED/domain/repository/driver/DriverRepository.kt"
rm -v "$SHARED/domain/repository/payment/TripPaymentRepository.kt"
rm -v "$SHARED/domain/repository/team/TeamRepository.kt"
rm -v "$SHARED/domain/repository/trip/TripRepository.kt"
rm -v "$SHARED/domain/repository/vehicle/VehicleRepository.kt"

echo ""
echo "=== Removing moved Repository Impls from sharedUI ==="
rm -v "$SHARED/data/repository/costs/CostsRepositoryImpl.kt"
rm -v "$SHARED/data/repository/costs/CostTypesRepositoryImpl.kt"
rm -v "$SHARED/data/repository/customer/CustomerRepositoryImpl.kt"
rm -v "$SHARED/data/repository/dashboard/DashboardRepositoryImpl.kt"
rm -v "$SHARED/data/repository/driver/DriverRepositoryImpl.kt"
rm -v "$SHARED/data/repository/payment/TripPaymentRepositoryImpl.kt"
rm -v "$SHARED/data/repository/team/TeamRepositoryImpl.kt"
rm -v "$SHARED/data/repository/trip/TripRepositoryImpl.kt"
rm -v "$SHARED/data/repository/vehicle/VehicleRepositoryImpl.kt"

echo ""
echo "=== Removing moved Use Cases from sharedUI ==="
rm -v "$SHARED/domain/usecase/costs/GetCostTypesUseCase.kt"
rm -v "$SHARED/domain/usecase/costs/InitializeCostTypesUseCase.kt"
rm -v "$SHARED/domain/usecase/customer/CustomerUseCases.kt"
rm -v "$SHARED/domain/usecase/dashboard/DashboardUseCases.kt"
rm -v "$SHARED/domain/usecase/dashboard/GetFinancialSummaryUseCase.kt"
rm -v "$SHARED/domain/usecase/driver/DriverUseCases.kt"
rm -v "$SHARED/domain/usecase/trip/TripUseCases.kt"
rm -v "$SHARED/domain/usecase/vehicle/VehicleUseCases.kt"

echo ""
echo "=== Removing empty directories from sharedUI ==="
find "$SHARED/domain/entity" -type d -empty -delete 2>/dev/null || true
find "$SHARED/domain/repository" -type d -empty -delete 2>/dev/null || true
find "$SHARED/domain/usecase" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/model" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/mapper" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/repository" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/datasource/driver" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/datasource/trip" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/datasource/vehicle" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/datasource/payment" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/datasource/reports" -type d -empty -delete 2>/dev/null || true
find "$SHARED/data/datasource/finance" -type d -empty -delete 2>/dev/null || true

echo ""
echo "=== Done! Remaining data files in sharedUI ==="
find "$SHARED/data" -name "*.kt" | sort
echo ""
echo "=== Remaining domain files in sharedUI ==="
find "$SHARED/domain" -name "*.kt" 2>/dev/null | sort || echo "(none)"

