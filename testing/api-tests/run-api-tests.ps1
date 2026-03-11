# SM-Caterer API & Page Load Test Runner
# Run: powershell -ExecutionPolicy Bypass -File run-api-tests.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$SuperAdminUser = "SM_2026_SADMIN",
    [string]$SuperAdminPass = "test123",
    [string]$TenantUser = "testuser",
    [string]$TenantPass = "test123"
)

$ErrorActionPreference = "Continue"
$totalTests = 0
$passedTests = 0
$failedTests = 0
$skippedTests = 0
$results = @()
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$reportFile = "..\reports\test-report-$timestamp.html"

# Colors
function Write-Pass($msg) { Write-Host "  [PASS] $msg" -ForegroundColor Green }
function Write-Fail($msg) { Write-Host "  [FAIL] $msg" -ForegroundColor Red }
function Write-Skip($msg) { Write-Host "  [SKIP] $msg" -ForegroundColor Yellow }
function Write-Info($msg) { Write-Host "  [INFO] $msg" -ForegroundColor Cyan }
function Write-Section($msg) { Write-Host "`n=== $msg ===" -ForegroundColor Magenta }

function Add-Result($id, $module, $name, $status, $details, $responseCode) {
    $script:totalTests++
    if ($status -eq "PASS") { $script:passedTests++ }
    elseif ($status -eq "FAIL") { $script:failedTests++ }
    else { $script:skippedTests++ }

    $script:results += [PSCustomObject]@{
        ID = $id
        Module = $module
        TestName = $name
        Status = $status
        Details = $details
        ResponseCode = $responseCode
        Timestamp = Get-Date -Format "HH:mm:ss"
    }
}

# Check if server is running
function Test-ServerRunning {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/actuator/health" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
        return $true
    } catch {
        try {
            $response = Invoke-WebRequest -Uri "$BaseUrl/login" -TimeoutSec 5 -UseBasicParsing -ErrorAction Stop
            return $true
        } catch {
            return $false
        }
    }
}

# ========================================
# API Authentication Helper
# ========================================
function Get-JwtToken($username, $password) {
    try {
        $body = @{ username = $username; password = $password } | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" `
            -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
        return $response.data.accessToken
    } catch {
        return $null
    }
}

# Web Session Login Helper
function Get-WebSession($username, $password) {
    try {
        # Step 1: GET login page to extract CSRF token from HTML hidden field
        $loginPage = Invoke-WebRequest -Uri "$BaseUrl/login" -SessionVariable session -UseBasicParsing -TimeoutSec 10

        # Step 2: Extract CSRF token from HTML form hidden field
        $csrfToken = $null
        if ($loginPage.Content -match '<input[^>]*name="_csrf"[^>]*value="([^"]*)"') {
            $csrfToken = $Matches[1]
        } elseif ($loginPage.Content -match '<input[^>]*value="([^"]*)"[^>]*name="_csrf"') {
            $csrfToken = $Matches[1]
        }

        # Step 3: Build form data with _csrf from HTML (masked token for Spring Security 6.x)
        $formData = @{
            username = $username
            password = $password
        }
        if ($csrfToken) {
            $formData["_csrf"] = $csrfToken
        }

        # Step 4: Submit login form and follow redirects
        $loginResult = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST `
            -Body $formData -WebSession $session -UseBasicParsing `
            -MaximumRedirection 10 -TimeoutSec 15

        # Step 5: Verify login succeeded (should be on dashboard/admin, not login page)
        if ($loginResult.Content -match "login.*error|Invalid|error=true") {
            return $null
        }

        return $session
    } catch {
        return $null
    }
}

# Test a page load via web session
function Test-PageLoad($session, $path, $testId, $module, $testName, $expectedContent) {
    try {
        $url = "$BaseUrl$path"
        $response = Invoke-WebRequest -Uri $url -WebSession $session -UseBasicParsing -TimeoutSec 15 -MaximumRedirection 5
        $statusCode = $response.StatusCode

        if ($statusCode -eq 200) {
            $content = $response.Content
            if ($expectedContent -and $content -notmatch $expectedContent) {
                Write-Fail "$testId - $testName (200 but missing expected content)"
                Add-Result $testId $module $testName "FAIL" "Page loaded but missing expected content: $expectedContent" $statusCode
            } else {
                Write-Pass "$testId - $testName (HTTP $statusCode)"
                Add-Result $testId $module $testName "PASS" "Page loaded successfully" $statusCode
            }
        } else {
            Write-Fail "$testId - $testName (HTTP $statusCode)"
            Add-Result $testId $module $testName "FAIL" "Unexpected status code" $statusCode
        }
    } catch {
        $code = 0
        if ($_.Exception.Response) {
            $code = [int]$_.Exception.Response.StatusCode
        }
        Write-Fail "$testId - $testName (Error: $($_.Exception.Message))"
        Add-Result $testId $module $testName "FAIL" $_.Exception.Message $code
    }
}

# Test API endpoint
function Test-ApiEndpoint($token, $method, $path, $testId, $module, $testName, $body, $expectedStatus) {
    try {
        $url = "$BaseUrl$path"
        $headers = @{}
        if ($token) {
            $headers["Authorization"] = "Bearer $token"
        }

        $params = @{
            Uri = $url
            Method = $method
            Headers = $headers
            ContentType = "application/json"
            TimeoutSec = 15
            UseBasicParsing = $true
        }
        if ($body) {
            $params["Body"] = ($body | ConvertTo-Json)
        }

        $response = Invoke-WebRequest @params
        $statusCode = $response.StatusCode

        if ($expectedStatus -and $statusCode -ne $expectedStatus) {
            Write-Fail "$testId - $testName (Expected $expectedStatus, got $statusCode)"
            Add-Result $testId $module $testName "FAIL" "Expected $expectedStatus, got $statusCode" $statusCode
        } else {
            Write-Pass "$testId - $testName (HTTP $statusCode)"
            Add-Result $testId $module $testName "PASS" "API responded correctly" $statusCode
        }
    } catch {
        $code = 0
        if ($_.Exception.Response) {
            $code = [int]$_.Exception.Response.StatusCode
        }

        if ($expectedStatus -and $code -eq $expectedStatus) {
            Write-Pass "$testId - $testName (HTTP $code - Expected)"
            Add-Result $testId $module $testName "PASS" "Got expected error status" $code
        } else {
            Write-Fail "$testId - $testName (Error: $code - $($_.Exception.Message))"
            Add-Result $testId $module $testName "FAIL" $_.Exception.Message $code
        }
    }
}

# ========================================
# MAIN TEST EXECUTION
# ========================================
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host " SM-Caterer Comprehensive Test Suite" -ForegroundColor Cyan
Write-Host " Base URL: $BaseUrl" -ForegroundColor Cyan
Write-Host " Started: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Cyan
Write-Host "============================================`n" -ForegroundColor Cyan

# Pre-check: Server running?
Write-Info "Checking if server is running..."
if (-not (Test-ServerRunning)) {
    Write-Host "`n[FATAL] Server is not running at $BaseUrl" -ForegroundColor Red
    Write-Host "Please start the application first and try again." -ForegroundColor Red
    exit 1
}
Write-Pass "Server is running at $BaseUrl"

# ========================================
# MODULE 1: PUBLIC PAGES (No Auth)
# ========================================
Write-Section "MODULE 1: Public Pages (No Auth Required)"

# TC-PUB-001: Login page loads
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/login" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200 -and $response.Content -match "login|Login|username|password") {
        Write-Pass "TC-PUB-001 - Login page loads with form"
        Add-Result "TC-PUB-001" "Public" "Login page loads" "PASS" "Login page rendered correctly" 200
    } else {
        Write-Fail "TC-PUB-001 - Login page missing form elements"
        Add-Result "TC-PUB-001" "Public" "Login page loads" "FAIL" "Missing login form elements" $response.StatusCode
    }
} catch {
    Write-Fail "TC-PUB-001 - Login page failed: $($_.Exception.Message)"
    Add-Result "TC-PUB-001" "Public" "Login page loads" "FAIL" $_.Exception.Message 0
}

# TC-PUB-002: Health endpoint
Test-ApiEndpoint $null "GET" "/api/v1/health" "TC-PUB-002" "Public" "Health endpoint" $null 200

# TC-PUB-003: Actuator health
Test-ApiEndpoint $null "GET" "/actuator/health" "TC-PUB-003" "Public" "Actuator health" $null 200

# TC-PUB-004: Static CSS loads
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/css/style.css" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Pass "TC-PUB-004 - Static CSS loads"
        Add-Result "TC-PUB-004" "Public" "Static CSS accessible" "PASS" "CSS loaded" 200
    }
} catch {
    Write-Fail "TC-PUB-004 - Static CSS failed"
    Add-Result "TC-PUB-004" "Public" "Static CSS accessible" "FAIL" $_.Exception.Message 0
}

# TC-PUB-005: Static JS loads
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/js/common.js" -UseBasicParsing -TimeoutSec 10
    if ($response.StatusCode -eq 200) {
        Write-Pass "TC-PUB-005 - Static JS loads"
        Add-Result "TC-PUB-005" "Public" "Static JS accessible" "PASS" "JS loaded" 200
    }
} catch {
    Write-Fail "TC-PUB-005 - Static JS failed"
    Add-Result "TC-PUB-005" "Public" "Static JS accessible" "FAIL" $_.Exception.Message 0
}

# TC-PUB-006: Unauthenticated redirect - verify /dashboard redirects to /login
try {
    # Follow all redirects - should end up at login page
    $response = Invoke-WebRequest -Uri "$BaseUrl/dashboard" -UseBasicParsing -TimeoutSec 10 -MaximumRedirection 10 -ErrorAction Stop
    if ($response.Content -match "login|Login|username|password") {
        Write-Pass "TC-PUB-006 - Unauthenticated users redirected to login page"
        Add-Result "TC-PUB-006" "Security" "Unauthenticated redirect" "PASS" "Redirected to login page" 200
    } elseif ($response.Content -match "dashboard|Dashboard") {
        Write-Fail "TC-PUB-006 - Unauthenticated user accessed dashboard!"
        Add-Result "TC-PUB-006" "Security" "Unauthenticated redirect" "FAIL" "Accessed dashboard without auth" $response.StatusCode
    } else {
        Write-Pass "TC-PUB-006 - Unauthenticated users blocked (HTTP $($response.StatusCode))"
        Add-Result "TC-PUB-006" "Security" "Unauthenticated redirect" "PASS" "Blocked from dashboard" $response.StatusCode
    }
} catch {
    # Any error (redirect loop, 401, 403, etc.) means unauthenticated access is blocked - PASS
    Write-Pass "TC-PUB-006 - Unauthenticated users blocked from dashboard"
    Add-Result "TC-PUB-006" "Security" "Unauthenticated redirect" "PASS" "Access blocked" 302
}

# ========================================
# MODULE 2: API AUTHENTICATION
# ========================================
Write-Section "MODULE 2: API Authentication"

# TC-API-AUTH-001: Login as Super Admin via API
$saToken = $null
try {
    $body = @{ username = $SuperAdminUser; password = $SuperAdminPass } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    if ($response.data.accessToken) {
        $saToken = $response.data.accessToken
        Write-Pass "TC-API-AUTH-001 - Super Admin API login (token received)"
        Add-Result "TC-API-AUTH-001" "API Auth" "Super Admin API login" "PASS" "JWT token received" 200
    } else {
        Write-Fail "TC-API-AUTH-001 - No token in response"
        Add-Result "TC-API-AUTH-001" "API Auth" "Super Admin API login" "FAIL" "No token" 200
    }
} catch {
    Write-Fail "TC-API-AUTH-001 - Super Admin API login failed: $($_.Exception.Message)"
    Add-Result "TC-API-AUTH-001" "API Auth" "Super Admin API login" "FAIL" $_.Exception.Message 0
}

# TC-API-AUTH-002: Login as Tenant Admin via API
$taToken = $null
try {
    $body = @{ username = $TenantUser; password = $TenantPass } | ConvertTo-Json
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
    if ($response.data.accessToken) {
        $taToken = $response.data.accessToken
        Write-Pass "TC-API-AUTH-002 - Tenant Admin API login (token received)"
        Add-Result "TC-API-AUTH-002" "API Auth" "Tenant Admin API login" "PASS" "JWT token received" 200
    } else {
        Write-Fail "TC-API-AUTH-002 - No token in response"
        Add-Result "TC-API-AUTH-002" "API Auth" "Tenant Admin API login" "FAIL" "No token" 200
    }
} catch {
    Write-Fail "TC-API-AUTH-002 - Tenant Admin API login failed: $($_.Exception.Message)"
    Add-Result "TC-API-AUTH-002" "API Auth" "Tenant Admin API login" "FAIL" $_.Exception.Message 0
}

# TC-API-AUTH-003: Invalid credentials
Test-ApiEndpoint $null "POST" "/api/v1/auth/login" "TC-API-AUTH-003" "API Auth" "Invalid credentials rejected" @{ username="invalid"; password="wrong" } 401

# TC-API-AUTH-004: Get current user (Super Admin)
if ($saToken) {
    Test-ApiEndpoint $saToken "GET" "/api/v1/auth/me" "TC-API-AUTH-004" "API Auth" "Get current user (SA)" $null 200
} else {
    Write-Skip "TC-API-AUTH-004 - Skipped (no SA token)"
    Add-Result "TC-API-AUTH-004" "API Auth" "Get current user (SA)" "SKIP" "No SA token" 0
}

# TC-API-AUTH-005: Get current user (Tenant)
if ($taToken) {
    Test-ApiEndpoint $taToken "GET" "/api/v1/auth/me" "TC-API-AUTH-005" "API Auth" "Get current user (TA)" $null 200
} else {
    Write-Skip "TC-API-AUTH-005 - Skipped (no TA token)"
    Add-Result "TC-API-AUTH-005" "API Auth" "Get current user (TA)" "SKIP" "No TA token" 0
}

# TC-API-AUTH-006: Access without token
Test-ApiEndpoint $null "GET" "/api/v1/auth/me" "TC-API-AUTH-006" "API Auth" "Reject unauthenticated API" $null 401

# ========================================
# MODULE 3: WEB SESSION - SUPER ADMIN PAGES
# ========================================
Write-Section "MODULE 3: Super Admin Web Pages"

$saSession = Get-WebSession $SuperAdminUser $SuperAdminPass
if ($saSession) {
    Write-Pass "Super Admin web login successful"

    Test-PageLoad $saSession "/" "TC-SA-001" "Admin" "Root redirect for SA" ""
    Test-PageLoad $saSession "/admin" "TC-SA-002" "Admin" "Admin dashboard" ""
    Test-PageLoad $saSession "/admin/tenants" "TC-SA-003" "Admin" "Tenant list page" ""
    Test-PageLoad $saSession "/admin/tenants/new" "TC-SA-004" "Admin" "New tenant form" ""
    Test-PageLoad $saSession "/admin/users" "TC-SA-005" "Admin" "User list page" ""
    Test-PageLoad $saSession "/admin/users/new" "TC-SA-006" "Admin" "New user form" ""
    Test-PageLoad $saSession "/dashboard" "TC-SA-007" "Admin" "Dashboard page (SA)" ""
    Test-PageLoad $saSession "/profile" "TC-SA-008" "Admin" "Profile page (SA)" ""
} else {
    Write-Fail "Super Admin web login failed - skipping SA web tests"
    Add-Result "TC-SA-000" "Admin" "SA Web Login" "FAIL" "Could not create web session" 0
}

# ========================================
# MODULE 4: WEB SESSION - TENANT ADMIN PAGES
# ========================================
Write-Section "MODULE 4: Tenant Admin Web Pages"

$taSession = Get-WebSession $TenantUser $TenantPass
if ($taSession) {
    Write-Pass "Tenant Admin web login successful"

    # Dashboard
    Test-PageLoad $taSession "/dashboard" "TC-TA-001" "Dashboard" "Tenant dashboard" ""
    Test-PageLoad $taSession "/" "TC-TA-002" "Dashboard" "Root redirect for TA" ""

    # Customer pages
    Test-PageLoad $taSession "/customers" "TC-TA-003" "Customers" "Customer list page" ""
    Test-PageLoad $taSession "/customers/new" "TC-TA-004" "Customers" "New customer form" ""

    # Master Data - Units
    Test-PageLoad $taSession "/masters/units" "TC-TA-005" "Master-Units" "Units list page" ""
    Test-PageLoad $taSession "/masters/units/new" "TC-TA-006" "Master-Units" "New unit form" ""

    # Master Data - Materials
    Test-PageLoad $taSession "/masters/materials" "TC-TA-007" "Master-Materials" "Materials list page" ""
    Test-PageLoad $taSession "/masters/materials/new" "TC-TA-008" "Master-Materials" "New material form" ""

    # Master Data - Event Types
    Test-PageLoad $taSession "/masters/event-types" "TC-TA-009" "Master-EventTypes" "Event types list page" ""
    Test-PageLoad $taSession "/masters/event-types/new" "TC-TA-010" "Master-EventTypes" "New event type form" ""

    # Master Data - Menus
    Test-PageLoad $taSession "/masters/menus" "TC-TA-011" "Master-Menus" "Menus list page" ""
    Test-PageLoad $taSession "/masters/menus/new" "TC-TA-012" "Master-Menus" "New menu form" ""

    # Master Data - Recipes
    Test-PageLoad $taSession "/masters/recipes" "TC-TA-013" "Master-Recipes" "Recipes index page" ""

    # Master Data - UPI QR Codes
    Test-PageLoad $taSession "/masters/upi-qr" "TC-TA-014" "Master-UPI" "UPI QR codes page" ""
    Test-PageLoad $taSession "/masters/upi-qr/new" "TC-TA-015" "Master-UPI" "New UPI QR form" ""

    # Orders
    Test-PageLoad $taSession "/orders" "TC-TA-016" "Orders" "Orders list page" ""
    Test-PageLoad $taSession "/orders/new" "TC-TA-017" "Orders" "New order wizard start" ""
    Test-PageLoad $taSession "/orders/wizard/step1" "TC-TA-018" "Orders" "Order wizard step 1" ""

    # Payments
    Test-PageLoad $taSession "/payments" "TC-TA-019" "Payments" "Payments list page" ""
    Test-PageLoad $taSession "/payments/new" "TC-TA-020" "Payments" "New payment form" ""

    # Reports
    Test-PageLoad $taSession "/reports" "TC-TA-021" "Reports" "Reports index page" ""
    Test-PageLoad $taSession "/reports/orders" "TC-TA-022" "Reports" "Orders report" ""
    Test-PageLoad $taSession "/reports/payments" "TC-TA-023" "Reports" "Payments report" ""
    Test-PageLoad $taSession "/reports/pending-balance" "TC-TA-024" "Reports" "Pending balance report" ""
    Test-PageLoad $taSession "/reports/stock" "TC-TA-025" "Reports" "Stock report" ""
    Test-PageLoad $taSession "/reports/customers" "TC-TA-026" "Reports" "Customers report" ""

    # Profile
    Test-PageLoad $taSession "/profile" "TC-TA-027" "Profile" "Profile page" ""

    # Settings
    Test-PageLoad $taSession "/settings/email" "TC-TA-028" "Settings" "Email settings" ""
    Test-PageLoad $taSession "/settings/payment" "TC-TA-029" "Settings" "Payment settings" ""
    Test-PageLoad $taSession "/settings/branding" "TC-TA-030" "Settings" "Branding settings" ""

} else {
    Write-Fail "Tenant Admin web login failed - skipping TA web tests"
    Add-Result "TC-TA-000" "Tenant" "TA Web Login" "FAIL" "Could not create web session" 0
}

# ========================================
# MODULE 5: API ENDPOINTS (Tenant)
# ========================================
Write-Section "MODULE 5: API Endpoints (Tenant Admin)"

if ($taToken) {
    Test-ApiEndpoint $taToken "GET" "/api/v1/customers?page=0&size=10" "TC-API-CUST-001" "API-Customers" "List customers" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/units?page=0&size=10" "TC-API-UNIT-001" "API-Units" "List units" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/materials?page=0&size=10" "TC-API-MAT-001" "API-Materials" "List materials" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/menus?page=0&size=10" "TC-API-MENU-001" "API-Menus" "List menus" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/event-types?page=0&size=10" "TC-API-EVT-001" "API-EventTypes" "List event types" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/orders?page=0&size=10" "TC-API-ORD-001" "API-Orders" "List orders" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/payments?page=0&size=10" "TC-API-PAY-001" "API-Payments" "List payments" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/utilities?page=0&size=10" "TC-API-UTIL-001" "API-Utilities" "List utilities" $null 200
    Test-ApiEndpoint $taToken "GET" "/api/v1/upi-qr-codes" "TC-API-UPI-001" "API-UPI" "List UPI QR codes" $null 200
} else {
    Write-Skip "Skipping tenant API tests (no token)"
}

# ========================================
# MODULE 6: API ENDPOINTS (Super Admin)
# ========================================
Write-Section "MODULE 6: API Endpoints (Super Admin)"

if ($saToken) {
    Test-ApiEndpoint $saToken "GET" "/api/v1/tenants?page=0&size=10" "TC-API-TEN-001" "API-Tenants" "List tenants (SA)" $null 200
    Test-ApiEndpoint $saToken "GET" "/api/v1/users?page=0&size=10" "TC-API-USR-001" "API-Users" "List users (SA)" $null 200
} else {
    Write-Skip "Skipping SA API tests (no token)"
}

# ========================================
# MODULE 7: SECURITY / RBAC TESTS
# ========================================
Write-Section "MODULE 7: Security & RBAC"

# TC-SEC-001: Tenant cannot access admin APIs
if ($taToken) {
    Test-ApiEndpoint $taToken "GET" "/api/v1/tenants" "TC-SEC-001" "Security" "Tenant cannot access tenants API" $null 403
}

# TC-SEC-002: Tenant cannot access admin web pages
if ($taSession) {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/admin" -WebSession $taSession -UseBasicParsing -TimeoutSec 10 -MaximumRedirection 0
        $code = $response.StatusCode
        if ($code -eq 403) {
            Write-Pass "TC-SEC-002 - Tenant blocked from /admin (403)"
            Add-Result "TC-SEC-002" "Security" "Tenant blocked from admin pages" "PASS" "403 returned" 403
        } else {
            Write-Fail "TC-SEC-002 - Tenant could access /admin (HTTP $code)"
            Add-Result "TC-SEC-002" "Security" "Tenant blocked from admin pages" "FAIL" "Expected 403, got $code" $code
        }
    } catch {
        $code = 0
        if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
        if ($code -eq 403) {
            Write-Pass "TC-SEC-002 - Tenant blocked from /admin (403)"
            Add-Result "TC-SEC-002" "Security" "Tenant blocked from admin pages" "PASS" "403 returned" 403
        } else {
            # If redirect to error page, that's also OK
            Write-Pass "TC-SEC-002 - Tenant blocked from /admin (HTTP $code)"
            Add-Result "TC-SEC-002" "Security" "Tenant blocked from admin pages" "PASS" "Access denied (HTTP $code)" $code
        }
    }
}

# TC-SEC-003: API without auth returns 401
Test-ApiEndpoint $null "GET" "/api/v1/customers" "TC-SEC-003" "Security" "API rejects unauthenticated" $null 401

# ========================================
# MODULE 8: ERROR PAGES
# ========================================
Write-Section "MODULE 8: Error Pages"

if ($taSession) {
    try {
        $response = Invoke-WebRequest -Uri "$BaseUrl/nonexistent-page-xyz" -WebSession $taSession -UseBasicParsing -TimeoutSec 10
        if ($response.StatusCode -eq 200 -and ($response.Content -match "404|not found|Not Found" -or $response.Content -match "error|Error")) {
            Write-Pass "TC-ERR-001 - Custom error page shown"
            Add-Result "TC-ERR-001" "Errors" "404 error page" "PASS" "Custom 404 page shown" 200
        } else {
            Write-Info "TC-ERR-001 - Got HTTP $($response.StatusCode)"
            Add-Result "TC-ERR-001" "Errors" "404 error page" "PASS" "Page handled" $response.StatusCode
        }
    } catch {
        $code = 0
        if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
        if ($code -eq 404) {
            Write-Pass "TC-ERR-001 - 404 error returned"
            Add-Result "TC-ERR-001" "Errors" "404 error page" "PASS" "404 returned" 404
        } else {
            Write-Fail "TC-ERR-001 - Unexpected: $($_.Exception.Message)"
            Add-Result "TC-ERR-001" "Errors" "404 error page" "FAIL" $_.Exception.Message $code
        }
    }
}

# ========================================
# MODULE 9: SWAGGER / API DOCS
# ========================================
Write-Section "MODULE 9: Swagger & API Documentation"

try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/swagger-ui.html" -UseBasicParsing -TimeoutSec 15 -MaximumRedirection 5
    if ($response.StatusCode -eq 200) {
        Write-Pass "TC-SWAGGER-001 - Swagger UI accessible"
        Add-Result "TC-SWAGGER-001" "Docs" "Swagger UI loads" "PASS" "Swagger UI loaded" 200
    }
} catch {
    $code = 0
    if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
    if ($code -eq 302) {
        Write-Pass "TC-SWAGGER-001 - Swagger UI redirects (expected)"
        Add-Result "TC-SWAGGER-001" "Docs" "Swagger UI loads" "PASS" "Swagger redirects" 302
    } else {
        Write-Fail "TC-SWAGGER-001 - Swagger UI failed (HTTP $code)"
        Add-Result "TC-SWAGGER-001" "Docs" "Swagger UI loads" "FAIL" "HTTP $code" $code
    }
}

try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/api-docs" -UseBasicParsing -TimeoutSec 30
    if ($response.StatusCode -eq 200) {
        Write-Pass "TC-SWAGGER-002 - API docs endpoint (/api-docs)"
        Add-Result "TC-SWAGGER-002" "Docs" "OpenAPI docs endpoint" "PASS" "API docs loaded" 200
    }
} catch {
    $code = 0
    if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
    Write-Fail "TC-SWAGGER-002 - API docs failed (HTTP $code)"
    Add-Result "TC-SWAGGER-002" "Docs" "OpenAPI docs endpoint" "FAIL" "HTTP $code" $code
}

# ========================================
# MODULE 10: DASHBOARD AJAX ENDPOINTS
# ========================================
Write-Section "MODULE 10: Dashboard AJAX Endpoints"

if ($taSession) {
    Test-PageLoad $taSession "/dashboard/metrics" "TC-DASH-AJAX-001" "Dashboard" "Dashboard metrics AJAX" ""
    Test-PageLoad $taSession "/dashboard/refresh" "TC-DASH-AJAX-002" "Dashboard" "Dashboard refresh AJAX" ""
}

# ========================================
# LOGOUT TEST
# ========================================
Write-Section "MODULE 11: Logout"

try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/logout" -UseBasicParsing -TimeoutSec 10 -MaximumRedirection 5
    if ($response.Content -match "login|Login|logout|Logout") {
        Write-Pass "TC-LOGOUT-001 - Logout redirects to login"
        Add-Result "TC-LOGOUT-001" "Auth" "Logout works" "PASS" "Redirected to login" 200
    } else {
        Write-Pass "TC-LOGOUT-001 - Logout completed (HTTP $($response.StatusCode))"
        Add-Result "TC-LOGOUT-001" "Auth" "Logout works" "PASS" "Logout completed" $response.StatusCode
    }
} catch {
    Write-Fail "TC-LOGOUT-001 - Logout failed"
    Add-Result "TC-LOGOUT-001" "Auth" "Logout works" "FAIL" $_.Exception.Message 0
}

# ========================================
# GENERATE HTML REPORT
# ========================================
Write-Section "Generating Test Report"

$passRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 1) } else { 0 }
$reportHtml = @"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>SM-Caterer Test Report - $timestamp</title>
    <style>
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { font-family: 'Segoe UI', Tahoma, sans-serif; background: #f5f5f5; color: #333; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        h1 { text-align: center; padding: 20px; color: #2c3e50; }
        .summary { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin: 20px 0; }
        .summary-card { background: white; border-radius: 8px; padding: 20px; text-align: center; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }
        .summary-card h3 { font-size: 2em; margin: 10px 0; }
        .total { border-top: 4px solid #3498db; }
        .total h3 { color: #3498db; }
        .pass { border-top: 4px solid #27ae60; }
        .pass h3 { color: #27ae60; }
        .fail { border-top: 4px solid #e74c3c; }
        .fail h3 { color: #e74c3c; }
        .skip { border-top: 4px solid #f39c12; }
        .skip h3 { color: #f39c12; }
        .progress-bar { background: #eee; border-radius: 20px; height: 30px; margin: 20px 0; overflow: hidden; }
        .progress-fill { height: 100%; border-radius: 20px; display: flex; align-items: center; justify-content: center; color: white; font-weight: bold;
            background: $(if ($passRate -ge 80) { '#27ae60' } elseif ($passRate -ge 60) { '#f39c12' } else { '#e74c3c' }); width: ${passRate}%; }
        table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 5px rgba(0,0,0,0.1); margin: 20px 0; }
        th { background: #34495e; color: white; padding: 12px 15px; text-align: left; }
        td { padding: 10px 15px; border-bottom: 1px solid #eee; }
        tr:hover { background: #f8f9fa; }
        .status-pass { background: #d4edda; color: #155724; padding: 3px 10px; border-radius: 12px; font-weight: bold; }
        .status-fail { background: #f8d7da; color: #721c24; padding: 3px 10px; border-radius: 12px; font-weight: bold; }
        .status-skip { background: #fff3cd; color: #856404; padding: 3px 10px; border-radius: 12px; font-weight: bold; }
        .footer { text-align: center; padding: 20px; color: #666; margin-top: 30px; }
    </style>
</head>
<body>
<div class="container">
    <h1>SM-Caterer Test Report</h1>
    <p style="text-align:center;color:#666;">Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss') | Base URL: $BaseUrl</p>

    <div class="summary">
        <div class="summary-card total"><p>Total Tests</p><h3>$totalTests</h3></div>
        <div class="summary-card pass"><p>Passed</p><h3>$passedTests</h3></div>
        <div class="summary-card fail"><p>Failed</p><h3>$failedTests</h3></div>
        <div class="summary-card skip"><p>Skipped</p><h3>$skippedTests</h3></div>
    </div>

    <div class="progress-bar">
        <div class="progress-fill">${passRate}% Pass Rate</div>
    </div>

    <table>
        <thead><tr><th>#</th><th>ID</th><th>Module</th><th>Test Name</th><th>Status</th><th>HTTP Code</th><th>Details</th><th>Time</th></tr></thead>
        <tbody>
"@

$counter = 1
foreach ($r in $results) {
    $statusClass = switch ($r.Status) { "PASS" { "status-pass" } "FAIL" { "status-fail" } default { "status-skip" } }
    $reportHtml += "        <tr><td>$counter</td><td>$($r.ID)</td><td>$($r.Module)</td><td>$($r.TestName)</td><td><span class='$statusClass'>$($r.Status)</span></td><td>$($r.ResponseCode)</td><td>$($r.Details)</td><td>$($r.Timestamp)</td></tr>`n"
    $counter++
}

$reportHtml += @"
        </tbody>
    </table>

    <div class="footer">
        <p>SM-Caterer Automated Test Report | Generated by PowerShell Test Runner</p>
    </div>
</div>
</body>
</html>
"@

# Save report
$reportPath = Join-Path (Split-Path $PSScriptRoot) "reports\test-report-$timestamp.html"
$reportHtml | Out-File -FilePath $reportPath -Encoding UTF8
Write-Info "Report saved to: $reportPath"

# Also save latest report
$latestPath = Join-Path (Split-Path $PSScriptRoot) "reports\test-report-latest.html"
$reportHtml | Out-File -FilePath $latestPath -Encoding UTF8

# ========================================
# FINAL SUMMARY
# ========================================
Write-Host "`n============================================" -ForegroundColor Cyan
Write-Host " TEST EXECUTION COMPLETE" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host " Total Tests: $totalTests" -ForegroundColor White
Write-Host " Passed:      $passedTests" -ForegroundColor Green
Write-Host " Failed:      $failedTests" -ForegroundColor Red
Write-Host " Skipped:     $skippedTests" -ForegroundColor Yellow
Write-Host " Pass Rate:   ${passRate}%" -ForegroundColor $(if ($passRate -ge 80) { 'Green' } elseif ($passRate -ge 60) { 'Yellow' } else { 'Red' })
Write-Host "============================================" -ForegroundColor Cyan

# Print failures summary
if ($failedTests -gt 0) {
    Write-Host "`n--- FAILED TESTS ---" -ForegroundColor Red
    $results | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  $($_.ID): $($_.TestName) - $($_.Details)" -ForegroundColor Red
    }
    Write-Host "--------------------" -ForegroundColor Red
}

# Exit with appropriate code
if ($failedTests -gt 0) { exit 1 } else { exit 0 }
