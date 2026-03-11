# Test branding settings save
try {
    $loginPage = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -SessionVariable s
    $csrf = ($loginPage.InputFields | Where-Object { $_.name -eq "_csrf" })[0].value
    $null = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -UseBasicParsing -WebSession $s -Body "username=testuser&password=test123&_csrf=$csrf" -MaximumRedirection 0 -ErrorAction SilentlyContinue

    $bp = Invoke-WebRequest -Uri "http://localhost:8080/settings/branding" -UseBasicParsing -WebSession $s
    $csrfs = @($bp.InputFields | Where-Object { $_.name -eq "_csrf" })
    Write-Host "Found $($csrfs.Count) CSRF tokens"
    $csrf2 = $csrfs[0].value

    $postBody = "displayName=TestCaterer&tagline=BestFood&primaryColor=%233498db&_csrf=$csrf2"
    Write-Host "Posting form data..."

    $r = Invoke-WebRequest -Uri "http://localhost:8080/settings/branding" -Method POST -UseBasicParsing -WebSession $s `
        -Body $postBody -MaximumRedirection 5
    Write-Host "Status: $($r.StatusCode)"
    Write-Host "Content length: $($r.Content.Length)"

    $hasSuccess = $r.Content -match 'alert-success'
    $hasError = $r.Content -match 'alert-danger'
    $hasWhitelabel = $r.Content -match 'Whitelabel'
    Write-Host "Success alert: $hasSuccess"
    Write-Host "Error alert: $hasError"
    Write-Host "Whitelabel: $hasWhitelabel"

    if ($hasError) {
        # Try to extract error
        if ($r.Content -match 'alert-danger.*?>(.*?)</div>') {
            Write-Host "Error content: $($matches[1])"
        }
    }
}
catch {
    Write-Host "EXCEPTION: $($_.Exception.GetType().Name): $($_.Exception.Message)"
}
