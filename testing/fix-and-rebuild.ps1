# Kill Java processes, clean rebuild, and fix bugs
$ErrorActionPreference = "Continue"
$projectDir = "D:\Projects\AI\Caterer\SM-Caterer"

# Step 1: Kill all Java processes
Write-Host "=== Step 1: Killing Java processes ===" -ForegroundColor Yellow
$javaProcs = Get-Process -Name java -ErrorAction SilentlyContinue
if ($javaProcs) {
    $javaProcs | Stop-Process -Force
    Write-Host "Killed $($javaProcs.Count) Java process(es)"
    Start-Sleep -Seconds 5
} else {
    Write-Host "No Java processes found"
}

# Verify no java running
$remaining = Get-Process -Name java -ErrorAction SilentlyContinue
if ($remaining) {
    Write-Host "WARNING: Java processes still running!" -ForegroundColor Red
    $remaining | Format-Table Id, ProcessName
    # Try taskkill as fallback
    taskkill /F /IM java.exe 2>$null
    Start-Sleep -Seconds 3
}

# Step 2: Clean and compile
Write-Host "`n=== Step 2: Maven clean compile ===" -ForegroundColor Yellow
Set-Location $projectDir
& .\mvnw.cmd clean compile 2>&1 | ForEach-Object { Write-Host $_ }
$buildResult = $LASTEXITCODE
Write-Host "`nBuild exit code: $buildResult"

if ($buildResult -ne 0) {
    Write-Host "BUILD FAILED! Trying again after removing target manually..." -ForegroundColor Red
    Remove-Item -Recurse -Force "$projectDir\target" -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
    & .\mvnw.cmd compile 2>&1 | ForEach-Object { Write-Host $_ }
    $buildResult = $LASTEXITCODE
    Write-Host "Retry build exit code: $buildResult"
}

Write-Host "`n=== Build Complete ===" -ForegroundColor Green
