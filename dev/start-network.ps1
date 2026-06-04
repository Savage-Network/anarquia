# Inicia Velocity + auth + lobby + game (janelas separadas)
$ErrorActionPreference = "Stop"
$DevRoot = $PSScriptRoot
$Runtime = Join-Path $DevRoot "runtime"
. (Join-Path $DevRoot "local.env.ps1")

function Start-ServerWindow {
    param([string]$Title, [string]$Dir, [string]$Jar, [int]$Port)
    if (-not (Test-Path (Join-Path $Dir $Jar))) {
        throw "Falta $Jar em $Dir — rode setup-local.ps1"
    }
    $extra = if ($Jar -eq "velocity.jar") { $VELOCITY_JVM_EXTRA } else { $PAPER_JVM_EXTRA }
    $cmd = "cd `"$Dir`"; java $extra -Xms$JAVA_XMS -Xmx$JAVA_XMX -jar $Jar"
    if ($Jar -eq "paper.jar") { $cmd += " --nogui" }
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd -WindowStyle Normal
    Write-Host "  [$Title] porta $Port"
}

Write-Host "=== Rede local Anarquia ===" -ForegroundColor Cyan
Write-Host "Proxy: localhost:$PORT_VELOCITY" -ForegroundColor Green

Start-ServerWindow -Title "auth" -Dir (Join-Path $Runtime "auth") -Jar "paper.jar" -Port $PORT_AUTH
Start-Sleep -Seconds 3
Start-ServerWindow -Title "lobby" -Dir (Join-Path $Runtime "lobby") -Jar "paper.jar" -Port $PORT_LOBBY
Start-Sleep -Seconds 2
Start-ServerWindow -Title "game" -Dir (Join-Path $Runtime "game") -Jar "paper.jar" -Port $PORT_GAME
Start-Sleep -Seconds 2
Start-ServerWindow -Title "velocity" -Dir (Join-Path $Runtime "velocity") -Jar "velocity.jar" -Port $PORT_VELOCITY

Write-Host "Servidores iniciados em janelas separadas." -ForegroundColor Green
