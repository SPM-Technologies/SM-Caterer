# Test the email test endpoint with CSRF
# Wait for app
for ($i = 0; $i -lt 10; $i++) {
    Start-Sleep -Seconds 3
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -TimeoutSec 3
        if ($r.StatusCode -eq 200) { Write-Host "App ready"; break }
    } catch { Write-Host "Waiting..." }
}

# Login as tenant admin
$loginPage = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -SessionVariable s
$csrf = ($loginPage.InputFields | Where-Object { $_.name -eq "_csrf" })[0].value
$null = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -UseBasicParsing -WebSession $s -Body "username=testuser&password=test123&_csrf=$csrf" -MaximumRedirection 0 -ErrorAction SilentlyContinue

# Get email settings page (to get session CSRF)
$emailPage = Invoke-WebRequest -Uri "http://localhost:8080/settings/email" -UseBasicParsing -WebSession $s
Write-Host "Email settings page loaded: $($emailPage.StatusCode)"

# Extract CSRF from meta tag
if ($emailPage.Content -match 'meta name="_csrf" content="([^"]+)"') {
    $csrfToken = $matches[1]
    Write-Host "CSRF token from meta: $($csrfToken.Substring(0,10))..."
} else {
    Write-Host "CSRF meta tag not found!"
    exit 1
}
if ($emailPage.Content -match 'meta name="_csrf_header" content="([^"]+)"') {
    $csrfHeader = $matches[1]
    Write-Host "CSRF header name: $csrfHeader"
}

# Test email endpoint with CSRF
try {
    $headers = @{
        "Content-Type" = "application/json"
        $csrfHeader = $csrfToken
    }
    $body = '{"testEmail":"test@example.com"}'

    $result = Invoke-WebRequest -Uri "http://localhost:8080/settings/email/test" -Method POST -UseBasicParsing -WebSession $s `
        -Headers $headers -Body $body
    Write-Host "Status: $($result.StatusCode)"
    Write-Host "Response: $($result.Content)"
} catch {
    $statusCode = $_.Exception.Response.StatusCode
    Write-Host "Error status: $statusCode"
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        try {
            $sr = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $respBody = $sr.ReadToEnd()
            Write-Host "Response: $($respBody.Substring(0, [Math]::Min(300, $respBody.Length)))"
        } catch {}
    }
}
