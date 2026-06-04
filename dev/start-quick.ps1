# Inicia servidor Paper rapido (so lobbyhost) — ideal para testar NPCs
$ErrorActionPreference = "Stop"
$DevRoot = $PSScriptRoot
$ServerDir = Join-Path $DevRoot "runtime\quick-lobby"
. (Join-Path $DevRoot "local.env.ps1")

if (-not (Test-Path (Join-Path $ServerDir "paper.jar"))) {
    Write-Host "Rode .\setup-local.ps1 primeiro." -ForegroundColor Red
    exit 1
}
if (-not (Test-Path (Join-Path $ServerDir "plugins\lobbyhost.jar"))) {
    Write-Host "Rode .\sync-plugins.ps1 primeiro." -ForegroundColor Red
    exit 1
}

Write-Host "=== Quick lobby — Paper $MC_VERSION — porta $PORT_QUICK ===" -ForegroundColor Cyan
Write-Host "Conecte: localhost:$PORT_QUICK (cliente 1.21.1, offline)" -ForegroundColor Green

Push-Location $ServerDir
& java "-Xms$JAVA_XMS" "-Xmx$JAVA_XMX" -jar paper.jar --nogui
Pop-Location
