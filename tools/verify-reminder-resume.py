"""Manual platform smoke: an opted-in reminder must recover when reopening a force-stopped test app.

Run after explicitly enabling a reminder in the separately installed P1 emulator build.
Only the dedicated emulator and disposable package are allowed; no data is removed.
"""
from pathlib import Path
import re
import subprocess
import time
from xml.etree import ElementTree

ADB = Path.home() / 'AppData/Local/Android/Sdk/platform-tools/adb.exe'
PACKAGE = 'com.yeoun.app.p1verify'
def adb(*args):
    return subprocess.check_output([str(ADB), '-s', 'emulator-5554', *args], timeout=20).decode('utf-8', errors='replace')
assert adb('shell', 'getprop', 'ro.kernel.qemu').strip() == '1'
def count():
    return len(re.findall(r'RTC_WAKEUP[^\n]*\b' + re.escape(PACKAGE) + r'\}', adb('shell', 'dumpsys', 'alarm')))
settings = ElementTree.fromstring(adb('exec-out', 'cat', '/data/data/' + PACKAGE + '/shared_prefs/yeoun_local_reminders.xml'))
assert any(n.get('name') == 'enabled' and n.get('value') == 'true' for n in settings), 'Explicitly opt in inside the disposable test package first'
adb('shell', 'am', 'force-stop', PACKAGE)
assert count() == 0, 'Android must cancel the stopped package alarm before resume is checked'
adb('shell', 'am', 'start', '-n', PACKAGE + '/com.yeoun.app.MainActivity')
time.sleep(2)
assert count() == 1, 'App startup must restore exactly one already opted-in native alarm'
print('PASS: force-stop cancelled the alarm; reopening restored exactly one previously enabled reminder')
