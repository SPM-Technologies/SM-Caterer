"""
Customer API Tests
Tests CRUD operations for customers
"""
from base_api_test import BaseAPITest
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import APIEndpoints


class CustomerAPITest(BaseAPITest):
    """Customer API Tests"""

    def __init__(self):
        super().__init__()
        self.test_customer_id = None

    def test_list_customers(self):
        """Test listing all customers"""
        if not self.login():
            self.record_result("List Customers", False, "Login failed")
            return False

        response = self.get(APIEndpoints.CUSTOMERS)
        passed = response.status_code == 200

        data = response.json() if passed else {}

        self.record_result(
            "List Customers",
            passed,
            f"Status: {response.status_code}",
            {"response_keys": list(data.keys()) if data else []}
        )
        return passed

    def test_list_customers_with_pagination(self):
        """Test listing customers with pagination"""
        if not self.login():
            self.record_result("List Customers - Paginated", False, "Login failed")
            return False

        response = self.get(APIEndpoints.CUSTOMERS, params={"page": 0, "size": 10})
        passed = response.status_code == 200

        data = response.json() if passed else {}

        self.record_result(
            "List Customers - Paginated",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_create_customer(self):
        """Test creating a new customer"""
        if not self.login():
            self.record_result("Create Customer", False, "Login failed")
            return False

        customer_data = {
            "name": "Test Customer API",
            "phone": "9876543210",
            "email": "test.api@example.com",
            "address": "123 Test Street",
            "city": "Test City",
            "state": "Test State",
            "pincode": "400001"
        }

        response = self.post(APIEndpoints.CUSTOMERS, customer_data)
        passed = response.status_code in [200, 201]

        data = response.json() if passed else {}
        if passed and data.get("success"):
            customer = data.get("data", {})
            self.test_customer_id = customer.get("id")

        self.record_result(
            "Create Customer",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_create_customer_validation(self):
        """Test customer creation with missing required fields"""
        if not self.login():
            self.record_result("Create Customer - Validation", False, "Login failed")
            return False

        # Missing required fields
        customer_data = {
            "email": "incomplete@example.com"
        }

        response = self.post(APIEndpoints.CUSTOMERS, customer_data)
        # Should return 400 Bad Request
        passed = response.status_code == 400

        self.record_result(
            "Create Customer - Validation",
            passed,
            f"Status: {response.status_code} (expected 400)",
            {"status_code": response.status_code}
        )
        return passed

    def test_get_customer_by_id(self):
        """Test getting a specific customer"""
        if not self.login():
            self.record_result("Get Customer by ID", False, "Login failed")
            return False

        # First create a customer if we don't have one
        if not self.test_customer_id:
            self.test_create_customer()

        if not self.test_customer_id:
            self.record_result("Get Customer by ID", False, "No customer ID available")
            return False

        response = self.get(f"{APIEndpoints.CUSTOMERS}/{self.test_customer_id}")
        passed = response.status_code == 200

        data = response.json() if passed else {}

        self.record_result(
            "Get Customer by ID",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_update_customer(self):
        """Test updating a customer"""
        if not self.login():
            self.record_result("Update Customer", False, "Login failed")
            return False

        if not self.test_customer_id:
            self.test_create_customer()

        if not self.test_customer_id:
            self.record_result("Update Customer", False, "No customer ID available")
            return False

        update_data = {
            "name": "Updated Test Customer",
            "phone": "9876543211",
            "email": "updated.api@example.com",
            "address": "456 Updated Street",
            "city": "Updated City",
            "state": "Updated State",
            "pincode": "400002"
        }

        response = self.put(f"{APIEndpoints.CUSTOMERS}/{self.test_customer_id}", update_data)
        passed = response.status_code == 200

        data = response.json() if passed else {}

        self.record_result(
            "Update Customer",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_search_customers(self):
        """Test searching customers"""
        if not self.login():
            self.record_result("Search Customers", False, "Login failed")
            return False

        response = self.get(APIEndpoints.CUSTOMERS, params={"search": "test"})
        passed = response.status_code == 200

        data = response.json() if passed else {}

        self.record_result(
            "Search Customers",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_nonexistent_customer(self):
        """Test getting a customer that doesn't exist"""
        if not self.login():
            self.record_result("Get Nonexistent Customer", False, "Login failed")
            return False

        response = self.get(f"{APIEndpoints.CUSTOMERS}/99999999")
        # Should return 404
        passed = response.status_code == 404

        self.record_result(
            "Get Nonexistent Customer",
            passed,
            f"Status: {response.status_code} (expected 404)",
            {"status_code": response.status_code}
        )
        return passed

    def test_delete_customer(self):
        """Test deleting a customer"""
        if not self.login():
            self.record_result("Delete Customer", False, "Login failed")
            return False

        # Create a customer to delete
        customer_data = {
            "name": "Customer To Delete",
            "phone": "9876543299",
            "email": "delete.me@example.com",
            "address": "Delete Street",
            "city": "Delete City",
            "state": "Delete State",
            "pincode": "400099"
        }

        create_response = self.post(APIEndpoints.CUSTOMERS, customer_data)
        if create_response.status_code not in [200, 201]:
            self.record_result("Delete Customer", False, "Failed to create customer for deletion")
            return False

        create_data = create_response.json()
        customer_id = create_data.get("data", {}).get("id")

        if not customer_id:
            self.record_result("Delete Customer", False, "No customer ID returned")
            return False

        response = self.delete(f"{APIEndpoints.CUSTOMERS}/{customer_id}")
        passed = response.status_code in [200, 204]

        self.record_result(
            "Delete Customer",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def run_all_tests(self):
        """Run all customer tests"""
        print("\n" + "=" * 60)
        print("CUSTOMER API TESTS")
        print("=" * 60)

        self.test_list_customers()
        self.test_list_customers_with_pagination()
        self.test_create_customer()
        self.test_create_customer_validation()
        self.test_get_customer_by_id()
        self.test_update_customer()
        self.test_search_customers()
        self.test_get_nonexistent_customer()
        self.test_delete_customer()

        return self.get_results_summary()


if __name__ == "__main__":
    test = CustomerAPITest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
