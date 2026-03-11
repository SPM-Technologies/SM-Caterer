"""
Navigation Frontend Tests
Tests all main navigation links and page loads
"""
from base_frontend_test import BaseFrontendTest
from selenium.webdriver.common.by import By
import time
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import WebRoutes


class NavigationTest(BaseFrontendTest):
    """Navigation Tests - Tests all page navigation and loading"""

    def test_dashboard_loads(self):
        """Test dashboard page loads"""
        if not self.login():
            self.record_result("Dashboard Loads", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.DASHBOARD)
            time.sleep(2)

            # Check for dashboard elements
            passed = "dashboard" in self.driver.current_url.lower() or \
                    self.find_element_safe(By.CSS_SELECTOR, ".dashboard, .card, .widget") is not None

            self.record_result(
                "Dashboard Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Dashboard Loads", False, str(e))
            return False

    def test_customers_page_loads(self):
        """Test customers list page loads"""
        if not self.login():
            self.record_result("Customers Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.CUSTOMERS_LIST)
            time.sleep(2)

            passed = "customer" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Customers Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Customers Page Loads", False, str(e))
            return False

    def test_orders_page_loads(self):
        """Test orders list page loads"""
        if not self.login():
            self.record_result("Orders Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.ORDERS_LIST)
            time.sleep(2)

            passed = "order" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Orders Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Orders Page Loads", False, str(e))
            return False

    def test_payments_page_loads(self):
        """Test payments list page loads"""
        if not self.login():
            self.record_result("Payments Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.PAYMENTS_LIST)
            time.sleep(2)

            passed = "payment" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Payments Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Payments Page Loads", False, str(e))
            return False

    def test_units_page_loads(self):
        """Test units master page loads"""
        if not self.login():
            self.record_result("Units Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.UNITS_LIST)
            time.sleep(2)

            passed = "unit" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Units Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Units Page Loads", False, str(e))
            return False

    def test_materials_page_loads(self):
        """Test materials master page loads"""
        if not self.login():
            self.record_result("Materials Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.MATERIALS_LIST)
            time.sleep(2)

            passed = "material" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Materials Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Materials Page Loads", False, str(e))
            return False

    def test_menus_page_loads(self):
        """Test menus master page loads"""
        if not self.login():
            self.record_result("Menus Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.MENUS_LIST)
            time.sleep(2)

            passed = "menu" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Menus Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Menus Page Loads", False, str(e))
            return False

    def test_event_types_page_loads(self):
        """Test event types master page loads"""
        if not self.login():
            self.record_result("Event Types Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.EVENT_TYPES_LIST)
            time.sleep(2)

            passed = "event" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Event Types Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Event Types Page Loads", False, str(e))
            return False

    def test_recipes_page_loads(self):
        """Test recipes page loads"""
        if not self.login():
            self.record_result("Recipes Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.RECIPES_LIST)
            time.sleep(2)

            passed = "recipe" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Recipes Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Recipes Page Loads", False, str(e))
            return False

    def test_reports_page_loads(self):
        """Test reports index page loads"""
        if not self.login():
            self.record_result("Reports Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.REPORTS)
            time.sleep(2)

            passed = "report" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Reports Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Reports Page Loads", False, str(e))
            return False

    def test_profile_page_loads(self):
        """Test profile page loads"""
        if not self.login():
            self.record_result("Profile Page", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.PROFILE)
            time.sleep(2)

            passed = "profile" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Profile Page Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Profile Page Loads", False, str(e))
            return False

    def test_sidebar_navigation(self):
        """Test sidebar navigation links are present"""
        if not self.login():
            self.record_result("Sidebar Navigation", False, "Login failed")
            return False

        try:
            self.navigate_to(WebRoutes.DASHBOARD)
            time.sleep(2)

            # Look for sidebar navigation
            sidebar = self.find_element_safe(By.CSS_SELECTOR, ".sidebar, nav, .nav-menu")

            nav_links = self.find_elements_safe(By.CSS_SELECTOR, "a[href*='/']")

            passed = len(nav_links) > 0

            self.record_result(
                "Sidebar Navigation",
                passed,
                f"Found {len(nav_links)} navigation links",
                {"link_count": len(nav_links)}
            )
            return passed

        except Exception as e:
            self.record_result("Sidebar Navigation", False, str(e))
            return False

    def run_all_tests(self):
        """Run all navigation tests"""
        print("\n" + "=" * 60)
        print("NAVIGATION FRONTEND TESTS")
        print("=" * 60)

        try:
            self.setup_driver()

            self.test_dashboard_loads()
            self.test_customers_page_loads()
            self.test_orders_page_loads()
            self.test_payments_page_loads()
            self.test_units_page_loads()
            self.test_materials_page_loads()
            self.test_menus_page_loads()
            self.test_event_types_page_loads()
            self.test_recipes_page_loads()
            self.test_reports_page_loads()
            self.test_profile_page_loads()
            self.test_sidebar_navigation()

        finally:
            self.teardown_driver()

        return self.get_results_summary()


if __name__ == "__main__":
    test = NavigationTest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
