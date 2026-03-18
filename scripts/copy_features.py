#!/usr/bin/env python3
"""Restore feature files from git and copy to feature modules."""
import subprocess
import shutil
import os

ROOT = '/Users/ashwani/KotlinMultiplateform/IndusJSFleet'
NL_BASE = os.path.join(ROOT, 'ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet')

features = {
    'vehicle': 'ijs-vehicle-lib',
    'trip': 'ijs-trip-lib',
    'customer': 'ijs-customer-lib',
    'payment': 'ijs-payment-lib',
    'team': 'ijs-team-lib',
    'reports': 'ijs-reports-lib',
    'finance': 'ijs-finance-lib',
}

dirs = ['data/model', 'data/datasource', 'data/mapper', 'data/repository', 
        'domain/entity', 'domain/repository', 'domain/usecase']

count = 0
for feature, target_module in features.items():
    for d in dirs:
        src_dir = os.path.join(NL_BASE, d, feature)
        if not os.path.isdir(src_dir):
            continue
        for fname in os.listdir(src_dir):
            if not fname.endswith('.kt'):
                continue
            rel_path = os.path.join('ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet', d, feature, fname)
            dst_dir = os.path.join(ROOT, target_module, 'src/commonMain/kotlin/com/indusjs/fleet', d, feature)
            dst_file = os.path.join(dst_dir, fname)
            os.makedirs(dst_dir, exist_ok=True)
            
            # Use git show to get the original content
            try:
                result = subprocess.run(
                    ['git', 'show', f'HEAD:{rel_path}'],
                    capture_output=True, text=True, cwd=ROOT
                )
                if result.returncode == 0 and len(result.stdout) > 50:
                    with open(dst_file, 'w') as f:
                        f.write(result.stdout)
                    count += 1
                    print(f'RESTORED: {d}/{feature}/{fname} -> {target_module}')
                else:
                    print(f'SKIP (not in git or too small): {d}/{feature}/{fname}')
            except Exception as e:
                print(f'ERROR: {d}/{feature}/{fname}: {e}')

print(f'Total restored: {count}')

