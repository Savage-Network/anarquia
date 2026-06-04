@echo off
chcp 65001 >nul
title Anarquia - Aplicar banco (database.yml)
cd /d "%~dp0"

echo.
echo Aplicando dev\secrets\database.yml nos servidores...
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0lib\Merge-DatabaseConfig.ps1" -DevRoot "%~dp0"

echo.
echo Linkando pasta shared (SQLite)...
call :LinkShared auth
call :LinkShared lobby
call :LinkShared game
call :LinkShared quick-lobby
call :LinkShared velocity

echo.
echo [OK] Reinicie os servidores Paper (feche as janelas e suba de novo).
pause
exit /b 0

:LinkShared
set SRV=%~1
set TARGET=%~dp0runtime\shared
set LINK=%~dp0runtime\%SRV%\shared
if not exist "%TARGET%" mkdir "%TARGET%"
if exist "%LINK%" rmdir "%LINK%" 2>nul
mklink /J "%LINK%" "%TARGET%" >nul 2>&1
if errorlevel 1 if not exist "%LINK%" mkdir "%LINK%"
exit /b 0
