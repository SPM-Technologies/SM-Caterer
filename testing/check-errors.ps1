$logFile = "D:\Projects\AI\Caterer\SM-Caterer\app-start.log"
Write-Host "=== Searching for errors in app log ==="
$errors = Select-String -Path $logFile -Pattern "ERROR|Exception|NullPointer|StackTrace|500" | Select-Object -Last 50
foreach ($e in $errors) {
    Write-Host $e.Line
}

Write-Host "`n=== Testing API endpoints directly ==="

# Test Super Admin login
Write-Host "`n--- Super Admin Login ---"
try {
    $body = '{"username":"SM_2026_SADMIN","password":"test123"}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    Write-Host "SUCCESS: Token = $($response.data.accessToken.Substring(0, 30))..."
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Response Body: $errorBody"
    }
}

# Test Tenant Admin login
Write-Host "`n--- Tenant Admin Login ---"
try {
    $body = '{"username":"testuser","password":"test123"}'
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    $token = $response.data.accessToken
    Write-Host "SUCCESS: Token = $($token.Substring(0, 30))..."

    # Test /auth/me
    Write-Host "`n--- GET /api/v1/auth/me ---"
    try {
        $headers = @{ Authorization = "Bearer $token" }
        $meResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me" -Method GET -Headers $headers -ContentType "application/json" -TimeoutSec 10
        Write-Host "SUCCESS: User = $($meResponse.data.username)"
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            Write-Host "Response: $($reader.ReadToEnd())"
        }
    }

    # Test /api/v1/customers
    Write-Host "`n--- GET /api/v1/customers ---"
    try {
        $custResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/customers?page=0&size=10" -Method GET -Headers $headers -ContentType "application/json" -TimeoutSec 10
        Write-Host "SUCCESS: $($custResponse | ConvertTo-Json -Depth 2)"
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            Write-Host "Response: $($reader.ReadToEnd())"
        }
    }
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
}
