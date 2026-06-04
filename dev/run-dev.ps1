# Deploy + inicia rede local sem usar .bat
# Uso: powershell -ExecutionPolicy Bypass -File dev/run-dev.ps1
#      powershell -ExecutionPolicy Bypass -File dev/run-dev.ps1 -SyncOnly
#      powershell -ExecutionPolicy Bypass -File dev/run-dev.ps1 -StopOnly

param(
    [switch]$SyncOnly,
    [switch]$StopOnly,
    [switch]$StartOnly
)

$ErrorActionPreference = "Stop"
$DevRoot = $PSScriptRoot
$RepoRoot = Split-Path $DevRoot -Parent
$Runtime = Join-Path $DevRoot "runtime"
. (Join-Path $DevRoot "local.env.ps1")

function Stop-DevServers {
    Write-Host "Parando servidores dev..." -ForegroundColor Cyan
    foreach ($port in $PORT_AUTH, $PORT_LOBBY, $PORT_GAME, $PORT_VELOCITY) {
        netstat -ano | Select-String ":$port\s" | Select-String "LISTENING" | ForEach-Object {
            if ($_ -match '\s(\d+)\s*$') {
                Stop-Process -Id ([int]$matches[1]) -Force -ErrorAction SilentlyContinue
            }
        }
    }
    Start-Sleep -Seconds 2
    @("auth", "lobby", "game") | ForEach-Object {
        $lock = Join-Path $Runtime "$_\world\session.lock"
        if (Test-Path $lock) { Remove-Item $lock -Force -ErrorAction SilentlyContinue }
    }
}

function Sync-DevPlugins {
    Write-Host "Build + sync..." -ForegroundColor Cyan
    Push-Location $RepoRoot
    & .\gradlew.bat buildAll --no-daemon --console=plain
    if ($LASTEXITCODE -ne 0) { Pop-Location; exit $LASTEXITCODE }
    Pop-Location

    $copy = @(
        @{ M = "lobbyhost"; J = "lobbyhost.jar"; S = @("quick-lobby", "lobby") },
        @{ M = "auth"; J = "auth.jar"; S = @("auth") },
        @{ M = "game"; J = "game.jar"; S = @("game") },
        @{ M = "proxyhost"; J = "proxyhost.jar"; S = @("velocity") }
    )
    foreach ($c in $copy) {
        $src = Join-Path $RepoRoot "$($c.M)\build\libs\$($c.J)"
        foreach ($srv in $c.S) {
            $dest = Join-Path $Runtime "$srv\plugins"
            New-Item -ItemType Directory -Force -Path $dest | Out-Null
            Copy-Item $src (Join-Path $dest $c.J) -Force
        }
    }

    $tpl = Join-Path $DevRoot "templates"
    foreach ($pc in @(
        @{ P = "lobbyhost"; S = @("quick-lobby", "lobby") },
        @{ P = "auth"; S = @("auth") },
        @{ P = "game"; S = @("game") },
        @{ P = "proxyhost"; S = @("velocity") }
    )) {
        $srcCfg = Join-Path $tpl $pc.P
        if (-not (Test-Path $srcCfg)) { continue }
        foreach ($srv in $pc.S) {
            $dest = Join-Path $Runtime "$srv\plugins\$($pc.P)"
            New-Item -ItemType Directory -Force -Path $dest | Out-Null
            Copy-Item "$srcCfg\*" $dest -Recurse -Force
        }
    }

    $res = Join-Path $RepoRoot "lobbyhost\src\main\resources"
    foreach ($srv in @("quick-lobby", "lobby")) {
        $destDir = Join-Path $Runtime "$srv\plugins\lobbyhost"
        New-Item -ItemType Directory -Force -Path $destDir | Out-Null
        Copy-Item (Join-Path $res "architect.json") $destDir -Force
        Copy-Item (Join-Path $res "npcs.yml") $destDir -Force
    }

    $shared = Join-Path $Runtime "shared"
    New-Item -ItemType Directory -Force -Path $shared | Out-Null
    if (-not (Test-Path (Join-Path $shared "anarquia-dev.db"))) {
        New-Item -ItemType File -Path (Join-Path $shared "anarquia-dev.db") | Out-Null
    }
    foreach ($srv in @("auth", "lobby", "game", "quick-lobby", "velocity")) {
        $link = Join-Path $Runtime "$srv\shared"
        if (Test-Path $link) {
            $i = Get-Item $link -Force -ErrorAction SilentlyContinue
            if ($i.LinkType -eq "Junction") { continue }
            Remove-Item $link -Recurse -Force -ErrorAction SilentlyContinue
        }
        cmd /c "mklink /J `"$link`" `"$shared`"" | Out-Null
    }

    foreach ($srv in @("quick-lobby", "lobby", "auth", "game")) {
        $remap = Join-Path $Runtime "$srv\plugins\.paper-remapped"
        if (Test-Path $remap) { Remove-Item $remap -Recurse -Force }
    }
    Write-Host "Sync OK." -ForegroundColor Green
}

function Start-DevServer {
    param([string]$Name, [string]$SubDir, [int]$Port)
    $fullDir = Join-Path $Runtime $SubDir
    if (-not (Test-Path (Join-Path $fullDir "paper.jar")) -and $Name -ne "velocity") {
        Write-Host "Falta setup: rode setup-local.ps1" -ForegroundColor Red
        exit 1
    }
    if (-not (Test-Path (Join-Path $fullDir "velocity.jar")) -and $Name -eq "velocity") {
        Write-Host "Falta velocity.jar — rode setup-local.ps1" -ForegroundColor Red
        exit 1
    }
    $logOut = Join-Path $fullDir "logs\console-live.log"
    $logErr = Join-Path $fullDir "logs\console-live-err.log"
    if ($Name -eq "velocity") {
        $argList = @("-Xms$JAVA_XMS", "-Xmx$JAVA_XMX", "--enable-native-access=ALL-UNNAMED", "-jar", "velocity.jar")
    } else {
        $argList = @("-Xms$JAVA_XMS", "-Xmx$JAVA_XMX", "-Dpaper.disablePluginRemapping=true", "-jar", "paper.jar", "--nogui")
    }
    $p = Start-Process java -ArgumentList $argList -WorkingDirectory $fullDir -PassThru -WindowStyle Hidden `
        -RedirectStandardOutput $logOut -RedirectStandardError $logErr
    Write-Host "[$Name] PID $($p.Id) porta $Port"
}

if ($StopOnly) {
    Stop-DevServers
    exit 0
}

if (-not $StartOnly) {
    if (-not (Test-Path (Join-Path $Runtime "velocity\velocity.jar"))) {
        Write-Host "Primeira vez: setup-local.ps1" -ForegroundColor Yellow
        & (Join-Path $DevRoot "setup-local.ps1")
    }
    Sync-DevPlugins
}
if ($SyncOnly) { exit 0 }

Stop-DevServers
Write-Host "Subindo rede..." -ForegroundColor Cyan
Start-DevServer auth auth $PORT_AUTH
Start-Sleep -Seconds 15
Start-DevServer lobby lobby $PORT_LOBBY
Start-Sleep -Seconds 12
Start-DevServer game game $PORT_GAME
Start-Sleep -Seconds 12
Start-DevServer velocity velocity $PORT_VELOCITY
Start-Sleep -Seconds 10

Write-Host ""
Write-Host "Conecte Minecraft 1.21.1 (offline): localhost:$PORT_VELOCITY" -ForegroundColor Green
Write-Host "Logs: dev/runtime/*/logs/console-live.log"
Write-Host "Parar:  powershell -File dev/run-dev.ps1 -StopOnly"
