@echo off
chcp 65001 >nul
title Anarquia - Iniciar Rede
cd /d "%~dp0"

call "%~dp0local.env.bat"

set RUNTIME=%~dp0runtime

if not exist "%RUNTIME%\velocity\velocity.jar" (
    echo [ERRO] Execute install-all.bat primeiro.
    pause
    exit /b 1
)

echo.
echo ========================================
echo   Rede Savage DEV
echo   Proxy: localhost:%PORT_VELOCITY%
echo   Cliente: 1.21.1 offline
echo ========================================
echo.
echo Abrindo servidores em janelas separadas...
echo Ordem: auth -^> lobby -^> game -^> velocity
echo.

start "Anarquia AUTH :%PORT_AUTH%" cmd /k "cd /d "%RUNTIME%\auth" && java %PAPER_JVM_EXTRA% -Xms%JAVA_XMS% -Xmx%JAVA_XMX% -jar paper.jar --nogui"
timeout /t 8 /nobreak >nul

start "Anarquia LOBBY :%PORT_LOBBY%" cmd /k "cd /d "%RUNTIME%\lobby" && java %PAPER_JVM_EXTRA% -Xms%JAVA_XMS% -Xmx%JAVA_XMX% -jar paper.jar --nogui"
timeout /t 6 /nobreak >nul

start "Anarquia GAME :%PORT_GAME%" cmd /k "cd /d "%RUNTIME%\game" && java %PAPER_JVM_EXTRA% -Xms%JAVA_XMS% -Xmx%JAVA_XMX% -jar paper.jar --nogui"
timeout /t 6 /nobreak >nul

start "Anarquia VELOCITY :%PORT_VELOCITY%" cmd /k "cd /d "%RUNTIME%\velocity" && java %VELOCITY_JVM_EXTRA% -Xms%JAVA_XMS% -Xmx%JAVA_XMX% -jar velocity.jar"

echo.
echo [OK] Servidores iniciados. Conecte em localhost:%PORT_VELOCITY%
echo.
pause
