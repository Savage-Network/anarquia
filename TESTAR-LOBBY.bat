@echo off
REM Atalho na raiz do projeto — instala (se necessario) e inicia lobby de teste
cd /d "%~dp0dev"

if not exist "runtime\quick-lobby\paper.jar" (
    echo Primeira execucao: instalando ambiente DEV...
    call install-all.bat
    if errorlevel 1 exit /b 1
)

if not exist "runtime\quick-lobby\plugins\lobbyhost.jar" (
    echo Atualizando plugins...
    call sync-plugins.bat
    if errorlevel 1 exit /b 1
)

call start-quick.bat
