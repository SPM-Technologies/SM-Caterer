#!/bin/bash
# ============================================
# SM-Caterer Quick Test
# Minimal test to verify all pages work
# ============================================

BASE_URL="${1:-http://localhost:8080}"
COOKIE_FILE="/tmp/smcaterer_cookies"

echo "SM-Caterer Quick Test"
echo "====================="
echo "Base URL: $BASE_URL"
echo ""

# Check application
echo -n "Checking application... "
HTTP=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/login" 2>/dev/null)
if [ "$HTTP" != "200" ]; then
    echo "FAILED (HTTP: $HTTP)"
    echo "Application not running!"
    exit 1
fi
echo "OK"

# Login
echo -n "Logging in... "
rm -f "$COOKIE_FILE"
CSRF=$(curl -s -c "$COOKIE_FILE" "$BASE_URL/login" | grep -o 'name="_csrf"[^>]*value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/')
curl -s -c "$COOKIE_FILE" -b "$COOKIE_FILE" -L -d "username=testuser&password=test123&_csrf=$CSRF" "$BASE_URL/login" -o /dev/null
DASH=$(curl -s -b "$COOKIE_FILE" -o /dev/null -w "%{http_code}" "$BASE_URL/dashboard")
if [ "$DASH" != "200" ]; then
    echo "FAILED"
    exit 1
fi
echo "OK"

echo ""
echo "Testing pages..."
echo "----------------"

FAILED=0
PASSED=0

# Test pages
PAGES=(
    "/dashboard:Dashboard"
    "/profile:Profile"
    "/masters/units:Units"
    "/masters/materials:Materials"
    "/masters/menus:Menus"
    "/masters/event-types:Event Types"
    "/masters/recipes:Recipes"
    "/masters/upi-qr:UPI QR"
    "/orders:Orders"
    "/customers:Customers"
    "/payments:Payments"
    "/reports:Reports"
    "/reports/pending-balance:Pending Balance"
)

for entry in "${PAGES[@]}"; do
    IFS=':' read -r path name <<< "$entry"
    # Follow redirects and get final status
    HTTP=$(curl -s -L -b "$COOKIE_FILE" -c "$COOKIE_FILE" -o /dev/null -w "%{http_code}" "$BASE_URL$path")
    if [ "$HTTP" == "200" ]; then
        echo "  [OK] $name"
        ((PASSED++))
    else
        echo "  [FAIL] $name (HTTP: $HTTP)"
        ((FAILED++))
    fi
done

echo ""
echo "----------------"
echo "Passed: $PASSED | Failed: $FAILED"

rm -f "$COOKIE_FILE"

if [ $FAILED -gt 0 ]; then
    exit 1
fi
echo ""
echo "All tests passed!"
