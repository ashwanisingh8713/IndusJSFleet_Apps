#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if ! command -v pod >/dev/null 2>&1; then
  echo "error: CocoaPods (pod) is not installed or not on PATH." >&2
  echo "Install on macOS, for example: brew install cocoapods" >&2
  exit 1
fi
exec pod install
