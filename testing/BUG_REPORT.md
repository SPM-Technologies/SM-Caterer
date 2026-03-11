# SM-Caterer Bug Report

## Summary
This document contains all identified bugs and issues found during testing of the SM-Caterer application.

---

## Critical / High Priority Bugs

### BUG-001: Super Admin Cannot Upload Logo When Creating Tenant
**Severity:** High
**Module:** Admin - Tenant Management
**Status:** OPEN

**Description:**
When Super Admin creates a new tenant, there is no option to upload a brand logo. The tenant form (`/admin/tenants/new`) only contains basic business information fields but lacks logo upload functionality.

**Steps to Reproduce:**
1. Login as Super Admin (SM_2026_SADMIN)
2. Go to Admin > Tenants
3. Click "Add Tenant"
4. Fill in tenant details
5. Notice: No logo upload field available

**Expected Behavior:**
Super Admin should be able to upload a brand logo while creating a tenant, which will be visible on the tenant's login page and reports.

**Current Behavior:**
No logo upload option available in tenant creation form.

**Affected Files:**
- `src/main/resources/templates/admin/tenants/form.html` - Missing logo upload field
- `src/main/java/com/smtech/SM_Caterer/web/controller/AdminController.java` - No logo handling in createTenant/updateTenant

**Suggested Fix:**
1. Add `enctype="multipart/form-data"` to form
2. Add logo upload input field
3. Update AdminController to handle MultipartFile
4. Save logo to uploads directory

---

### BUG-002: Tenant Logo Not Displayed on Login Page
**Severity:** High
**Module:** Authentication - Login Page
**Status:** OPEN

**Description:**
The login page always shows the generic "SM-Caterer" branding and icon. Even when a tenant has configured their logo through branding settings, it is not displayed on the login page.

**Steps to Reproduce:**
1. Login as Tenant Admin (testuser)
2. Go to Settings > Branding
3. Upload a logo
4. Logout
5. View login page
6. Notice: Generic SM-Caterer logo shown, not tenant logo

**Expected Behavior:**
If a tenant has a logo configured, it should be displayed on the login page instead of the default icon.

**Current Behavior:**
Login page always shows `<i class="bi bi-shop"></i>` icon and "SM-Caterer" text.

**Affected Files:**
- `src/main/resources/templates/auth/login.html` - Hard-coded branding
- `src/main/java/com/smtech/SM_Caterer/web/controller/LoginController.java` - Not passing branding to view

**Suggested Fix:**
1. Detect tenant from username/domain (if multi-domain) or show selector
2. Fetch tenant branding information
3. Pass branding to login.html template
4. Conditionally display tenant logo or default

**Note:** This requires architectural decision on how to identify tenant at login page.

---

### BUG-003: Edit Tenant Form Missing Logo Management
**Severity:** High
**Module:** Admin - Tenant Management
**Status:** OPEN

**Description:**
When editing an existing tenant, there is no way to upload, view, or remove the tenant's logo.

**Steps to Reproduce:**
1. Login as Super Admin
2. Go to Admin > Tenants
3. Click Edit on any tenant
4. Notice: No logo management section

**Expected Behavior:**
Should be able to view current logo, upload new logo, or remove logo.

**Current Behavior:**
No logo-related fields in edit form.

**Affected Files:**
- `src/main/resources/templates/admin/tenants/form.html`
- `src/main/java/com/smtech/SM_Caterer/web/controller/AdminController.java`

---

## Medium Priority Bugs

### BUG-004: Logo Path Storage Issue
**Severity:** Medium
**Module:** Settings - Branding
**Status:** NEEDS VERIFICATION

**Description:**
The logo path is stored as an absolute path in the database, which may cause issues when deploying to different environments.

**Location:**
`src/main/java/com/smtech/SM_Caterer/web/controller/TenantSettingsController.java` line 327

**Current Code:**
```java
return filePath.toString();  // Returns absolute path
```

**Suggested Fix:**
Store relative path from uploads directory.

---

### BUG-005: Missing Validation for Logo File Type
**Severity:** Medium
**Module:** Settings - Branding
**Status:** NEEDS VERIFICATION

**Description:**
While the controller validates file type, the validation may not be robust against malicious file uploads.

**Current Validation:**
```java
if (contentType == null || !contentType.startsWith("image/")) {
    throw new IOException("Invalid file type");
}
```

**Suggested Enhancement:**
- Check actual file magic bytes, not just Content-Type header
- Validate allowed extensions (.png, .jpg, .jpeg, .gif)
- Add antivirus scan for uploaded files in production

---

### BUG-006: Branding Primary Color Not Applied
**Severity:** Medium
**Module:** UI - Branding
**Status:** NEEDS VERIFICATION

**Description:**
The primary color saved in branding settings may not be applied consistently across the application UI.

**Steps to Reproduce:**
1. Go to Settings > Branding
2. Change primary color
3. Save
4. Navigate through app
5. Check if color is applied

**Expected Behavior:**
Primary brand color should be visible in UI accents.

---

## Low Priority / Enhancement Requests

### ENH-001: Public Branding API Endpoint
**Type:** Enhancement
**Module:** API

**Description:**
Need a public API endpoint to fetch tenant branding without authentication, for use on login page.

**Suggested Endpoint:**
```
GET /api/public/branding/{tenantCode}
```

**Response:**
```json
{
  "logoUrl": "/uploads/logos/tenant_1_abc.png",
  "displayName": "Demo Caterers",
  "tagline": "Best Catering Services",
  "primaryColor": "#3498db"
}
```

---

### ENH-002: Tenant Selector on Login Page
**Type:** Enhancement
**Module:** Authentication

**Description:**
For multi-tenant systems, consider adding a tenant selector or using subdomain-based tenant identification to show appropriate branding on login.

**Options:**
1. Subdomain-based: `tenant1.app.com/login`
2. Path-based: `app.com/tenant1/login`
3. Selector dropdown on login page

---

### ENH-003: Logo Validation Improvements
**Type:** Enhancement
**Module:** Settings

**Description:**
Add client-side validation for logo uploads:
- File type check
- File size preview
- Image preview before upload
- Recommended dimensions hint (e.g., 200x80px)

---

## Test Environment

- **Application URL:** http://localhost:8080
- **Database:** MySQL
- **Test Accounts:**
  - Super Admin: SM_2026_SADMIN / test123
  - Tenant Admin: testuser / test123

---

## Bug Statistics

| Severity | Count | Open | Fixed | Verified |
|----------|-------|------|-------|----------|
| Critical | 0     | 0    | 0     | 0        |
| High     | 3     | 3    | 0     | 0        |
| Medium   | 3     | 3    | 0     | 0        |
| Low      | 3     | 3    | 0     | 0        |
| **Total**| **9** | **9**| **0** | **0**    |

---

## Report Information

- **Report Date:** 2026-01-17
- **Tester:** Automated Testing Suite
- **Version:** 0.0.1-SNAPSHOT
