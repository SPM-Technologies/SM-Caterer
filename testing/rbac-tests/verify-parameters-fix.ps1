# Login as Manager and check dashboard + customers for -parameters error
$BaseUrl = "http://localhost:8080"

# Get JWT token for Manager
$body = @{ username = "testmanager"; password = "test123" } | ConvertTo-Json
$resp = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
$token = $resp.data.accessToken
Write-Host "Manager JWT obtained"

$headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }

# Test all API endpoints that use @RequestParam
$endpoints = @(
    "/api/v1/customers?page=0&size=10",
    "/api/v1/orders?page=0&size=10",
    "/api/v1/menus?page=0&size=10",
    "/api/v1/materials?page=0&size=10",
    "/api/v1/units?page=0&size=10",
    "/api/v1/event-types?page=0&size=10",
    "/api/v1/payments?page=0&size=10",
    "/api/v1/upi-qr-codes?page=0&size=10",
    "/api/v1/recipe-items?page=0&size=10",
    "/api/v1/utilities?page=0&size=10",
    "/api/v1/users?page=0&size=10"
)

$allGood = $true
foreach ($ep in $endpoints) {
    try {
        $r = Invoke-WebRequest -Uri "$BaseUrl$ep" -Headers $headers -UseBasicParsing -TimeoutSec 10
        if ($r.Content -match "parameters|Name for argument") {
            Write-Host "  FAIL: $ep -> Contains -parameters error" -ForegroundColor Red
            $allGood = $false
        } else {
            Write-Host "  OK: $ep -> $($r.StatusCode)" -ForegroundColor Green
        }
    } catch {
        $status = if ($_.Exception.Response) { [int]$_.Exception.Response.StatusCode } else { 0 }
        Write-Host "  ERROR: $ep -> $status" -ForegroundColor Red
        $allGood = $false
    }
}

# Also test web dashboard as Manager (session-based)
Write-Host "`nTesting web dashboard (session)..."
try {
    $loginPage = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session -UseBasicParsing -TimeoutSec 10
    $csrfToken = $null
    if ($loginPage.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
        $csrfToken = $Matches[1]
    }
    $formData = @{ username = "testmanager"; password = "test123" }
    if ($csrfToken) { $formData["_csrf"] = $csrfToken }
    $loginResult = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $formData -WebSession $session -UseBasicParsing -MaximumRedirection 10 -TimeoutSec 15

    # Check dashboard
    $dashResp = Invoke-WebRequest -Uri "$BaseUrl/dashboard" -WebSession $session -UseBasicParsing -TimeoutSec 10
    if ($dashResp.Content -match "parameters|Name for argument") {
        Write-Host "  FAIL: /dashboard -> Contains -parameters error in HTML" -ForegroundColor Red
        $allGood = $false
    } else {
        Write-Host "  OK: /dashboard -> No -parameters error" -ForegroundColor Green
    }

    # Check customers
    $custResp = Invoke-WebRequest -Uri "$BaseUrl/customers" -WebSession $session -UseBasicParsing -TimeoutSec 10
    if ($custResp.Content -match "parameters|Name for argument") {
        Write-Host "  FAIL: /customers -> Contains -parameters error in HTML" -ForegroundColor Red
        $allGood = $false
    } else {
        Write-Host "  OK: /customers -> No -parameters error" -ForegroundColor Green
    }

    # Check dashboard metrics AJAX
    $metricsResp = Invoke-WebRequest -Uri "$BaseUrl/dashboard/metrics" -WebSession $session -UseBasicParsing -TimeoutSec 10
    if ($metricsResp.Content -match "parameters|Name for argument") {
        Write-Host "  FAIL: /dashboard/metrics -> Contains -parameters error" -ForegroundColor Red
        $allGood = $false
    } else {
        Write-Host "  OK: /dashboard/metrics -> No -parameters error" -ForegroundColor Green
    }
} catch {
    Write-Host "  ERROR: Web test failed - $($_.Exception.Message)" -ForegroundColor Red
    $allGood = $false
}

if ($allGood) {
    Write-Host "`nAll endpoints clean - no -parameters errors!" -ForegroundColor Green
} else {
    Write-Host "`nSome endpoints still have issues!" -ForegroundColor Red
}
