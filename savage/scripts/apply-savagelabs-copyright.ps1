$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $root 'settings.gradle.kts'))) {
  throw "Repo root not found from $PSScriptRoot"
}

$enc = New-Object System.Text.UTF8Encoding $false
$marker = 'savagemc.net'

$headerJava = @'
/*
 * Copyright (c) SavageLabs - https://savagemc.net
 * All rights reserved.
 *
 * Authors: zrfadev, desenhar, henriaum
 *
 * Unauthorized copying, modification, distribution, or use of this software,
 * via any medium, is strictly prohibited without express written permission
 * from SavageLabs.
 */

'@

$headerYaml = @'
# Copyright (c) SavageLabs - https://savagemc.net
# All rights reserved.
# Authors: zrfadev, desenhar, henriaum
# Unauthorized copying or distribution is prohibited without permission from SavageLabs.

'@

$headerGradle = @'
/*
 * Copyright (c) SavageLabs - https://savagemc.net
 * All rights reserved.
 * Authors: zrfadev, desenhar, henriaum
 * Unauthorized copying or distribution is prohibited without permission from SavageLabs.
 */

'@

function Test-HasMarker([string]$content) {
  return ($content.Contains('SavageLabs') -and $content.Contains($marker))
}

$javaCount = 0
Get-ChildItem -Path $root -Recurse -Filter '*.java' -File | Where-Object {
  $_.FullName -match '[\\/]src[\\/]main[\\/]java[\\/]' -and $_.FullName -notmatch '[\\/]build[\\/]'
} | ForEach-Object {
  if ($_.Name -eq 'BlockChanger.java') { return }
  $text = [System.IO.File]::ReadAllText($_.FullName)
  if (Test-HasMarker $text) { return }
  [System.IO.File]::WriteAllText($_.FullName, $headerJava + $text, $enc)
  $script:javaCount++
}

$ymlCount = 0
Get-ChildItem -Path $root -Recurse -File | Where-Object {
  ($_.Extension -eq '.yml' -or $_.Extension -eq '.yaml') -and
  $_.FullName -match '[\\/]src[\\/]main[\\/]resources[\\/]' -and
  $_.FullName -notmatch '[\\/]build[\\/]'
} | ForEach-Object {
  $text = [System.IO.File]::ReadAllText($_.FullName)
  if (Test-HasMarker $text) { return }
  [System.IO.File]::WriteAllText($_.FullName, $headerYaml + $text, $enc)
  $script:ymlCount++
}

$gradleCount = 0
Get-ChildItem -Path $root -Recurse -Filter '*.gradle.kts' -File | Where-Object {
  $_.FullName -notmatch '[\\/]build[\\/]'
} | ForEach-Object {
  $text = [System.IO.File]::ReadAllText($_.FullName)
  if (Test-HasMarker $text) { return }
  [System.IO.File]::WriteAllText($_.FullName, $headerGradle + $text, $enc)
  $script:gradleCount++
}

Write-Host "Java files updated: $javaCount"
Write-Host "YAML files updated: $ymlCount"
Write-Host "Gradle KTS files updated: $gradleCount"
