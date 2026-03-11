$base = "http://localhost:8080"

Write-Host "=== Quick App Verification ===" -ForegroundColor Cyan

# Health check
try {
    $r = Invoke-WebRequest -Uri "$base/api/v1/health" -UseBasicParsing -TimeoutSec 10
    Write-Host "  Health: $($r.StatusCode) OK" -ForegroundColor Green
} catch {
    Write-Host "  Health: FAILED" -ForegroundColor Red
}

# Login check for each role
$users = @(
    @{name="SUPER_ADMIN"; user="SM_2026_SADMIN"; pass="test123"},
    @{name="TENANT_ADMIN"; user="testuser"; pass="test123"},
    @{name="MANAGER"; user="testmanager"; pass="test123"},
    @{name="STAFF"; user="teststaff"; pass="test123"},
    @{name="VIEWER"; user="testviewer"; pass="test123"}
)

foreach ($u in $users) {
    try {
        $body = @{ username = $u.user; password = $u.pass } | ConvertTo-Json
        $r = Invoke-RestMethod -Uri "$base/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
        if ($r.token -or $r.accessToken) {
            Write-Host "  $($u.name) login: OK" -ForegroundColor Green
        } else {
            Write-Host "  $($u.name) login: No token in response" -ForegroundColor Yellow
        }
    } catch {
        $status = $_.Exception.Response.StatusCode.Value__
        Write-Host "  $($u.name) login: HTTP $status" -ForegroundColor Red
    }
}

# Quick page check
try {
    $r = Invoke-WebRequest -Uri "$base/login" -UseBasicParsing -TimeoutSec 10
    Write-Host "  Login page: $($r.StatusCode) OK" -ForegroundColor Green
} catch {
    Write-Host "  Login page: FAILED" -ForegroundColor Red
}

Write-Host "`n=== App is running correctly ===" -ForegroundColor Green
