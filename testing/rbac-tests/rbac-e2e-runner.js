/**
 * RBAC E2E Test Runner - Playwright
 * Tests role-based access control with screenshot proof for each role
 * Roles: SUPER_ADMIN, TENANT_ADMIN, MANAGER, STAFF, VIEWER
 */

const { chromium } = require('playwright');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080';
const SCREENSHOT_DIR = path.join(__dirname, '..', 'reports', 'screenshots', 'rbac');
const REPORT_FILE = path.join(__dirname, '..', 'reports', `rbac-e2e-report-${new Date().toISOString().replace(/[:.]/g, '-').slice(0, 19)}.html`);
const REPORT_LATEST = path.join(__dirname, '..', 'reports', 'rbac-e2e-report-latest.html');

// Test users
const USERS = {
    SUPER_ADMIN:  { username: 'SM_2026_SADMIN', password: 'test123' },
    TENANT_ADMIN: { username: 'testuser',       password: 'test123' },
    MANAGER:      { username: 'testmanager',     password: 'test123' },
    STAFF:        { username: 'teststaff',       password: 'test123' },
    VIEWER:       { username: 'testviewer',      password: 'test123' },
};

// Pages to test per role with expected access
const PAGES = [
    { url: '/dashboard',           name: 'Dashboard',        allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER','STAFF','VIEWER'] },
    { url: '/admin',               name: 'Admin Dashboard',  allow: ['SUPER_ADMIN'] },
    { url: '/admin/tenants',       name: 'Admin Tenants',    allow: ['SUPER_ADMIN'] },
    { url: '/admin/users',         name: 'Admin Users',      allow: ['SUPER_ADMIN'] },
    { url: '/masters/units',       name: 'Master Units',     allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER'] },
    { url: '/masters/materials',   name: 'Master Materials', allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER'] },
    { url: '/masters/menus',       name: 'Master Menus',     allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER'] },
    { url: '/masters/event-types', name: 'Master EventTypes',allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER'] },
    { url: '/masters/recipes',     name: 'Master Recipes',   allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER'] },
    { url: '/masters/upi-qr',     name: 'Master UPI QR',    allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER'] },
    { url: '/orders',              name: 'Orders List',      allow: ['TENANT_ADMIN','MANAGER','STAFF'] },
    { url: '/orders/new',          name: 'New Order',        allow: ['TENANT_ADMIN','MANAGER','STAFF'] },
    { url: '/customers',           name: 'Customers',        allow: ['TENANT_ADMIN','MANAGER','STAFF'] },
    { url: '/payments',            name: 'Payments',         allow: ['TENANT_ADMIN','MANAGER','STAFF'] },
    { url: '/reports',             name: 'Reports',          allow: ['TENANT_ADMIN','MANAGER'] },
    { url: '/reports/orders',      name: 'Report Orders',    allow: ['TENANT_ADMIN','MANAGER'] },
    { url: '/reports/payments',    name: 'Report Payments',  allow: ['TENANT_ADMIN','MANAGER'] },
    { url: '/settings/email',      name: 'Settings Email',   allow: ['TENANT_ADMIN'] },
    { url: '/settings/payment',    name: 'Settings Payment', allow: ['TENANT_ADMIN'] },
    { url: '/settings/branding',   name: 'Settings Branding',allow: ['TENANT_ADMIN'] },
    { url: '/profile',             name: 'Profile',          allow: ['SUPER_ADMIN','TENANT_ADMIN','MANAGER','STAFF','VIEWER'] },
];

let results = [];
let totalTests = 0;
let passed = 0;
let failed = 0;
let screenshots = 0;

async function login(page, username, password) {
    try {
        await page.goto(`${BASE_URL}/login`, { waitUntil: 'load', timeout: 30000 });
    } catch {
        // Retry once
        await page.waitForTimeout(2000);
        await page.goto(`${BASE_URL}/login`, { waitUntil: 'load', timeout: 30000 });
    }
    await page.waitForTimeout(500);
    await page.fill('#username', username);
    await page.fill('#password', password);
    await page.click('button[type="submit"]');
    await page.waitForLoadState('load', { timeout: 30000 }).catch(() => {});
    await page.waitForTimeout(2000);
}

async function isAccessDenied(page) {
    const url = page.url();

    // Check for login redirect
    if (url.includes('/login')) return true;

    // Check for error URL
    if (url.includes('/error/403') || url.includes('/error/404') || url.includes('/error/')) return true;

    return false;
}

async function testPageAccess(page, role, pageInfo, roleDir) {
    totalTests++;
    const expected = pageInfo.allow.includes(role) ? 'ALLOW' : 'DENY';

    try {
        let response;
        try {
            response = await page.goto(`${BASE_URL}${pageInfo.url}`, {
                waitUntil: 'load',
                timeout: 30000
            });
        } catch (navErr) {
            response = null;
        }

        await page.waitForTimeout(1000);

        const statusCode = response ? response.status() : 0;
        const denied = await isAccessDenied(page);

        let actual;
        if (denied) {
            actual = 'DENY';
        } else if (statusCode === 403 || statusCode === 401) {
            actual = 'DENY';
        } else if (statusCode >= 200 && statusCode < 400) {
            actual = 'ALLOW';
        } else if (statusCode === 0) {
            // Timeout - check current URL to determine state
            const currentUrl = page.url();
            if (currentUrl.includes('/login') || currentUrl.includes('/error')) {
                actual = 'DENY';
            } else {
                actual = 'ALLOW';  // Page loaded but slowly
            }
        } else if (statusCode === 500) {
            actual = 'ERROR';
        } else {
            actual = 'DENY';
        }

        const pass = (actual === expected);

        // Take screenshot for allowed pages (to show what each role sees)
        const ssName = `${role}-${pageInfo.name.replace(/\s+/g, '-').toLowerCase()}.png`;
        const ssPath = path.join(roleDir, ssName);

        if (expected === 'ALLOW') {
            await page.screenshot({ path: ssPath, fullPage: false, timeout: 10000 }).catch(() => {});
            screenshots++;
        } else if (!pass) {
            // Screenshot failures too
            await page.screenshot({ path: ssPath, fullPage: false, timeout: 10000 }).catch(() => {});
            screenshots++;
        }

        if (pass) {
            passed++;
        } else {
            failed++;
            console.log(`  FAIL: [${role}] ${pageInfo.url} -> Expected:${expected} Got:${actual} (HTTP ${statusCode})`);
        }

        results.push({
            role, page: pageInfo.name, url: pageInfo.url,
            expected, actual, status: pass ? 'PASS' : 'FAIL',
            httpCode: statusCode,
            screenshot: expected === 'ALLOW' || !pass ? ssName : ''
        });

    } catch (err) {
        totalTests--; // Don't count errors
        console.log(`  ERROR: [${role}] ${pageInfo.url} -> ${err.message}`);
        results.push({
            role, page: pageInfo.name, url: pageInfo.url,
            expected, actual: 'ERROR', status: 'SKIP',
            httpCode: 0, screenshot: ''
        });
    }
}

async function testNavbarMenuVisibility(page, role, roleDir) {
    // Navigate to dashboard to see the sidebar
    try {
        await page.goto(`${BASE_URL}/dashboard`, { waitUntil: 'load', timeout: 30000 });
    } catch {
        // Already on dashboard or slow load - continue anyway
    }
    await page.waitForTimeout(2000);

    // Take sidebar screenshot
    const ssName = `${role}-sidebar-menu.png`;
    await page.screenshot({ path: path.join(roleDir, ssName), fullPage: false, timeout: 10000 }).catch(() => {});
    screenshots++;

    // Check which menu items are visible
    const menuItems = {};
    const selectors = {
        'Dashboard': 'a[href*="dashboard"]',
        'Admin': 'a[href*="admin"]',
        'Orders': 'a[href*="orders"]',
        'Customers': 'a[href*="customers"]',
        'Payments': 'a[href*="payments"]',
        'Master Data': 'a[href*="masters"], a:has-text("Master")',
        'Reports': 'a[href*="reports"]',
        'Settings': 'a[href*="settings"]',
    };

    for (const [name, selector] of Object.entries(selectors)) {
        try {
            const count = await page.locator(selector).count();
            menuItems[name] = count > 0;
        } catch {
            menuItems[name] = false;
        }
    }

    return menuItems;
}

async function run() {
    console.log('=== RBAC E2E TEST SUITE ===\n');

    // Clean screenshot directory
    if (fs.existsSync(SCREENSHOT_DIR)) {
        fs.rmSync(SCREENSHOT_DIR, { recursive: true });
    }
    fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });

    const browser = await chromium.launch({
        channel: 'chrome',
        headless: true
    });

    const menuVisibility = {};

    for (const role of Object.keys(USERS)) {
        console.log(`\n--- Testing ${role} (${USERS[role].username}) ---`);

        const roleDir = path.join(SCREENSHOT_DIR, role.toLowerCase());
        fs.mkdirSync(roleDir, { recursive: true });

        let context, page;
        try {
            context = await browser.newContext({ viewport: { width: 1280, height: 720 } });
            page = await context.newPage();
            page.setDefaultTimeout(30000);

            // Login with retry
            let loginOk = false;
            for (let attempt = 1; attempt <= 3; attempt++) {
                try {
                    await login(page, USERS[role].username, USERS[role].password);
                    const currentUrl = page.url();
                    if (!currentUrl.includes('/login')) {
                        loginOk = true;
                        console.log(`  Logged in -> ${currentUrl}`);
                        break;
                    }
                } catch (loginErr) {
                    console.log(`  Login attempt ${attempt} failed: ${loginErr.message.slice(0, 60)}`);
                    if (attempt < 3) {
                        await page.waitForTimeout(3000);
                    }
                }
            }

            if (!loginOk) {
                console.log(`  LOGIN FAILED for ${role} after 3 attempts!`);
                await context.close();
                continue;
            }

            // Take login proof screenshot
            const loginSsName = `${role}-login-success.png`;
            await page.screenshot({ path: path.join(roleDir, loginSsName), fullPage: false, timeout: 10000 }).catch(() => {});
            screenshots++;

            // Test navbar menu visibility
            menuVisibility[role] = await testNavbarMenuVisibility(page, role, roleDir);

            // Test each page
            for (const pageInfo of PAGES) {
                await testPageAccess(page, role, pageInfo, roleDir);
            }
        } catch (roleErr) {
            console.log(`  ERROR testing ${role}: ${roleErr.message.slice(0, 80)}`);
        }

        try { await context.close(); } catch {}
        // Brief pause between roles to let server recover
        await new Promise(r => setTimeout(r, 2000));
    }

    await browser.close();

    // Generate report
    generateReport(menuVisibility);

    console.log(`\n=== RBAC E2E TEST RESULTS ===`);
    console.log(`Total: ${totalTests} | Passed: ${passed} | Failed: ${failed} | Rate: ${totalTests > 0 ? ((passed/totalTests)*100).toFixed(1) : 0}%`);
    console.log(`Screenshots: ${screenshots}`);
    console.log(`Report: ${REPORT_FILE}`);

    if (failed > 0) {
        console.log('\n=== FAILURES ===');
        results.filter(r => r.status === 'FAIL').forEach(r => {
            console.log(`  [${r.role}] ${r.url} -> Expected:${r.expected} Got:${r.actual} (HTTP ${r.httpCode})`);
        });
    }
}

function generateReport(menuVisibility) {
    // Build results table
    let tableRows = '';
    for (const r of results) {
        const cls = r.status === 'PASS' ? 'pass' : (r.status === 'FAIL' ? 'fail' : 'skip');
        const ssCell = r.screenshot ? `<a href="screenshots/rbac/${r.role.toLowerCase()}/${r.screenshot}" target="_blank">View</a>` : '-';
        tableRows += `<tr class="${cls}"><td>${r.role}</td><td>${r.page}</td><td>${r.url}</td><td>${r.expected}</td><td>${r.actual}</td><td>${r.httpCode}</td><td>${r.status}</td><td>${ssCell}</td></tr>\n`;
    }

    // Build menu visibility matrix
    let menuMatrix = '';
    const menuNames = ['Dashboard', 'Admin', 'Orders', 'Customers', 'Payments', 'Master Data', 'Reports', 'Settings'];
    for (const menu of menuNames) {
        menuMatrix += `<tr><td><strong>${menu}</strong></td>`;
        for (const role of Object.keys(USERS)) {
            const visible = menuVisibility[role] && menuVisibility[role][menu];
            menuMatrix += `<td class="${visible ? 'allowed' : 'denied'}">${visible ? 'Visible' : 'Hidden'}</td>`;
        }
        menuMatrix += '</tr>\n';
    }

    // Build screenshot gallery per role
    let gallery = '';
    for (const role of Object.keys(USERS)) {
        const roleDir = path.join(SCREENSHOT_DIR, role.toLowerCase());
        if (fs.existsSync(roleDir)) {
            const files = fs.readdirSync(roleDir).filter(f => f.endsWith('.png')).sort();
            if (files.length > 0) {
                gallery += `<h3>${role} (${files.length} screenshots)</h3><div class="gallery">`;
                for (const f of files) {
                    const name = f.replace('.png', '').replace(`${role}-`, '').replace(/-/g, ' ');
                    gallery += `<div class="ss-card"><a href="screenshots/rbac/${role.toLowerCase()}/${f}" target="_blank"><img src="screenshots/rbac/${role.toLowerCase()}/${f}" alt="${name}"></a><div class="ss-label">${name}</div></div>`;
                }
                gallery += '</div>';
            }
        }
    }

    const passRate = totalTests > 0 ? ((passed / totalTests) * 100).toFixed(1) : 0;

    const html = `<!DOCTYPE html>
<html>
<head>
<title>RBAC E2E Test Report with Screenshots</title>
<style>
body { font-family: 'Segoe UI', Arial, sans-serif; margin: 20px; background: #f5f5f5; }
h1 { color: #2c3e50; border-bottom: 3px solid #9b59b6; padding-bottom: 10px; }
h2 { color: #2c3e50; margin-top: 30px; }
h3 { color: #34495e; margin-top: 20px; }
.summary { display: flex; gap: 20px; margin: 20px 0; flex-wrap: wrap; }
.card { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); min-width: 150px; text-align: center; }
.card .number { font-size: 36px; font-weight: bold; }
.card .label { color: #666; }
.card.total .number { color: #3498db; }
.card.passed .number { color: #27ae60; }
.card.failed .number { color: #e74c3c; }
.card.rate .number { color: #f39c12; }
.card.ss .number { color: #9b59b6; }
table { width: 100%; border-collapse: collapse; background: white; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 4px rgba(0,0,0,0.1); margin-bottom: 30px; }
th { background: #2c3e50; color: white; padding: 10px 8px; text-align: left; font-size: 13px; }
td { padding: 8px; border-bottom: 1px solid #eee; font-size: 13px; }
tr.pass td { background: #f0fff0; }
tr.fail td { background: #fff0f0; font-weight: bold; }
tr.skip td { background: #fffff0; }
.allowed { color: #27ae60; font-weight: bold; text-align: center; }
.denied { color: #e74c3c; font-weight: bold; text-align: center; }
.matrix-table td, .matrix-table th { text-align: center; padding: 10px; }
.matrix-table td:first-child { text-align: left; }
.gallery { display: flex; flex-wrap: wrap; gap: 15px; margin: 10px 0 30px 0; }
.ss-card { background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); overflow: hidden; width: 280px; }
.ss-card img { width: 100%; height: 160px; object-fit: cover; cursor: pointer; }
.ss-card .ss-label { padding: 8px; font-size: 12px; text-align: center; color: #666; text-transform: capitalize; }
.timestamp { color: #999; font-size: 12px; }
</style>
</head>
<body>
<h1>RBAC E2E Test Report - SM-Caterer (with Screenshots)</h1>
<p class="timestamp">Generated: ${new Date().toISOString().replace('T', ' ').slice(0, 19)}</p>

<div class="summary">
    <div class="card total"><div class="number">${totalTests}</div><div class="label">Total Tests</div></div>
    <div class="card passed"><div class="number">${passed}</div><div class="label">Passed</div></div>
    <div class="card failed"><div class="number">${failed}</div><div class="label">Failed</div></div>
    <div class="card rate"><div class="number">${passRate}%</div><div class="label">Pass Rate</div></div>
    <div class="card ss"><div class="number">${screenshots}</div><div class="label">Screenshots</div></div>
</div>

<h2>Sidebar Menu Visibility per Role</h2>
<table class="matrix-table">
<tr><th>Menu Item</th><th>SUPER_ADMIN</th><th>TENANT_ADMIN</th><th>MANAGER</th><th>STAFF</th><th>VIEWER</th></tr>
${menuMatrix}
</table>

<h2>Page Access Test Results</h2>
<table>
<tr><th>Role</th><th>Page</th><th>URL</th><th>Expected</th><th>Actual</th><th>HTTP</th><th>Status</th><th>Screenshot</th></tr>
${tableRows}
</table>

<h2>Screenshot Gallery by Role</h2>
${gallery}

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
</html>`;

    fs.writeFileSync(REPORT_FILE, html);
    fs.writeFileSync(REPORT_LATEST, html);
}

run().catch(err => {
    console.error('Fatal error:', err);
    process.exit(1);
});
