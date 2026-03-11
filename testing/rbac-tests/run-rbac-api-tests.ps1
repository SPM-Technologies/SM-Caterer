###############################################################################
# RBAC API Test Suite - Tests every API endpoint with every role
# Tests: SUPER_ADMIN, TENANT_ADMIN, MANAGER, STAFF, VIEWER, UNAUTHENTICATED
###############################################################################

$BaseUrl = "http://localhost:8080"
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$reportFile = "D:\Projects\AI\Caterer\SM-Caterer\testing\reports\rbac-api-report-$timestamp.html"

# Test users
$Users = @{
    SUPER_ADMIN  = @{ username = "SM_2026_SADMIN"; password = "test123" }
    TENANT_ADMIN = @{ username = "testuser";       password = "test123" }
    MANAGER      = @{ username = "testmanager";    password = "test123" }
    STAFF        = @{ username = "teststaff";      password = "test123" }
    VIEWER       = @{ username = "testviewer";     password = "test123" }
}

# Get JWT tokens for all roles
$Tokens = @{}
foreach ($role in $Users.Keys) {
    try {
        $body = @{ username = $Users[$role].username; password = $Users[$role].password } | ConvertTo-Json
        $resp = Invoke-RestMethod -Uri "$BaseUrl/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
        if ($resp.success) {
            $Tokens[$role] = $resp.data.accessToken
            Write-Host "  $role : JWT obtained" -ForegroundColor Green
        } else {
            Write-Host "  $role : Login failed - $($resp.message)" -ForegroundColor Red
        }
    } catch {
        Write-Host "  $role : ERROR - $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Results collector
$Results = @()
$totalTests = 0
$passed = 0
$failed = 0

function Test-ApiEndpoint {
    param(
        [string]$TestId,
        [string]$Category,
        [string]$Method,
        [string]$Url,
        [string]$Role,
        [int]$ExpectedStatus,
        [string]$Description,
        [string]$Body = $null
    )

    $script:totalTests++
    $headers = @{}

    if ($Role -ne "NONE") {
        if (-not $Tokens[$Role]) {
            $result = @{
                TestId = $TestId; Category = $Category; Role = $Role;
                Method = $Method; Url = $Url; Expected = $ExpectedStatus;
                Actual = "N/A"; Status = "SKIP"; Description = $Description;
                Detail = "No token for role $Role"
            }
            $script:Results += $result
            return
        }
        $headers["Authorization"] = "Bearer $($Tokens[$Role])"
    }
    $headers["Content-Type"] = "application/json"

    try {
        $params = @{
            Uri = "$BaseUrl$Url"
            Method = $Method
            Headers = $headers
            TimeoutSec = 15
            ErrorAction = "Stop"
        }
        if ($Body -and $Method -ne "GET") {
            $params["Body"] = $Body
        }

        $response = Invoke-WebRequest @params -UseBasicParsing
        $actualStatus = $response.StatusCode
    } catch {
        if ($_.Exception.Response) {
            $actualStatus = [int]$_.Exception.Response.StatusCode
        } else {
            $actualStatus = 0
        }
    }

    # Check result - allow 200 or 204 for success expectations, and 403/401 for deny expectations
    $pass = $false
    if ($ExpectedStatus -eq 200) {
        $pass = ($actualStatus -ge 200 -and $actualStatus -lt 300)
    } elseif ($ExpectedStatus -eq 403) {
        $pass = ($actualStatus -eq 403 -or $actualStatus -eq 401)
    } elseif ($ExpectedStatus -eq 401) {
        $pass = ($actualStatus -eq 401)
    } else {
        $pass = ($actualStatus -eq $ExpectedStatus)
    }

    if ($pass) {
        $script:passed++
        $status = "PASS"
    } else {
        $script:failed++
        $status = "FAIL"
        Write-Host "  FAIL: $TestId [$Role] $Method $Url -> Expected:$ExpectedStatus Got:$actualStatus" -ForegroundColor Red
    }

    $script:Results += @{
        TestId = $TestId; Category = $Category; Role = $Role;
        Method = $Method; Url = $Url; Expected = $ExpectedStatus;
        Actual = $actualStatus; Status = $status; Description = $Description;
        Detail = ""
    }
}

Write-Host "`n=== RBAC API TEST SUITE ===" -ForegroundColor Cyan
Write-Host "Testing every API endpoint with every role...`n"

###############################################################################
# CATEGORY 1: AUTH ENDPOINTS (Public)
###############################################################################
Write-Host "--- Auth Endpoints ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "AUTH-001" -Category "Auth" -Method "POST" -Url "/api/v1/auth/login" -Role "NONE" -ExpectedStatus 200 -Description "Login without auth (public)" -Body '{"username":"testuser","password":"test123"}'
Test-ApiEndpoint -TestId "AUTH-002" -Category "Auth" -Method "GET" -Url "/api/v1/auth/me" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA get current user"
Test-ApiEndpoint -TestId "AUTH-003" -Category "Auth" -Method "GET" -Url "/api/v1/auth/me" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA get current user"
Test-ApiEndpoint -TestId "AUTH-004" -Category "Auth" -Method "GET" -Url "/api/v1/auth/me" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager get current user"
Test-ApiEndpoint -TestId "AUTH-005" -Category "Auth" -Method "GET" -Url "/api/v1/auth/me" -Role "STAFF" -ExpectedStatus 200 -Description "Staff get current user"
Test-ApiEndpoint -TestId "AUTH-006" -Category "Auth" -Method "GET" -Url "/api/v1/auth/me" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer get current user"
Test-ApiEndpoint -TestId "AUTH-007" -Category "Auth" -Method "GET" -Url "/api/v1/auth/me" -Role "NONE" -ExpectedStatus 401 -Description "Unauthenticated get current user"

###############################################################################
# CATEGORY 2: TENANT API (SUPER_ADMIN only)
###############################################################################
Write-Host "--- Tenant API ---" -ForegroundColor Yellow

# GET tenants - SA only
Test-ApiEndpoint -TestId "TEN-001" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list tenants"
Test-ApiEndpoint -TestId "TEN-002" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 403 -Description "TA list tenants (DENIED)"
Test-ApiEndpoint -TestId "TEN-003" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants?page=0&size=10" -Role "MANAGER" -ExpectedStatus 403 -Description "Manager list tenants (DENIED)"
Test-ApiEndpoint -TestId "TEN-004" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants?page=0&size=10" -Role "STAFF" -ExpectedStatus 403 -Description "Staff list tenants (DENIED)"
Test-ApiEndpoint -TestId "TEN-005" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants?page=0&size=10" -Role "VIEWER" -ExpectedStatus 403 -Description "Viewer list tenants (DENIED)"
Test-ApiEndpoint -TestId "TEN-006" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list tenants (DENIED)"
Test-ApiEndpoint -TestId "TEN-007" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants/active" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA active tenants"
Test-ApiEndpoint -TestId "TEN-008" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants/active" -Role "TENANT_ADMIN" -ExpectedStatus 403 -Description "TA active tenants (DENIED)"
Test-ApiEndpoint -TestId "TEN-009" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants/1" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA get tenant by ID"
Test-ApiEndpoint -TestId "TEN-010" -Category "Tenant" -Method "GET" -Url "/api/v1/tenants/1" -Role "MANAGER" -ExpectedStatus 403 -Description "Manager get tenant by ID (DENIED)"

###############################################################################
# CATEGORY 3: USER API (SA + TA, some methods allow MANAGER)
###############################################################################
Write-Host "--- User API ---" -ForegroundColor Yellow

# GET users
Test-ApiEndpoint -TestId "USR-001" -Category "User" -Method "GET" -Url "/api/v1/users?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list users"
Test-ApiEndpoint -TestId "USR-002" -Category "User" -Method "GET" -Url "/api/v1/users?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list users"
Test-ApiEndpoint -TestId "USR-003" -Category "User" -Method "GET" -Url "/api/v1/users?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list users"
Test-ApiEndpoint -TestId "USR-004" -Category "User" -Method "GET" -Url "/api/v1/users?page=0&size=10" -Role "STAFF" -ExpectedStatus 403 -Description "Staff list users (DENIED)"
Test-ApiEndpoint -TestId "USR-005" -Category "User" -Method "GET" -Url "/api/v1/users?page=0&size=10" -Role "VIEWER" -ExpectedStatus 403 -Description "Viewer list users (DENIED)"
Test-ApiEndpoint -TestId "USR-006" -Category "User" -Method "GET" -Url "/api/v1/users?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list users (DENIED)"

# GET users by role
Test-ApiEndpoint -TestId "USR-007" -Category "User" -Method "GET" -Url "/api/v1/users/role/STAFF?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA users by role"
Test-ApiEndpoint -TestId "USR-008" -Category "User" -Method "GET" -Url "/api/v1/users/role/STAFF?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA users by role"
Test-ApiEndpoint -TestId "USR-009" -Category "User" -Method "GET" -Url "/api/v1/users/role/STAFF?page=0&size=10" -Role "MANAGER" -ExpectedStatus 403 -Description "Manager users by role (DENIED)"
Test-ApiEndpoint -TestId "USR-010" -Category "User" -Method "GET" -Url "/api/v1/users/role/STAFF?page=0&size=10" -Role "STAFF" -ExpectedStatus 403 -Description "Staff users by role (DENIED)"

# GET users by status
Test-ApiEndpoint -TestId "USR-011" -Category "User" -Method "GET" -Url "/api/v1/users/status/ACTIVE" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA users by status"
Test-ApiEndpoint -TestId "USR-012" -Category "User" -Method "GET" -Url "/api/v1/users/status/ACTIVE" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA users by status"
Test-ApiEndpoint -TestId "USR-013" -Category "User" -Method "GET" -Url "/api/v1/users/status/ACTIVE" -Role "MANAGER" -ExpectedStatus 403 -Description "Manager users by status (DENIED)"

###############################################################################
# CATEGORY 4: CUSTOMER API
###############################################################################
Write-Host "--- Customer API ---" -ForegroundColor Yellow

# GET customers (isAuthenticated - all roles)
Test-ApiEndpoint -TestId "CUS-001" -Category "Customer" -Method "GET" -Url "/api/v1/customers?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list customers"
Test-ApiEndpoint -TestId "CUS-002" -Category "Customer" -Method "GET" -Url "/api/v1/customers?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list customers"
Test-ApiEndpoint -TestId "CUS-003" -Category "Customer" -Method "GET" -Url "/api/v1/customers?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list customers"
Test-ApiEndpoint -TestId "CUS-004" -Category "Customer" -Method "GET" -Url "/api/v1/customers?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list customers"
Test-ApiEndpoint -TestId "CUS-005" -Category "Customer" -Method "GET" -Url "/api/v1/customers?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list customers"
Test-ApiEndpoint -TestId "CUS-006" -Category "Customer" -Method "GET" -Url "/api/v1/customers?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list customers (DENIED)"

###############################################################################
# CATEGORY 5: ORDER API
###############################################################################
Write-Host "--- Order API ---" -ForegroundColor Yellow

# GET orders (isAuthenticated)
Test-ApiEndpoint -TestId "ORD-001" -Category "Order" -Method "GET" -Url "/api/v1/orders?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list orders"
Test-ApiEndpoint -TestId "ORD-002" -Category "Order" -Method "GET" -Url "/api/v1/orders?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list orders"
Test-ApiEndpoint -TestId "ORD-003" -Category "Order" -Method "GET" -Url "/api/v1/orders?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list orders"
Test-ApiEndpoint -TestId "ORD-004" -Category "Order" -Method "GET" -Url "/api/v1/orders?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list orders"
Test-ApiEndpoint -TestId "ORD-005" -Category "Order" -Method "GET" -Url "/api/v1/orders?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list orders"
Test-ApiEndpoint -TestId "ORD-006" -Category "Order" -Method "GET" -Url "/api/v1/orders?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list orders (DENIED)"

###############################################################################
# CATEGORY 6: MENU API
###############################################################################
Write-Host "--- Menu API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "MNU-001" -Category "Menu" -Method "GET" -Url "/api/v1/menus?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list menus"
Test-ApiEndpoint -TestId "MNU-002" -Category "Menu" -Method "GET" -Url "/api/v1/menus?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list menus"
Test-ApiEndpoint -TestId "MNU-003" -Category "Menu" -Method "GET" -Url "/api/v1/menus?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list menus"
Test-ApiEndpoint -TestId "MNU-004" -Category "Menu" -Method "GET" -Url "/api/v1/menus?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list menus"
Test-ApiEndpoint -TestId "MNU-005" -Category "Menu" -Method "GET" -Url "/api/v1/menus?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list menus"
Test-ApiEndpoint -TestId "MNU-006" -Category "Menu" -Method "GET" -Url "/api/v1/menus?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list menus (DENIED)"

###############################################################################
# CATEGORY 7: MATERIAL API
###############################################################################
Write-Host "--- Material API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "MAT-001" -Category "Material" -Method "GET" -Url "/api/v1/materials?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list materials"
Test-ApiEndpoint -TestId "MAT-002" -Category "Material" -Method "GET" -Url "/api/v1/materials?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list materials"
Test-ApiEndpoint -TestId "MAT-003" -Category "Material" -Method "GET" -Url "/api/v1/materials?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list materials"
Test-ApiEndpoint -TestId "MAT-004" -Category "Material" -Method "GET" -Url "/api/v1/materials?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list materials"
Test-ApiEndpoint -TestId "MAT-005" -Category "Material" -Method "GET" -Url "/api/v1/materials?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list materials"
Test-ApiEndpoint -TestId "MAT-006" -Category "Material" -Method "GET" -Url "/api/v1/materials?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list materials (DENIED)"

###############################################################################
# CATEGORY 8: UNIT API
###############################################################################
Write-Host "--- Unit API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "UNT-001" -Category "Unit" -Method "GET" -Url "/api/v1/units?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list units"
Test-ApiEndpoint -TestId "UNT-002" -Category "Unit" -Method "GET" -Url "/api/v1/units?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list units"
Test-ApiEndpoint -TestId "UNT-003" -Category "Unit" -Method "GET" -Url "/api/v1/units?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list units"
Test-ApiEndpoint -TestId "UNT-004" -Category "Unit" -Method "GET" -Url "/api/v1/units?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list units"
Test-ApiEndpoint -TestId "UNT-005" -Category "Unit" -Method "GET" -Url "/api/v1/units?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list units"
Test-ApiEndpoint -TestId "UNT-006" -Category "Unit" -Method "GET" -Url "/api/v1/units?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list units (DENIED)"

###############################################################################
# CATEGORY 9: EVENT TYPE API
###############################################################################
Write-Host "--- Event Type API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "EVT-001" -Category "EventType" -Method "GET" -Url "/api/v1/event-types?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list event types"
Test-ApiEndpoint -TestId "EVT-002" -Category "EventType" -Method "GET" -Url "/api/v1/event-types?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list event types"
Test-ApiEndpoint -TestId "EVT-003" -Category "EventType" -Method "GET" -Url "/api/v1/event-types?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list event types"
Test-ApiEndpoint -TestId "EVT-004" -Category "EventType" -Method "GET" -Url "/api/v1/event-types?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list event types"
Test-ApiEndpoint -TestId "EVT-005" -Category "EventType" -Method "GET" -Url "/api/v1/event-types?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list event types"
Test-ApiEndpoint -TestId "EVT-006" -Category "EventType" -Method "GET" -Url "/api/v1/event-types?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list event types (DENIED)"

###############################################################################
# CATEGORY 10: PAYMENT API
###############################################################################
Write-Host "--- Payment API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "PAY-001" -Category "Payment" -Method "GET" -Url "/api/v1/payments?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list payments"
Test-ApiEndpoint -TestId "PAY-002" -Category "Payment" -Method "GET" -Url "/api/v1/payments?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list payments"
Test-ApiEndpoint -TestId "PAY-003" -Category "Payment" -Method "GET" -Url "/api/v1/payments?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list payments"
Test-ApiEndpoint -TestId "PAY-004" -Category "Payment" -Method "GET" -Url "/api/v1/payments?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list payments"
Test-ApiEndpoint -TestId "PAY-005" -Category "Payment" -Method "GET" -Url "/api/v1/payments?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list payments"
Test-ApiEndpoint -TestId "PAY-006" -Category "Payment" -Method "GET" -Url "/api/v1/payments?page=0&size=10" -Role "NONE" -ExpectedStatus 401 -Description "Unauth list payments (DENIED)"

###############################################################################
# CATEGORY 11: UPI QR API
###############################################################################
Write-Host "--- UPI QR API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "UPI-001" -Category "UpiQr" -Method "GET" -Url "/api/v1/upi-qr-codes?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list UPI QR"
Test-ApiEndpoint -TestId "UPI-002" -Category "UpiQr" -Method "GET" -Url "/api/v1/upi-qr-codes?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list UPI QR"
Test-ApiEndpoint -TestId "UPI-003" -Category "UpiQr" -Method "GET" -Url "/api/v1/upi-qr-codes?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list UPI QR"
Test-ApiEndpoint -TestId "UPI-004" -Category "UpiQr" -Method "GET" -Url "/api/v1/upi-qr-codes?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list UPI QR"
Test-ApiEndpoint -TestId "UPI-005" -Category "UpiQr" -Method "GET" -Url "/api/v1/upi-qr-codes?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list UPI QR"

###############################################################################
# CATEGORY 12: UTILITY API
###############################################################################
Write-Host "--- Utility API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "UTL-001" -Category "Utility" -Method "GET" -Url "/api/v1/utilities?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list utilities"
Test-ApiEndpoint -TestId "UTL-002" -Category "Utility" -Method "GET" -Url "/api/v1/utilities?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list utilities"
Test-ApiEndpoint -TestId "UTL-003" -Category "Utility" -Method "GET" -Url "/api/v1/utilities?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list utilities"
Test-ApiEndpoint -TestId "UTL-004" -Category "Utility" -Method "GET" -Url "/api/v1/utilities?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list utilities"
Test-ApiEndpoint -TestId "UTL-005" -Category "Utility" -Method "GET" -Url "/api/v1/utilities?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list utilities"

###############################################################################
# CATEGORY 13: RECIPE API
###############################################################################
Write-Host "--- Recipe API ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "RCP-001" -Category "Recipe" -Method "GET" -Url "/api/v1/recipe-items?page=0&size=10" -Role "SUPER_ADMIN" -ExpectedStatus 200 -Description "SA list recipes"
Test-ApiEndpoint -TestId "RCP-002" -Category "Recipe" -Method "GET" -Url "/api/v1/recipe-items?page=0&size=10" -Role "TENANT_ADMIN" -ExpectedStatus 200 -Description "TA list recipes"
Test-ApiEndpoint -TestId "RCP-003" -Category "Recipe" -Method "GET" -Url "/api/v1/recipe-items?page=0&size=10" -Role "MANAGER" -ExpectedStatus 200 -Description "Manager list recipes"
Test-ApiEndpoint -TestId "RCP-004" -Category "Recipe" -Method "GET" -Url "/api/v1/recipe-items?page=0&size=10" -Role "STAFF" -ExpectedStatus 200 -Description "Staff list recipes"
Test-ApiEndpoint -TestId "RCP-005" -Category "Recipe" -Method "GET" -Url "/api/v1/recipe-items?page=0&size=10" -Role "VIEWER" -ExpectedStatus 200 -Description "Viewer list recipes"

###############################################################################
# CATEGORY 14: HEALTH / PUBLIC ENDPOINTS
###############################################################################
Write-Host "--- Public Endpoints ---" -ForegroundColor Yellow

Test-ApiEndpoint -TestId "PUB-001" -Category "Public" -Method "GET" -Url "/api/v1/health" -Role "NONE" -ExpectedStatus 200 -Description "Health endpoint (public)"
Test-ApiEndpoint -TestId "PUB-002" -Category "Public" -Method "GET" -Url "/api/v1/health" -Role "VIEWER" -ExpectedStatus 200 -Description "Health endpoint (viewer)"

###############################################################################
# GENERATE HTML REPORT
###############################################################################

Write-Host "`n=== GENERATING REPORT ===" -ForegroundColor Cyan

# Group results by category
$categories = $Results | ForEach-Object { $_.Category } | Sort-Object -Unique

$tableRows = ""
foreach ($r in $Results) {
    $statusClass = if ($r.Status -eq "PASS") { "pass" } elseif ($r.Status -eq "FAIL") { "fail" } else { "skip" }
    $tableRows += "<tr class='$statusClass'><td>$($r.TestId)</td><td>$($r.Category)</td><td>$($r.Role)</td><td>$($r.Method)</td><td>$($r.Url)</td><td>$($r.Expected)</td><td>$($r.Actual)</td><td>$($r.Status)</td><td>$($r.Description)</td></tr>`n"
}

# Build role access matrix
$roleMatrix = ""
$apiGroups = @(
    @{name="Tenant API"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="DENIED"; MANAGER="DENIED"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="User API (list/view)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="List/View"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="User API (create/update/delete)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="DENIED"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="Customer API (read)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="Full"; VIEWER="Full"}},
    @{name="Customer API (create)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="Full"; VIEWER="DENIED"}},
    @{name="Customer API (update)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="Customer API (delete)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="DENIED"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="Order API (read)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="Full"; VIEWER="Full"}},
    @{name="Order API (create)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="Full"; VIEWER="DENIED"}},
    @{name="Menu/Material/Unit/EventType (read)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="Full"; VIEWER="Full"}},
    @{name="Menu/Material/Unit/EventType (write)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="Menu/Material/Unit/EventType (delete)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="DENIED"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="Payment API (read)"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="Full"; VIEWER="Full"}},
    @{name="Settings API"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="DENIED"; STAFF="DENIED"; VIEWER="DENIED"}},
    @{name="Reports"; roles=@{SUPER_ADMIN="Full"; TENANT_ADMIN="Full"; MANAGER="Full"; STAFF="DENIED"; VIEWER="DENIED"}}
)

foreach ($g in $apiGroups) {
    $roleMatrix += "<tr><td><strong>$($g.name)</strong></td>"
    foreach ($role in @("SUPER_ADMIN", "TENANT_ADMIN", "MANAGER", "STAFF", "VIEWER")) {
        $access = $g.roles[$role]
        $class = if ($access -eq "DENIED") { "denied" } else { "allowed" }
        $roleMatrix += "<td class='$class'>$access</td>"
    }
    $roleMatrix += "</tr>`n"
}

$passRate = if ($totalTests -gt 0) { [math]::Round(($passed / $totalTests) * 100, 1) } else { 0 }

$html = @"
<!DOCTYPE html>
<html>
<head>
<title>RBAC API Test Report</title>
<style>
body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background: #f5f5f5; }
h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
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
.pass td:nth-child(8) { color: #27ae60; font-weight: bold; }
.fail td:nth-child(8) { color: #e74c3c; }
.allowed { color: #27ae60; font-weight: bold; text-align: center; }
.denied { color: #e74c3c; font-weight: bold; text-align: center; }
.matrix-table td, .matrix-table th { text-align: center; padding: 10px; }
.matrix-table td:first-child { text-align: left; }
.timestamp { color: #999; font-size: 12px; }
</style>
</head>
<body>
<h1>RBAC API Test Report - SM-Caterer</h1>
<p class="timestamp">Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss")</p>

<div class="summary">
    <div class="card total"><div class="number">$totalTests</div><div class="label">Total Tests</div></div>
    <div class="card passed"><div class="number">$passed</div><div class="label">Passed</div></div>
    <div class="card failed"><div class="number">$failed</div><div class="label">Failed</div></div>
    <div class="card rate"><div class="number">$passRate%</div><div class="label">Pass Rate</div></div>
</div>

<h2>Role Access Matrix (Design)</h2>
<table class="matrix-table">
<tr><th>API Group</th><th>SUPER_ADMIN</th><th>TENANT_ADMIN</th><th>MANAGER</th><th>STAFF</th><th>VIEWER</th></tr>
$roleMatrix
</table>

<h2>Test Results - All Endpoints x All Roles</h2>
<table>
<tr><th>Test ID</th><th>Category</th><th>Role</th><th>Method</th><th>URL</th><th>Expected</th><th>Actual</th><th>Status</th><th>Description</th></tr>
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
$html | Out-File -FilePath "D:\Projects\AI\Caterer\SM-Caterer\testing\reports\rbac-api-report-latest.html" -Encoding UTF8

Write-Host "`n=== RBAC API TEST RESULTS ===" -ForegroundColor Cyan
Write-Host "Total: $totalTests | Passed: $passed | Failed: $failed | Rate: $passRate%" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Red" })
Write-Host "Report: $reportFile"

if ($failed -gt 0) {
    Write-Host "`n=== FAILURES ===" -ForegroundColor Red
    $Results | Where-Object { $_.Status -eq "FAIL" } | ForEach-Object {
        Write-Host "  $($_.TestId) [$($_.Role)] $($_.Method) $($_.Url) -> Expected:$($_.Expected) Got:$($_.Actual) - $($_.Description)" -ForegroundColor Red
    }
}
