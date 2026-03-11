# Debug web login - try different CSRF approaches
$BaseUrl = "http://localhost:8080"

# Approach 1: Use Invoke-WebRequest with auto-redirect following
Write-Host "=== Approach 1: Follow redirects, extract CSRF from HTML ===" -ForegroundColor Cyan

$loginPage = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session -UseBasicParsing -TimeoutSec 10
$cookieUri = [System.Uri]"$BaseUrl"

# Get CSRF token from HTML form
$csrfToken = $null
if ($loginPage.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
    $csrfToken = $Matches[1]
}

# Get XSRF-TOKEN cookie
$xsrfCookie = $null
$cookies = $session.Cookies.GetCookies($cookieUri)
foreach ($c in $cookies) {
    if ($c.Name -eq "XSRF-TOKEN") { $xsrfCookie = $c.Value }
}

Write-Host "CSRF from HTML: $($csrfToken.Substring(0, 20))..."
Write-Host "XSRF cookie: $xsrfCookie"

# Try submitting with only _csrf from HTML (no X-XSRF-TOKEN header)
Write-Host "`n--- Test A: _csrf from HTML, no header ---"
try {
    $body = @{
        username = "testuser"
        password = "test123"
        _csrf = $csrfToken
    }
    $result = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $body -WebSession $session -UseBasicParsing -MaximumRedirection 0 -TimeoutSec 10 -ErrorAction Stop
    Write-Host "OK: $($result.StatusCode)"
} catch {
    $code = 0
    $loc = ""
    if ($_.Exception.Response) {
        $code = [int]$_.Exception.Response.StatusCode
        $loc = $_.Exception.Response.Headers["Location"]
    }
    Write-Host "Response: HTTP $code, Location: $loc"
    if ($loc -match "error") {
        Write-Host "FAILED - login error" -ForegroundColor Red
    } elseif ($code -eq 302 -and $loc -and $loc -notmatch "error") {
        Write-Host "SUCCESS - redirected to $loc" -ForegroundColor Green
    }
}

# Try submitting with XSRF cookie value as _csrf (no masking)
Write-Host "`n--- Test B: XSRF cookie value as _csrf ---"
# Need a fresh session
$loginPage2 = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session2 -UseBasicParsing -TimeoutSec 10
$cookies2 = $session2.Cookies.GetCookies($cookieUri)
$xsrfCookie2 = $null
foreach ($c in $cookies2) {
    if ($c.Name -eq "XSRF-TOKEN") { $xsrfCookie2 = $c.Value }
}
try {
    $body = @{
        username = "testuser"
        password = "test123"
        _csrf = $xsrfCookie2
    }
    $result = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $body -WebSession $session2 -UseBasicParsing -MaximumRedirection 0 -TimeoutSec 10 -ErrorAction Stop
    Write-Host "OK: $($result.StatusCode)"
} catch {
    $code = 0
    $loc = ""
    if ($_.Exception.Response) {
        $code = [int]$_.Exception.Response.StatusCode
        $loc = $_.Exception.Response.Headers["Location"]
    }
    Write-Host "Response: HTTP $code, Location: $loc"
    if ($loc -match "error") {
        Write-Host "FAILED - login error" -ForegroundColor Red
    } elseif ($code -eq 302 -and $loc -and $loc -notmatch "error") {
        Write-Host "SUCCESS - redirected to $loc" -ForegroundColor Green
    }
}

# Try submitting with X-XSRF-TOKEN header (AJAX style)
Write-Host "`n--- Test C: X-XSRF-TOKEN header only ---"
$loginPage3 = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session3 -UseBasicParsing -TimeoutSec 10
$cookies3 = $session3.Cookies.GetCookies($cookieUri)
$xsrfCookie3 = $null
foreach ($c in $cookies3) {
    if ($c.Name -eq "XSRF-TOKEN") { $xsrfCookie3 = $c.Value }
}
try {
    $body = @{
        username = "testuser"
        password = "test123"
    }
    $headers = @{ "X-XSRF-TOKEN" = $xsrfCookie3 }
    $result = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $body -WebSession $session3 -UseBasicParsing -MaximumRedirection 0 -TimeoutSec 10 -Headers $headers -ErrorAction Stop
    Write-Host "OK: $($result.StatusCode)"
} catch {
    $code = 0
    $loc = ""
    if ($_.Exception.Response) {
        $code = [int]$_.Exception.Response.StatusCode
        $loc = $_.Exception.Response.Headers["Location"]
    }
    Write-Host "Response: HTTP $code, Location: $loc"
    if ($loc -match "error") {
        Write-Host "FAILED - login error" -ForegroundColor Red
    } elseif ($code -eq 302 -and $loc -and $loc -notmatch "error") {
        Write-Host "SUCCESS - redirected to $loc" -ForegroundColor Green
    }
}

# Try submitting with both _csrf AND X-XSRF-TOKEN
Write-Host "`n--- Test D: Both _csrf from HTML AND X-XSRF-TOKEN header ---"
$loginPage4 = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session4 -UseBasicParsing -TimeoutSec 10
$csrfToken4 = $null
if ($loginPage4.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
    $csrfToken4 = $Matches[1]
}
$cookies4 = $session4.Cookies.GetCookies($cookieUri)
$xsrfCookie4 = $null
foreach ($c in $cookies4) {
    if ($c.Name -eq "XSRF-TOKEN") { $xsrfCookie4 = $c.Value }
}
try {
    $body = @{
        username = "testuser"
        password = "test123"
        _csrf = $csrfToken4
    }
    $headers = @{ "X-XSRF-TOKEN" = $xsrfCookie4 }
    $result = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $body -WebSession $session4 -UseBasicParsing -MaximumRedirection 0 -TimeoutSec 10 -Headers $headers -ErrorAction Stop
    Write-Host "OK: $($result.StatusCode)"
} catch {
    $code = 0
    $loc = ""
    if ($_.Exception.Response) {
        $code = [int]$_.Exception.Response.StatusCode
        $loc = $_.Exception.Response.Headers["Location"]
    }
    Write-Host "Response: HTTP $code, Location: $loc"
    if ($loc -match "error") {
        Write-Host "FAILED - login error" -ForegroundColor Red
    } elseif ($code -eq 302 -and $loc -and $loc -notmatch "error") {
        Write-Host "SUCCESS - redirected to $loc" -ForegroundColor Green
    }
}

# Full flow with redirect following
Write-Host "`n--- Test E: Full flow with redirects ---"
$loginPage5 = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session5 -UseBasicParsing -TimeoutSec 10
$csrfToken5 = $null
if ($loginPage5.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
    $csrfToken5 = $Matches[1]
}
try {
    $body = @{
        username = "testuser"
        password = "test123"
        _csrf = $csrfToken5
    }
    $result = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $body -WebSession $session5 -UseBasicParsing -MaximumRedirection 10 -TimeoutSec 10 -ErrorAction Stop
    Write-Host "Final URL status: HTTP $($result.StatusCode)"
    if ($result.Content -match "dashboard|Dashboard") {
        Write-Host "SUCCESS - Dashboard loaded!" -ForegroundColor Green
    } elseif ($result.Content -match "login|Login") {
        Write-Host "FAILED - Still on login page" -ForegroundColor Red
    } else {
        Write-Host "Page content (first 200 chars): $($result.Content.Substring(0, [Math]::Min(200, $result.Content.Length)))"
    }
} catch {
    Write-Host "Error: $($_.Exception.Message)"
}
