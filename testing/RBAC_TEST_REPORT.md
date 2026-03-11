# SM-Caterer RBAC (Role-Based Access Control) Test Report

## Test Summary

| Test Suite | Tests | Passed | Failed | Pass Rate |
|------------|-------|--------|--------|-----------|
| API RBAC Tests | 89 | 89 | 0 | 100% |
| Web UI RBAC Tests | 91 | 91 | 0 | 100% |
| E2E RBAC Tests (Playwright) | 105 | 105 | 0 | 100% |
| **Total** | **285** | **285** | **0** | **100%** |

## Roles Tested

| Role | Username | Password | Description |
|------|----------|----------|-------------|
| SUPER_ADMIN | SM_2026_SADMIN | test123 | System-wide admin (tenants, users) |
| TENANT_ADMIN | testuser | test123 | Full tenant control (all modules) |
| MANAGER | testmanager | test123 | Operational (orders, customers, payments, masters, reports) |
| STAFF | teststaff | test123 | Basic operations (orders, customers, payments) |
| VIEWER | testviewer | test123 | Read-only (dashboard only) |

## RBAC Access Matrix (Verified)

### Web UI Pages

| Page | SUPER_ADMIN | TENANT_ADMIN | MANAGER | STAFF | VIEWER |
|------|:-----------:|:------------:|:-------:|:-----:|:------:|
| Dashboard | YES | YES | YES | YES | YES |
| Admin Dashboard | YES | NO | NO | NO | NO |
| Admin Tenants | YES | NO | NO | NO | NO |
| Admin Users | YES | NO | NO | NO | NO |
| Master Data (all) | YES | YES | YES | NO | NO |
| Orders | NO* | YES | YES | YES | NO |
| Customers | NO* | YES | YES | YES | NO |
| Payments | NO* | YES | YES | YES | NO |
| Reports (all) | NO* | YES | YES | NO | NO |
| Settings (all) | NO | YES | NO | NO | NO |
| Profile | YES | YES | YES | YES | YES |

*SUPER_ADMIN is excluded from tenant-scoped pages (orders, customers, payments, reports) because these require tenantId context.

### API Endpoints

| Endpoint | SA | TA | MGR | STAFF | VIEWER | Unauth |
|----------|:--:|:--:|:---:|:-----:|:------:|:------:|
| GET /api/v1/tenants | YES | NO | NO | NO | NO | NO |
| GET /api/v1/users (list) | YES | YES | YES | NO | NO | NO |
| GET /api/v1/users/role/* | YES | YES | NO | NO | NO | NO |
| GET /api/v1/customers | YES | YES | YES | YES | YES | NO |
| GET /api/v1/orders | YES | YES | YES | YES | YES | NO |
| GET /api/v1/menus | YES | YES | YES | YES | YES | NO |
| GET /api/v1/materials | YES | YES | YES | YES | YES | NO |
| GET /api/v1/units | YES | YES | YES | YES | YES | NO |
| GET /api/v1/event-types | YES | YES | YES | YES | YES | NO |
| GET /api/v1/payments | YES | YES | YES | YES | YES | NO |
| GET /api/v1/upi-qr-codes | YES | YES | YES | YES | YES | NO |
| GET /api/v1/utilities | YES | YES | YES | YES | YES | NO |
| GET /api/v1/recipe-items | YES | YES | YES | YES | YES | NO |
| POST /api/v1/auth/login | PUBLIC | PUBLIC | PUBLIC | PUBLIC | PUBLIC | PUBLIC |
| GET /api/v1/health | PUBLIC | PUBLIC | PUBLIC | PUBLIC | PUBLIC | PUBLIC |

## Bugs Found During RBAC Testing

### Bug 8: MEDIUM - SecurityConfig Mismatch for MANAGER on User API
- **Symptom**: MANAGER role gets 403 when accessing GET /api/v1/users despite `@PreAuthorize` annotation allowing it
- **Root Cause**: SecurityConfig URL-level security at line 110 had `.requestMatchers("/api/v1/users/**").hasAnyRole("SUPER_ADMIN", "TENANT_ADMIN")` which blocks MANAGER before the method-level `@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'TENANT_ADMIN', 'MANAGER')")` is evaluated
- **Fix**: Added MANAGER to URL-level security: `.hasAnyRole("SUPER_ADMIN", "TENANT_ADMIN", "MANAGER")`
- **File**: `src/main/java/com/smtech/SM_Caterer/config/SecurityConfig.java`

### Note: Settings Page - No Root /settings Route
- **Observation**: GET /settings returns 404 because TenantSettingsController has no handler for the root path
- **Available routes**: `/settings/email`, `/settings/payment`, `/settings/branding`
- **Status**: Not a bug - by design (settings are accessed via sub-routes)

## Files Modified

| File | Change |
|------|--------|
| `src/main/java/.../config/SecurityConfig.java` | Added MANAGER to /api/v1/users/** URL security |
| `src/main/resources/db/migration/V1.1.1__Add_Test_Users_For_RBAC.sql` | New migration - MANAGER, STAFF, VIEWER test users |

## Test Files Created

| File | Description |
|------|-------------|
| `testing/rbac-tests/run-rbac-api-tests.ps1` | API RBAC test suite (89 tests, all 5 roles + unauthenticated) |
| `testing/rbac-tests/run-rbac-web-tests.ps1` | Web UI RBAC test suite (91 tests, all 5 roles) |
| `testing/rbac-tests/rbac-e2e-runner.js` | Playwright E2E RBAC tests (105 tests, 62 screenshots) |

## Reports Generated

| Report | Path |
|--------|------|
| API RBAC Report | `testing/reports/rbac-api-report-latest.html` |
| Web UI RBAC Report | `testing/reports/rbac-web-report-latest.html` |
| E2E RBAC Report | `testing/reports/rbac-e2e-report-latest.html` |

## Screenshot Evidence (62 screenshots)

| Role | Screenshots | Pages Captured |
|------|:-----------:|----------------|
| SUPER_ADMIN | 13 | Dashboard, admin, tenants, users, masters, profile |
| TENANT_ADMIN | 20 | Dashboard, all masters, orders, customers, payments, reports, settings, profile |
| MANAGER | 17 | Dashboard, masters, orders, customers, payments, reports, profile |
| STAFF | 8 | Dashboard, orders, customers, payments, profile |
| VIEWER | 4 | Dashboard, profile, sidebar menu, login |

## Sidebar Menu Visibility (Verified via E2E)

| Menu Item | SUPER_ADMIN | TENANT_ADMIN | MANAGER | STAFF | VIEWER |
|-----------|:-----------:|:------------:|:-------:|:-----:|:------:|
| Dashboard | Visible | Visible | Visible | Visible | Visible |
| Admin | Visible | Hidden | Hidden | Hidden | Hidden |
| Orders | Hidden | Visible | Visible | Visible | Hidden |
| Customers | Hidden | Visible | Visible | Visible | Hidden |
| Payments | Hidden | Visible | Visible | Visible | Hidden |
| Master Data | Hidden | Visible | Visible | Hidden | Hidden |
| Reports | Hidden | Visible | Visible | Hidden | Hidden |
| Settings | Hidden | Visible | Hidden | Hidden | Hidden |
