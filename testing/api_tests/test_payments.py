"""
Payment API Tests
Tests CRUD operations for payments
"""
from base_api_test import BaseAPITest
from datetime import datetime
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import APIEndpoints


class PaymentAPITest(BaseAPITest):
    """Payment API Tests"""

    def __init__(self):
        super().__init__()
        self.test_payment_id = None
        self.test_order_id = None

    def get_test_order(self):
        """Get an order ID for payment testing"""
        if self.test_order_id:
            return self.test_order_id

        response = self.get(APIEndpoints.ORDERS, params={"size": 1})
        if response.status_code == 200:
            data = response.json()
            content = data.get("data", {}).get("content", [])
            if content:
                self.test_order_id = content[0].get("id")
        return self.test_order_id

    def test_list_payments(self):
        """Test listing all payments"""
        if not self.login():
            self.record_result("List Payments", False, "Login failed")
            return False

        response = self.get(APIEndpoints.PAYMENTS)
        passed = response.status_code == 200

        self.record_result(
            "List Payments",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_list_payments_with_pagination(self):
        """Test listing payments with pagination"""
        if not self.login():
            self.record_result("List Payments - Paginated", False, "Login failed")
            return False

        response = self.get(APIEndpoints.PAYMENTS, params={"page": 0, "size": 10})
        passed = response.status_code == 200

        self.record_result(
            "List Payments - Paginated",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_create_payment(self):
        """Test creating a new payment"""
        if not self.login():
            self.record_result("Create Payment", False, "Login failed")
            return False

        order_id = self.get_test_order()
        if not order_id:
            self.record_result("Create Payment", False, "No order available for payment")
            return False

        payment_data = {
            "orderId": order_id,
            "amount": 1000.00,
            "paymentMethod": "CASH",
            "paymentDate": datetime.now().strftime("%Y-%m-%d"),
            "notes": "Test payment via API"
        }

        response = self.post(APIEndpoints.PAYMENTS, payment_data)
        passed = response.status_code in [200, 201]

        data = response.json() if passed else {}
        if passed and data.get("success"):
            self.test_payment_id = data.get("data", {}).get("id")

        self.record_result(
            "Create Payment",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_create_payment_validation(self):
        """Test payment creation with invalid data"""
        if not self.login():
            self.record_result("Create Payment - Validation", False, "Login failed")
            return False

        # Invalid - negative amount
        payment_data = {
            "orderId": 1,
            "amount": -100.00,
            "paymentMethod": "CASH"
        }

        response = self.post(APIEndpoints.PAYMENTS, payment_data)
        passed = response.status_code == 400

        self.record_result(
            "Create Payment - Validation",
            passed,
            f"Status: {response.status_code} (expected 400)",
            {"status_code": response.status_code}
        )
        return passed

    def test_get_payment_by_id(self):
        """Test getting a payment by ID"""
        if not self.login():
            self.record_result("Get Payment by ID", False, "Login failed")
            return False

        if not self.test_payment_id:
            self.test_create_payment()

        if not self.test_payment_id:
            # Try to get any existing payment
            response = self.get(APIEndpoints.PAYMENTS, params={"size": 1})
            if response.status_code == 200:
                data = response.json()
                content = data.get("data", {}).get("content", [])
                if content:
                    self.test_payment_id = content[0].get("id")

        if not self.test_payment_id:
            self.record_result("Get Payment by ID", False, "No payment ID available")
            return False

        response = self.get(f"{APIEndpoints.PAYMENTS}/{self.test_payment_id}")
        passed = response.status_code == 200

        self.record_result(
            "Get Payment by ID",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_get_payments_by_order(self):
        """Test getting payments by order ID"""
        if not self.login():
            self.record_result("Get Payments by Order", False, "Login failed")
            return False

        order_id = self.get_test_order()
        if not order_id:
            self.record_result("Get Payments by Order", False, "No order available")
            return False

        response = self.get(f"{APIEndpoints.ORDERS}/{order_id}/payments")
        passed = response.status_code == 200

        self.record_result(
            "Get Payments by Order",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_get_nonexistent_payment(self):
        """Test getting a payment that doesn't exist"""
        if not self.login():
            self.record_result("Get Nonexistent Payment", False, "Login failed")
            return False

        response = self.get(f"{APIEndpoints.PAYMENTS}/99999999")
        passed = response.status_code == 404

        self.record_result(
            "Get Nonexistent Payment",
            passed,
            f"Status: {response.status_code} (expected 404)",
            {"status_code": response.status_code}
        )
        return passed

    def test_payment_methods(self):
        """Test different payment methods"""
        if not self.login():
            self.record_result("Payment Methods", False, "Login failed")
            return False

        order_id = self.get_test_order()
        if not order_id:
            self.record_result("Payment Methods", False, "No order available")
            return False

        methods = ["CASH", "UPI", "CARD", "BANK_TRANSFER"]
        all_passed = True

        for method in methods:
            payment_data = {
                "orderId": order_id,
                "amount": 100.00,
                "paymentMethod": method,
                "paymentDate": datetime.now().strftime("%Y-%m-%d")
            }

            response = self.post(APIEndpoints.PAYMENTS, payment_data)
            if response.status_code not in [200, 201]:
                all_passed = False
                break

        self.record_result(
            "Payment Methods",
            all_passed,
            "All payment methods work" if all_passed else "Some methods failed",
            {"methods_tested": methods}
        )
        return all_passed

    def run_all_tests(self):
        """Run all payment tests"""
        print("\n" + "=" * 60)
        print("PAYMENT API TESTS")
        print("=" * 60)

        self.test_list_payments()
        self.test_list_payments_with_pagination()
        self.test_create_payment()
        self.test_create_payment_validation()
        self.test_get_payment_by_id()
        self.test_get_payments_by_order()
        self.test_get_nonexistent_payment()
        self.test_payment_methods()

        return self.get_results_summary()


if __name__ == "__main__":
    test = PaymentAPITest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
