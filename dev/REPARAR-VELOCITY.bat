@echo off
chcp 65001 >nul
title Anarquia - Reparar Velocity
cd /d "%~dp0"

set SRC=%~dp0templates\velocity
set DST=%~dp0runtime\velocity

if not exist "%DST%\velocity.jar" (
    echo [ERRO] velocity.jar nao encontrado. Rode install-all.bat primeiro.
    pause
    exit /b 1
)

echo Copiando velocity.toml e forwarding.secret...
xcopy /Y /I "%SRC%\*" "%DST%\" >nul

echo.
echo [OK] Velocity configurado para auth / lobby / game.
echo     Reinicie a janela do Velocity.
pause
