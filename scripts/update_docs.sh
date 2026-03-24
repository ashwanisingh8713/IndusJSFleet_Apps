#!/bin/bash
set -e

cd /Users/ashwani/KotlinMultiplateform/IndusJSFleet

for file in Docs/Code_Migration_Refactore/*.md AGENTS.md .github/copilot-instructions.md sharedUI/AGENTS.md; do
    if [ -f "$file" ]; then
        # Module directory renames
        sed -i '' 's/ijs-vehicle-lib/feat-vehicle/g' "$file"
        sed -i '' 's/ijs-trip-lib/feat-trip/g' "$file"
        sed -i '' 's/ijs-team-lib/feat-team/g' "$file"
        sed -i '' 's/ijs-reports-lib/feat-report/g' "$file"
        sed -i '' 's/ijs-payment-lib/feat-payment/g' "$file"
        sed -i '' 's/ijs-finance-lib/feat-finance/g' "$file"
        sed -i '' 's/ijs-driver-lib/feat-driver/g' "$file"
        sed -i '' 's/ijs-customer-lib/feat-customer/g' "$file"

        # Android namespace renames
        sed -i '' 's/com\.indusjs\.fleet\.vehicle/com.ijs.vehicle/g' "$file"
        sed -i '' 's/com\.indusjs\.fleet\.trip/com.ijs.trip/g' "$file"
        sed -i '' 's/com\.indusjs\.fleet\.team/com.ijs.team/g' "$file"
        sed -i '' 's/com\.indusjs\.fleet\.reports/com.ijs.reports/g' "$file"
        sed -i '' 's/com\.indusjs\.fleet\.payment/com.ijs.payment/g' "$file"
        sed -i '' 's/com\.indusjs\.fleet\.finance/com.ijs.finance/g' "$file"
        sed -i '' 's/com\.indusjs\.fleet\.driver/com.ijs.driver/g' "$file"
        sed -i '' 's/com\.indusjs\.fleet\.customer/com.ijs.customer/g' "$file"

        echo "Updated: $file"
    fi
done

echo "=== Documentation updated ==="

