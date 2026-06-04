@echo off
chcp 65001 >nul
title Anarquia - Sync Plugins
cd /d "%~dp0"

set DEV_ROOT=%~dp0
set REPO_ROOT=%DEV_ROOT%..
set RUNTIME=%DEV_ROOT%runtime

echo.
echo ========================================
echo   Build + copiar plugins para dev/
echo ========================================
echo.

cd /d "%REPO_ROOT%"
call gradlew.bat buildAll --no-daemon --console=plain
if errorlevel 1 (
    echo [ERRO] Build falhou.
    pause
    exit /b 1
)

if not exist "%RUNTIME%\shared" mkdir "%RUNTIME%\shared"

echo.
echo Copiando JARs...

call :CopyJar lobbyhost lobbyhost.jar quick-lobby
call :CopyJar lobbyhost lobbyhost.jar lobby
call :CopyJar auth auth.jar auth
call :CopyJar game game.jar game
call :CopyJar proxyhost proxyhost.jar velocity

echo.
echo Limpando cache do Paper Plugin Remapper...
call :ClearRemapCache quick-lobby
call :ClearRemapCache lobby
call :ClearRemapCache auth
call :ClearRemapCache game

echo.
echo Copiando configs de dev...

call :CopyCfg lobbyhost quick-lobby
call :CopyCfg lobbyhost lobby
call :CopyCfg auth auth
call :CopyCfg game game
call :CopyCfg proxyhost velocity

echo.
echo Copiando config Velocity...
if exist "%DEV_ROOT%templates\velocity" xcopy /Y /I "%DEV_ROOT%templates\velocity\*" "%RUNTIME%\velocity\" >nul

echo.
echo Aplicando banco (dev/secrets/database.yml)...
powershell -NoProfile -ExecutionPolicy Bypass -File "%DEV_ROOT%lib\Merge-DatabaseConfig.ps1" -DevRoot "%DEV_ROOT%"
if errorlevel 1 (
    echo [ERRO] Falha ao aplicar database.yml
    pause
    exit /b 1
)

echo.
echo Copiando architect.json e npcs.yml...

if not exist "%RUNTIME%\quick-lobby\plugins\lobbyhost" mkdir "%RUNTIME%\quick-lobby\plugins\lobbyhost"
if not exist "%RUNTIME%\lobby\plugins\lobbyhost" mkdir "%RUNTIME%\lobby\plugins\lobbyhost"
copy /Y "%REPO_ROOT%\lobbyhost\src\main\resources\architect.json" "%RUNTIME%\quick-lobby\plugins\lobbyhost\" >nul
copy /Y "%REPO_ROOT%\lobbyhost\src\main\resources\npcs.yml" "%RUNTIME%\quick-lobby\plugins\lobbyhost\" >nul
copy /Y "%REPO_ROOT%\lobbyhost\src\main\resources\architect.json" "%RUNTIME%\lobby\plugins\lobbyhost\" >nul
copy /Y "%REPO_ROOT%\lobbyhost\src\main\resources\npcs.yml" "%RUNTIME%\lobby\plugins\lobbyhost\" >nul

echo.
echo Linkando pasta shared (SQLite)...
call :LinkShared auth
call :LinkShared lobby
call :LinkShared game
call :LinkShared quick-lobby
call :LinkShared velocity

echo.
echo [OK] Sync concluido!
echo     Banco: dev/secrets/database.yml (SQLite local ou copie database.mysql.yml)
echo.
if /i not "%~1"=="--silent" pause
exit /b 0

:ClearRemapCache
set SRV=%~1
if exist "%RUNTIME%\%SRV%\plugins\.paper-remapped" (
    rmdir /S /Q "%RUNTIME%\%SRV%\plugins\.paper-remapped"
    echo   .paper-remapped removido em %SRV%
)
exit /b 0

:CopyJar
set MOD=%~1
set JAR=%~2
set SRV=%~3
if not exist "%REPO_ROOT%\%MOD%\build\libs\%JAR%" (
    echo [ERRO] Nao encontrado: %MOD%\build\libs\%JAR%
    exit /b 1
)
if not exist "%RUNTIME%\%SRV%\plugins" mkdir "%RUNTIME%\%SRV%\plugins"
copy /Y "%REPO_ROOT%\%MOD%\build\libs\%JAR%" "%RUNTIME%\%SRV%\plugins\" >nul
call :VerifySqliteJar "%REPO_ROOT%\%MOD%\build\libs\%JAR%" "%MOD%"
if errorlevel 1 exit /b 1
echo   %JAR% -^> %SRV%/plugins/
exit /b 0

:VerifySqliteJar
set JAR_PATH=%~1
set MOD_NAME=%~2
jar tf "%JAR_PATH%" 2>nul | findstr /i /c:"libs/sqlite" >nul
if not errorlevel 1 (
    echo [ERRO] %MOD_NAME%: SQLite relocado no JAR — remova relocate org.sqlite do build.gradle.kts e rode buildAll de novo.
    exit /b 1
)
jar tf "%JAR_PATH%" 2>nul | findstr /i /c:"org/sqlite/core/NativeDB" >nul
if errorlevel 1 (
    echo [ERRO] %MOD_NAME%: org.sqlite nao encontrado no JAR.
    exit /b 1
)
exit /b 0

:CopyCfg
set PLG=%~1
set SRV=%~2
if not exist "%DEV_ROOT%templates\%PLG%" exit /b 0
if not exist "%RUNTIME%\%SRV%\plugins\%PLG%" mkdir "%RUNTIME%\%SRV%\plugins\%PLG%"
xcopy /E /Y /I "%DEV_ROOT%templates\%PLG%\*" "%RUNTIME%\%SRV%\plugins\%PLG%\" >nul
echo   config %PLG% -^> %SRV%/plugins/%PLG%/
exit /b 0

:LinkShared
set SRV=%~1
set TARGET=%RUNTIME%\shared
set LINK=%RUNTIME%\%SRV%\shared
if exist "%LINK%" rmdir "%LINK%" 2>nul
mklink /J "%LINK%" "%TARGET%" >nul 2>&1
if errorlevel 1 (
    if not exist "%LINK%" mkdir "%LINK%"
)
exit /b 0
