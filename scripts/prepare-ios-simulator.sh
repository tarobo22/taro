#!/bin/bash
# Verify Xcode's simulator-only identity; never inject device entitlements into a macOS signature.
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
codesign --verify --strict "$app"
python3 - "$app/Yeoun" "$proof/verified-entitlements.plist" <<'PY'
import plistlib, struct, sys
from pathlib import Path
data = Path(sys.argv[1]).read_bytes()
assert struct.unpack_from('<I', data)[0] == 0xfeedfacf, 'Expected native 64-bit simulator executable'
offset, payload = 32, None
for _ in range(struct.unpack_from('<I', data, 16)[0]):
    command, size = struct.unpack_from('<II', data, offset)
    assert size >= 8 and offset + size <= len(data), 'Invalid Mach-O command'
    if command == 0x19:  # LC_SEGMENT_64
        for index in range(struct.unpack_from('<I', data, offset + 64)[0]):
            section = offset + 72 + index * 80
            name, segment = struct.unpack_from('<16s16s', data, section)
            if name.rstrip(b'\0') == b'__entitlements' and segment.rstrip(b'\0') == b'__TEXT':
                _, length, position = struct.unpack_from('<QQI', data, section + 32)
                payload = data[position:position + length].rstrip(b'\0')
    offset += size
assert payload, 'Simulator keychain identity is missing from __TEXT,__entitlements'
entitlements = plistlib.loads(payload)
identity = entitlements.get('application-identifier', '')
assert identity == 'com.yeoun.app' or identity.endswith('.com.yeoun.app'), 'Unexpected simulator app identity'
# Security.framework uses application-identifier as the default group if no explicit group is declared.
assert identity in entitlements.get('keychain-access-groups', [identity]), 'Unexpected simulator keychain group'
Path(sys.argv[2]).write_bytes(plistlib.dumps(entitlements))
print('Verified embedded simulator identity:', identity)
PY
