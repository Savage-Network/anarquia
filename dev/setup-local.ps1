# Cria ambiente local Paper 1.21.1 + Velocity para testes
$ErrorActionPreference = "Stop"
$DevRoot = $PSScriptRoot
$Runtime = Join-Path $DevRoot "runtime"
. (Join-Path $DevRoot "local.env.ps1")
. (Join-Path $DevRoot "lib\Download-Runtime.ps1")

Write-Host "=== Anarquia - setup local (MC $MC_VERSION) ===" -ForegroundColor Cyan

$dirs = @(
    (Join-Path $Runtime "shared"),
    (Join-Path $Runtime "quick-lobby"),
    (Join-Path $Runtime "velocity"),
    (Join-Path $Runtime "auth"),
    (Join-Path $Runtime "lobby"),
    (Join-Path $Runtime "game")
)
foreach ($d in $dirs) { New-Item -ItemType Directory -Force -Path $d | Out-Null }

$paperUrl = Get-PaperJarUrl -Version $MC_VERSION
$velocityUrl = Get-VelocityJarUrl -Version $VELOCITY_VERSION

$servers = @(
    @{ Name = "quick-lobby"; Jar = "paper.jar" },
    @{ Name = "auth"; Jar = "paper.jar" },
    @{ Name = "lobby"; Jar = "paper.jar" },
    @{ Name = "game"; Jar = "paper.jar" }
)

foreach ($s in $servers) {
    $dir = Join-Path $Runtime $s.Name
    Download-FileIfMissing -Url $paperUrl -Dest (Join-Path $dir $s.Jar)
    Ensure-Eula -ServerDir $dir
    New-Item -ItemType Directory -Force -Path (Join-Path $dir "plugins") | Out-Null
}

Download-FileIfMissing -Url $velocityUrl -Dest (Join-Path $Runtime "velocity\velocity.jar")
New-Item -ItemType Directory -Force -Path (Join-Path $Runtime "velocity\plugins") | Out-Null

$tplRoot = Join-Path $DevRoot "templates"
function Copy-ServerTemplate {
    param([string]$Name)
    $src = Join-Path $tplRoot $Name
    $dst = Join-Path $Runtime $Name
    if (Test-Path $src) {
        Copy-Item "$src\*" $dst -Recurse -Force -ErrorAction SilentlyContinue
    }
    $paper = Join-Path $tplRoot "paper-server"
    if (Test-Path $paper) {
        Copy-Item "$paper\*" $dst -Recurse -Force -ErrorAction SilentlyContinue
    }
}
Copy-ServerTemplate "quick-lobby"
Copy-ServerTemplate "auth"
Copy-ServerTemplate "lobby"
Copy-ServerTemplate "game"
Copy-ServerTemplate "velocity"

Write-Host ""
Write-Host "Setup concluido em $Runtime" -ForegroundColor Green
Write-Host "Proximo: sync-plugins.bat" -ForegroundColor Yellow
