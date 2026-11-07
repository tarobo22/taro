#!/bin/bash
# Simulator-only identity. This is not a distribution certificate or device build.
set -euo pipefail
app="$1"
proof="$2"
mkdir -p "$proof"
python3 - "$app/Info.plist" <<'PY'
import plistlib, sys
with open(sys.argv[1], 'rb') as stream:
    info = plistlib.load(stream)
assert 'iPhoneSimulator' in info['CFBundleSupportedPlatforms'], 'Simulator build required'
assert info['CFBundleIdentifier'] == 'com.yeoun.app', 'Unexpected app identifier'
assert info['YeounAPIBaseURL'] == 'https://yeoun-43-201-209-249.sslip.io/api', 'Demo API missing from native Info.plist'
PY
python3 - "$proof/simulator-entitlements.plist" <<'PY'
import plistlib, sys
with open(sys.argv[1], 'wb') as stream:
    plistlib.dump({'application-identifier': 'com.yeoun.app',
                  'keychain-access-groups': ['com.yeoun.app'],
                  'get-task-allow': True}, stream)
PY
codesign --force --sign - --entitlements "$proof/simulator-entitlements.plist" --generate-entitlement-der "$app"
codesign --verify --strict "$app"
codesign -d --entitlements - "$app" > "$proof/verified-entitlements.plist"
python3 - "$proof/verified-entitlements.plist" <<'PY'
import plistlib, sys
with open(sys.argv[1], 'rb') as stream:
    entitlements = plistlib.load(stream)
assert entitlements['application-identifier'] == 'com.yeoun.app'
assert 'com.yeoun.app' in entitlements['keychain-access-groups']
PY
