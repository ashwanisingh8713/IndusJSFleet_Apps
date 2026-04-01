#!/usr/bin/env python3
import os

files = [
    "screen-driver/src/commonMain/kotlin/com/ijs/driver/presentation/detail/DriverDetailOverviewContent.kt",
]

skip_composable_fns = {'getStatusDisplayName', 'getLicenseTypeLabel', 'formatDate'}

for fpath in files:
    with open(fpath, 'r') as f:
        content = f.read()
    
    lines = content.split('\n')
    new_lines = []
    
    for i, line in enumerate(lines):
        stripped = line.strip()
        if stripped.startswith('internal fun '):
            fn_name = stripped.split('(')[0].replace('internal fun ', '').strip()
            prev_line = new_lines[-1].strip() if new_lines else ''
            if prev_line != '@Composable' and fn_name not in skip_composable_fns:
                new_lines.append('@Composable')
        new_lines.append(line)
    
    new_content = '\n'.join(new_lines)
    with open(fpath, 'w') as f:
        f.write(new_content)
    print(f"Fixed {fpath}")

print("Done")

