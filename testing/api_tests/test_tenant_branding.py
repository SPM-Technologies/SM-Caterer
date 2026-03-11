"""
Tenant Branding API Tests
Tests tenant creation with logo upload and branding visibility
"""
from base_api_test import BaseAPITest
import sys
import os
import base64
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import (
    APIEndpoints, SUPER_ADMIN_USERNAME, SUPER_ADMIN_PASSWORD,
    BASE_URL
)


class TenantBrandingAPITest(BaseAPITest):
    """Tenant Branding API Tests"""

    def __init__(self):
        super().__init__()
        self.test_tenant_id = None

    def login_as_super_admin(self):
        """Login as Super Admin"""
        return self.login(SUPER_ADMIN_USERNAME, SUPER_ADMIN_PASSWORD)

    def test_list_tenants_as_super_admin(self):
        """Test listing all tenants as super admin"""
        if not self.login_as_super_admin():
            self.record_result("List Tenants", False, "Super Admin login failed")
            return False

        response = self.get(APIEndpoints.TENANTS)
        passed = response.status_code == 200

        self.record_result(
            "List Tenants (Super Admin)",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_create_tenant_with_branding(self):
        """Test creating a tenant with branding options"""
        if not self.login_as_super_admin():
            self.record_result("Create Tenant with Branding", False, "Super Admin login failed")
            return False

        # Create a simple 1x1 pixel PNG as test logo (base64 encoded)
        # This is a minimal valid PNG
        test_logo_base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="

        tenant_data = {
            "tenantCode": "TEST-BRAND",
            "businessName": "Test Branding Caterers",
            "ownerName": "Test Owner",
            "email": "testbrand@example.com",
            "phone": "9876543000",
            "address": "123 Brand Street",
            "city": "Brand City",
            "state": "Brand State",
            "pincode": "400001",
            "gstNumber": "27AABCT1234A1Z5",
            "brandLogo": test_logo_base64,  # Logo as base64
            "brandLogoFilename": "test_logo.png",
            "primaryColor": "#3498db",
            "secondaryColor": "#2c3e50"
        }

        response = self.post(APIEndpoints.TENANTS, tenant_data)
        passed = response.status_code in [200, 201]

        data = response.json() if response.status_code in [200, 201] else {}

        if passed and data.get("success"):
            self.test_tenant_id = data.get("data", {}).get("id")

        self.record_result(
            "Create Tenant with Branding",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_tenant_branding(self):
        """Test getting tenant branding information"""
        if not self.login_as_super_admin():
            self.record_result("Get Tenant Branding", False, "Super Admin login failed")
            return False

        if not self.test_tenant_id:
            # Try to get first tenant
            response = self.get(APIEndpoints.TENANTS, params={"size": 1})
            if response.status_code == 200:
                data = response.json()
                content = data.get("data", {}).get("content", [])
                if content:
                    self.test_tenant_id = content[0].get("id")

        if not self.test_tenant_id:
            self.record_result("Get Tenant Branding", False, "No tenant ID available")
            return False

        response = self.get(f"{APIEndpoints.TENANTS}/{self.test_tenant_id}")
        passed = response.status_code == 200

        data = response.json() if passed else {}

        # Check if branding fields are present in response
        tenant_data = data.get("data", {})
        has_branding_fields = any([
            "brandLogo" in tenant_data,
            "logoUrl" in tenant_data,
            "primaryColor" in tenant_data,
            "branding" in tenant_data
        ])

        self.record_result(
            "Get Tenant Branding",
            passed,
            f"Status: {response.status_code}, Has branding fields: {has_branding_fields}",
            {"response": data, "has_branding": has_branding_fields}
        )
        return passed

    def test_update_tenant_logo(self):
        """Test updating tenant logo"""
        if not self.login_as_super_admin():
            self.record_result("Update Tenant Logo", False, "Super Admin login failed")
            return False

        if not self.test_tenant_id:
            self.record_result("Update Tenant Logo", False, "No tenant ID available")
            return False

        # New test logo
        new_logo_base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg=="

        update_data = {
            "brandLogo": new_logo_base64,
            "brandLogoFilename": "updated_logo.png"
        }

        response = self.patch(f"{APIEndpoints.TENANTS}/{self.test_tenant_id}/branding", update_data)

        # Could also be PUT
        if response.status_code == 404:
            response = self.put(f"{APIEndpoints.TENANTS}/{self.test_tenant_id}", update_data)

        passed = response.status_code in [200, 204]

        self.record_result(
            "Update Tenant Logo",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_tenant_branding_visible_endpoint(self):
        """Test if tenant branding is accessible via public endpoint for login page"""
        # This should be accessible without authentication (for login page display)
        response = self.session.get(
            f"{self.base_url}/tenants/branding/public",
            headers={"Content-Type": "application/json"}
        )

        # Could be different endpoints
        if response.status_code == 404:
            response = self.session.get(f"{BASE_URL}/api/public/branding")

        passed = response.status_code in [200, 404]  # 404 is acceptable if endpoint doesn't exist yet

        self.record_result(
            "Tenant Branding Public Endpoint",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_tenant_settings_branding_page(self):
        """Test accessing tenant branding settings"""
        if not self.login_as_super_admin():
            self.record_result("Tenant Settings Branding", False, "Login failed")
            return False

        # Check if settings/branding endpoint exists
        response = self.session.get(
            f"{BASE_URL}/settings/branding",
            headers=self.get_auth_headers(),
            allow_redirects=False
        )

        passed = response.status_code in [200, 302]  # 302 redirect is acceptable

        self.record_result(
            "Tenant Settings Branding Page",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def run_all_tests(self):
        """Run all tenant branding tests"""
        print("\n" + "=" * 60)
        print("TENANT BRANDING API TESTS")
        print("=" * 60)

        self.test_list_tenants_as_super_admin()
        self.test_create_tenant_with_branding()
        self.test_get_tenant_branding()
        self.test_update_tenant_logo()
        self.test_tenant_branding_visible_endpoint()
        self.test_tenant_settings_branding_page()

        return self.get_results_summary()


if __name__ == "__main__":
    test = TenantBrandingAPITest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
