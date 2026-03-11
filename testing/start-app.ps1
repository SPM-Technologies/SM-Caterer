# Start the Spring Boot application in background
$projectDir = "D:\Projects\AI\Caterer\SM-Caterer"
$logFile = "$projectDir\app-start.log"

Write-Host "Starting SM-Caterer application..."
Write-Host "Log file: $logFile"

# Clear old log
"" | Out-File $logFile -Encoding UTF8

# Start the application
$process = Start-Process -FilePath "$projectDir\mvnw.cmd" `
    -ArgumentList "spring-boot:run" `
    -WorkingDirectory $projectDir `
    -RedirectStandardOutput $logFile `
    -RedirectStandardError "$projectDir\app-error.log" `
    -PassThru -WindowStyle Hidden

Write-Host "Started with PID: $($process.Id)"
Write-Host "Waiting for server to be ready..."

# Wait for server
$maxWait = 120
$elapsed = 0
$interval = 5

while ($elapsed -lt $maxWait) {
    Start-Sleep -Seconds $interval
    $elapsed += $interval

    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/login" -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        Write-Host "SERVER READY after ${elapsed}s (PID: $($process.Id))"
        exit 0
    } catch {
        Write-Host "Waiting... (${elapsed}s)"
    }
}

Write-Host "TIMEOUT: Server did not start within ${maxWait}s"
exit 1
