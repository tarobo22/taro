# Dot-source this file to configure only the current terminal. No global PATH changes.
$taskNode=Join-Path $env:USERPROFILE '.cache/yeoun-tools/node-v24.22.2-win-x64'
if(Test-Path -LiteralPath (Join-Path $taskNode 'node.exe')){$env:Path=$taskNode+';'+$env:Path}
if(-not $env:ANDROID_HOME){
  $taskSdk=Join-Path $env:LOCALAPPDATA 'Android/Sdk'
  if(Test-Path -LiteralPath $taskSdk){$env:ANDROID_HOME=$taskSdk}
}
if($env:ANDROID_HOME){$env:Path=(Join-Path $env:ANDROID_HOME 'platform-tools')+';'+$env:Path}
if(-not $env:JAVA_HOME){
  $taskJava=Get-Command java.exe -ErrorAction SilentlyContinue
  if($taskJava){$env:JAVA_HOME=Split-Path (Split-Path $taskJava.Source -Parent) -Parent}
}
