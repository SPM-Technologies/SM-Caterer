# SM-Caterer Testing Suite

## Overview
This folder contains comprehensive automated tests for the SM-Caterer multi-tenant catering management system.

## Directory Structure
```
testing/
  |-- README.md                     # This file
  |-- TEST_CASES.md                 # Detailed test cases documentation
  |-- run-all-tests.ps1             # Master test runner (PS + Node)
  |-- api-tests/
  |   |-- run-api-tests.ps1         # PowerShell API + page load tests (55+ tests)
  |-- frontend-tests/
  |   |-- package.json              # Node.js dependencies
  |   |-- test-runner.js            # Playwright E2E tests with screenshots (45+ tests)
  |-- reports/
  |   |-- test-report-latest.html   # Latest API test report
  |   |-- frontend-report-latest.html # Latest frontend test report
  |   |-- screenshots/              # Screenshot proof from E2E tests
```

## Prerequisites

- **Application running** at http://localhost:8080
- **PowerShell** 5.1+ (for API tests)
- **Node.js** 18+ (for frontend tests)
- **npm** (for installing Playwright)

## Quick Start

### Run All Tests
```powershell
cd D:\Projects\AI\Caterer\SM-Caterer\testing
powershell -ExecutionPolicy Bypass -File run-all-tests.ps1
```

### Run Only API Tests
```powershell
cd D:\Projects\AI\Caterer\SM-Caterer\testing
powershell -ExecutionPolicy Bypass -File run-all-tests.ps1 -ApiOnly
```

### Run Only Frontend E2E Tests (with screenshots)
```powershell
cd D:\Projects\AI\Caterer\SM-Caterer\testing\frontend-tests
npm install
npx playwright install chromium
node test-runner.js
```

## Test Coverage (100+ tests total)

### API Tests (~55 tests)
- Public pages (login, health, static resources)
- API Authentication (JWT login, token refresh, current user)
- Super Admin web pages (admin dashboard, tenants, users)
- Tenant Admin web pages (all modules)
- API endpoints (CRUD for all entities)
- Security / RBAC (role-based access control)
- Error pages
- Swagger / API docs

### Frontend E2E Tests (~45 tests with screenshots)
- Login/logout flows with proof screenshots
- Dashboard rendering
- All master data pages (Units, Materials, Menus, Event Types, Recipes, UPI QR)
- Customer management pages
- Order management (list, wizard steps)
- Payment pages
- Reports pages
- Settings pages
- Security / RBAC from browser
- Form validation
- Navigation / sidebar links

## Test Credentials

| Role | Username | Password |
|------|----------|----------|
| Super Admin | SM_2026_SADMIN | test123 |
| Tenant Admin | testuser | test123 |

## Reports & Screenshots

After running tests, HTML reports are generated in `reports/`:
- **test-report-latest.html** - API test results with pass/fail/skip counts
- **frontend-report-latest.html** - Frontend E2E results with screenshot links
- **screenshots/** - Full-page screenshots of every tested page (organized by module)

## CI/CD Integration

The test runner returns exit code:
- `0` - All tests passed
- `1` - Some tests failed
