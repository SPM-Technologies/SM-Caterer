# SM-Caterer Basic Test Agent

A testing agent that automatically tests all pages/menus in the SM-Caterer application and reports any issues.

## Quick Start

### Windows (CMD/PowerShell)
```cmd
cd SM-Caterer/agents/basicTest
run-tests.cmd
```

### Linux/Mac/Git Bash
```bash
cd SM-Caterer/agents/basicTest
chmod +x run-tests.sh
./run-tests.sh
```

## What It Tests

The agent tests all major pages in the application:

| Category | Pages |
|----------|-------|
| **Core** | Dashboard, Profile |
| **Masters** | Units, Materials, Menus, Event Types, Recipes, UPI QR |
| **Orders** | Order List, Order Wizard |
| **Customers** | Customer List, New Customer |
| **Payments** | Payment List, New Payment |
| **Reports** | Reports Index, Order Report, Payment Report, Pending Balance |

## Features

- Automatically checks if application is running
- Handles login and session management
- Tests each page for correct HTTP status
- Detects error pages and exceptions
- Generates text and HTML reports
- Color-coded console output

## Configuration

Edit `config.json` to customize:

```json
{
  "application": {
    "baseUrl": "http://localhost:8080",
    "credentials": {
      "username": "testuser",
      "password": "test123"
    }
  },
  "pages": [
    {
      "name": "Dashboard",
      "path": "/dashboard",
      "expectedStatus": 200,
      "requiresAuth": true,
      "category": "core"
    }
  ]
}
```

## Reports

Reports are saved in the `reports/` directory:

- `test_report_YYYYMMDD_HHMMSS.txt` - Text format
- `test_report_YYYYMMDD_HHMMSS.html` - HTML format (with styling)

## Exit Codes

- `0` - All tests passed
- `1` - One or more tests failed

## Troubleshooting

### Application Not Running
The agent will report if the application is not running. Start it with:
```cmd
cd SM-Caterer
mvnw spring-boot:run
```

### Login Failed
Check that the test user credentials in `config.json` match your database.

### Session Expired
The agent automatically re-logs in if the session expires during testing.

## Adding New Pages

To add a new page to test, edit `config.json`:

```json
{
  "name": "My New Page",
  "path": "/path/to/page",
  "expectedStatus": 200,
  "requiresAuth": true,
  "category": "custom"
}
```
