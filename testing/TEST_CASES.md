# SM-Caterer Test Cases Documentation

## Overview
This document contains comprehensive test cases for the SM-Caterer multi-tenant catering management system.

---

## 1. Authentication Module

### 1.1 Login Tests
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| AUTH-001 | Valid Tenant Admin Login | 1. Go to /login 2. Enter valid tenant admin credentials 3. Click Login | Redirect to dashboard | High |
| AUTH-002 | Valid Super Admin Login | 1. Go to /login 2. Enter super admin credentials 3. Click Login | Redirect to admin dashboard | High |
| AUTH-003 | Invalid Credentials | 1. Go to /login 2. Enter wrong credentials 3. Click Login | Error message shown, stay on login | High |
| AUTH-004 | Empty Username | 1. Go to /login 2. Leave username empty 3. Click Login | Validation error | High |
| AUTH-005 | Empty Password | 1. Go to /login 2. Leave password empty 3. Click Login | Validation error | High |
| AUTH-006 | Remember Me | 1. Login with remember-me checked 2. Close browser 3. Reopen | Session persisted | Medium |
| AUTH-007 | Session Timeout | 1. Login 2. Wait for session timeout 3. Try action | Redirect to login with expired message | Medium |

### 1.2 Logout Tests
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| AUTH-008 | Successful Logout | 1. Login 2. Click Logout | Redirect to login with success message | High |
| AUTH-009 | Access After Logout | 1. Logout 2. Try to access dashboard | Redirect to login | High |

### 1.3 API Authentication Tests
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| AUTH-API-001 | Login API Valid | POST /api/v1/auth/login with valid credentials | 200 OK with JWT tokens | High |
| AUTH-API-002 | Login API Invalid | POST /api/v1/auth/login with invalid credentials | 401 Unauthorized | High |
| AUTH-API-003 | Token Refresh | POST /api/v1/auth/refresh with valid refresh token | 200 OK with new access token | High |
| AUTH-API-004 | Get Current User | GET /api/v1/auth/me with valid token | 200 OK with user info | High |
| AUTH-API-005 | Access Without Token | GET /api/v1/auth/me without token | 401 Unauthorized | High |

---

## 2. Customer Module

### 2.1 Customer List
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| CUST-001 | View Customer List | 1. Login 2. Go to Customers | List of customers displayed | High |
| CUST-002 | Pagination | 1. Go to Customers 2. Navigate pages | Pagination works correctly | Medium |
| CUST-003 | Search Customer | 1. Go to Customers 2. Search by name | Filtered results shown | High |

### 2.2 Customer CRUD
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| CUST-004 | Create Customer | 1. Click Add Customer 2. Fill form 3. Save | Customer created, redirect to list | High |
| CUST-005 | Create - Validation | 1. Click Add Customer 2. Submit empty form | Validation errors shown | High |
| CUST-006 | Edit Customer | 1. Click Edit on customer 2. Update data 3. Save | Customer updated | High |
| CUST-007 | Delete Customer | 1. Click Delete 2. Confirm | Customer deleted | Medium |
| CUST-008 | View Customer Details | 1. Click on customer name | Customer details page shown | Medium |

---

## 3. Order Module

### 3.1 Order List
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| ORD-001 | View Order List | 1. Login 2. Go to Orders | List of orders displayed | High |
| ORD-002 | Filter by Status | 1. Go to Orders 2. Filter by PENDING | Only pending orders shown | High |
| ORD-003 | Search Orders | 1. Go to Orders 2. Search | Filtered results | Medium |

### 3.2 Order Wizard
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| ORD-004 | Step 1 - Customer Selection | 1. Go to New Order 2. Select customer | Customer selected, can proceed | High |
| ORD-005 | Step 1 - New Customer | 1. New Order 2. Create new customer | Customer created and selected | High |
| ORD-006 | Step 2 - Event Details | 1. Complete Step 1 2. Fill event details | Data saved, can proceed | High |
| ORD-007 | Step 2 - Validation | 1. Step 2 2. Submit without required fields | Validation errors | High |
| ORD-008 | Step 3 - Menu Selection | 1. Complete Step 2 2. Add menu items | Items added to order | High |
| ORD-009 | Step 4 - Utilities | 1. Complete Step 3 2. Add utilities | Utilities added | Medium |
| ORD-010 | Step 5 - Summary | 1. Complete Step 4 2. Review summary | All data displayed correctly | High |
| ORD-011 | Create Order | 1. Complete wizard 2. Submit | Order created successfully | High |

### 3.3 Order Workflow
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| ORD-012 | Submit Order | 1. View DRAFT order 2. Click Submit | Status changes to PENDING | High |
| ORD-013 | Approve Order | 1. View PENDING order 2. Click Approve | Status changes to CONFIRMED | High |
| ORD-014 | Reject Order | 1. View PENDING order 2. Click Reject | Status changes to CANCELLED | High |
| ORD-015 | Start Order | 1. View CONFIRMED order 2. Click Start | Status changes to IN_PROGRESS | High |
| ORD-016 | Complete Order | 1. View IN_PROGRESS order 2. Click Complete | Status changes to COMPLETED | High |
| ORD-017 | Print Order | 1. View order 2. Click Print | Print preview shown | Medium |

---

## 4. Payment Module

### 4.1 Payment Recording
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| PAY-001 | Record Cash Payment | 1. Go to Payments 2. Record payment 3. Select CASH | Payment recorded | High |
| PAY-002 | Record UPI Payment | 1. Record payment 2. Select UPI | Payment recorded with QR | High |
| PAY-003 | Record Card Payment | 1. Record payment 2. Select CARD | Payment recorded | High |
| PAY-004 | Validation - Negative Amount | 1. Record payment 2. Enter negative amount | Validation error | High |
| PAY-005 | View Payment List | 1. Go to Payments | List displayed | High |
| PAY-006 | Payment from Order | 1. View order 2. Click Record Payment | Payment linked to order | High |

---

## 5. Master Data Module

### 5.1 Units
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| MD-001 | List Units | 1. Go to Master Data > Units | Units listed | High |
| MD-002 | Create Unit | 1. Add Unit 2. Fill form 3. Save | Unit created | High |
| MD-003 | Edit Unit | 1. Edit unit 2. Update 3. Save | Unit updated | High |
| MD-004 | Delete Unit | 1. Delete unit 2. Confirm | Unit deleted | Medium |

### 5.2 Materials
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| MD-005 | List Materials | 1. Go to Materials | Materials listed | High |
| MD-006 | Create Material | 1. Add Material 2. Fill form 3. Save | Material created | High |
| MD-007 | Material with Unit | 1. Create material with unit | Material linked to unit | High |

### 5.3 Menus
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| MD-008 | List Menus | 1. Go to Menus | Menus listed | High |
| MD-009 | Create Menu VEG | 1. Add Menu 2. Category=VEG 3. Save | VEG menu created | High |
| MD-010 | Create Menu NON-VEG | 1. Add Menu 2. Category=NON_VEG 3. Save | NON_VEG menu created | High |

### 5.4 Event Types
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| MD-011 | List Event Types | 1. Go to Event Types | Event types listed | High |
| MD-012 | Create Event Type | 1. Add Event Type 2. Save | Event type created | High |

### 5.5 Recipes
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| MD-013 | List Recipes | 1. Go to Recipes | Recipes listed | Medium |
| MD-014 | Create Recipe | 1. Add Recipe 2. Link menu+materials | Recipe created | Medium |

### 5.6 UPI QR Codes
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| MD-015 | List UPI QR | 1. Go to UPI QR Codes | QR codes listed | Medium |
| MD-016 | Create UPI QR | 1. Add QR 2. Enter UPI ID 3. Save | QR code created | Medium |
| MD-017 | Generate QR Image | 1. View QR code | QR image displayed | Medium |

---

## 6. Reports Module

### 6.1 Reports
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| REP-001 | Orders Report | 1. Go to Reports > Orders | Order report displayed | High |
| REP-002 | Payments Report | 1. Go to Reports > Payments | Payment report displayed | High |
| REP-003 | Pending Balance | 1. Go to Reports > Pending Balance | Balance report displayed | High |
| REP-004 | Stock Report | 1. Go to Reports > Stock | Stock report displayed | Medium |
| REP-005 | Customer Report | 1. Go to Reports > Customers | Customer report displayed | Medium |
| REP-006 | Export to Excel | 1. View report 2. Click Export | Excel file downloaded | Medium |
| REP-007 | Date Filter | 1. Reports 2. Select date range | Filtered results | High |

---

## 7. Settings Module

### 7.1 Email Settings
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| SET-001 | View Email Settings | 1. Go to Settings > Email | Settings form displayed | High |
| SET-002 | Save Email Settings | 1. Fill SMTP settings 2. Save | Settings saved | High |
| SET-003 | Test Email | 1. Configure 2. Send test email | Test email sent | Medium |

### 7.2 Payment Settings
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| SET-004 | View Payment Settings | 1. Go to Settings > Payment | Settings displayed | High |
| SET-005 | Save UPI Settings | 1. Fill UPI ID 2. Save | Settings saved | High |

### 7.3 Branding Settings
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| SET-006 | View Branding Settings | 1. Go to Settings > Branding | Settings displayed | High |
| SET-007 | Upload Logo | 1. Select logo file 2. Save | Logo uploaded and displayed | High |
| SET-008 | Update Display Name | 1. Change display name 2. Save | Name updated | High |
| SET-009 | Update Primary Color | 1. Change color 2. Save | Color updated | Medium |
| SET-010 | Remove Logo | 1. Click Remove Logo 2. Confirm | Logo removed | Medium |

---

## 8. Admin Module (Super Admin)

### 8.1 Tenant Management
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| ADM-001 | View Tenant List | 1. Login as Super Admin 2. Go to Tenants | Tenants listed | High |
| ADM-002 | Create Tenant | 1. Add Tenant 2. Fill form 3. Save | Tenant created | High |
| ADM-003 | Create Tenant with Logo | 1. Add Tenant 2. Upload logo 3. Save | Tenant created with logo | **High (BUG)** |
| ADM-004 | Edit Tenant | 1. Edit tenant 2. Update 3. Save | Tenant updated | High |
| ADM-005 | Delete Tenant | 1. Delete tenant 2. Confirm | Tenant deleted (soft) | High |
| ADM-006 | Tenant Status Change | 1. Change status to SUSPENDED | Tenant suspended | High |

### 8.2 User Management
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| ADM-007 | View User List | 1. Go to Users | Users listed | High |
| ADM-008 | Create User | 1. Add User 2. Fill form 3. Save | User created | High |
| ADM-009 | Assign User to Tenant | 1. Create user 2. Select tenant | User assigned | High |
| ADM-010 | Edit User | 1. Edit user 2. Update 3. Save | User updated | High |
| ADM-011 | Delete User | 1. Delete user 2. Confirm | User deleted | High |
| ADM-012 | Cannot Delete Self | 1. Try delete own account | Error shown | High |

---

## 9. Tenant Branding (Special Tests)

### 9.1 Logo on Login Page
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| BRAND-001 | Display Logo on Login | 1. Configure tenant logo 2. Go to login | Tenant logo displayed | **High (BUG)** |
| BRAND-002 | Default Logo | 1. No logo configured 2. Go to login | Default app logo shown | Medium |

### 9.2 Logo on Reports
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| BRAND-003 | Logo on Order Report | 1. Configure logo 2. View order report | Logo in report header | High |
| BRAND-004 | Logo on Print | 1. Configure logo 2. Print order | Logo on printout | High |
| BRAND-005 | Logo on PDF Receipt | 1. Configure logo 2. Generate PDF | Logo in PDF | High |

### 9.3 Super Admin Logo Upload
| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| BRAND-006 | Upload Logo at Tenant Creation | 1. Super Admin creates tenant 2. Upload logo | Logo saved with tenant | **High (MISSING)** |
| BRAND-007 | Edit Tenant Logo | 1. Edit tenant 2. Change logo | Logo updated | **High (MISSING)** |

---

## 10. Multi-tenant Isolation Tests

| ID | Test Case | Steps | Expected Result | Priority |
|----|-----------|-------|-----------------|----------|
| MT-001 | Tenant A Cannot See Tenant B Data | 1. Login as Tenant A 2. Try access Tenant B customer | Access denied or not visible | Critical |
| MT-002 | Orders Isolated | 1. Create order in Tenant A 2. Login Tenant B | Order not visible in Tenant B | Critical |
| MT-003 | Payments Isolated | 1. Create payment in Tenant A 2. Check Tenant B | Payment not visible | Critical |

---

## Test Execution Status Legend
- **PASS**: Test passed successfully
- **FAIL**: Test failed - bug found
- **SKIP**: Test skipped
- **BLOCK**: Test blocked by another issue
- **N/A**: Not applicable

## Priority Legend
- **Critical**: Must pass for system to be usable
- **High**: Important functionality
- **Medium**: Standard functionality
- **Low**: Nice to have
