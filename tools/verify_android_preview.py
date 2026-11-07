"""Inspect delivered preview binaries; no device data or private signing material is read."""
from pathlib import Path
import argparse, hashlib, json, re, subprocess, zipfile
p=argparse.ArgumentParser()
p.add_argument('apk')
p.add_argument('--abi',required=True)
p.add_argument('--package',required=True)
p.add_argument('--api-url',required=True)
p.add_argument('--sdk',required=True)
p.add_argument('--java',required=True)
p.add_argument('--output',required=True)
a=p.parse_args()
apk=Path(a.apk).resolve();sdk=Path(a.sdk);root=Path(__file__).resolve().parents[1]
signed=subprocess.run([a.java,'-jar',str(sdk/'build-tools/37.0.0/lib/apksigner.jar'),'verify','--verbose',str(apk)],capture_output=True,text=True,check=True)
badging=subprocess.run([str(sdk/'build-tools/37.0.0/aapt.exe'),'dump','badging',str(apk)],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
resources=subprocess.run([str(sdk/'build-tools/37.0.0/aapt2.exe'),'dump','resources',str(apk)],capture_output=True,text=True,encoding='utf-8',errors='replace',check=True).stdout
mapping={};current=None
for line in resources.splitlines():
    resource=re.match(r'\s*resource 0x[0-9a-f]+ (\S+)',line)
    if resource:current=resource.group(1)
    file=re.search(r'\(file\) (\S+)',line)
    if file and current:mapping[current]=file.group(1)
package=re.search(r"package: name='([^']+)'",badging)
if not package or package.group(1)!=a.package:raise SystemExit('Unexpected application ID')
with zipfile.ZipFile(apk) as archive:
    files=archive.namelist()
    abis=sorted({name.split('/')[1] for name in files if name.startswith('lib/') and name.endswith('.so')})
    if abis!=[a.abi]:raise SystemExit('Unexpected native architecture set')
    if archive.getinfo('assets/index.android.bundle').file_size<100000:raise SystemExit('Bundled application is missing')
    expected={'drawable/assets_skins_'+skin+'_'+pose for skin in ('cafe','clinic','playroom') for pose in ('rest','greeting','explain')}
    poses={name:path for name,path in mapping.items() if name.startswith('drawable/assets_skins_')}
    if set(poses)!=expected or not all(path in files for path in poses.values()):raise SystemExit('Expected all nine logical companion resources and their files')
    if a.api_url.encode() not in archive.read('assets/index.android.bundle'):raise SystemExit('The packaged runtime still contains an older API configuration')
    meow=mapping.get('raw/yeoun_meow')
    if not meow:raise SystemExit('Native cat cue missing')
    meow_sha=hashlib.sha256(archive.read(meow)).hexdigest()
    if meow_sha!=hashlib.sha256((root/'assets/audio/cat_meow.wav').read_bytes()).hexdigest():raise SystemExit('Cat cue differs from the licensed asset')
receipt={
 'artifact':apk.name,'bytes':apk.stat().st_size,'sha256':hashlib.sha256(apk.read_bytes()).hexdigest(),
 'applicationId':a.package,'abi':abis,'apiBaseUrl':a.api_url,
 'signing':'Android SDK managed debug certificate; local preview, not store distribution',
 'signatureVerified':signed.returncode==0,'allNinePoseAssets':poses,'nativeMeowSha256':meow_sha,'apiUrlVerifiedInBundle':True,
 'bundledJavaScript':True,'sourceCommit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=root,text=True).strip()
}
out=Path(a.output);out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({key:receipt[key] for key in ('artifact','applicationId','abi','signatureVerified','bytes')},ensure_ascii=False))
