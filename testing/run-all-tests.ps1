# SM-Caterer Master Test Runner
# Runs both API tests and Frontend E2E tests
# Usage: powershell -ExecutionPolicy Bypass -File run-all-tests.ps1

param(
    [switch]$ApiOnly,
    [switch]$FrontendOnly,
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Continue"
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "============================================" -ForegroundColor Cyan
Write-Host " SM-Caterer Master Test Runner" -ForegroundColor Cyan
Write-Host " $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan

# Check server
Write-Host "`nChecking if server is running at $BaseUrl..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/login" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
    Write-Host "Server is running!" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Server is NOT running at $BaseUrl" -ForegroundColor Red
    Write-Host "Please start the application first." -ForegroundColor Red
    exit 1
}

$apiResult = 0
$feResult = 0

# Run API Tests
if (-not $FrontendOnly) {
    Write-Host "`n--- Running API & Page Load Tests ---" -ForegroundColor Magenta
    $apiScript = Join-Path $scriptDir "api-tests\run-api-tests.ps1"
    & powershell -ExecutionPolicy Bypass -File $apiScript -BaseUrl $BaseUrl
    $apiResult = $LASTEXITCODE
    Write-Host "`nAPI Tests completed with exit code: $apiResult" -ForegroundColor $(if ($apiResult -eq 0) { 'Green' } else { 'Red' })
}

# Run Frontend Tests
if (-not $ApiOnly) {
    Write-Host "`n--- Running Frontend E2E Tests ---" -ForegroundColor Magenta
    $feDir = Join-Path $scriptDir "frontend-tests"

    # Check if node_modules exists
    if (-not (Test-Path (Join-Path $feDir "node_modules"))) {
        Write-Host "Installing Playwright dependencies..." -ForegroundColor Yellow
        Push-Location $feDir
        npm install
        npx playwright install chromium
        Pop-Location
    }

    Push-Location $feDir
    node test-runner.js
    $feResult = $LASTEXITCODE
    Pop-Location
    Write-Host "`nFrontend Tests completed with exit code: $feResult" -ForegroundColor $(if ($feResult -eq 0) { 'Green' } else { 'Red' })
}

# Summary
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host " ALL TESTS COMPLETE" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Reports available at:" -ForegroundColor White
Write-Host "   API Report:      $scriptDir\reports\test-report-latest.html" -ForegroundColor White
Write-Host "   Frontend Report: $scriptDir\reports\frontend-report-latest.html" -ForegroundColor White
Write-Host "   Screenshots:     $scriptDir\reports\screenshots\" -ForegroundColor White
Write-Host "============================================" -ForegroundColor Cyan

if ($apiResult -ne 0 -or $feResult -ne 0) {
    Write-Host "`nSome tests FAILED. Check reports for details." -ForegroundColor Red
    exit 1
} else {
    Write-Host "`nAll tests PASSED!" -ForegroundColor Green
    exit 0
}
