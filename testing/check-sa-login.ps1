# Check if SA user exists and test login in detail
$logFile = "D:\Projects\AI\Caterer\SM-Caterer\app-start.log"

# Search for SA login errors specifically
Write-Host "=== SA Login Errors in Log ===" -ForegroundColor Yellow
$saErrors = Select-String -Path $logFile -Pattern "SM_2026_SADMIN|SUPER_ADMIN|super.admin|SuperAdmin" | ForEach-Object { $_.Line.Trim() }
foreach ($line in $saErrors) {
    Write-Host $line
}

# Also search for the NoSuchMethodError context
Write-Host "`n=== NoSuchMethodError Context ===" -ForegroundColor Yellow
$nsme = Select-String -Path $logFile -Pattern "NoSuchMethodError" | ForEach-Object { $_.Line.Trim() }
foreach ($line in $nsme) {
    Write-Host $line
}

# Try SA login with Pass@54321 (the old password from migration)
Write-Host "`n=== Testing SA Login with old password (Pass@54321) ===" -ForegroundColor Yellow
try {
    $body = '{"username":"SM_2026_SADMIN","password":"Pass@54321"}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    Write-Host "SUCCESS with old password!"
} catch {
    Write-Host "FAILED with old password: $($_.Exception.Message)"
}

# Try SA login with new password
Write-Host "`n=== Testing SA Login with new password (test123) ===" -ForegroundColor Yellow
try {
    $body = '{"username":"SM_2026_SADMIN","password":"test123"}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    Write-Host "SUCCESS with new password!"
    Write-Host "Token: $($response.data.accessToken.Substring(0, 30))..."
} catch {
    Write-Host "FAILED with new password: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        Write-Host "Body: $($reader.ReadToEnd())"
    }
}

# Check directly via DB query approach - check if user exists in the DB
Write-Host "`n=== Checking last 10 log lines ===" -ForegroundColor Yellow
Get-Content $logFile -Tail 10 | ForEach-Object { Write-Host $_ }
