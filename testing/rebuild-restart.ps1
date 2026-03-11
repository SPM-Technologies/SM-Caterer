# Stop Java, rebuild, and restart
$projectDir = "D:\Projects\AI\Caterer\SM-Caterer"
$logFile = "$projectDir\app-start.log"

# Step 1: Kill Java
Write-Host "=== Stopping Java ===" -ForegroundColor Yellow
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 5
Write-Host "Java stopped"

# Step 2: Clean compile
Write-Host "`n=== Rebuilding ===" -ForegroundColor Yellow
Set-Location $projectDir
& .\mvnw.cmd clean compile 2>&1 | Select-Object -Last 5

if ($LASTEXITCODE -ne 0) {
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    exit 1
}
Write-Host "Build successful" -ForegroundColor Green

# Step 3: Start app
Write-Host "`n=== Starting Application ===" -ForegroundColor Yellow
"" | Out-File $logFile -Encoding UTF8
$process = Start-Process -FilePath "$projectDir\mvnw.cmd" `
    -ArgumentList "spring-boot:run" `
    -WorkingDirectory $projectDir `
    -RedirectStandardOutput $logFile `
    -RedirectStandardError "$projectDir\app-error.log" `
    -PassThru -WindowStyle Hidden

Write-Host "Started with PID: $($process.Id)"

# Step 4: Wait for ready
$maxWait = 120
$elapsed = 0
$interval = 5
while ($elapsed -lt $maxWait) {
    Start-Sleep -Seconds $interval
    $elapsed += $interval
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/login" -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        Write-Host "SERVER READY after ${elapsed}s" -ForegroundColor Green
        exit 0
    } catch {
        Write-Host "Waiting... (${elapsed}s)"
    }
}
Write-Host "TIMEOUT!" -ForegroundColor Red
exit 1
