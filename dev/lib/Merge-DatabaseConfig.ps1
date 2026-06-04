# Aplica dev/secrets/database.yml na secao "database:" dos config.yml dos plugins em runtime.
param(
    [string]$DevRoot = (Split-Path $PSScriptRoot -Parent)
)

$Targets = @(
    @{ Server = "auth";        Plugin = "auth" },
    @{ Server = "lobby";       Plugin = "lobbyhost" },
    @{ Server = "game";        Plugin = "game" },
    @{ Server = "quick-lobby"; Plugin = "lobbyhost" },
    @{ Server = "velocity";    Plugin = "proxyhost" }
)

$ErrorActionPreference = "Stop"
$Runtime = Join-Path $DevRoot "runtime"
$DbFile = Join-Path $DevRoot "secrets\database.yml"

if (-not (Test-Path $DbFile)) {
    Write-Host "[AVISO] secrets\database.yml nao encontrado. Copie database.yml.example e configure." -ForegroundColor Yellow
    exit 0
}

$dbBody = Get-Content $DbFile -Encoding UTF8 |
    Where-Object { $_ -notmatch '^\s*#' -and $_.Trim() -ne '' } |
    ForEach-Object { "  $_" }

$tipo = ($dbBody | Where-Object { $_ -match 'type:\s*(\S+)' } | ForEach-Object { $_ -replace '.*type:\s*','' }) | Select-Object -First 1
if ($tipo -and $tipo -notin @('MYSQL', 'SQLITE')) {
    Write-Host "[AVISO] database.yml: type deve ser MYSQL ou SQLITE (atual: $tipo)" -ForegroundColor Yellow
}

function Update-ConfigDatabase {
    param([string]$ConfigPath)

    $lines = Get-Content $ConfigPath -Encoding UTF8
    $out = New-Object System.Collections.Generic.List[string]
    $skipping = $false
    $inserted = $false

    foreach ($line in $lines) {
        if ($line -eq 'database:') {
            if (-not $inserted) {
                $out.Add('database:')
                foreach ($b in $dbBody) { $out.Add($b) }
                $inserted = $true
            }
            $skipping = $true
            continue
        }

        if ($skipping) {
            if ($line -match '^[^\s#]') {
                $skipping = $false
                $out.Add($line)
            }
            continue
        }

        $out.Add($line)
    }

    if (-not $inserted) {
        $out.Add('database:')
        foreach ($b in $dbBody) { $out.Add($b) }
    }

    $text = ($out.ToArray() -join [Environment]::NewLine) + [Environment]::NewLine
    [System.IO.File]::WriteAllText($ConfigPath, $text, [System.Text.UTF8Encoding]::new($false))
}

foreach ($t in $Targets) {
    $cfg = Join-Path $Runtime "$($t.Server)\plugins\$($t.Plugin)\config.yml"
    if (-not (Test-Path $cfg)) { continue }
    Update-ConfigDatabase -ConfigPath $cfg
    $tipo = ($dbBody | Where-Object { $_ -match 'type:\s*(\S+)' } | ForEach-Object { $_ -replace '.*type:\s*','' }) | Select-Object -First 1
    Write-Host "  database $tipo -> $($t.Server)/plugins/$($t.Plugin)/config.yml"
}
