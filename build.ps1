#!/usr/bin/env pwsh
# Local build helper - sets JAVA_HOME for this process only (does not touch your global env).
# Runs Gradle on JDK 17; the Gradle toolchain auto-resolves JDK 8 for the Java-8 (1.7.10) steps.
# Usage:  .\build.ps1            (runs "build")
#         .\build.ps1 compileJava
#         .\build.ps1 build --refresh-dependencies

$ErrorActionPreference = 'Stop'

$jdk17 = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.17.10-hotspot'

if (-not (Test-Path $jdk17)) {
    Write-Error "JDK 17 not found at: $jdk17`nEdit `$jdk17 in build.ps1 to point at your JDK 17 install."
}

$env:JAVA_HOME = $jdk17
$env:PATH = "$jdk17\bin;$env:PATH"

# Default task is "build" if none given.
# [string[]] cast prevents PowerShell from collapsing a single-element array to a scalar
# (which would make the @ splat enumerate the string's characters).
[string[]]$gradleArgs = if ($args.Count -gt 0) { $args } else { @('build') }

Write-Host "JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "gradlew $($gradleArgs -join ' ')" -ForegroundColor Cyan

& "$PSScriptRoot\gradlew.bat" @gradleArgs
exit $LASTEXITCODE
