function Get-PaperJarUrl {
    param([string]$Version)
    $base = "https://api.papermc.io/v2/projects/paper"
    $response = Invoke-RestMethod "$base/versions/$Version/builds" -ErrorAction Stop
    if (-not $response.builds -or $response.builds.Count -eq 0) {
        throw "Nenhum build Paper para $Version"
    }
    $last = $response.builds[-1]
    $buildNum = if ($null -ne $last.build) { $last.build } else { $last }
    $jarName = $last.downloads.application.name
    if (-not $jarName) {
        $jarName = "paper-$Version-$buildNum.jar"
    }
    return "$base/versions/$Version/builds/$buildNum/downloads/$jarName"
}

function Get-VelocityJarUrl {
    param([string]$Version)
    $base = "https://api.papermc.io/v2/projects/velocity"
    $project = Invoke-RestMethod $base -ErrorAction Stop
    $ver = $project.versions | Where-Object { $_ -eq $Version } | Select-Object -First 1
    if (-not $ver) {
        $ver = $project.versions[-1]
        Write-Warning "Velocity $Version nao encontrado; usando $ver"
    }
    $response = Invoke-RestMethod "$base/versions/$ver/builds" -ErrorAction Stop
    if (-not $response.builds -or $response.builds.Count -eq 0) {
        throw "Nenhum build Velocity para $ver"
    }
    $last = $response.builds[-1]
    $buildNum = if ($null -ne $last.build) { $last.build } else { $last }
    $jarName = $last.downloads.application.name
    if (-not $jarName) {
        $jarName = "velocity-$buildNum.jar"
    }
    return "$base/versions/$ver/builds/$buildNum/downloads/$jarName"
}

function Download-FileIfMissing {
    param([string]$Url, [string]$Dest)
    if (Test-Path $Dest) {
        Write-Host "  OK (cache) $(Split-Path $Dest -Leaf)"
        return
    }
    Write-Host "  Baixando $(Split-Path $Dest -Leaf) ..."
    New-Item -ItemType Directory -Force -Path (Split-Path $Dest) | Out-Null
    Invoke-WebRequest -Uri $Url -OutFile $Dest -UseBasicParsing
}

function Ensure-Eula {
    param([string]$ServerDir)
    $eula = Join-Path $ServerDir "eula.txt"
    if (-not (Test-Path $eula)) {
        Set-Content $eula "eula=true" -Encoding ASCII
    } else {
        (Get-Content $eula) -replace "eula=false", "eula=true" | Set-Content $eula -Encoding ASCII
    }
}
