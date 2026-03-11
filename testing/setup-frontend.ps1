# Install frontend test dependencies
$feDir = "D:\Projects\AI\Caterer\SM-Caterer\testing\frontend-tests"
Set-Location $feDir

Write-Host "=== Installing npm packages ===" -ForegroundColor Yellow
& cmd /c "cd /d $feDir && npm install 2>&1"
Write-Host "npm install exit code: $LASTEXITCODE"

Write-Host "`n=== Installing Playwright Chromium ===" -ForegroundColor Yellow
& cmd /c "cd /d $feDir && npx playwright install chromium 2>&1"
Write-Host "Playwright install exit code: $LASTEXITCODE"

Write-Host "`nSetup complete!" -ForegroundColor Green
