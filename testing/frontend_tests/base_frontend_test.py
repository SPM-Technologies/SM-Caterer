"""
Base Frontend Test class with common utilities
Uses Selenium for browser automation
"""
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.firefox.service import Service as FirefoxService
from selenium.webdriver.edge.service import Service as EdgeService
from webdriver_manager.chrome import ChromeDriverManager
from webdriver_manager.firefox import GeckoDriverManager
from webdriver_manager.microsoft import EdgeChromiumDriverManager
from selenium.common.exceptions import TimeoutException, NoSuchElementException
import time
import os
import sys
from datetime import datetime

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import (
    BASE_URL, TENANT_ADMIN_USERNAME, TENANT_ADMIN_PASSWORD,
    SUPER_ADMIN_USERNAME, SUPER_ADMIN_PASSWORD,
    DEFAULT_TIMEOUT, PAGE_LOAD_TIMEOUT, ELEMENT_WAIT_TIMEOUT,
    HEADLESS_MODE, BROWSER, SCREENSHOT_DIR, WebRoutes
)


class BaseFrontendTest:
    """Base class for frontend tests with common functionality"""

    def __init__(self):
        self.driver = None
        self.base_url = BASE_URL
        self.test_results = []
        self.screenshots = []

    def setup_driver(self):
        """Initialize the WebDriver"""
        if BROWSER.lower() == "firefox":
            options = webdriver.FirefoxOptions()
            if HEADLESS_MODE:
                options.add_argument("--headless")
            self.driver = webdriver.Firefox(
                service=FirefoxService(GeckoDriverManager().install()),
                options=options
            )
        elif BROWSER.lower() == "edge":
            options = webdriver.EdgeOptions()
            if HEADLESS_MODE:
                options.add_argument("--headless")
            self.driver = webdriver.Edge(
                service=EdgeService(EdgeChromiumDriverManager().install()),
                options=options
            )
        else:  # Default to Chrome
            options = webdriver.ChromeOptions()
            if HEADLESS_MODE:
                options.add_argument("--headless")
            options.add_argument("--no-sandbox")
            options.add_argument("--disable-dev-shm-usage")
            options.add_argument("--window-size=1920,1080")
            self.driver = webdriver.Chrome(
                service=ChromeService(ChromeDriverManager().install()),
                options=options
            )

        self.driver.set_page_load_timeout(PAGE_LOAD_TIMEOUT)
        self.driver.implicitly_wait(ELEMENT_WAIT_TIMEOUT)

    def teardown_driver(self):
        """Close the WebDriver"""
        if self.driver:
            self.driver.quit()

    def navigate_to(self, path: str):
        """Navigate to a path"""
        url = f"{self.base_url}{path}"
        self.driver.get(url)

    def wait_for_element(self, by: By, value: str, timeout: int = ELEMENT_WAIT_TIMEOUT):
        """Wait for an element to be present"""
        try:
            element = WebDriverWait(self.driver, timeout).until(
                EC.presence_of_element_located((by, value))
            )
            return element
        except TimeoutException:
            return None

    def wait_for_clickable(self, by: By, value: str, timeout: int = ELEMENT_WAIT_TIMEOUT):
        """Wait for an element to be clickable"""
        try:
            element = WebDriverWait(self.driver, timeout).until(
                EC.element_to_be_clickable((by, value))
            )
            return element
        except TimeoutException:
            return None

    def wait_for_url_contains(self, text: str, timeout: int = ELEMENT_WAIT_TIMEOUT):
        """Wait for URL to contain specific text"""
        try:
            WebDriverWait(self.driver, timeout).until(
                EC.url_contains(text)
            )
            return True
        except TimeoutException:
            return False

    def find_element_safe(self, by: By, value: str):
        """Safely find an element, return None if not found"""
        try:
            return self.driver.find_element(by, value)
        except NoSuchElementException:
            return None

    def find_elements_safe(self, by: By, value: str):
        """Safely find elements, return empty list if not found"""
        try:
            return self.driver.find_elements(by, value)
        except NoSuchElementException:
            return []

    def login(self, username: str = None, password: str = None) -> bool:
        """Login through the web interface"""
        username = username or TENANT_ADMIN_USERNAME
        password = password or TENANT_ADMIN_PASSWORD

        try:
            self.navigate_to(WebRoutes.LOGIN)
            time.sleep(1)

            # Find and fill username field
            username_field = self.wait_for_element(By.NAME, "username")
            if not username_field:
                username_field = self.wait_for_element(By.ID, "username")
            if not username_field:
                return False

            username_field.clear()
            username_field.send_keys(username)

            # Find and fill password field
            password_field = self.find_element_safe(By.NAME, "password")
            if not password_field:
                password_field = self.find_element_safe(By.ID, "password")
            if not password_field:
                return False

            password_field.clear()
            password_field.send_keys(password)

            # Find and click login button
            login_button = self.find_element_safe(By.CSS_SELECTOR, "button[type='submit']")
            if not login_button:
                login_button = self.find_element_safe(By.XPATH, "//button[contains(text(), 'Login')]")
            if not login_button:
                login_button = self.find_element_safe(By.XPATH, "//button[contains(text(), 'Sign')]")

            if login_button:
                login_button.click()
                time.sleep(2)

                # Check if redirected to dashboard
                if "dashboard" in self.driver.current_url.lower():
                    return True
                if "login" not in self.driver.current_url.lower():
                    return True

            return False

        except Exception as e:
            print(f"Login error: {e}")
            return False

    def take_screenshot(self, name: str):
        """Take a screenshot"""
        timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
        filename = f"{name}_{timestamp}.png"
        filepath = os.path.join(SCREENSHOT_DIR, filename)

        try:
            self.driver.save_screenshot(filepath)
            self.screenshots.append(filepath)
            return filepath
        except Exception as e:
            print(f"Screenshot error: {e}")
            return None

    def record_result(self, test_name: str, passed: bool, message: str = "", details: any = None):
        """Record test result"""
        result = {
            "test_name": test_name,
            "passed": passed,
            "message": message,
            "details": details,
            "url": self.driver.current_url if self.driver else None
        }
        self.test_results.append(result)
        status = "PASS" if passed else "FAIL"
        print(f"  [{status}] {test_name}: {message}")

        # Take screenshot on failure
        if not passed and self.driver:
            self.take_screenshot(f"FAIL_{test_name.replace(' ', '_')}")

    def get_results_summary(self):
        """Get summary of test results"""
        total = len(self.test_results)
        passed = sum(1 for r in self.test_results if r["passed"])
        failed = total - passed
        return {
            "total": total,
            "passed": passed,
            "failed": failed,
            "pass_rate": (passed / total * 100) if total > 0 else 0,
            "results": self.test_results,
            "screenshots": self.screenshots
        }

    def is_element_visible(self, by: By, value: str) -> bool:
        """Check if an element is visible"""
        try:
            element = self.driver.find_element(by, value)
            return element.is_displayed()
        except NoSuchElementException:
            return False

    def get_page_title(self) -> str:
        """Get the current page title"""
        return self.driver.title

    def get_current_url(self) -> str:
        """Get the current URL"""
        return self.driver.current_url

    def check_page_loads(self, path: str, expected_element: tuple = None) -> bool:
        """Check if a page loads successfully"""
        try:
            self.navigate_to(path)
            time.sleep(1)

            # Check for error pages
            if "error" in self.driver.title.lower() or "404" in self.driver.title:
                return False

            # Check for expected element if provided
            if expected_element:
                element = self.wait_for_element(expected_element[0], expected_element[1], timeout=5)
                return element is not None

            # Check page loaded (no error status)
            return "error" not in self.driver.current_url.lower()

        except Exception as e:
            print(f"Page load error: {e}")
            return False
