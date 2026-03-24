#!/bin/bash
set -e

cd /Users/ashwani/KotlinMultiplateform/IndusJSFleet

BASE="src/commonMain/kotlin"

copy_feature() {
    local MODULE=$1
    local FEAT=$2
    local OLD_BASE="$MODULE/$BASE/com/indusjs/fleet"
    local NEW_BASE="$MODULE/$BASE/com/ijs/$FEAT"

    # Data layer
    for sublayer in datasource mapper model repository; do
        local old_dir="$OLD_BASE/data/$sublayer/$FEAT"
        local new_dir="$NEW_BASE/data/$sublayer"
        if [ -d "$old_dir" ]; then
            mkdir -p "$new_dir"
            cp "$old_dir"/*.kt "$new_dir/" 2>/dev/null && echo "  Copied data/$sublayer" || echo "  No files in data/$sublayer"
        fi
    done

    # Domain layer
    for sublayer in entity repository usecase; do
        local old_dir="$OLD_BASE/domain/$sublayer/$FEAT"
        local new_dir="$NEW_BASE/domain/$sublayer"
        if [ -d "$old_dir" ]; then
            mkdir -p "$new_dir"
            cp "$old_dir"/*.kt "$new_dir/" 2>/dev/null && echo "  Copied domain/$sublayer" || echo "  No files in domain/$sublayer"
        fi
    done
}

# Clean up any partial copies from failed attempt
for mod in feat-vehicle feat-trip feat-team feat-report feat-payment feat-finance feat-driver feat-customer; do
    rm -rf "$mod/$BASE/com/ijs" 2>/dev/null
done

echo "=== VEHICLE ==="
copy_feature "feat-vehicle" "vehicle"

echo "=== TRIP ==="
copy_feature "feat-trip" "trip"

echo "=== TEAM ==="
copy_feature "feat-team" "team"

echo "=== REPORTS ==="
copy_feature "feat-report" "reports"

echo "=== PAYMENT ==="
copy_feature "feat-payment" "payment"

echo "=== FINANCE ==="
copy_feature "feat-finance" "finance"

echo "=== DRIVER ==="
copy_feature "feat-driver" "driver"

echo "=== CUSTOMER (data + domain) ==="
copy_feature "feat-customer" "customer"

echo "=== CUSTOMER (presentation) ==="
CUST_OLD="feat-customer/$BASE/com/indusjs/fleet/presentation/customer"
CUST_NEW="feat-customer/$BASE/com/ijs/customer/presentation"
mkdir -p "$CUST_NEW"
mkdir -p "$CUST_NEW/list"
mkdir -p "$CUST_NEW/create"
mkdir -p "$CUST_NEW/detail"
cp "$CUST_OLD/CustomerFeatureFacade.kt" "$CUST_NEW/"
cp "$CUST_OLD/list/"*.kt "$CUST_NEW/list/"
cp "$CUST_OLD/create/"*.kt "$CUST_NEW/create/"
cp "$CUST_OLD/detail/"*.kt "$CUST_NEW/detail/"
echo "  Copied presentation layer"

echo ""
echo "=== ALL FILES COPIED ==="

# Now remove old directory trees
for mod in feat-vehicle feat-trip feat-team feat-report feat-payment feat-finance feat-driver feat-customer; do
    rm -rf "$mod/$BASE/com/indusjs"
    echo "Removed old source tree from $mod"
done

echo ""
echo "=== OLD TREES REMOVED ==="

