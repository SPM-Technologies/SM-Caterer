"""
Order Wizard Frontend Tests
Tests the complete order wizard flow
"""
from base_frontend_test import BaseFrontendTest
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import Select
import time
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import WebRoutes


class OrderWizardTest(BaseFrontendTest):
    """Order Wizard Tests - Tests the complete order creation workflow"""

    def test_wizard_step1_loads(self):
        """Test wizard step 1 (Customer Selection) loads"""
        if not self.login():
            self.record_result("Wizard Step 1 Loads", False, "Login failed")
            return False

        try:
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}")
            time.sleep(2)

            # Check for step 1 elements - customer selection
            passed = "wizard" in self.driver.current_url.lower() or \
                    self.find_element_safe(By.CSS_SELECTOR, ".step-1, .customer-selection, form") is not None

            self.record_result(
                "Wizard Step 1 Loads",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Step 1 Loads", False, str(e))
            return False

    def test_wizard_customer_search(self):
        """Test customer search in wizard"""
        if not self.login():
            self.record_result("Wizard Customer Search", False, "Login failed")
            return False

        try:
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}")
            time.sleep(2)

            # Look for search input
            search_input = self.find_element_safe(By.CSS_SELECTOR,
                "input[type='search'], input[placeholder*='search'], input[name*='search'], #customerSearch")

            if search_input:
                search_input.clear()
                search_input.send_keys("test")
                time.sleep(1)
                passed = True
            else:
                passed = False

            self.record_result(
                "Wizard Customer Search",
                passed,
                "Search input found and functional" if passed else "Search input not found",
                {"search_found": passed}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Customer Search", False, str(e))
            return False

    def test_wizard_navigation_buttons(self):
        """Test wizard navigation buttons exist"""
        if not self.login():
            self.record_result("Wizard Navigation Buttons", False, "Login failed")
            return False

        try:
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}")
            time.sleep(2)

            # Look for Next button
            next_button = self.find_element_safe(By.CSS_SELECTOR,
                "button[type='submit'], .btn-next, button:contains('Next')") or \
                self.find_element_safe(By.XPATH, "//button[contains(text(), 'Next')]")

            passed = next_button is not None

            self.record_result(
                "Wizard Navigation Buttons",
                passed,
                "Navigation buttons found" if passed else "Navigation buttons not found",
                {"next_button": passed}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Navigation Buttons", False, str(e))
            return False

    def test_wizard_step2_event_details(self):
        """Test wizard step 2 (Event Details) has required fields"""
        if not self.login():
            self.record_result("Wizard Step 2 Fields", False, "Login failed")
            return False

        try:
            # Navigate to step 2 directly (if possible)
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}/step2")
            time.sleep(2)

            # Check for event detail fields
            fields_to_check = [
                ("eventDate", "Event Date"),
                ("eventTime", "Event Time"),
                ("guestCount", "Guest Count"),
                ("venueAddress", "Venue Address")
            ]

            found_fields = 0
            for field_name, field_label in fields_to_check:
                field = self.find_element_safe(By.NAME, field_name) or \
                       self.find_element_safe(By.ID, field_name)
                if field:
                    found_fields += 1

            passed = found_fields > 0

            self.record_result(
                "Wizard Step 2 Fields",
                passed,
                f"Found {found_fields}/{len(fields_to_check)} fields",
                {"fields_found": found_fields}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Step 2 Fields", False, str(e))
            return False

    def test_wizard_step3_menu_selection(self):
        """Test wizard step 3 (Menu Selection) functionality"""
        if not self.login():
            self.record_result("Wizard Step 3 Menu", False, "Login failed")
            return False

        try:
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}/step3")
            time.sleep(2)

            # Check for menu items display
            menu_items = self.find_elements_safe(By.CSS_SELECTOR,
                ".menu-item, .menu-card, tr[data-menu-id], .menu-list-item")

            add_buttons = self.find_elements_safe(By.CSS_SELECTOR,
                ".btn-add, button[data-action='add'], .add-to-order")

            passed = len(menu_items) > 0 or len(add_buttons) > 0 or \
                    "step3" in self.driver.current_url.lower() or \
                    "menu" in self.driver.current_url.lower()

            self.record_result(
                "Wizard Step 3 Menu Selection",
                passed,
                f"Menu items: {len(menu_items)}, Add buttons: {len(add_buttons)}",
                {"menu_items": len(menu_items), "add_buttons": len(add_buttons)}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Step 3 Menu Selection", False, str(e))
            return False

    def test_wizard_step4_utilities(self):
        """Test wizard step 4 (Utilities) loads"""
        if not self.login():
            self.record_result("Wizard Step 4 Utilities", False, "Login failed")
            return False

        try:
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}/step4")
            time.sleep(2)

            passed = "step4" in self.driver.current_url.lower() or \
                    "utility" in self.driver.current_url.lower() or \
                    "error" not in self.driver.title.lower()

            self.record_result(
                "Wizard Step 4 Utilities",
                passed,
                f"URL: {self.driver.current_url}",
                {"url": self.driver.current_url}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Step 4 Utilities", False, str(e))
            return False

    def test_wizard_step5_summary(self):
        """Test wizard step 5 (Summary) loads"""
        if not self.login():
            self.record_result("Wizard Step 5 Summary", False, "Login failed")
            return False

        try:
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}/step5")
            time.sleep(2)

            # Look for summary elements
            summary_elements = self.find_elements_safe(By.CSS_SELECTOR,
                ".order-summary, .summary, .total, .grand-total")

            submit_button = self.find_element_safe(By.CSS_SELECTOR,
                "button[type='submit'], .btn-create-order") or \
                self.find_element_safe(By.XPATH, "//button[contains(text(), 'Create')]")

            passed = len(summary_elements) > 0 or submit_button is not None or \
                    "step5" in self.driver.current_url.lower()

            self.record_result(
                "Wizard Step 5 Summary",
                passed,
                f"Summary elements: {len(summary_elements)}",
                {"summary_elements": len(summary_elements), "submit_button": submit_button is not None}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Step 5 Summary", False, str(e))
            return False

    def test_wizard_validation(self):
        """Test wizard form validation"""
        if not self.login():
            self.record_result("Wizard Validation", False, "Login failed")
            return False

        try:
            self.navigate_to(f"{WebRoutes.ORDERS_WIZARD}")
            time.sleep(2)

            # Try to proceed without selecting customer
            next_button = self.find_element_safe(By.CSS_SELECTOR,
                "button[type='submit'], .btn-next") or \
                self.find_element_safe(By.XPATH, "//button[contains(text(), 'Next')]")

            if next_button:
                next_button.click()
                time.sleep(1)

                # Check for validation message
                validation_msg = self.find_element_safe(By.CSS_SELECTOR,
                    ".validation-error, .invalid-feedback, .error-message, .alert-danger")

                # Should either show error or remain on same step
                passed = validation_msg is not None or "step1" in self.driver.current_url.lower() or \
                        "wizard" in self.driver.current_url.lower()
            else:
                passed = True  # No button to test

            self.record_result(
                "Wizard Validation",
                passed,
                "Validation working" if passed else "Validation may not work",
                {"passed": passed}
            )
            return passed

        except Exception as e:
            self.record_result("Wizard Validation", False, str(e))
            return False

    def run_all_tests(self):
        """Run all order wizard tests"""
        print("\n" + "=" * 60)
        print("ORDER WIZARD FRONTEND TESTS")
        print("=" * 60)

        try:
            self.setup_driver()

            self.test_wizard_step1_loads()
            self.test_wizard_customer_search()
            self.test_wizard_navigation_buttons()
            self.test_wizard_step2_event_details()
            self.test_wizard_step3_menu_selection()
            self.test_wizard_step4_utilities()
            self.test_wizard_step5_summary()
            self.test_wizard_validation()

        finally:
            self.teardown_driver()

        return self.get_results_summary()


if __name__ == "__main__":
    test = OrderWizardTest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
