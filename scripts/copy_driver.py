#!/usr/bin/env python3
"""Restore driver feature files from git and copy to ijs-driver-lib."""
import subprocess
import os

ROOT = '/Users/ashwani/KotlinMultiplateform/IndusJSFleet'
NL_BASE = os.path.join(ROOT, 'ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet')

dirs = ['data/model', 'data/datasource', 'data/mapper', 'data/repository', 
        'domain/entity', 'domain/repository', 'domain/usecase']

count = 0
for d in dirs:
    src_dir = os.path.join(NL_BASE, d, 'driver')
    if not os.path.isdir(src_dir):
        continue
    for fname in os.listdir(src_dir):
        if not fname.endswith('.kt'):
            continue
        rel_path = os.path.join('ijs-network-lib/src/commonMain/kotlin/com/indusjs/fleet', d, 'driver', fname)
        dst_dir = os.path.join(ROOT, 'ijs-driver-lib/src/commonMain/kotlin/com/indusjs/fleet', d, 'driver')
        dst_file = os.path.join(dst_dir, fname)
        os.makedirs(dst_dir, exist_ok=True)
        
        try:
            result = subprocess.run(
                ['git', 'show', f'HEAD:{rel_path}'],
                capture_output=True, text=True, cwd=ROOT
            )
            if result.returncode == 0 and len(result.stdout) > 50:
                with open(dst_file, 'w') as f:
                    f.write(result.stdout)
                count += 1
                print(f'RESTORED: {d}/driver/{fname} -> ijs-driver-lib')
            else:
                print(f'SKIP: {d}/driver/{fname}')
        except Exception as e:
            print(f'ERROR: {d}/driver/{fname}: {e}')

print(f'Total restored: {count}')

