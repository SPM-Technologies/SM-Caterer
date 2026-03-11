# Check if Playwright/Chromium is available
$feDir = "D:\Projects\AI\Caterer\SM-Caterer\testing\frontend-tests"

# Check node_modules
Write-Host "=== Node modules ===" -ForegroundColor Yellow
if (Test-Path "$feDir\node_modules\playwright-core") {
    Write-Host "playwright-core installed"
} else {
    Write-Host "playwright-core NOT installed"
}

# Check browser cache paths
Write-Host "`n=== Browser cache paths ===" -ForegroundColor Yellow
$userHome = $env:USERPROFILE
$playwrightCache = "$userHome\AppData\Local\ms-playwright"
if (Test-Path $playwrightCache) {
    Write-Host "Playwright cache exists at $playwrightCache"
    Get-ChildItem $playwrightCache -Depth 1 | ForEach-Object { Write-Host "  $($_.FullName)" }
} else {
    Write-Host "No Playwright cache found"
}

# Try running with npx directly
Write-Host "`n=== Trying npx playwright --version ===" -ForegroundColor Yellow
& cmd /c "cd /d $feDir && npx playwright --version 2>&1"

# Check if chrome.exe exists in common locations
Write-Host "`n=== Checking for Chrome ===" -ForegroundColor Yellow
$chromePaths = @(
    "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
    "$env:ProgramFiles(x86)\Google\Chrome\Application\chrome.exe",
    "$env:LOCALAPPDATA\Google\Chrome\Application\chrome.exe"
)
foreach ($path in $chromePaths) {
    if (Test-Path $path) {
        Write-Host "Found Chrome at: $path"
    }
}

# Check for existing Playwright browsers
Write-Host "`n=== Playwright browser status ===" -ForegroundColor Yellow
& cmd /c "cd /d $feDir && npx playwright install --list 2>&1"
