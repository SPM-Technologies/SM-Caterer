$feDir = "D:\Projects\AI\Caterer\SM-Caterer\testing\frontend-tests"
Write-Host "Running Playwright E2E tests..."
& cmd /c "cd /d $feDir && node test-runner.js 2>&1"
Write-Host "Exit code: $LASTEXITCODE"
