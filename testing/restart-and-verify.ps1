# Stop all Java processes
Write-Host "Stopping Java processes..." -ForegroundColor Yellow
Get-Process -Name java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep 3

# Clean compile
Write-Host "Clean compiling..." -ForegroundColor Yellow
Set-Location "D:\Projects\AI\Caterer\SM-Caterer"
$result = & mvn clean compile -q 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compile FAILED:" -ForegroundColor Red
    Write-Host $result
    exit 1
}
Write-Host "Compile OK" -ForegroundColor Green

# Start app in background
Write-Host "Starting application..." -ForegroundColor Yellow
$process = Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory "D:\Projects\AI\Caterer\SM-Caterer" -PassThru -WindowStyle Hidden
Write-Host "Started PID: $($process.Id)"

# Wait for app to be ready
$maxWait = 60
$waited = 0
while ($waited -lt $maxWait) {
    Start-Sleep 5
    $waited += 5
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/health" -UseBasicParsing -TimeoutSec 5
        if ($r.StatusCode -eq 200) {
            Write-Host "App ready after ${waited}s" -ForegroundColor Green
            break
        }
    } catch {
        Write-Host "  Waiting... (${waited}s)" -ForegroundColor Gray
    }
}

if ($waited -ge $maxWait) {
    Write-Host "App failed to start in ${maxWait}s" -ForegroundColor Red
    exit 1
}

# Quick verification
Write-Host "`n=== Verification ===" -ForegroundColor Cyan

# Health
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/health" -UseBasicParsing -TimeoutSec 10
    Write-Host "  Health endpoint: $($r.StatusCode) OK" -ForegroundColor Green
} catch {
    Write-Host "  Health endpoint: FAILED" -ForegroundColor Red
}

# Login page
try {
    $r = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -TimeoutSec 10
    Write-Host "  Login page: $($r.StatusCode) OK" -ForegroundColor Green
} catch {
    Write-Host "  Login page: FAILED" -ForegroundColor Red
}

# API logins
$users = @(
    @{name="SUPER_ADMIN"; user="SM_2026_SADMIN"; pass="test123"},
    @{name="TENANT_ADMIN"; user="testuser"; pass="test123"},
    @{name="MANAGER"; user="testmanager"; pass="test123"},
    @{name="STAFF"; user="teststaff"; pass="test123"},
    @{name="VIEWER"; user="testviewer"; pass="test123"}
)

foreach ($u in $users) {
    try {
        $body = @{ username = $u.user; password = $u.pass } | ConvertTo-Json
        $r = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
        if ($r.success -eq $true) {
            Write-Host "  $($u.name) login: OK (token received)" -ForegroundColor Green
        } else {
            Write-Host "  $($u.name) login: Response unexpected" -ForegroundColor Yellow
        }
    } catch {
        $status = $_.Exception.Response.StatusCode.Value__
        Write-Host "  $($u.name) login: HTTP $status FAILED" -ForegroundColor Red
    }
}

# Dashboard page (web login)
try {
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $loginPage = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -WebSession $session -TimeoutSec 10
    $csrf = ($loginPage.InputFields | Where-Object { $_.name -eq '_csrf' }).value
    $loginBody = "username=testuser&password=test123&_csrf=$csrf"
    $loginResp = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -Body $loginBody -ContentType "application/x-www-form-urlencoded" -WebSession $session -UseBasicParsing -TimeoutSec 10 -MaximumRedirection 5
    if ($loginResp.StatusCode -eq 200 -and -not $loginResp.BaseResponse.RequestMessage.RequestUri.ToString().Contains('/login')) {
        Write-Host "  Web login + dashboard: OK" -ForegroundColor Green
    } else {
        Write-Host "  Web login: Redirected to $($loginResp.BaseResponse.RequestMessage.RequestUri)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  Web login: Error - $($_.Exception.Message.Substring(0, [Math]::Min(80, $_.Exception.Message.Length)))" -ForegroundColor Red
}

Write-Host "`n=== All checks complete ===" -ForegroundColor Cyan
