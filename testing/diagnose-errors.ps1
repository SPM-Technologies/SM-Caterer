# Diagnose remaining test failures
Write-Host "=== Checking app log for errors ===" -ForegroundColor Yellow
$logFile = "D:\Projects\AI\Caterer\SM-Caterer\app-start.log"
$errorLog = "D:\Projects\AI\Caterer\SM-Caterer\app-error.log"

# Check if migration ran
Write-Host "`n--- Flyway Migrations ---"
$migrationLines = Select-String -Path $logFile -Pattern "V1\." | ForEach-Object { $_.Line.Trim() }
foreach ($line in $migrationLines) {
    Write-Host $line
}

# Check for errors
Write-Host "`n--- Recent Errors in app log ---"
$errorLines = Select-String -Path $logFile -Pattern "ERROR|Exception|StackTrace|NullPointer|500" | Select-Object -Last 20
foreach ($line in $errorLines) {
    Write-Host $line.Line.Trim()
}

# Check error log
Write-Host "`n--- Error log ---"
if (Test-Path $errorLog) {
    $errContent = Get-Content $errorLog -Tail 30
    foreach ($line in $errContent) {
        Write-Host $line
    }
}

# Test SA login directly and capture full error
Write-Host "`n--- Super Admin Login Test ---"
try {
    $body = '{"username":"SM_2026_SADMIN","password":"test123"}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    Write-Host "SUCCESS: $($response | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response Body: $errorBody"
    }
}

# Test /auth/me with TA token
Write-Host "`n--- Auth/Me Test ---"
try {
    $body = '{"username":"testuser","password":"test123"}'
    $loginResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    $token = $loginResp.data.accessToken
    Write-Host "TA Token obtained: $($token.Substring(0, 30))..."

    $headers = @{ Authorization = "Bearer $token" }
    $meResp = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me" -Method GET -Headers $headers -ContentType "application/json" -TimeoutSec 10
    Write-Host "SUCCESS: $($meResp | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response Body: $errorBody"
    }
}

# Test /api-docs
Write-Host "`n--- API Docs Test ---"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/api-docs" -UseBasicParsing -TimeoutSec 10
    Write-Host "SUCCESS: HTTP $($response.StatusCode)"
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response Body: $errorBody"
    }
}

# Test actuator
Write-Host "`n--- Actuator Health Test ---"
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 10
    Write-Host "SUCCESS: HTTP $($response.StatusCode), Content: $($response.Content)"
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response Body: $errorBody"
    }
}
