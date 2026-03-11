"""
Login Page Frontend Tests
Tests login functionality through the web interface
"""
from base_frontend_test import BaseFrontendTest
from selenium.webdriver.common.by import By
import time
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import (
    TENANT_ADMIN_USERNAME, TENANT_ADMIN_PASSWORD,
    SUPER_ADMIN_USERNAME, SUPER_ADMIN_PASSWORD,
    WebRoutes
)


class LoginPageTest(BaseFrontendTest):
    """Login Page Tests"""

    def test_login_page_loads(self):
        """Test that login page loads correctly"""
        try:
            self.navigate_to(WebRoutes.LOGIN)
            time.sleep(1)

            # Check for login form elements
            username_field = self.find_element_safe(By.NAME, "username") or \
                           self.find_element_safe(By.ID, "username")
            password_field = self.find_element_safe(By.NAME, "password") or \
                           self.find_element_safe(By.ID, "password")
            login_button = self.find_element_safe(By.CSS_SELECTOR, "button[type='submit']")

            passed = username_field is not None and password_field is not None

            self.record_result(
                "Login Page Loads",
                passed,
                "All form elements found" if passed else "Missing form elements",
                {
                    "username_field": username_field is not None,
                    "password_field": password_field is not None,
                    "login_button": login_button is not None
                }
            )
            return passed

        except Exception as e:
            self.record_result("Login Page Loads", False, str(e))
            return False

    def test_login_valid_tenant_admin(self):
        """Test login with valid tenant admin credentials"""
        try:
            passed = self.login(TENANT_ADMIN_USERNAME, TENANT_ADMIN_PASSWORD)
            time.sleep(1)

            # Verify redirect to dashboard
            if passed:
                passed = "dashboard" in self.driver.current_url.lower() or \
                        "login" not in self.driver.current_url.lower()

            self.record_result(
                "Login - Valid Tenant Admin",
                passed,
                f"Redirected to: {self.driver.current_url}",
                {"final_url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Login - Valid Tenant Admin", False, str(e))
            return False

    def test_login_valid_super_admin(self):
        """Test login with valid super admin credentials"""
        try:
            passed = self.login(SUPER_ADMIN_USERNAME, SUPER_ADMIN_PASSWORD)
            time.sleep(1)

            self.record_result(
                "Login - Valid Super Admin",
                passed,
                f"Redirected to: {self.driver.current_url}",
                {"final_url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Login - Valid Super Admin", False, str(e))
            return False

    def test_login_invalid_credentials(self):
        """Test login with invalid credentials"""
        try:
            self.navigate_to(WebRoutes.LOGIN)
            time.sleep(1)

            username_field = self.find_element_safe(By.NAME, "username") or \
                           self.find_element_safe(By.ID, "username")
            password_field = self.find_element_safe(By.NAME, "password") or \
                           self.find_element_safe(By.ID, "password")

            username_field.clear()
            username_field.send_keys("invalid_user")
            password_field.clear()
            password_field.send_keys("wrong_password")

            login_button = self.find_element_safe(By.CSS_SELECTOR, "button[type='submit']")
            login_button.click()
            time.sleep(2)

            # Should remain on login page or show error
            still_on_login = "login" in self.driver.current_url.lower()
            error_message = self.find_element_safe(By.CSS_SELECTOR, ".alert-danger, .error, .text-danger")

            passed = still_on_login or error_message is not None

            self.record_result(
                "Login - Invalid Credentials",
                passed,
                "Correctly rejected invalid login" if passed else "Unexpected behavior",
                {
                    "still_on_login": still_on_login,
                    "error_shown": error_message is not None
                }
            )
            return passed

        except Exception as e:
            self.record_result("Login - Invalid Credentials", False, str(e))
            return False

    def test_login_empty_fields(self):
        """Test login with empty fields"""
        try:
            self.navigate_to(WebRoutes.LOGIN)
            time.sleep(1)

            login_button = self.find_element_safe(By.CSS_SELECTOR, "button[type='submit']")
            login_button.click()
            time.sleep(1)

            # Should show validation or remain on login
            still_on_login = "login" in self.driver.current_url.lower()

            self.record_result(
                "Login - Empty Fields",
                still_on_login,
                "Form validation working" if still_on_login else "Unexpected submission",
                {"still_on_login": still_on_login}
            )
            return still_on_login

        except Exception as e:
            self.record_result("Login - Empty Fields", False, str(e))
            return False

    def test_remember_me_checkbox(self):
        """Test remember me checkbox exists"""
        try:
            self.navigate_to(WebRoutes.LOGIN)
            time.sleep(1)

            remember_me = self.find_element_safe(By.NAME, "remember-me") or \
                         self.find_element_safe(By.ID, "remember-me") or \
                         self.find_element_safe(By.CSS_SELECTOR, "input[type='checkbox']")

            passed = remember_me is not None

            self.record_result(
                "Remember Me Checkbox",
                passed,
                "Checkbox found" if passed else "Checkbox not found",
                {"found": passed}
            )
            return passed

        except Exception as e:
            self.record_result("Remember Me Checkbox", False, str(e))
            return False

    def test_logout(self):
        """Test logout functionality"""
        try:
            # First login
            if not self.login():
                self.record_result("Logout", False, "Login failed")
                return False

            time.sleep(1)

            # Try to find logout link/button
            logout_link = self.find_element_safe(By.CSS_SELECTOR, "a[href*='logout']") or \
                         self.find_element_safe(By.XPATH, "//a[contains(text(), 'Logout')]") or \
                         self.find_element_safe(By.XPATH, "//button[contains(text(), 'Logout')]")

            if logout_link:
                logout_link.click()
                time.sleep(2)

                # Should redirect to login page
                passed = "login" in self.driver.current_url.lower()

                self.record_result(
                    "Logout",
                    passed,
                    f"Redirected to: {self.driver.current_url}",
                    {"final_url": self.driver.current_url}
                )
                return passed
            else:
                self.record_result("Logout", False, "Logout link not found")
                return False

        except Exception as e:
            self.record_result("Logout", False, str(e))
            return False

    def run_all_tests(self):
        """Run all login page tests"""
        print("\n" + "=" * 60)
        print("LOGIN PAGE FRONTEND TESTS")
        print("=" * 60)

        try:
            self.setup_driver()

            self.test_login_page_loads()
            self.test_login_valid_tenant_admin()

            # Re-navigate to login for next test
            self.navigate_to(WebRoutes.LOGOUT)
            time.sleep(1)

            self.test_login_valid_super_admin()

            self.navigate_to(WebRoutes.LOGOUT)
            time.sleep(1)

            self.test_login_invalid_credentials()
            self.test_login_empty_fields()
            self.test_remember_me_checkbox()
            self.test_logout()

        finally:
            self.teardown_driver()

        return self.get_results_summary()


if __name__ == "__main__":
    test = LoginPageTest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
