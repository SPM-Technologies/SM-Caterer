#!/bin/bash
# ============================================
# SM-Caterer Basic Test Agent
# Tests all pages and reports issues
# ============================================

set -e

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
CONFIG_FILE="$SCRIPT_DIR/config.json"
REPORT_DIR="$SCRIPT_DIR/reports"
COOKIE_FILE="$SCRIPT_DIR/.cookies"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$REPORT_DIR/test_report_$TIMESTAMP.txt"
HTML_REPORT="$REPORT_DIR/test_report_$TIMESTAMP.html"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# Arrays to store results
declare -a FAILED_PAGES
declare -a PASSED_PAGES

# ============================================
# Utility Functions
# ============================================

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

# ============================================
# Setup Functions
# ============================================

setup() {
    log_info "Setting up test environment..."

    # Create reports directory
    mkdir -p "$REPORT_DIR"

    # Clean old cookies
    rm -f "$COOKIE_FILE"

    # Check if jq is available for JSON parsing
    if ! command -v jq &> /dev/null; then
        log_warning "jq not found. Using grep for JSON parsing (limited functionality)"
        USE_JQ=false
    else
        USE_JQ=true
    fi

    # Read configuration
    BASE_URL=$(grep -o '"baseUrl"[[:space:]]*:[[:space:]]*"[^"]*"' "$CONFIG_FILE" | head -1 | sed 's/.*: *"\([^"]*\)".*/\1/')
    USERNAME=$(grep -o '"username"[[:space:]]*:[[:space:]]*"[^"]*"' "$CONFIG_FILE" | head -1 | sed 's/.*: *"\([^"]*\)".*/\1/')
    PASSWORD=$(grep -o '"password"[[:space:]]*:[[:space:]]*"[^"]*"' "$CONFIG_FILE" | head -1 | sed 's/.*: *"\([^"]*\)".*/\1/')

    log_info "Base URL: $BASE_URL"
    log_info "Test User: $USERNAME"
}

# ============================================
# Application Check
# ============================================

check_application() {
    log_info "Checking if application is running..."

    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/login" 2>/dev/null || echo "000")

    if [ "$HTTP_CODE" != "200" ]; then
        log_error "Application is not running at $BASE_URL (HTTP: $HTTP_CODE)"
        echo ""
        log_info "Attempting to start application..."

        cd "$PROJECT_ROOT"

        # Check if already running on port 8080
        if netstat -ano 2>/dev/null | grep -q ":8080.*LISTENING"; then
            log_warning "Port 8080 is in use but not responding. Please check the application."
            return 1
        fi

        # Start the application
        ./mvnw.cmd spring-boot:run -DskipTests > "$REPORT_DIR/app_startup.log" 2>&1 &
        APP_PID=$!

        log_info "Starting application (PID: $APP_PID)..."
        log_info "Waiting for application to be ready (max 120 seconds)..."

        for i in {1..24}; do
            sleep 5
            HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/login" 2>/dev/null || echo "000")
            if [ "$HTTP_CODE" == "200" ]; then
                log_success "Application started successfully!"
                return 0
            fi
            echo -n "."
        done

        log_error "Failed to start application within timeout"
        return 1
    fi

    log_success "Application is running"
    return 0
}

# ============================================
# Login Function
# ============================================

login() {
    log_info "Logging in as $USERNAME..."

    # Get CSRF token
    CSRF_TOKEN=$(curl -s -c "$COOKIE_FILE" "$BASE_URL/login" | grep -o 'name="_csrf"[^>]*value="[^"]*"' | sed 's/.*value="\([^"]*\)".*/\1/')

    if [ -z "$CSRF_TOKEN" ]; then
        log_error "Failed to get CSRF token"
        return 1
    fi

    # Perform login
    LOGIN_RESPONSE=$(curl -s -c "$COOKIE_FILE" -b "$COOKIE_FILE" -L \
        -d "username=$USERNAME&password=$PASSWORD&_csrf=$CSRF_TOKEN" \
        -w "\n%{http_code}" \
        "$BASE_URL/login")

    HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -1)

    # Check if login was successful by trying to access dashboard
    DASHBOARD_CODE=$(curl -s -b "$COOKIE_FILE" -o /dev/null -w "%{http_code}" "$BASE_URL/dashboard")

    if [ "$DASHBOARD_CODE" == "200" ]; then
        log_success "Login successful"
        return 0
    else
        log_error "Login failed (Dashboard returned: $DASHBOARD_CODE)"
        return 1
    fi
}

# ============================================
# Test Single Page
# ============================================

test_page() {
    local PAGE_NAME="$1"
    local PAGE_PATH="$2"
    local EXPECTED_STATUS="$3"
    local REQUIRES_AUTH="$4"
    local CATEGORY="$5"

    ((TOTAL_TESTS++))

    # Build URL
    local URL="$BASE_URL$PAGE_PATH"

    # Make request
    if [ "$REQUIRES_AUTH" == "true" ]; then
        RESPONSE=$(curl -s -b "$COOKIE_FILE" -w "\n%{http_code}" "$URL")
    else
        RESPONSE=$(curl -s -w "\n%{http_code}" "$URL")
    fi

    HTTP_CODE=$(echo "$RESPONSE" | tail -1)
    BODY=$(echo "$RESPONSE" | sed '$d')

    # Check for redirect to login (session expired)
    if [ "$REQUIRES_AUTH" == "true" ] && [ "$HTTP_CODE" == "302" ]; then
        LOCATION=$(curl -s -b "$COOKIE_FILE" -D - "$URL" 2>/dev/null | grep -i "^location:" | sed 's/.*: *//' | tr -d '\r')
        if [[ "$LOCATION" == *"/login"* ]]; then
            log_warning "$PAGE_NAME: Session expired, re-logging in..."
            login
            # Retry the request
            RESPONSE=$(curl -s -b "$COOKIE_FILE" -w "\n%{http_code}" "$URL")
            HTTP_CODE=$(echo "$RESPONSE" | tail -1)
            BODY=$(echo "$RESPONSE" | sed '$d')
        fi
    fi

    # Check result
    if [ "$HTTP_CODE" == "$EXPECTED_STATUS" ]; then
        # Additional check: ensure no error in page content
        if echo "$BODY" | grep -q "Whitelabel Error Page\|Exception\|500 Internal Server Error"; then
            log_error "$PAGE_NAME ($PAGE_PATH): Got $HTTP_CODE but page contains error"
            FAILED_PAGES+=("$PAGE_NAME|$PAGE_PATH|$HTTP_CODE|Contains error content")
            ((FAILED_TESTS++))
        else
            log_success "$PAGE_NAME ($PAGE_PATH): $HTTP_CODE"
            PASSED_PAGES+=("$PAGE_NAME|$PAGE_PATH|$HTTP_CODE")
            ((PASSED_TESTS++))
        fi
    else
        log_error "$PAGE_NAME ($PAGE_PATH): Expected $EXPECTED_STATUS, got $HTTP_CODE"
        FAILED_PAGES+=("$PAGE_NAME|$PAGE_PATH|$HTTP_CODE|Expected $EXPECTED_STATUS")
        ((FAILED_TESTS++))
    fi
}

# ============================================
# Run All Tests
# ============================================

run_tests() {
    log_info "Running page tests..."
    echo ""

    # Parse pages from config and test each one
    # Using grep/sed since jq might not be available on all systems

    # Extract page entries
    grep -o '"name"[[:space:]]*:[[:space:]]*"[^"]*"' "$CONFIG_FILE" | while read -r line; do
        echo "$line"
    done > /tmp/page_names.txt

    grep -o '"path"[[:space:]]*:[[:space:]]*"[^"]*"' "$CONFIG_FILE" | while read -r line; do
        echo "$line"
    done > /tmp/page_paths.txt

    grep -o '"expectedStatus"[[:space:]]*:[[:space:]]*[0-9]*' "$CONFIG_FILE" | while read -r line; do
        echo "$line"
    done > /tmp/page_statuses.txt

    grep -o '"requiresAuth"[[:space:]]*:[[:space:]]*[a-z]*' "$CONFIG_FILE" | while read -r line; do
        echo "$line"
    done > /tmp/page_auths.txt

    grep -o '"category"[[:space:]]*:[[:space:]]*"[^"]*"' "$CONFIG_FILE" | while read -r line; do
        echo "$line"
    done > /tmp/page_categories.txt

    # Process each page
    paste /tmp/page_names.txt /tmp/page_paths.txt /tmp/page_statuses.txt /tmp/page_auths.txt /tmp/page_categories.txt | while IFS=$'\t' read -r name path status auth category; do
        PAGE_NAME=$(echo "$name" | sed 's/.*: *"\([^"]*\)".*/\1/')
        PAGE_PATH=$(echo "$path" | sed 's/.*: *"\([^"]*\)".*/\1/')
        EXPECTED_STATUS=$(echo "$status" | sed 's/.*: *\([0-9]*\).*/\1/')
        REQUIRES_AUTH=$(echo "$auth" | sed 's/.*: *\([a-z]*\).*/\1/')
        CATEGORY=$(echo "$category" | sed 's/.*: *"\([^"]*\)".*/\1/')

        test_page "$PAGE_NAME" "$PAGE_PATH" "$EXPECTED_STATUS" "$REQUIRES_AUTH" "$CATEGORY"
    done

    # Cleanup temp files
    rm -f /tmp/page_names.txt /tmp/page_paths.txt /tmp/page_statuses.txt /tmp/page_auths.txt /tmp/page_categories.txt
}

# ============================================
# Generate Report
# ============================================

generate_report() {
    log_info "Generating test report..."

    # Text Report
    {
        echo "============================================"
        echo "SM-Caterer Basic Test Report"
        echo "============================================"
        echo "Date: $(date)"
        echo "Base URL: $BASE_URL"
        echo ""
        echo "SUMMARY"
        echo "--------------------------------------------"
        echo "Total Tests: $TOTAL_TESTS"
        echo "Passed: $PASSED_TESTS"
        echo "Failed: $FAILED_TESTS"
        echo "Skipped: $SKIPPED_TESTS"
        echo ""

        if [ $FAILED_TESTS -gt 0 ]; then
            echo "FAILED TESTS"
            echo "--------------------------------------------"
            for failure in "${FAILED_PAGES[@]}"; do
                IFS='|' read -r name path code reason <<< "$failure"
                echo "  - $name"
                echo "    Path: $path"
                echo "    Status: $code"
                echo "    Reason: $reason"
                echo ""
            done
        fi

        echo ""
        echo "PASSED TESTS"
        echo "--------------------------------------------"
        for success in "${PASSED_PAGES[@]}"; do
            IFS='|' read -r name path code <<< "$success"
            echo "  - $name ($path): $code"
        done

    } > "$REPORT_FILE"

    # HTML Report
    {
        cat << 'HTMLHEAD'
<!DOCTYPE html>
<html>
<head>
    <title>SM-Caterer Test Report</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { max-width: 1000px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        h1 { color: #333; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
        .summary { display: flex; gap: 20px; margin: 20px 0; }
        .stat { padding: 15px 25px; border-radius: 8px; text-align: center; }
        .stat.total { background: #3498db; color: white; }
        .stat.passed { background: #27ae60; color: white; }
        .stat.failed { background: #e74c3c; color: white; }
        .stat h3 { margin: 0; font-size: 2em; }
        .stat p { margin: 5px 0 0; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #f8f9fa; }
        .status-pass { color: #27ae60; font-weight: bold; }
        .status-fail { color: #e74c3c; font-weight: bold; }
        .timestamp { color: #666; font-size: 0.9em; }
    </style>
</head>
<body>
<div class="container">
HTMLHEAD

        echo "<h1>SM-Caterer Test Report</h1>"
        echo "<p class='timestamp'>Generated: $(date)</p>"
        echo "<p>Base URL: $BASE_URL</p>"

        echo "<div class='summary'>"
        echo "  <div class='stat total'><h3>$TOTAL_TESTS</h3><p>Total Tests</p></div>"
        echo "  <div class='stat passed'><h3>$PASSED_TESTS</h3><p>Passed</p></div>"
        echo "  <div class='stat failed'><h3>$FAILED_TESTS</h3><p>Failed</p></div>"
        echo "</div>"

        if [ $FAILED_TESTS -gt 0 ]; then
            echo "<h2>Failed Tests</h2>"
            echo "<table><tr><th>Page</th><th>Path</th><th>Status</th><th>Reason</th></tr>"
            for failure in "${FAILED_PAGES[@]}"; do
                IFS='|' read -r name path code reason <<< "$failure"
                echo "<tr><td>$name</td><td>$path</td><td class='status-fail'>$code</td><td>$reason</td></tr>"
            done
            echo "</table>"
        fi

        echo "<h2>All Tests</h2>"
        echo "<table><tr><th>Page</th><th>Path</th><th>Status</th></tr>"
        for success in "${PASSED_PAGES[@]}"; do
            IFS='|' read -r name path code <<< "$success"
            echo "<tr><td>$name</td><td>$path</td><td class='status-pass'>$code OK</td></tr>"
        done
        for failure in "${FAILED_PAGES[@]}"; do
            IFS='|' read -r name path code reason <<< "$failure"
            echo "<tr><td>$name</td><td>$path</td><td class='status-fail'>$code FAIL</td></tr>"
        done
        echo "</table>"

        echo "</div></body></html>"

    } > "$HTML_REPORT"

    log_info "Text report: $REPORT_FILE"
    log_info "HTML report: $HTML_REPORT"
}

# ============================================
# Cleanup
# ============================================

cleanup() {
    rm -f "$COOKIE_FILE"
}

# ============================================
# Main
# ============================================

main() {
    echo ""
    echo "============================================"
    echo "  SM-Caterer Basic Test Agent"
    echo "============================================"
    echo ""

    setup

    if ! check_application; then
        log_error "Cannot proceed without running application"
        exit 1
    fi

    if ! login; then
        log_error "Cannot proceed without successful login"
        exit 1
    fi

    echo ""
    run_tests

    echo ""
    echo "============================================"
    echo "  Test Summary"
    echo "============================================"
    echo ""
    echo -e "Total: $TOTAL_TESTS | ${GREEN}Passed: $PASSED_TESTS${NC} | ${RED}Failed: $FAILED_TESTS${NC}"
    echo ""

    generate_report

    cleanup

    if [ $FAILED_TESTS -gt 0 ]; then
        log_error "Some tests failed. Check the report for details."
        exit 1
    else
        log_success "All tests passed!"
        exit 0
    fi
}

# Run main function
main "$@"
