###############################################################################
# RBAC Web UI Test Suite - Tests every web page/route with every role
# Tests: SUPER_ADMIN, TENANT_ADMIN, MANAGER, STAFF, VIEWER
###############################################################################

$BaseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportFile = "D:\Projects\AI\Caterer\SM-Caterer\testing\reports\rbac-web-report-$timestamp.html"

# Results collector
$Results = @()
$totalTests = 0
$passed = 0
$failed = 0

# Web login function - extracts CSRF from HTML hidden field
function Get-WebSession($username, $password) {
    try {
        $loginPage = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session -UseBasicParsing -TimeoutSec 10
        $csrfToken = $null
        if ($loginPage.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
            $csrfToken = $Matches[1]
        }
        $formData = @{ username = $username; password = $password }
        if ($csrfToken) { $formData["_csrf"] = $csrfToken }
        $loginResult = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $formData -WebSession $session -UseBasicParsing -MaximumRedirection 10 -TimeoutSec 15
        if ($loginResult.Content -match "login.*error|Invalid|error=true") { return $null }
        return $session
    } catch {
        return $null
    }
}

# Login all users
Write-Host "Logging in test users..." -ForegroundColor Cyan
$Users = @{
    SUPER_ADMIN  = @{ username = "SM_2026_SADMIN"; password = "test123"; session = $null }
    TENANT_ADMIN = @{ username = "testuser";       password = "test123"; session = $null }
    MANAGER      = @{ username = "testmanager";    password = "test123"; session = $null }
    STAFF        = @{ username = "teststaff";      password = "test123"; session = $null }
    VIEWER       = @{ username = "testviewer";     password = "test123"; session = $null }
}

foreach ($role in $Users.Keys) {
    $Users[$role].session = Get-WebSession $Users[$role].username $Users[$role].password
    if ($Users[$role].session) {
        Write-Host "  $role : Session OK" -ForegroundColor Green
    } else {
        Write-Host "  $role : Login FAILED" -ForegroundColor Red
    }
}

function Test-WebPage {
    param(
        [string]$TestId,
        [string]$Category,
        [string]$Url,
        [string]$Role,
        [string]$ExpectedResult,  # "ALLOW" or "DENY"
        [string]$Description
    )

    $script:totalTests++

    if (-not $Users[$Role].session) {
        $script:Results += @{
            TestId = $TestId; Category = $Category; Role = $Role;
            Url = $Url; Expected = $ExpectedResult; Actual = "N/A";
            Status = "SKIP"; Description = $Description; Detail = "No session"
        }
        return
    }

    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl$Url" -WebSession $Users[$Role].session -UseBasicParsing -MaximumRedirection 10 -TimeoutSec 15
        $statusCode = $response.StatusCode
        $content = $response.Content

        # Check if redirected to login page or access denied
        # Use specific login page markers - not just "username" field (which exists on user creation forms too)
        $isLoginPage = $content -match "login-form|login-card|loginProcessingUrl|Login to SM-Caterer|login\?error"
        $isAccessDenied = ($statusCode -eq 403) -or ($content -match "Access Denied|Forbidden|error/403")
        $isErrorPage = ($statusCode -eq 404) -or ($content -match "Page Not Found|404|error/404")

        if ($ExpectedResult -eq "ALLOW") {
            if (-not $isLoginPage -and -not $isAccessDenied -and $statusCode -ge 200 -and $statusCode -lt 400) {
                $actualResult = "ALLOW"
            } else {
                $actualResult = "DENY"
            }
        } else {
            # Expected DENY
            if ($isAccessDenied -or $isLoginPage -or $statusCode -eq 403 -or $statusCode -eq 404) {
                $actualResult = "DENY"
            } else {
                $actualResult = "ALLOW"
            }
        }
    } catch {
        $errStatus = 0
        if ($_.Exception.Response) {
            $errStatus = [int]$_.Exception.Response.StatusCode
        }
        if ($ExpectedResult -eq "DENY" -and ($errStatus -eq 403 -or $errStatus -eq 401 -or $errStatus -eq 404)) {
            $actualResult = "DENY"
        } elseif ($errStatus -eq 500) {
            $actualResult = "ERROR-500"
        } else {
            $actualResult = "ERROR-$errStatus"
        }
    }

    $pass = ($actualResult -eq $ExpectedResult)
    if ($pass) {
        $script:passed++
        $status = "PASS"
    } else {
        $script:failed++
        $status = "FAIL"
        Write-Host "  FAIL: $TestId [$Role] $Url -> Expected:$ExpectedResult Got:$actualResult" -ForegroundColor Red
    }

    $script:Results += @{
        TestId = $TestId; Category = $Category; Role = $Role;
        Url = $Url; Expected = $ExpectedResult; Actual = $actualResult;
        Status = $status; Description = $Description; Detail = ""
    }
}

Write-Host "`n=== RBAC WEB UI TEST SUITE ===" -ForegroundColor Cyan
Write-Host "Testing every web page with every role...`n"

###############################################################################
# DASHBOARD - All authenticated users
###############################################################################
Write-Host "--- Dashboard ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-DASH-001" -Category "Dashboard" -Url "/dashboard" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA access dashboard"
Test-WebPage -TestId "WEB-DASH-002" -Category "Dashboard" -Url "/dashboard" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA access dashboard"
Test-WebPage -TestId "WEB-DASH-003" -Category "Dashboard" -Url "/dashboard" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager access dashboard"
Test-WebPage -TestId "WEB-DASH-004" -Category "Dashboard" -Url "/dashboard" -Role "STAFF" -ExpectedResult "ALLOW" -Description "Staff access dashboard"
Test-WebPage -TestId "WEB-DASH-005" -Category "Dashboard" -Url "/dashboard" -Role "VIEWER" -ExpectedResult "ALLOW" -Description "Viewer access dashboard"

###############################################################################
# ADMIN PAGES - SUPER_ADMIN only
###############################################################################
Write-Host "--- Admin Pages ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-ADM-001" -Category "Admin" -Url "/admin" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA access admin"
Test-WebPage -TestId "WEB-ADM-002" -Category "Admin" -Url "/admin" -Role "TENANT_ADMIN" -ExpectedResult "DENY" -Description "TA access admin (DENIED)"
Test-WebPage -TestId "WEB-ADM-003" -Category "Admin" -Url "/admin" -Role "MANAGER" -ExpectedResult "DENY" -Description "Manager access admin (DENIED)"
Test-WebPage -TestId "WEB-ADM-004" -Category "Admin" -Url "/admin" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff access admin (DENIED)"
Test-WebPage -TestId "WEB-ADM-005" -Category "Admin" -Url "/admin" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer access admin (DENIED)"

Test-WebPage -TestId "WEB-ADM-006" -Category "Admin" -Url "/admin/tenants" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA tenant list"
Test-WebPage -TestId "WEB-ADM-007" -Category "Admin" -Url "/admin/tenants" -Role "TENANT_ADMIN" -ExpectedResult "DENY" -Description "TA tenant list (DENIED)"
Test-WebPage -TestId "WEB-ADM-008" -Category "Admin" -Url "/admin/tenants" -Role "MANAGER" -ExpectedResult "DENY" -Description "Manager tenant list (DENIED)"

Test-WebPage -TestId "WEB-ADM-009" -Category "Admin" -Url "/admin/tenants/new" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA new tenant form"
Test-WebPage -TestId "WEB-ADM-010" -Category "Admin" -Url "/admin/tenants/new" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff new tenant form (DENIED)"

Test-WebPage -TestId "WEB-ADM-011" -Category "Admin" -Url "/admin/users" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA user list"
Test-WebPage -TestId "WEB-ADM-012" -Category "Admin" -Url "/admin/users" -Role "TENANT_ADMIN" -ExpectedResult "DENY" -Description "TA user list (DENIED)"
Test-WebPage -TestId "WEB-ADM-013" -Category "Admin" -Url "/admin/users" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer user list (DENIED)"

Test-WebPage -TestId "WEB-ADM-014" -Category "Admin" -Url "/admin/users/new" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA new user form"
Test-WebPage -TestId "WEB-ADM-015" -Category "Admin" -Url "/admin/users/new" -Role "MANAGER" -ExpectedResult "DENY" -Description "Manager new user form (DENIED)"

###############################################################################
# MASTER DATA - SA + TA + MANAGER
###############################################################################
Write-Host "--- Master Data Pages ---" -ForegroundColor Yellow

# Units
Test-WebPage -TestId "WEB-MAS-001" -Category "Masters" -Url "/masters/units" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA units page"
Test-WebPage -TestId "WEB-MAS-002" -Category "Masters" -Url "/masters/units" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA units page"
Test-WebPage -TestId "WEB-MAS-003" -Category "Masters" -Url "/masters/units" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager units page"
Test-WebPage -TestId "WEB-MAS-004" -Category "Masters" -Url "/masters/units" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff units page (DENIED)"
Test-WebPage -TestId "WEB-MAS-005" -Category "Masters" -Url "/masters/units" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer units page (DENIED)"

# Materials
Test-WebPage -TestId "WEB-MAS-006" -Category "Masters" -Url "/masters/materials" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA materials page"
Test-WebPage -TestId "WEB-MAS-007" -Category "Masters" -Url "/masters/materials" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager materials page"
Test-WebPage -TestId "WEB-MAS-008" -Category "Masters" -Url "/masters/materials" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff materials page (DENIED)"
Test-WebPage -TestId "WEB-MAS-009" -Category "Masters" -Url "/masters/materials" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer materials page (DENIED)"

# Menus
Test-WebPage -TestId "WEB-MAS-010" -Category "Masters" -Url "/masters/menus" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA menus page"
Test-WebPage -TestId "WEB-MAS-011" -Category "Masters" -Url "/masters/menus" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager menus page"
Test-WebPage -TestId "WEB-MAS-012" -Category "Masters" -Url "/masters/menus" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff menus page (DENIED)"

# Event Types
Test-WebPage -TestId "WEB-MAS-013" -Category "Masters" -Url "/masters/event-types" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA event types page"
Test-WebPage -TestId "WEB-MAS-014" -Category "Masters" -Url "/masters/event-types" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager event types page"
Test-WebPage -TestId "WEB-MAS-015" -Category "Masters" -Url "/masters/event-types" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff event types page (DENIED)"

# Recipes
Test-WebPage -TestId "WEB-MAS-016" -Category "Masters" -Url "/masters/recipes" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA recipes page"
Test-WebPage -TestId "WEB-MAS-017" -Category "Masters" -Url "/masters/recipes" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager recipes page"
Test-WebPage -TestId "WEB-MAS-018" -Category "Masters" -Url "/masters/recipes" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff recipes page (DENIED)"
Test-WebPage -TestId "WEB-MAS-019" -Category "Masters" -Url "/masters/recipes" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer recipes page (DENIED)"

# UPI QR
Test-WebPage -TestId "WEB-MAS-020" -Category "Masters" -Url "/masters/upi-qr" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA UPI QR page"
Test-WebPage -TestId "WEB-MAS-021" -Category "Masters" -Url "/masters/upi-qr" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager UPI QR page"
Test-WebPage -TestId "WEB-MAS-022" -Category "Masters" -Url "/masters/upi-qr" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff UPI QR page (DENIED)"

###############################################################################
# ORDERS - TA + MANAGER + STAFF
###############################################################################
Write-Host "--- Orders Pages ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-ORD-001" -Category "Orders" -Url "/orders" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA orders list"
Test-WebPage -TestId "WEB-ORD-002" -Category "Orders" -Url "/orders" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager orders list"
Test-WebPage -TestId "WEB-ORD-003" -Category "Orders" -Url "/orders" -Role "STAFF" -ExpectedResult "ALLOW" -Description "Staff orders list"
Test-WebPage -TestId "WEB-ORD-004" -Category "Orders" -Url "/orders" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer orders list (DENIED)"
Test-WebPage -TestId "WEB-ORD-005" -Category "Orders" -Url "/orders" -Role "SUPER_ADMIN" -ExpectedResult "DENY" -Description "SA orders list (DENIED - no tenant scope)"

Test-WebPage -TestId "WEB-ORD-006" -Category "Orders" -Url "/orders/new" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA new order wizard"
Test-WebPage -TestId "WEB-ORD-007" -Category "Orders" -Url "/orders/new" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager new order wizard"
Test-WebPage -TestId "WEB-ORD-008" -Category "Orders" -Url "/orders/new" -Role "STAFF" -ExpectedResult "ALLOW" -Description "Staff new order wizard"
Test-WebPage -TestId "WEB-ORD-009" -Category "Orders" -Url "/orders/new" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer new order (DENIED)"

###############################################################################
# CUSTOMERS - TA + MANAGER + STAFF
###############################################################################
Write-Host "--- Customers Pages ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-CUS-001" -Category "Customers" -Url "/customers" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA customers list"
Test-WebPage -TestId "WEB-CUS-002" -Category "Customers" -Url "/customers" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager customers list"
Test-WebPage -TestId "WEB-CUS-003" -Category "Customers" -Url "/customers" -Role "STAFF" -ExpectedResult "ALLOW" -Description "Staff customers list"
Test-WebPage -TestId "WEB-CUS-004" -Category "Customers" -Url "/customers" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer customers list (DENIED)"
Test-WebPage -TestId "WEB-CUS-005" -Category "Customers" -Url "/customers" -Role "SUPER_ADMIN" -ExpectedResult "DENY" -Description "SA customers list (DENIED - no tenant scope)"

###############################################################################
# PAYMENTS - TA + MANAGER + STAFF
###############################################################################
Write-Host "--- Payments Pages ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-PAY-001" -Category "Payments" -Url "/payments" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA payments list"
Test-WebPage -TestId "WEB-PAY-002" -Category "Payments" -Url "/payments" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager payments list"
Test-WebPage -TestId "WEB-PAY-003" -Category "Payments" -Url "/payments" -Role "STAFF" -ExpectedResult "ALLOW" -Description "Staff payments list"
Test-WebPage -TestId "WEB-PAY-004" -Category "Payments" -Url "/payments" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer payments list (DENIED)"
Test-WebPage -TestId "WEB-PAY-005" -Category "Payments" -Url "/payments" -Role "SUPER_ADMIN" -ExpectedResult "DENY" -Description "SA payments list (DENIED - no tenant scope)"

###############################################################################
# REPORTS - TA + MANAGER
###############################################################################
Write-Host "--- Reports Pages ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-RPT-001" -Category "Reports" -Url "/reports" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA reports page"
Test-WebPage -TestId "WEB-RPT-002" -Category "Reports" -Url "/reports" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager reports page"
Test-WebPage -TestId "WEB-RPT-003" -Category "Reports" -Url "/reports" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff reports page (DENIED)"
Test-WebPage -TestId "WEB-RPT-004" -Category "Reports" -Url "/reports" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer reports page (DENIED)"
Test-WebPage -TestId "WEB-RPT-005" -Category "Reports" -Url "/reports" -Role "SUPER_ADMIN" -ExpectedResult "DENY" -Description "SA reports page (DENIED - no tenant scope)"

# Report sub-pages
Test-WebPage -TestId "WEB-RPT-006" -Category "Reports" -Url "/reports/orders" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA order report"
Test-WebPage -TestId "WEB-RPT-007" -Category "Reports" -Url "/reports/orders" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager order report"
Test-WebPage -TestId "WEB-RPT-008" -Category "Reports" -Url "/reports/orders" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff order report (DENIED)"

Test-WebPage -TestId "WEB-RPT-009" -Category "Reports" -Url "/reports/payments" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA payment report"
Test-WebPage -TestId "WEB-RPT-010" -Category "Reports" -Url "/reports/payments" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff payment report (DENIED)"

Test-WebPage -TestId "WEB-RPT-011" -Category "Reports" -Url "/reports/stock" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA stock report"
Test-WebPage -TestId "WEB-RPT-012" -Category "Reports" -Url "/reports/stock" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer stock report (DENIED)"

Test-WebPage -TestId "WEB-RPT-013" -Category "Reports" -Url "/reports/customers" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA customer report"
Test-WebPage -TestId "WEB-RPT-014" -Category "Reports" -Url "/reports/pending-balance" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA pending balance report"

###############################################################################
# SETTINGS - TENANT_ADMIN only
###############################################################################
Write-Host "--- Settings Pages ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-SET-001" -Category "Settings" -Url "/settings/email" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA settings email page"
Test-WebPage -TestId "WEB-SET-002" -Category "Settings" -Url "/settings/email" -Role "MANAGER" -ExpectedResult "DENY" -Description "Manager settings (DENIED)"
Test-WebPage -TestId "WEB-SET-003" -Category "Settings" -Url "/settings/email" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff settings (DENIED)"
Test-WebPage -TestId "WEB-SET-004" -Category "Settings" -Url "/settings/email" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer settings (DENIED)"
Test-WebPage -TestId "WEB-SET-005" -Category "Settings" -Url "/settings/email" -Role "SUPER_ADMIN" -ExpectedResult "DENY" -Description "SA settings (DENIED - SecurityConfig restricts to TA only)"

Test-WebPage -TestId "WEB-SET-006" -Category "Settings" -Url "/settings/email" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA email settings"
Test-WebPage -TestId "WEB-SET-007" -Category "Settings" -Url "/settings/email" -Role "MANAGER" -ExpectedResult "DENY" -Description "Manager email settings (DENIED)"

Test-WebPage -TestId "WEB-SET-008" -Category "Settings" -Url "/settings/payment" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA payment settings"
Test-WebPage -TestId "WEB-SET-009" -Category "Settings" -Url "/settings/payment" -Role "STAFF" -ExpectedResult "DENY" -Description "Staff payment settings (DENIED)"

Test-WebPage -TestId "WEB-SET-010" -Category "Settings" -Url "/settings/branding" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA branding settings"
Test-WebPage -TestId "WEB-SET-011" -Category "Settings" -Url "/settings/branding" -Role "VIEWER" -ExpectedResult "DENY" -Description "Viewer branding settings (DENIED)"

###############################################################################
# PROFILE - All authenticated
###############################################################################
Write-Host "--- Profile Pages ---" -ForegroundColor Yellow

Test-WebPage -TestId "WEB-PRF-001" -Category "Profile" -Url "/profile" -Role "SUPER_ADMIN" -ExpectedResult "ALLOW" -Description "SA profile page"
Test-WebPage -TestId "WEB-PRF-002" -Category "Profile" -Url "/profile" -Role "TENANT_ADMIN" -ExpectedResult "ALLOW" -Description "TA profile page"
Test-WebPage -TestId "WEB-PRF-003" -Category "Profile" -Url "/profile" -Role "MANAGER" -ExpectedResult "ALLOW" -Description "Manager profile page"
Test-WebPage -TestId "WEB-PRF-004" -Category "Profile" -Url "/profile" -Role "STAFF" -ExpectedResult "ALLOW" -Description "Staff profile page"
Test-WebPage -TestId "WEB-PRF-005" -Category "Profile" -Url "/profile" -Role "VIEWER" -ExpectedResult "ALLOW" -Description "Viewer profile page"

###############################################################################
# GENERATE HTML REPORT
###############################################################################

Write-Host "`n=== GENERATING REPORT ===" -ForegroundColor Cyan

$tableRows = ""
foreach ($r in $Results) {
    $statusClass = if ($r.Status -eq "PASS") { "pass" } elseif ($r.Status -eq "FAIL") { "fail" } else { "skip" }
    $tableRows += "<tr class='$statusClass'><td>$($r.TestId)</td><td>$($r.Category)</td><td>$($r.Role)</td><td>$($r.Url)</td><td>$($r.Expected)</td><td>$($r.Actual)</td><td>$($r.Status)</td><td>$($r.Description)</td></tr>`n"
}

# Build web access matrix
$webMatrix = ""
$webGroups = @(
    @{page="Dashboard"; sa="Y"; ta="Y"; mgr="Y"; staff="Y"; viewer="Y"},
    @{page="Admin Dashboard"; sa="Y"; ta="N"; mgr="N"; staff="N"; viewer="N"},
    @{page="Admin Tenants"; sa="Y"; ta="N"; mgr="N"; staff="N"; viewer="N"},
    @{page="Admin Users"; sa="Y"; ta="N"; mgr="N"; staff="N"; viewer="N"},
    @{page="Masters (Units, Materials, Menus, EventTypes, Recipes, UPI QR)"; sa="Y"; ta="Y"; mgr="Y"; staff="N"; viewer="N"},
    @{page="Orders"; sa="N*"; ta="Y"; mgr="Y"; staff="Y"; viewer="N"},
    @{page="Customers"; sa="N*"; ta="Y"; mgr="Y"; staff="Y"; viewer="N"},
    @{page="Payments"; sa="N*"; ta="Y"; mgr="Y"; staff="Y"; viewer="N"},
    @{page="Reports"; sa="N*"; ta="Y"; mgr="Y"; staff="N"; viewer="N"},
    @{page="Settings (Email, Payment, Branding)"; sa="N*"; ta="Y"; mgr="N"; staff="N"; viewer="N"},
    @{page="Profile"; sa="Y"; ta="Y"; mgr="Y"; staff="Y"; viewer="Y"}
)

foreach ($g in $webGroups) {
    $webMatrix += "<tr><td><strong>$($g.page)</strong></td>"
    foreach ($v in @($g.sa, $g.ta, $g.mgr, $g.staff, $g.viewer)) {
        $class = if ($v -eq "Y") { "allowed" } elseif ($v -eq "N*") { "note" } else { "denied" }
        $webMatrix += "<td class='$class'>$v</td>"
    }
    $webMatrix += "</tr>`n"
}

$passRate = if ($totalTests -gt 0) { [math]::Round(($passed / $totalTests) * 100, 1) } else { 0 }

$html = @"
<!DOCTYPE html>
<html>
<head>
<title>RBAC Web UI Test Report</title>
<style>
body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background: #f5f5f5; }
h1 { color: #2c3e50; border-bottom: 3px solid #e67e22; padding-bottom: 10px; }
h2 { color: #2c3e50; margin-top: 30px; }
.summary { display: flex; gap: 20px; margin: 20px 0; }
.card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); min-width: 150px; text-align: center; }
.card .number { font-size: 36px; font-weight: bold; }
.card .label { color: #666; }
.card.total .number { color: #3498db; }
.card.passed .number { color: #27ae60; }
.card.failed .number { color: #e74c3c; }
.card.rate .number { color: #f39c12; }
table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 30px; }
th { background: #2c3e50; color: white; padding: 12px 8px; text-align: left; font-size: 13px; }
td { padding: 8px; border-bottom: 1px solid #eee; font-size: 13px; }
tr.pass td { background: #f0fff0; }
tr.fail td { background: #fff0f0; font-weight: bold; }
tr.skip td { background: #fffff0; }
tr:hover td { background: #e8f4f8 !important; }
.allowed { color: #27ae60; font-weight: bold; text-align: center; }
.denied { color: #e74c3c; font-weight: bold; text-align: center; }
.note { color: #f39c12; font-weight: bold; text-align: center; }
.matrix-table td, .matrix-table th { text-align: center; padding: 10px; }
.matrix-table td:first-child { text-align: left; }
.timestamp { color: #999; font-size: 12px; }
p.note-text { color: #666; font-size: 12px; font-style: italic; }
</style>
</head>
<body>
<h1>RBAC Web UI Test Report - SM-Caterer</h1>
<p class="timestamp">Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")</p>

<div class="summary">
    <div class="card total"><div class="number">$totalTests</div><div class="label">Total Tests</div></div>
    <div class="card passed"><div class="number">$passed</div><div class="label">Passed</div></div>
    <div class="card failed"><div class="number">$failed</div><div class="label">Failed</div></div>
    <div class="card rate"><div class="number">$passRate%</div><div class="label">Pass Rate</div></div>
</div>

<h2>Web Page Access Matrix (Design)</h2>
<table class="matrix-table">
<tr><th>Web Page</th><th>SUPER_ADMIN</th><th>TENANT_ADMIN</th><th>MANAGER</th><th>STAFF</th><th>VIEWER</th></tr>
$webMatrix
</table>
<p class="note-text">N* = SUPER_ADMIN has the role permissions but these pages are tenant-scoped (require tenantId), which SA does not have.</p>

<h2>Test Results - All Pages x All Roles</h2>
<table>
<tr><th>Test ID</th><th>Category</th><th>Role</th><th>URL</th><th>Expected</th><th>Actual</th><th>Status</th><th>Description</th></tr>
$tableRows
</table>

<h2>Test Users</h2>
<table>
<tr><th>Role</th><th>Username</th><th>Password</th></tr>
<tr><td>SUPER_ADMIN</td><td>SM_2026_SADMIN</td><td>test123</td></tr>
<tr><td>TENANT_ADMIN</td><td>testuser</td><td>test123</td></tr>
<tr><td>MANAGER</td><td>testmanager</td><td>test123</td></tr>
<tr><td>STAFF</td><td>teststaff</td><td>test123</td></tr>
<tr><td>VIEWER</td><td>testviewer</td><td>test123</td></tr>
</table>

</body>
</html>
"@

$html | Out-File -FilePath $reportFile -Encoding UTF8
$html | Out-File -FilePath "D:\Projects\AI\Caterer\SM-Caterer\testing\reports\rbac-web-report-latest.html" -Encoding UTF8

Write-Host "`n=== RBAC WEB UI TEST RESULTS ===" -ForegroundColor Cyan
Write-Host "Total: $totalTests | Passed: $passed | Failed: $failed | Rate: $passRate%" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "Report: $reportFile"

if ($failed -gt 0) {
    Write-Host "`n=== FAILURES ===" -ForegroundColor Red
    $Results | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  $($_.TestId) [$($_.Role)] $($_.Url) -> Expected:$($_.Expected) Got:$($_.Actual) - $($_.Description)" -ForegroundColor Red
    }
}
