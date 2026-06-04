@echo off
chcp 65001 >nul
title Anarquia - Instalacao Completa DEV
cd /d "%~dp0"

echo.
echo ############################################################
echo   INSTALACAO COMPLETA - Testes locais Paper 1.21.1
echo   (download servidores + build plugins + configs)
echo ############################################################
echo.

call "%~dp0setup-local.bat" --silent
if errorlevel 1 exit /b 1

echo.
echo --- Build e sync dos plugins ---
echo.

call "%~dp0sync-plugins.bat" --silent
if errorlevel 1 exit /b 1

echo.
echo ############################################################
echo   INSTALACAO CONCLUIDA!
echo.
echo   Teste rapido (lobby + NPCs):
echo     start-quick.bat
echo     Minecraft 1.21.1 -^> localhost:25565
echo.
echo   Rede completa (proxy):
echo     start-network.bat
echo     Minecraft 1.21.1 -^> localhost:25577
echo ############################################################
echo.
pause
