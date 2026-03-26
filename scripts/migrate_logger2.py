#!/usr/bin/env python3
"""Migrate files using fully-qualified Kermit Logger calls."""
import re
import os

BASE = "/Users/ashwani/KotlinMultiplateform/IndusJSFleet"

# DriverRepositoryImpl
f1 = os.path.join(BASE, "screen-driver/src/commonMain/kotlin/com/ijs/driver/data/repository/DriverRepositoryImpl.kt")
with open(f1, "r") as f:
    content = f.read()

# Replace co.touchlab.kermit.Logger.X("tag", e) { "msg" }
content = re.sub(
    r'co\.touchlab\.kermit\.Logger\.(\w+)\("DriverRepository",\s*(\w+)\)\s*\{\s*(.*?)\s*\}',
    lambda m: f'logger.{m.group(1)}(TAG_DRIVER_REMOTE_DS, {m.group(3)}, {m.group(2)})',
    content,
)
# Replace co.touchlab.kermit.Logger.X("tag") { "msg" }
content = re.sub(
    r'co\.touchlab\.kermit\.Logger\.(\w+)\("DriverRepository"\)\s*\{\s*(.*?)\s*\}',
    lambda m: f'logger.{m.group(1)}(TAG_DRIVER_REMOTE_DS, {m.group(2)})',
    content,
)

with open(f1, "w") as f:
    f.write(content)
print("Fixed DriverRepositoryImpl")

# HttpClientProvider - keep using co.touchlab.kermit.Logger directly since
# this is an infrastructure file that creates the HttpClient (before DI is ready).
# We'll leave it as-is for now since it's a singleton factory.
print("HttpClientProvider: SKIPPED (infrastructure, pre-DI)")

print("Done!")

