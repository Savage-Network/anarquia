@echo off
chcp 65001 >nul
title Anarquia - Setup Local 1.21.1
cd /d "%~dp0"

echo.
echo ========================================
echo   Anarquia - Setup Local (Paper 1.21.1)
echo ========================================
echo.

where java >nul 2>&1
if errorlevel 1 (
    echo [ERRO] Java nao encontrado. Instale Java 21 e adicione ao PATH.
    pause
    exit /b 1
)

java -version 2>&1 | findstr /i "version" 
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup-local.ps1"
if errorlevel 1 (
    echo.
    echo [ERRO] Setup falhou.
    pause
    exit /b 1
)

echo.
echo [OK] Setup concluido. Proximo passo: sync-plugins.bat
echo.
if /i not "%~1"=="--silent" pause
