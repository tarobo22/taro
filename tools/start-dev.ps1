$ErrorActionPreference='Stop'
. (Join-Path $PSScriptRoot 'use-env.ps1')
$repoRoot=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
Push-Location -LiteralPath $repoRoot
try { & npm start } finally { Pop-Location }
