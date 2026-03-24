#!/usr/bin/env python3
import sys

replacements = {
    'com.indusjs.fleet.presentation.team.list.TeamListViewModel': 'com.ijs.team.presentation.list.TeamListViewModel',
    'com.indusjs.fleet.presentation.team.create.CreateTeamMemberViewModel': 'com.ijs.team.presentation.create.CreateTeamMemberViewModel',
    'com.indusjs.fleet.presentation.team.detail.TeamMemberDetailViewModel': 'com.ijs.team.presentation.detail.TeamMemberDetailViewModel',
    'com.indusjs.fleet.presentation.drivers.DriversViewModel': 'com.ijs.driver.presentation.DriversViewModel',
    'com.indusjs.fleet.presentation.drivers.create.CreateDriverViewModel': 'com.ijs.driver.presentation.create.CreateDriverViewModel',
    'com.indusjs.fleet.presentation.drivers.detail.DriverDetailViewModel': 'com.ijs.driver.presentation.detail.DriverDetailViewModel',
    'com.indusjs.fleet.presentation.drivers.cost.DriverCostEntryViewModel': 'com.ijs.driver.presentation.cost.DriverCostEntryViewModel',
    'com.indusjs.fleet.presentation.vehicles.VehiclesViewModel': 'com.ijs.vehicle.presentation.VehiclesViewModel',
    'com.indusjs.fleet.presentation.vehicles.AddVehicleViewModel': 'com.ijs.vehicle.presentation.AddVehicleViewModel',
    'com.indusjs.fleet.presentation.vehicles.detail.VehicleDetailViewModel': 'com.ijs.vehicle.presentation.detail.VehicleDetailViewModel',
    'com.indusjs.fleet.presentation.vehicles.costs.MaintenanceCostEntryViewModel': 'com.ijs.vehicle.presentation.costs.MaintenanceCostEntryViewModel',
    'com.indusjs.fleet.presentation.trips.TripsViewModel': 'com.ijs.trip.presentation.TripsViewModel',
    'com.indusjs.fleet.presentation.trips.create.CreateTripViewModel': 'com.ijs.trip.presentation.create.CreateTripViewModel',
    'com.indusjs.fleet.presentation.trips.detail.TripDetailViewModel': 'com.ijs.trip.presentation.detail.TripDetailViewModel',
    'com.indusjs.fleet.presentation.trips.cost.TripCostEntryViewModel': 'com.ijs.trip.presentation.cost.TripCostEntryViewModel',
    'com.indusjs.fleet.presentation.customers.list.CustomersListViewModel': 'com.ijs.customer.presentation.list.CustomersListViewModel',
    'com.indusjs.fleet.presentation.customers.detail.CustomerDetailViewModel': 'com.ijs.customer.presentation.detail.CustomerDetailViewModel',
    'com.indusjs.fleet.presentation.customers.create.CreateCustomerViewModel': 'com.ijs.customer.presentation.create.CreateCustomerViewModel',
    'com.indusjs.fleet.presentation.payments.PaymentsViewModel': 'com.ijs.payment.presentation.PaymentsViewModel',
    'com.indusjs.fleet.presentation.payments.PaymentDetailViewModel': 'com.ijs.payment.presentation.PaymentDetailViewModel',
    'com.indusjs.fleet.presentation.payments.AddPaymentViewModel': 'com.ijs.payment.presentation.AddPaymentViewModel',
    'com.indusjs.fleet.presentation.reports.ReportsViewModel': 'com.ijs.reports.presentation.ReportsViewModel',
    'com.indusjs.fleet.presentation.reports.consolidated.ConsolidatedPLViewModel': 'com.ijs.reports.presentation.consolidated.ConsolidatedPLViewModel',
    'com.indusjs.fleet.presentation.reports.cost.CostAnalysisViewModel': 'com.ijs.reports.presentation.cost.CostAnalysisViewModel',
    'com.indusjs.fleet.presentation.reports.trip.TripPLViewModel': 'com.ijs.reports.presentation.trip.TripPLViewModel',
    'com.indusjs.fleet.presentation.reports.vehicle.VehiclePLViewModel': 'com.ijs.reports.presentation.vehicle.VehiclePLViewModel',
    'com.indusjs.fleet.presentation.finance.VehicleFinanceViewModel': 'com.ijs.finance.presentation.VehicleFinanceViewModel',
}

files = [
    'sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/ViewModelProvider.kt',
    'sharedUI/src/commonMain/kotlin/com/indusjs/fleet/di/DefaultViewModelProvider.kt',
]

for fp in files:
    with open(fp, 'r') as f:
        content = f.read()
    for old, new in replacements.items():
        content = content.replace(old, new)
    with open(fp, 'w') as f:
        f.write(content)
    print(f'Updated {fp}')

for fp in files:
    with open(fp, 'r') as f:
        lines = f.readlines()
    old_count = sum(1 for l in lines if 'com.indusjs.fleet.presentation.' in l)
    print(f'{fp}: remaining old presentation imports = {old_count}')

