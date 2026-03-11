"""
Base API Test class with common utilities
"""
import requests
import json
from typing import Optional, Dict, Any
import sys
import os

# Add parent directory to path
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import API_BASE_URL, TENANT_ADMIN_USERNAME, TENANT_ADMIN_PASSWORD, APIEndpoints


class BaseAPITest:
    """Base class for API tests with common functionality"""

    def __init__(self):
        self.base_url = API_BASE_URL
        self.session = requests.Session()
        self.access_token = None
        self.refresh_token = None
        self.current_user = None
        self.test_results = []

    def login(self, username: str = None, password: str = None) -> bool:
        """Login and get JWT tokens"""
        username = username or TENANT_ADMIN_USERNAME
        password = password or TENANT_ADMIN_PASSWORD

        try:
            response = self.session.post(
                f"{self.base_url}{APIEndpoints.LOGIN}",
                json={"username": username, "password": password},
                headers={"Content-Type": "application/json"}
            )

            if response.status_code == 200:
                data = response.json()
                if data.get("success"):
                    auth_data = data.get("data", {})
                    self.access_token = auth_data.get("accessToken")
                    self.refresh_token = auth_data.get("refreshToken")
                    self.current_user = auth_data.get("user")
                    return True
            return False
        except Exception as e:
            print(f"Login error: {e}")
            return False

    def get_auth_headers(self) -> Dict[str, str]:
        """Get headers with authentication token"""
        headers = {"Content-Type": "application/json"}
        if self.access_token:
            headers["Authorization"] = f"Bearer {self.access_token}"
        return headers

    def get(self, endpoint: str, params: Optional[Dict] = None) -> requests.Response:
        """Make GET request"""
        return self.session.get(
            f"{self.base_url}{endpoint}",
            headers=self.get_auth_headers(),
            params=params
        )

    def post(self, endpoint: str, data: Optional[Dict] = None) -> requests.Response:
        """Make POST request"""
        return self.session.post(
            f"{self.base_url}{endpoint}",
            headers=self.get_auth_headers(),
            json=data
        )

    def put(self, endpoint: str, data: Optional[Dict] = None) -> requests.Response:
        """Make PUT request"""
        return self.session.put(
            f"{self.base_url}{endpoint}",
            headers=self.get_auth_headers(),
            json=data
        )

    def patch(self, endpoint: str, data: Optional[Dict] = None) -> requests.Response:
        """Make PATCH request"""
        return self.session.patch(
            f"{self.base_url}{endpoint}",
            headers=self.get_auth_headers(),
            json=data
        )

    def delete(self, endpoint: str) -> requests.Response:
        """Make DELETE request"""
        return self.session.delete(
            f"{self.base_url}{endpoint}",
            headers=self.get_auth_headers()
        )

    def record_result(self, test_name: str, passed: bool, message: str = "", details: Any = None):
        """Record test result"""
        result = {
            "test_name": test_name,
            "passed": passed,
            "message": message,
            "details": details
        }
        self.test_results.append(result)
        status = "PASS" if passed else "FAIL"
        print(f"  [{status}] {test_name}: {message}")

    def get_results_summary(self) -> Dict:
        """Get summary of test results"""
        total = len(self.test_results)
        passed = sum(1 for r in self.test_results if r["passed"])
        failed = total - passed
        return {
            "total": total,
            "passed": passed,
            "failed": failed,
            "pass_rate": (passed / total * 100) if total > 0 else 0,
            "results": self.test_results
        }
