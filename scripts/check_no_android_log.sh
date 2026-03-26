#!/bin/bash
# check_no_android_log.sh - Enforcement script
# Ensures no direct android.util.Log or co.touchlab.kermit.Logger usage
# in feature modules. All logging must go through FleetLogger.
#
# Usage: ./scripts/check_no_android_log.sh
# Returns exit code 1 if violations found.

set -euo pipefail

VIOLATIONS=0
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "=== Checking for direct Logger usage violations ==="

# Check for android.util.Log usage
ANDROID_LOG=$(grep -rn "android.util.Log" --include="*.kt" \
  "$ROOT_DIR/screen-"*/src \
  "$ROOT_DIR/sharedUI/src" \
  "$ROOT_DIR/ijs-network-lib/src" \
  "$ROOT_DIR/ijs-core-lib/src" \
  2>/dev/null | grep -v "build/" || true)

if [ -n "$ANDROID_LOG" ]; then
  echo "❌ android.util.Log usage found:"
  echo "$ANDROID_LOG"
  VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check for direct co.touchlab.kermit.Logger usage (excluding HttpClientProvider and ijs-logger-lib)
KERMIT_LOG=$(grep -rn "co.touchlab.kermit.Logger\|Logger.withTag\|import co.touchlab.kermit.Logger" \
  --include="*.kt" \
  "$ROOT_DIR/screen-"*/src \
  "$ROOT_DIR/sharedUI/src" \
  "$ROOT_DIR/ijs-network-lib/src" \
  "$ROOT_DIR/ijs-core-lib/src" \
  2>/dev/null | grep -v "build/" | grep -v "HttpClientProvider" || true)

if [ -n "$KERMIT_LOG" ]; then
  echo "❌ Direct Kermit Logger usage found (use FleetLogger instead):"
  echo "$KERMIT_LOG"
  VIOLATIONS=$((VIOLATIONS + 1))
fi

# Check for old-style 'private val log = Logger.withTag' pattern
OLD_PATTERN=$(grep -rn "private val log = Logger" --include="*.kt" \
  "$ROOT_DIR/screen-"*/src \
  "$ROOT_DIR/sharedUI/src" \
  "$ROOT_DIR/ijs-network-lib/src" \
  2>/dev/null | grep -v "build/" || true)

if [ -n "$OLD_PATTERN" ]; then
  echo "❌ Old Logger.withTag pattern found (use FleetLogger injection):"
  echo "$OLD_PATTERN"
  VIOLATIONS=$((VIOLATIONS + 1))
fi

if [ "$VIOLATIONS" -eq 0 ]; then
  echo "✅ No logging violations found. All logging uses FleetLogger."
  exit 0
else
  echo ""
  echo "❌ Found $VIOLATIONS violation type(s). Use FleetLogger for all logging."
  exit 1
fi

