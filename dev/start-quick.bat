@echo off
chcp 65001 >nul
title Anarquia - Quick Lobby (1.21.1)
cd /d "%~dp0"

call "%~dp0local.env.bat"

set SERVER_DIR=%~dp0runtime\quick-lobby

if not exist "%SERVER_DIR%\paper.jar" (
    echo [ERRO] paper.jar nao encontrado. Execute install-all.bat primeiro.
    pause
    exit /b 1
)
if not exist "%SERVER_DIR%\plugins\lobbyhost.jar" (
    echo [ERRO] lobbyhost.jar nao encontrado. Execute sync-plugins.bat primeiro.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Quick Lobby - Paper %MC_VERSION%
echo   Conecte: localhost:%PORT_QUICK%
echo   Cliente Minecraft 1.21.1 (offline)
echo ========================================
echo.
echo Comandos uteis: /npc list, /tag, /fly
echo.

cd /d "%SERVER_DIR%"
java %PAPER_JVM_EXTRA% -Xms%JAVA_XMS% -Xmx%JAVA_XMX% -jar paper.jar --nogui

pause
