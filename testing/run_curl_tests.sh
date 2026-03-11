#!/bin/bash
# SM-Caterer API Test Script using cURL
# ======================================

BASE_URL="http://localhost:8080"
API_URL="$BASE_URL/api/v1"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test result
print_result() {
    local test_name=$1
    local expected=$2
    local actual=$3

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    if [ "$expected" = "$actual" ]; then
        echo -e "  ${GREEN}[PASS]${NC} $test_name (HTTP $actual)"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "  ${RED}[FAIL]${NC} $test_name (Expected: $expected, Got: $actual)"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

echo "========================================"
echo "SM-CATERER API TESTS"
echo "========================================"
echo "Target: $BASE_URL"
echo "Date: $(date)"
echo ""

# Check server
echo "Checking server availability..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/login" 2>/dev/null)
if [ "$HTTP_CODE" != "200" ]; then
    echo -e "${RED}ERROR: Server not responding (HTTP $HTTP_CODE)${NC}"
    exit 1
fi
echo -e "${GREEN}Server is UP${NC}"
echo ""

# ======================================
# AUTHENTICATION TESTS
# ======================================
echo "----------------------------------------"
echo "1. AUTHENTICATION TESTS"
echo "----------------------------------------"

# Test 1.1: Login with valid tenant admin
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"testuser","password":"test123"}')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
print_result "Login - Valid Tenant Admin" "200" "$HTTP_CODE"

# Extract token for subsequent tests
TOKEN=$(echo "$BODY" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# Test 1.2: Login with valid super admin
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"SM_2026_SADMIN","password":"test123"}')
print_result "Login - Valid Super Admin" "200" "$HTTP_CODE"

# Test 1.3: Login with invalid credentials
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"invalid","password":"wrong"}')
print_result "Login - Invalid Credentials" "401" "$HTTP_CODE"

# Test 1.4: Get current user
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/auth/me" \
    -H "Authorization: Bearer $TOKEN")
print_result "Get Current User" "200" "$HTTP_CODE"

# Test 1.5: Access without token
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/auth/me")
print_result "Access Without Token (should be 401/403)" "401" "$HTTP_CODE"

echo ""

# ======================================
# CUSTOMER TESTS
# ======================================
echo "----------------------------------------"
echo "2. CUSTOMER TESTS"
echo "----------------------------------------"

# Test 2.1: List customers
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/customers" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Customers" "200" "$HTTP_CODE"

# Test 2.2: Create customer
RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/customers" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"name":"Test Customer","phone":"9876543210","email":"test@example.com","address":"Test Address","city":"Mumbai","state":"Maharashtra","pincode":"400001"}')
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | sed '$d')
if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "201" ]; then
    print_result "Create Customer" "200/201" "$HTTP_CODE"
    CUSTOMER_ID=$(echo "$BODY" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
else
    print_result "Create Customer" "200/201" "$HTTP_CODE"
fi

# Test 2.3: Get customer by ID (if we have one)
if [ -n "$CUSTOMER_ID" ]; then
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/customers/$CUSTOMER_ID" \
        -H "Authorization: Bearer $TOKEN")
    print_result "Get Customer by ID" "200" "$HTTP_CODE"
fi

# Test 2.4: Get non-existent customer
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/customers/99999" \
    -H "Authorization: Bearer $TOKEN")
print_result "Get Non-existent Customer" "404" "$HTTP_CODE"

echo ""

# ======================================
# MASTER DATA TESTS
# ======================================
echo "----------------------------------------"
echo "3. MASTER DATA TESTS"
echo "----------------------------------------"

# Units
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/units" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Units" "200" "$HTTP_CODE"

# Materials
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/materials" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Materials" "200" "$HTTP_CODE"

# Menus
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/menus" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Menus" "200" "$HTTP_CODE"

# Event Types
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/event-types" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Event Types" "200" "$HTTP_CODE"

echo ""

# ======================================
# ORDER TESTS
# ======================================
echo "----------------------------------------"
echo "4. ORDER TESTS"
echo "----------------------------------------"

# List orders
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/orders" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Orders" "200" "$HTTP_CODE"

# Get non-existent order
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/orders/99999" \
    -H "Authorization: Bearer $TOKEN")
print_result "Get Non-existent Order" "404" "$HTTP_CODE"

echo ""

# ======================================
# PAYMENT TESTS
# ======================================
echo "----------------------------------------"
echo "5. PAYMENT TESTS"
echo "----------------------------------------"

# List payments
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/payments" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Payments" "200" "$HTTP_CODE"

echo ""

# ======================================
# WEB PAGE TESTS
# ======================================
echo "----------------------------------------"
echo "6. WEB PAGE TESTS"
echo "----------------------------------------"

# Login page
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/login")
print_result "Login Page Loads" "200" "$HTTP_CODE"

# Dashboard (should redirect to login without session)
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -L "$BASE_URL/dashboard")
print_result "Dashboard (redirects to login)" "200" "$HTTP_CODE"

# Swagger UI
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/swagger-ui.html")
print_result "Swagger UI" "200" "$HTTP_CODE"

echo ""

# ======================================
# SUPER ADMIN TESTS
# ======================================
echo "----------------------------------------"
echo "7. SUPER ADMIN TESTS"
echo "----------------------------------------"

# Get super admin token
SA_RESPONSE=$(curl -s -X POST "$API_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"SM_2026_SADMIN","password":"test123"}')
SA_TOKEN=$(echo "$SA_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

# List tenants
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/tenants" \
    -H "Authorization: Bearer $SA_TOKEN")
print_result "List Tenants (Super Admin)" "200" "$HTTP_CODE"

# Tenant access denied for regular user
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$API_URL/tenants" \
    -H "Authorization: Bearer $TOKEN")
print_result "List Tenants (Regular User - Denied)" "403" "$HTTP_CODE"

echo ""

# ======================================
# SUMMARY
# ======================================
echo "========================================"
echo "TEST SUMMARY"
echo "========================================"
echo "Total Tests:  $TOTAL_TESTS"
echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed:       ${RED}$FAILED_TESTS${NC}"

if [ $TOTAL_TESTS -gt 0 ]; then
    PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo "Pass Rate:    $PASS_RATE%"
fi

echo ""
echo "Test completed at $(date)"

# Exit with error code if tests failed
if [ $FAILED_TESTS -gt 0 ]; then
    exit 1
fi
exit 0
