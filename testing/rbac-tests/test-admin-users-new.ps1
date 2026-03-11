try {
    $loginPage = Invoke-WebRequest -Uri "http://localhost:8080/login" -SessionVariable session -UseBasicParsing -TimeoutSec 10
    $csrfToken = $null
    if ($loginPage.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
        $csrfToken = $Matches[1]
    }
    $formData = @{ username = "SM_2026_SADMIN"; password = "test123" }
    if ($csrfToken) { $formData["_csrf"] = $csrfToken }
    $loginResult = Invoke-WebRequest -Uri "http://localhost:8080/login" -Method POST -Body $formData -WebSession $session -UseBasicParsing -MaximumRedirection 10 -TimeoutSec 15
    Write-Host "Login status: $($loginResult.StatusCode)"
    Write-Host "Login page URL: $($loginResult.BaseResponse.ResponseUri)"
    Write-Host "Login content has dashboard: $($loginResult.Content -match 'dashboard|Dashboard|Admin')"

    # Now try /admin/users/new
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8080/admin/users/new" -WebSession $session -UseBasicParsing -MaximumRedirection 10 -TimeoutSec 15
        Write-Host "admin/users/new status: $($resp.StatusCode)"
        Write-Host "Contains form: $($resp.Content -match 'user|Username|form')"
        Write-Host "Is login page: $($resp.Content -match 'login-form|Login.*SM-Caterer|id=""username""')"
    } catch {
        Write-Host "Error accessing admin/users/new: $($_.Exception.Message)"
        if ($_.Exception.Response) {
            Write-Host "Status code: $([int]$_.Exception.Response.StatusCode)"
        }
    }

    # Also try /admin (dashboard)
    try {
        $resp2 = Invoke-WebRequest -Uri "http://localhost:8080/admin" -WebSession $session -UseBasicParsing -MaximumRedirection 10 -TimeoutSec 15
        Write-Host "admin status: $($resp2.StatusCode)"
        Write-Host "Admin content has dashboard: $($resp2.Content -match 'dashboard|Admin|System')"
    } catch {
        Write-Host "Error accessing admin: $($_.Exception.Message)"
    }
} catch {
    Write-Host "Login error: $($_.Exception.Message)"
}
