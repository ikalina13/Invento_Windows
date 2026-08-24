<#
    Builds a self-contained, portable Windows package for Invento.
    The output bundles its own Java runtime, so target machines need
    NO Java or Maven installed - just copy the folder (or the zip)
    and double-click Invento.exe.

    Usage:  powershell -ExecutionPolicy Bypass -File build-portable.ps1
#>

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot
Set-Location $root

$appName    = "Invento"
$appVersion = "1.0.0"
$vendor     = "ICT Laboratory"
$mainJar    = "device-lending-system-$appVersion.jar"
$mainClass  = "com.ict.lending.Launcher"
$iconPath   = Join-Path $root "icons\Invento.ico"

$distDir    = Join-Path $root "dist"
$imageDir   = Join-Path $distDir $appName
$zipPath    = Join-Path $distDir "$appName-Portable-Windows.zip"

Write-Host "==> Building shaded JAR with Maven..." -ForegroundColor Cyan
& "$root\mvnw.cmd" clean package
if ($LASTEXITCODE -ne 0) { throw "Maven build failed." }

$jarPath = Join-Path $root "target\$mainJar"
if (-not (Test-Path $jarPath)) {
    throw "Expected jar not found at $jarPath. Check the version in pom.xml matches `$appVersion` in this script."
}

if (Test-Path $distDir) {
    Write-Host "==> Cleaning previous dist output..." -ForegroundColor Cyan
    Remove-Item $distDir -Recurse -Force
}
New-Item -ItemType Directory -Path $distDir | Out-Null

# jpackage bundles every jar found in --input onto the classpath. target/ also
# contains maven-shade-plugin's backup of the pre-shade (dependency-less) jar,
# which breaks the classpath if included. Stage only the shaded jar.
$stageDir = Join-Path $distDir "stage"
New-Item -ItemType Directory -Path $stageDir | Out-Null
Copy-Item $jarPath -Destination $stageDir

Write-Host "==> Packaging self-contained app with jpackage..." -ForegroundColor Cyan
jpackage `
    --type app-image `
    --input "$stageDir" `
    --dest "$distDir" `
    --name $appName `
    --main-jar $mainJar `
    --main-class $mainClass `
    --icon "$iconPath" `
    --app-version $appVersion `
    --vendor "$vendor" `
    --java-options "-Dfile.encoding=UTF-8"

if ($LASTEXITCODE -ne 0) { throw "jpackage failed." }
Remove-Item $stageDir -Recurse -Force

Write-Host "==> Compressing portable package..." -ForegroundColor Cyan
Compress-Archive -Path $imageDir -DestinationPath $zipPath -Force

Write-Host ""
Write-Host "Done. Portable app:" -ForegroundColor Green
Write-Host "  Folder: $imageDir"
Write-Host "  Zip:    $zipPath"
Write-Host ""
Write-Host "Copy either one to a flash drive or upload the zip to a GitHub Release." -ForegroundColor Green
Write-Host "End users just run Invento.exe inside the folder - no Java install needed." -ForegroundColor Green
