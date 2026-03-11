# Stop existing Java processes
$javaProcs = Get-Process -Name "java" -ErrorAction SilentlyContinue
if ($javaProcs) {
    Write-Host "Stopping Java processes..."
    $javaProcs | Stop-Process -Force
    Start-Sleep -Seconds 5
}

# Rebuild
Set-Location "D:\Projects\AI\Caterer\SM-Caterer"
Write-Host "Clean building (with -parameters flag)..."
& mvn clean compile -q 2>&1
Write-Host "Build exit code: $LASTEXITCODE"

# Start app
Write-Host "Starting application..."
Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WindowStyle Hidden -WorkingDirectory "D:\Projects\AI\Caterer\SM-Caterer"

# Wait for app to be ready
Write-Host "Waiting for app to start..."
$maxWait = 90
$waited = 0
while ($waited -lt $maxWait) {
    Start-Sleep -Seconds 3
    $waited += 3
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -TimeoutSec 5 -ErrorAction Stop
        if ($response.StatusCode -eq 200) {
            Write-Host "App is ready! (waited ${waited}s)"
            break
        }
    } catch {
        Write-Host "  Waiting... (${waited}s)"
    }
}

if ($waited -ge $maxWait) {
    Write-Host "ERROR: App did not start within ${maxWait}s"
    exit 1
}

# Verify new users exist
Write-Host "`nVerifying new test users..."
$roles = @(
    @{user="testmanager"; role="MANAGER"},
    @{user="teststaff"; role="STAFF"},
    @{user="testviewer"; role="VIEWER"}
)

foreach ($r in $roles) {
    try {
        $body = @{username=$r.user; password="test123"} | ConvertTo-Json
        $resp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
        if ($resp.success) {
            Write-Host "  $($r.user) ($($r.role)): LOGIN OK"
        } else {
            Write-Host "  $($r.user) ($($r.role)): LOGIN FAILED - $($resp.message)"
        }
    } catch {
        Write-Host "  $($r.user) ($($r.role)): ERROR - $($_.Exception.Message)"
    }
}
