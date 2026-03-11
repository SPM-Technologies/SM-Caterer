# Test logo upload via multipart form
try {
    # Login
    $loginPage = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -SessionVariable s
    $csrf = ($loginPage.InputFields | Where-Object { $_.name -eq "_csrf" })[0].value
    $null = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -UseBasicParsing -WebSession $s -Body "username=testuser&password=test123&_csrf=$csrf" -MaximumRedirection 0 -ErrorAction SilentlyContinue

    # Get branding page and CSRF
    $bp = Invoke-WebRequest -Uri "http://localhost:8080/settings/branding" -UseBasicParsing -WebSession $s
    $csrf2 = ($bp.InputFields | Where-Object { $_.name -eq "_csrf" })[0].value
    Write-Host "CSRF obtained: $($csrf2.Substring(0,10))..."

    # Create a small test PNG
    $pngBase64 = "iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAYAAACNMs+9AAAAFElEQVQYV2P8z8BQz0BFwDiqkOoKAQBf9AoL/k2KVAAAAABJRU5ErkJggg=="
    $pngBytes = [Convert]::FromBase64String($pngBase64)
    $tempFile = Join-Path $env:TEMP "test_logo.png"
    [System.IO.File]::WriteAllBytes($tempFile, $pngBytes)
    Write-Host "Test image created: $($pngBytes.Length) bytes"

    # Use .NET HttpClient for proper multipart upload
    Add-Type -AssemblyName System.Net.Http

    $handler = New-Object System.Net.Http.HttpClientHandler
    $handler.CookieContainer = New-Object System.Net.CookieContainer
    # Copy session cookies
    foreach ($cookie in $s.Cookies.GetCookies("http://localhost:8080")) {
        $handler.CookieContainer.Add($cookie)
    }
    $handler.AllowAutoRedirect = $true

    $client = New-Object System.Net.Http.HttpClient($handler)

    $content = New-Object System.Net.Http.MultipartFormDataContent
    $content.Add((New-Object System.Net.Http.StringContent($csrf2)), "_csrf")
    $content.Add((New-Object System.Net.Http.StringContent("Test Caterer")), "displayName")
    $content.Add((New-Object System.Net.Http.StringContent("Best Food")), "tagline")
    $content.Add((New-Object System.Net.Http.StringContent("#3498db")), "primaryColor")

    $fileContent = New-Object System.Net.Http.ByteArrayContent(,[byte[]]$pngBytes)
    $fileContent.Headers.ContentType = [System.Net.Http.Headers.MediaTypeHeaderValue]::Parse("image/png")
    $content.Add($fileContent, "logoFile", "test_logo.png")

    Write-Host "Sending multipart POST..."
    $response = $client.PostAsync("http://localhost:8080/settings/branding", $content).Result
    $responseBody = $response.Content.ReadAsStringAsync().Result

    Write-Host "Status: $($response.StatusCode) ($([int]$response.StatusCode))"
    Write-Host "Content length: $($responseBody.Length)"

    if ($responseBody -match 'alert-success') { Write-Host "SUCCESS: Branding saved with logo!" }
    if ($responseBody -match 'alert-danger') {
        Write-Host "ERROR found in response"
        if ($responseBody -match 'alert-danger[^>]*>.*?<span[^>]*>(.*?)</span>') {
            Write-Host "Error message: $($matches[1])"
        }
    }
    if ($responseBody -match 'Whitelabel Error Page') {
        Write-Host "WHITELABEL ERROR"
        if ($responseBody -match '<div>(.*?)</div>') {
            Write-Host "Error: $($matches[1])"
        }
    }
    if ($responseBody -match 'Logo uploaded') { Write-Host "Logo is showing as uploaded!" }
    if ($responseBody -match 'No logo') { Write-Host "Still showing 'No logo uploaded'" }

    $client.Dispose()
}
catch {
    Write-Host "EXCEPTION: $($_.Exception.GetType().Name)"
    Write-Host $_.Exception.Message
    if ($_.Exception.InnerException) {
        Write-Host "Inner: $($_.Exception.InnerException.Message)"
    }
}
