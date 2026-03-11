"""
Authentication API Tests
Tests login, logout, token refresh, and user info endpoints
"""
from base_api_test import BaseAPITest
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import (
    APIEndpoints, SUPER_ADMIN_USERNAME, SUPER_ADMIN_PASSWORD,
    TENANT_ADMIN_USERNAME, TENANT_ADMIN_PASSWORD
)


class AuthAPITest(BaseAPITest):
    """Authentication API Tests"""

    def test_login_valid_tenant_admin(self):
        """Test login with valid tenant admin credentials"""
        response = self.session.post(
            f"{self.base_url}{APIEndpoints.LOGIN}",
            json={"username": TENANT_ADMIN_USERNAME, "password": TENANT_ADMIN_PASSWORD},
            headers={"Content-Type": "application/json"}
        )

        passed = response.status_code == 200
        data = response.json() if response.status_code == 200 else {}

        if passed:
            passed = data.get("success", False) and "accessToken" in data.get("data", {})

        self.record_result(
            "Login - Valid Tenant Admin",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_login_valid_super_admin(self):
        """Test login with valid super admin credentials"""
        response = self.session.post(
            f"{self.base_url}{APIEndpoints.LOGIN}",
            json={"username": SUPER_ADMIN_USERNAME, "password": SUPER_ADMIN_PASSWORD},
            headers={"Content-Type": "application/json"}
        )

        passed = response.status_code == 200
        data = response.json() if response.status_code == 200 else {}

        if passed:
            passed = data.get("success", False) and "accessToken" in data.get("data", {})

        self.record_result(
            "Login - Valid Super Admin",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_login_invalid_credentials(self):
        """Test login with invalid credentials"""
        response = self.session.post(
            f"{self.base_url}{APIEndpoints.LOGIN}",
            json={"username": "invalid_user", "password": "wrong_password"},
            headers={"Content-Type": "application/json"}
        )

        # Should return 401 Unauthorized
        passed = response.status_code == 401

        self.record_result(
            "Login - Invalid Credentials",
            passed,
            f"Status: {response.status_code} (expected 401)",
            {"status_code": response.status_code}
        )
        return passed

    def test_login_empty_credentials(self):
        """Test login with empty credentials"""
        response = self.session.post(
            f"{self.base_url}{APIEndpoints.LOGIN}",
            json={"username": "", "password": ""},
            headers={"Content-Type": "application/json"}
        )

        # Should return 400 Bad Request
        passed = response.status_code == 400

        self.record_result(
            "Login - Empty Credentials",
            passed,
            f"Status: {response.status_code} (expected 400)",
            {"status_code": response.status_code}
        )
        return passed

    def test_get_current_user(self):
        """Test getting current user info after login"""
        if not self.login():
            self.record_result("Get Current User", False, "Login failed")
            return False

        response = self.get(APIEndpoints.ME)
        passed = response.status_code == 200

        data = response.json() if passed else {}
        if passed:
            passed = data.get("success", False) and "username" in data.get("data", {})

        self.record_result(
            "Get Current User",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_user_without_auth(self):
        """Test getting user info without authentication"""
        # Don't login - use a fresh session
        fresh_session = BaseAPITest()
        response = fresh_session.get(APIEndpoints.ME)

        # Should return 401 or 403
        passed = response.status_code in [401, 403]

        self.record_result(
            "Get User Without Auth",
            passed,
            f"Status: {response.status_code} (expected 401 or 403)",
            {"status_code": response.status_code}
        )
        return passed

    def test_token_refresh(self):
        """Test refreshing access token"""
        if not self.login():
            self.record_result("Token Refresh", False, "Login failed")
            return False

        response = self.session.post(
            f"{self.base_url}{APIEndpoints.REFRESH}",
            json={"refreshToken": self.refresh_token},
            headers={"Content-Type": "application/json"}
        )

        passed = response.status_code == 200
        data = response.json() if passed else {}

        if passed:
            passed = data.get("success", False) and "accessToken" in data.get("data", {})

        self.record_result(
            "Token Refresh",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_token_refresh_invalid(self):
        """Test refreshing with invalid token"""
        response = self.session.post(
            f"{self.base_url}{APIEndpoints.REFRESH}",
            json={"refreshToken": "invalid_token_here"},
            headers={"Content-Type": "application/json"}
        )

        # Should return 401 Unauthorized
        passed = response.status_code == 401

        self.record_result(
            "Token Refresh - Invalid Token",
            passed,
            f"Status: {response.status_code} (expected 401)",
            {"status_code": response.status_code}
        )
        return passed

    def test_logout(self):
        """Test logout endpoint"""
        if not self.login():
            self.record_result("Logout", False, "Login failed")
            return False

        response = self.post(APIEndpoints.LOGOUT)
        passed = response.status_code == 200

        self.record_result(
            "Logout",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def run_all_tests(self):
        """Run all authentication tests"""
        print("\n" + "=" * 60)
        print("AUTHENTICATION API TESTS")
        print("=" * 60)

        self.test_login_valid_tenant_admin()
        self.test_login_valid_super_admin()
        self.test_login_invalid_credentials()
        self.test_login_empty_credentials()
        self.test_get_current_user()
        self.test_get_user_without_auth()
        self.test_token_refresh()
        self.test_token_refresh_invalid()
        self.test_logout()

        return self.get_results_summary()


if __name__ == "__main__":
    test = AuthAPITest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
