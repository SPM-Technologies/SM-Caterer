# Test tenant creation
# Wait for app
for ($i = 0; $i -lt 20; $i++) {
    Start-Sleep -Seconds 5
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -TimeoutSec 3
        if ($r.StatusCode -eq 200) { Write-Host "App ready"; break }
    } catch { Write-Host "Waiting... ($i)" }
}

# Login as Super Admin
$loginPage = Invoke-WebRequest -Uri "http://localhost:8080/login" -UseBasicParsing -SessionVariable s
$csrf = ($loginPage.InputFields | Where-Object { $_.name -eq "_csrf" })[0].value
$null = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -UseBasicParsing -WebSession $s -Body "username=SM_2026_SADMIN&password=test123&_csrf=$csrf" -MaximumRedirection 0 -ErrorAction SilentlyContinue

# Get tenant form
$form = Invoke-WebRequest -Uri "http://localhost:8080/admin/tenants/new" -UseBasicParsing -WebSession $s
$csrf2 = ($form.InputFields | Where-Object { $_.name -eq "_csrf" })[0].value
Write-Host "Form loaded, CSRF obtained"

# Create tenant with minimal required fields (leave optional fields empty)
$body = @{
    tenantCode = "TEST_TENANT_01"
    businessName = "Test Caterer Business"
    email = "test@example.com"
    status = "ACTIVE"
    _csrf = $csrf2
}
$bodyStr = ($body.GetEnumerator() | ForEach-Object { "$($_.Key)=$([uri]::EscapeDataString($_.Value))" }) -join "&"

try {
    $result = Invoke-WebRequest -Uri "http://localhost:8080/admin/tenants" -Method POST -UseBasicParsing -WebSession $s `
        -Body $bodyStr -ContentType "application/x-www-form-urlencoded" -MaximumRedirection 5
    Write-Host "Status: $($result.StatusCode)"

    if ($result.Content -match 'alert-success') { Write-Host "SUCCESS: Tenant created!" }
    if ($result.Content -match 'alert-danger') { Write-Host "ERROR found" }
    if ($result.Content -match 'text-danger') {
        Write-Host "VALIDATION ERRORS found"
        # Extract error messages
        $matches2 = [regex]::Matches($result.Content, 'class="text-danger"[^>]*>(.*?)</div>')
        foreach ($m in $matches2) {
            Write-Host "  - $($m.Groups[1].Value.Trim())"
        }
    }
} catch {
    Write-Host "EXCEPTION: $($_.Exception.Message)"
}
