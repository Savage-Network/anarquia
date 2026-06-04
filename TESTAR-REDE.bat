@echo off
REM Atalho na raiz — rede completa Velocity + auth + lobby + game
cd /d "%~dp0dev"

if not exist "runtime\velocity\velocity.jar" (
    echo Primeira execucao: instalando ambiente DEV...
    call install-all.bat
    if errorlevel 1 exit /b 1
)

if not exist "runtime\lobby\plugins\lobbyhost.jar" (
    call sync-plugins.bat
    if errorlevel 1 exit /b 1
)

call start-network.bat
