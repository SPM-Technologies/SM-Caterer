"""
SM-Caterer Testing Configuration
"""
import os

# Base Configuration
BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")
API_BASE_URL = f"{BASE_URL}/api/v1"

# Test Credentials
SUPER_ADMIN_USERNAME = "SM_2026_SADMIN"
SUPER_ADMIN_PASSWORD = "test123"

TENANT_ADMIN_USERNAME = "testuser"
TENANT_ADMIN_PASSWORD = "test123"

# Test Timeouts (seconds)
DEFAULT_TIMEOUT = 30
PAGE_LOAD_TIMEOUT = 60
ELEMENT_WAIT_TIMEOUT = 10

# Browser Configuration
HEADLESS_MODE = os.getenv("HEADLESS", "false").lower() == "true"
BROWSER = os.getenv("BROWSER", "chrome")  # chrome, firefox, edge

# Report Configuration
REPORT_DIR = os.path.join(os.path.dirname(__file__), "reports")
SCREENSHOT_DIR = os.path.join(REPORT_DIR, "screenshots")

# Ensure directories exist
os.makedirs(REPORT_DIR, exist_ok=True)
os.makedirs(SCREENSHOT_DIR, exist_ok=True)

# API Endpoints
class APIEndpoints:
    # Auth
    LOGIN = "/auth/login"
    LOGOUT = "/auth/logout"
    ME = "/auth/me"
    REFRESH = "/auth/refresh"
    CHANGE_PASSWORD = "/auth/password"

    # Customers
    CUSTOMERS = "/customers"

    # Orders
    ORDERS = "/orders"
    ORDER_MENU_ITEMS = "/order-menu-items"
    ORDER_UTILITIES = "/order-utilities"

    # Payments
    PAYMENTS = "/payments"

    # Master Data
    UNITS = "/units"
    MATERIALS = "/materials"
    MATERIAL_GROUPS = "/material-groups"
    MENUS = "/menus"
    EVENT_TYPES = "/event-types"
    RECIPES = "/recipe-items"
    UPI_QR_CODES = "/upi-qr-codes"
    UTILITIES = "/utilities"

    # Tenants
    TENANTS = "/tenants"

    # Users
    USERS = "/users"


# Web Routes
class WebRoutes:
    LOGIN = "/login"
    DASHBOARD = "/dashboard"
    LOGOUT = "/logout"

    # Customers
    CUSTOMERS_LIST = "/customers"
    CUSTOMERS_NEW = "/customers/new"

    # Orders
    ORDERS_LIST = "/orders"
    ORDERS_WIZARD = "/orders/wizard"

    # Payments
    PAYMENTS_LIST = "/payments"
    PAYMENTS_NEW = "/payments/new"

    # Master Data
    UNITS_LIST = "/masters/units"
    MATERIALS_LIST = "/masters/materials"
    MENUS_LIST = "/masters/menus"
    EVENT_TYPES_LIST = "/masters/event-types"
    RECIPES_LIST = "/masters/recipes"
    UPI_QR_LIST = "/masters/upi-qr"

    # Reports
    REPORTS = "/reports"
    REPORTS_ORDERS = "/reports/orders"
    REPORTS_PAYMENTS = "/reports/payments"
    REPORTS_STOCK = "/reports/stock"
    REPORTS_CUSTOMERS = "/reports/customers"
    REPORTS_PENDING_BALANCE = "/reports/pending-balance"

    # Settings
    SETTINGS_EMAIL = "/settings/email"
    SETTINGS_PAYMENT = "/settings/payment"
    SETTINGS_BRANDING = "/settings/branding"

    # Profile
    PROFILE = "/profile"

    # Admin
    ADMIN_DASHBOARD = "/admin/dashboard"
    ADMIN_TENANTS = "/admin/tenants"
    ADMIN_USERS = "/admin/users"
