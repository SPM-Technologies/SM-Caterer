# Debug web login flow
$BaseUrl = "http://localhost:8080"

Write-Host "=== Step 1: GET /login page ===" -ForegroundColor Yellow
$loginPage = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session -UseBasicParsing -TimeoutSec 10

Write-Host "Status: $($loginPage.StatusCode)"
Write-Host "Content length: $($loginPage.Content.Length)"

# Check cookies
Write-Host "`n=== Cookies after GET ===" -ForegroundColor Yellow
$cookieUri = [System.Uri]"$BaseUrl"
$cookies = $session.Cookies.GetCookies($cookieUri)
foreach ($cookie in $cookies) {
    Write-Host "  $($cookie.Name) = $($cookie.Value)"
}

# Extract CSRF from HTML hidden field
Write-Host "`n=== Extracting CSRF from HTML ===" -ForegroundColor Yellow
$csrfToken = $null
if ($loginPage.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
    $csrfToken = $Matches[1]
    Write-Host "Found _csrf in HTML: $csrfToken"
} elseif ($loginPage.Content -match '<input[^>]*value="([^"]*)"[^>]*name="_csrf"') {
    $csrfToken = $Matches[1]
    Write-Host "Found _csrf in HTML (alt): $csrfToken"
} else {
    Write-Host "No _csrf hidden field found in HTML"
    # Show form section of HTML
    if ($loginPage.Content -match '(<form[^>]*>[\s\S]*?</form>)') {
        Write-Host "`nForm HTML:"
        Write-Host $Matches[1].Substring(0, [Math]::Min(1000, $Matches[1].Length))
    }
}

# Also check for XSRF-TOKEN cookie
$xsrfCookie = $cookies | Where-Object { $_.Name -eq "XSRF-TOKEN" }
if ($xsrfCookie) {
    Write-Host "XSRF-TOKEN cookie: $($xsrfCookie.Value)"
    if (-not $csrfToken) { $csrfToken = $xsrfCookie.Value }
}

# Try login with testuser
Write-Host "`n=== Step 2: POST /login (testuser) ===" -ForegroundColor Yellow
$formData = "username=testuser&password=test123"
if ($csrfToken) {
    $formData += "&_csrf=$csrfToken"
}
Write-Host "Form data: $formData"

try {
    $headers = @{}
    if ($xsrfCookie) {
        $headers["X-XSRF-TOKEN"] = $xsrfCookie.Value
    }

    $loginResult = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST `
        -Body $formData -WebSession $session -UseBasicParsing `
        -ContentType "application/x-www-form-urlencoded" `
        -MaximumRedirection 0 -TimeoutSec 10 -Headers $headers -ErrorAction Stop

    Write-Host "Status: $($loginResult.StatusCode)"
} catch {
    $code = 0
    if ($_.Exception.Response) {
        $code = [int]$_.Exception.Response.StatusCode
        Write-Host "Status: $code"
        $location = $_.Exception.Response.Headers["Location"]
        Write-Host "Location: $location"
    } else {
        Write-Host "Error: $($_.Exception.Message)"
    }

    if ($code -eq 302) {
        $location = $_.Exception.Response.Headers["Location"]
        if ($location -match "error") {
            Write-Host "LOGIN FAILED - redirected to error page" -ForegroundColor Red
        } else {
            Write-Host "LOGIN SUCCESS - redirected to: $location" -ForegroundColor Green

            # Follow redirect
            try {
                $redirectUrl = if ($location -match "^http") { $location } else { "$BaseUrl$location" }
                $dashPage = Invoke-WebRequest -Uri $redirectUrl -WebSession $session -UseBasicParsing -TimeoutSec 10
                Write-Host "Dashboard loaded: HTTP $($dashPage.StatusCode), Content length: $($dashPage.Content.Length)"
            } catch {
                Write-Host "Follow redirect error: $($_.Exception.Message)"
            }
        }
    }
}

# Check cookies after login
Write-Host "`n=== Cookies after POST ===" -ForegroundColor Yellow
$cookies2 = $session.Cookies.GetCookies($cookieUri)
foreach ($cookie in $cookies2) {
    Write-Host "  $($cookie.Name) = $($cookie.Value)"
}
