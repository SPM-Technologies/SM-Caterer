"""
Order API Tests
Tests CRUD operations for orders
"""
from base_api_test import BaseAPITest
from datetime import datetime, timedelta
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import APIEndpoints


class OrderAPITest(BaseAPITest):
    """Order API Tests"""

    def __init__(self):
        super().__init__()
        self.test_order_id = None
        self.test_customer_id = None

    def setup_test_data(self):
        """Create test data needed for order tests"""
        if not self.login():
            return False

        # Create a test customer
        customer_data = {
            "name": "Order Test Customer",
            "phone": "9876543100",
            "email": "order.test@example.com",
            "address": "Order Test Street",
            "city": "Order City",
            "state": "Order State",
            "pincode": "400100"
        }

        response = self.post(APIEndpoints.CUSTOMERS, customer_data)
        if response.status_code in [200, 201]:
            data = response.json()
            self.test_customer_id = data.get("data", {}).get("id")
            return True
        return False

    def test_list_orders(self):
        """Test listing all orders"""
        if not self.login():
            self.record_result("List Orders", False, "Login failed")
            return False

        response = self.get(APIEndpoints.ORDERS)
        passed = response.status_code == 200

        self.record_result(
            "List Orders",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_list_orders_with_filters(self):
        """Test listing orders with status filter"""
        if not self.login():
            self.record_result("List Orders - Filtered", False, "Login failed")
            return False

        response = self.get(APIEndpoints.ORDERS, params={"status": "PENDING"})
        passed = response.status_code == 200

        self.record_result(
            "List Orders - Filtered",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_create_order(self):
        """Test creating a new order"""
        if not self.test_customer_id:
            self.setup_test_data()

        if not self.test_customer_id:
            self.record_result("Create Order", False, "No customer ID available")
            return False

        event_date = (datetime.now() + timedelta(days=7)).strftime("%Y-%m-%d")

        order_data = {
            "customerId": self.test_customer_id,
            "eventDate": event_date,
            "eventTime": "12:00",
            "eventTypeId": None,  # Will need to be set to a valid event type
            "guestCount": 100,
            "venueAddress": "Test Venue Address",
            "venueName": "Test Venue",
            "notes": "Test order created via API test"
        }

        response = self.post(APIEndpoints.ORDERS, order_data)
        passed = response.status_code in [200, 201]

        data = response.json() if passed else {}
        if passed and data.get("success"):
            self.test_order_id = data.get("data", {}).get("id")

        self.record_result(
            "Create Order",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_order_by_id(self):
        """Test getting an order by ID"""
        if not self.login():
            self.record_result("Get Order by ID", False, "Login failed")
            return False

        if not self.test_order_id:
            self.test_create_order()

        if not self.test_order_id:
            # Try to get any existing order
            response = self.get(APIEndpoints.ORDERS, params={"size": 1})
            if response.status_code == 200:
                data = response.json()
                content = data.get("data", {}).get("content", [])
                if content:
                    self.test_order_id = content[0].get("id")

        if not self.test_order_id:
            self.record_result("Get Order by ID", False, "No order ID available")
            return False

        response = self.get(f"{APIEndpoints.ORDERS}/{self.test_order_id}")
        passed = response.status_code == 200

        self.record_result(
            "Get Order by ID",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_get_nonexistent_order(self):
        """Test getting an order that doesn't exist"""
        if not self.login():
            self.record_result("Get Nonexistent Order", False, "Login failed")
            return False

        response = self.get(f"{APIEndpoints.ORDERS}/99999999")
        passed = response.status_code == 404

        self.record_result(
            "Get Nonexistent Order",
            passed,
            f"Status: {response.status_code} (expected 404)",
            {"status_code": response.status_code}
        )
        return passed

    def test_update_order(self):
        """Test updating an order"""
        if not self.login():
            self.record_result("Update Order", False, "Login failed")
            return False

        if not self.test_order_id:
            self.test_create_order()

        if not self.test_order_id:
            self.record_result("Update Order", False, "No order ID available")
            return False

        update_data = {
            "guestCount": 150,
            "notes": "Updated test order"
        }

        response = self.put(f"{APIEndpoints.ORDERS}/{self.test_order_id}", update_data)
        passed = response.status_code == 200

        self.record_result(
            "Update Order",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_order_status_update(self):
        """Test updating order status"""
        if not self.login():
            self.record_result("Update Order Status", False, "Login failed")
            return False

        if not self.test_order_id:
            self.test_create_order()

        if not self.test_order_id:
            self.record_result("Update Order Status", False, "No order ID available")
            return False

        # Try to update status to CONFIRMED
        response = self.patch(
            f"{APIEndpoints.ORDERS}/{self.test_order_id}/status",
            {"status": "CONFIRMED"}
        )
        passed = response.status_code == 200

        self.record_result(
            "Update Order Status",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_order_menu_items(self):
        """Test getting order menu items"""
        if not self.login():
            self.record_result("Get Order Menu Items", False, "Login failed")
            return False

        if not self.test_order_id:
            # Get first available order
            response = self.get(APIEndpoints.ORDERS, params={"size": 1})
            if response.status_code == 200:
                data = response.json()
                content = data.get("data", {}).get("content", [])
                if content:
                    self.test_order_id = content[0].get("id")

        if not self.test_order_id:
            self.record_result("Get Order Menu Items", False, "No order ID available")
            return False

        response = self.get(f"{APIEndpoints.ORDERS}/{self.test_order_id}/menu-items")
        passed = response.status_code == 200

        self.record_result(
            "Get Order Menu Items",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def run_all_tests(self):
        """Run all order tests"""
        print("\n" + "=" * 60)
        print("ORDER API TESTS")
        print("=" * 60)

        self.setup_test_data()
        self.test_list_orders()
        self.test_list_orders_with_filters()
        self.test_create_order()
        self.test_get_order_by_id()
        self.test_get_nonexistent_order()
        self.test_update_order()
        self.test_order_status_update()
        self.test_order_menu_items()

        return self.get_results_summary()


if __name__ == "__main__":
    test = OrderAPITest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
