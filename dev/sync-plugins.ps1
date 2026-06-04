# Compila plugins e copia JARs + configs de dev
$ErrorActionPreference = "Stop"
$DevRoot = $PSScriptRoot
$RepoRoot = Split-Path $DevRoot -Parent
$Runtime = Join-Path $DevRoot "runtime"
. (Join-Path $DevRoot "local.env.ps1")

Write-Host "=== Build + sync plugins ===" -ForegroundColor Cyan

Push-Location $RepoRoot
& .\gradlew.bat buildAll --no-daemon --console=plain
if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
Pop-Location

$sharedDb = Join-Path $Runtime "shared\anarquia-dev.db"
New-Item -ItemType Directory -Force -Path (Split-Path $sharedDb) | Out-Null

function Sync-Plugin {
    param(
        [string]$Module,
        [string]$JarName,
        [string[]]$TargetServers
    )
    $srcJar = Join-Path $RepoRoot "$Module\build\libs\$JarName"
    if (-not (Test-Path $srcJar)) {
        throw "JAR nao encontrado: $srcJar"
    }
    foreach ($server in $TargetServers) {
        $plugins = Join-Path $Runtime "$server\plugins"
        New-Item -ItemType Directory -Force -Path $plugins | Out-Null
        Copy-Item $srcJar (Join-Path $plugins $JarName) -Force
        Write-Host "  $JarName -> $server/plugins/"
    }
}

Sync-Plugin -Module "lobbyhost" -JarName "lobbyhost.jar" -TargetServers @("quick-lobby", "lobby")
Sync-Plugin -Module "auth" -JarName "auth.jar" -TargetServers @("auth")
Sync-Plugin -Module "game" -JarName "game.jar" -TargetServers @("game")
Sync-Plugin -Module "proxyhost" -JarName "proxyhost.jar" -TargetServers @("velocity")

# Configs de dev por plugin
$tpl = Join-Path $DevRoot "templates"
$pluginConfigs = @(
    @{ Plugin = "lobbyhost"; Servers = @("quick-lobby", "lobby") },
    @{ Plugin = "auth"; Servers = @("auth") },
    @{ Plugin = "game"; Servers = @("game") },
    @{ Plugin = "proxyhost"; Servers = @("velocity") }
)

foreach ($pc in $pluginConfigs) {
    $srcCfg = Join-Path $tpl $pc.Plugin
    if (-not (Test-Path $srcCfg)) { continue }
    foreach ($srv in $pc.Servers) {
        $dest = Join-Path $Runtime "$srv\plugins\$($pc.Plugin)"
        New-Item -ItemType Directory -Force -Path $dest | Out-Null
        Copy-Item "$srcCfg\*" $dest -Recurse -Force
        Write-Host "  config $($pc.Plugin) -> $srv/plugins/$($pc.Plugin)/"
    }
}

Write-Host "Aplicando MySQL (dev/secrets/database.yml)..." -ForegroundColor Cyan
& (Join-Path $DevRoot "lib\Merge-DatabaseConfig.ps1") -DevRoot $DevRoot

# architect.json + npcs.yml no lobby
$res = Join-Path $RepoRoot "lobbyhost\src\main\resources"
foreach ($srv in @("quick-lobby", "lobby")) {
    $destDir = Join-Path $Runtime "$srv\plugins\lobbyhost"
    New-Item -ItemType Directory -Force -Path $destDir | Out-Null
    Copy-Item (Join-Path $res "architect.json") (Join-Path $destDir "architect.json") -Force
    Copy-Item (Join-Path $res "npcs.yml") (Join-Path $destDir "npcs.yml") -Force
}

# Limpar cache do Paper Plugin Remapper
foreach ($srv in @("quick-lobby", "auth", "lobby", "game")) {
    $remap = Join-Path $Runtime "$srv\plugins\.paper-remapped"
    if (Test-Path $remap) {
        Remove-Item $remap -Recurse -Force
        Write-Host "  .paper-remapped removido em $srv"
    }
}

Write-Host ""
Write-Host "Sync OK. Banco compartilhado: $sharedDb" -ForegroundColor Green
