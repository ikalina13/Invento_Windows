<#
    Builds Windows distributables for Invento:
      1. A portable, self-contained app folder + zip (no install, run from
         anywhere - USB drives, shared folders).
      2. A proper Windows installer (.exe) with Start Menu/Desktop shortcuts,
         installable without admin rights.

    Both bundle their own Java runtime - target machines need NO Java or
    Maven installed. The installer step needs the WiX Toolset (v3); if it's
    not present, the script still produces the portable build and skips the
    installer with a warning instead of failing outright.

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

$commonArgs = @(
    "--input", "$stageDir",
    "--name", $appName,
    "--main-jar", $mainJar,
    "--main-class", $mainClass,
    "--icon", "$iconPath",
    "--app-version", $appVersion,
    "--vendor", $vendor,
    "--java-options", "-Dfile.encoding=UTF-8"
)

Write-Host "==> Packaging portable app-image with jpackage..." -ForegroundColor Cyan
jpackage --type app-image --dest "$distDir" @commonArgs
if ($LASTEXITCODE -ne 0) { throw "jpackage (app-image) failed." }

Write-Host "==> Compressing portable package..." -ForegroundColor Cyan
Compress-Archive -Path $imageDir -DestinationPath $zipPath -Force

Write-Host "==> Packaging Windows installer with jpackage..." -ForegroundColor Cyan
$installerOk = $true
try {
    jpackage --type exe --dest "$distDir" @commonArgs `
        --win-shortcut `
        --win-menu `
        --win-per-user-install `
        --win-shortcut-prompt `
        --description "ICT Laboratory Device Lending Management System"
    if ($LASTEXITCODE -ne 0) { $installerOk = $false }
} catch {
    $installerOk = $false
    Write-Host "  jpackage installer step failed: $_" -ForegroundColor Yellow
}

if (-not $installerOk) {
    Write-Host "  Skipping installer (WiX Toolset v3 is required: 'choco install wixtoolset')." -ForegroundColor Yellow
}

Remove-Item $stageDir -Recurse -Force

Write-Host ""
Write-Host "Done. Portable app:" -ForegroundColor Green
Write-Host "  Folder: $imageDir"
Write-Host "  Zip:    $zipPath"
if ($installerOk) {
    $installerPath = Get-ChildItem $distDir -Filter "*.exe" | Select-Object -First 1
    Write-Host "Installer:" -ForegroundColor Green
    Write-Host "  $($installerPath.FullName)"
}
Write-Host ""
Write-Host "Copy the portable zip to a flash drive, or share the installer .exe for a normal install." -ForegroundColor Green
Write-Host "Either way, no Java install is needed on the target machine." -ForegroundColor Green
