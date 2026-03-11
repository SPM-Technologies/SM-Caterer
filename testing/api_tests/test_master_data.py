"""
Master Data API Tests
Tests CRUD operations for Units, Materials, Menus, Event Types
"""
from base_api_test import BaseAPITest
import sys
import os
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from config import APIEndpoints


class MasterDataAPITest(BaseAPITest):
    """Master Data API Tests"""

    def __init__(self):
        super().__init__()
        self.test_unit_id = None
        self.test_material_id = None
        self.test_menu_id = None
        self.test_event_type_id = None

    # ==================== UNITS TESTS ====================

    def test_list_units(self):
        """Test listing all units"""
        if not self.login():
            self.record_result("List Units", False, "Login failed")
            return False

        response = self.get(APIEndpoints.UNITS)
        passed = response.status_code == 200

        self.record_result(
            "List Units",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_create_unit(self):
        """Test creating a new unit"""
        if not self.login():
            self.record_result("Create Unit", False, "Login failed")
            return False

        unit_data = {
            "unitCode": "TEST",
            "unitName": "Test Unit"
        }

        response = self.post(APIEndpoints.UNITS, unit_data)
        passed = response.status_code in [200, 201]

        data = response.json() if passed else {}
        if passed and data.get("success"):
            self.test_unit_id = data.get("data", {}).get("id")

        self.record_result(
            "Create Unit",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_unit_by_id(self):
        """Test getting a unit by ID"""
        if not self.login():
            self.record_result("Get Unit by ID", False, "Login failed")
            return False

        if not self.test_unit_id:
            self.test_create_unit()

        if not self.test_unit_id:
            self.record_result("Get Unit by ID", False, "No unit ID available")
            return False

        response = self.get(f"{APIEndpoints.UNITS}/{self.test_unit_id}")
        passed = response.status_code == 200

        self.record_result(
            "Get Unit by ID",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_update_unit(self):
        """Test updating a unit"""
        if not self.login():
            self.record_result("Update Unit", False, "Login failed")
            return False

        if not self.test_unit_id:
            self.test_create_unit()

        if not self.test_unit_id:
            self.record_result("Update Unit", False, "No unit ID available")
            return False

        update_data = {
            "unitCode": "TEST",
            "unitName": "Updated Test Unit"
        }

        response = self.put(f"{APIEndpoints.UNITS}/{self.test_unit_id}", update_data)
        passed = response.status_code == 200

        self.record_result(
            "Update Unit",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    # ==================== MATERIALS TESTS ====================

    def test_list_materials(self):
        """Test listing all materials"""
        if not self.login():
            self.record_result("List Materials", False, "Login failed")
            return False

        response = self.get(APIEndpoints.MATERIALS)
        passed = response.status_code == 200

        self.record_result(
            "List Materials",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_create_material(self):
        """Test creating a new material"""
        if not self.login():
            self.record_result("Create Material", False, "Login failed")
            return False

        # First ensure we have a unit
        if not self.test_unit_id:
            self.test_create_unit()

        material_data = {
            "materialCode": "TEST-MAT",
            "name": "Test Material",
            "unitId": self.test_unit_id
        }

        response = self.post(APIEndpoints.MATERIALS, material_data)
        passed = response.status_code in [200, 201]

        data = response.json() if passed else {}
        if passed and data.get("success"):
            self.test_material_id = data.get("data", {}).get("id")

        self.record_result(
            "Create Material",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_material_by_id(self):
        """Test getting a material by ID"""
        if not self.login():
            self.record_result("Get Material by ID", False, "Login failed")
            return False

        if not self.test_material_id:
            self.test_create_material()

        if not self.test_material_id:
            self.record_result("Get Material by ID", False, "No material ID available")
            return False

        response = self.get(f"{APIEndpoints.MATERIALS}/{self.test_material_id}")
        passed = response.status_code == 200

        self.record_result(
            "Get Material by ID",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    # ==================== MENUS TESTS ====================

    def test_list_menus(self):
        """Test listing all menus"""
        if not self.login():
            self.record_result("List Menus", False, "Login failed")
            return False

        response = self.get(APIEndpoints.MENUS)
        passed = response.status_code == 200

        self.record_result(
            "List Menus",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_create_menu(self):
        """Test creating a new menu item"""
        if not self.login():
            self.record_result("Create Menu", False, "Login failed")
            return False

        menu_data = {
            "menuCode": "TEST-MENU",
            "name": "Test Menu Item",
            "category": "VEG",
            "costPerServe": 150.00
        }

        response = self.post(APIEndpoints.MENUS, menu_data)
        passed = response.status_code in [200, 201]

        data = response.json() if passed else {}
        if passed and data.get("success"):
            self.test_menu_id = data.get("data", {}).get("id")

        self.record_result(
            "Create Menu",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_menu_by_id(self):
        """Test getting a menu by ID"""
        if not self.login():
            self.record_result("Get Menu by ID", False, "Login failed")
            return False

        if not self.test_menu_id:
            self.test_create_menu()

        if not self.test_menu_id:
            self.record_result("Get Menu by ID", False, "No menu ID available")
            return False

        response = self.get(f"{APIEndpoints.MENUS}/{self.test_menu_id}")
        passed = response.status_code == 200

        self.record_result(
            "Get Menu by ID",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    # ==================== EVENT TYPES TESTS ====================

    def test_list_event_types(self):
        """Test listing all event types"""
        if not self.login():
            self.record_result("List Event Types", False, "Login failed")
            return False

        response = self.get(APIEndpoints.EVENT_TYPES)
        passed = response.status_code == 200

        self.record_result(
            "List Event Types",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def test_create_event_type(self):
        """Test creating a new event type"""
        if not self.login():
            self.record_result("Create Event Type", False, "Login failed")
            return False

        event_type_data = {
            "eventCode": "TEST-EVT",
            "name": "Test Event Type",
            "description": "A test event type"
        }

        response = self.post(APIEndpoints.EVENT_TYPES, event_type_data)
        passed = response.status_code in [200, 201]

        data = response.json() if passed else {}
        if passed and data.get("success"):
            self.test_event_type_id = data.get("data", {}).get("id")

        self.record_result(
            "Create Event Type",
            passed,
            f"Status: {response.status_code}",
            {"response": data}
        )
        return passed

    def test_get_event_type_by_id(self):
        """Test getting an event type by ID"""
        if not self.login():
            self.record_result("Get Event Type by ID", False, "Login failed")
            return False

        if not self.test_event_type_id:
            self.test_create_event_type()

        if not self.test_event_type_id:
            self.record_result("Get Event Type by ID", False, "No event type ID available")
            return False

        response = self.get(f"{APIEndpoints.EVENT_TYPES}/{self.test_event_type_id}")
        passed = response.status_code == 200

        self.record_result(
            "Get Event Type by ID",
            passed,
            f"Status: {response.status_code}",
            {"status_code": response.status_code}
        )
        return passed

    def run_all_tests(self):
        """Run all master data tests"""
        print("\n" + "=" * 60)
        print("MASTER DATA API TESTS")
        print("=" * 60)

        # Units
        print("\n--- Units ---")
        self.test_list_units()
        self.test_create_unit()
        self.test_get_unit_by_id()
        self.test_update_unit()

        # Materials
        print("\n--- Materials ---")
        self.test_list_materials()
        self.test_create_material()
        self.test_get_material_by_id()

        # Menus
        print("\n--- Menus ---")
        self.test_list_menus()
        self.test_create_menu()
        self.test_get_menu_by_id()

        # Event Types
        print("\n--- Event Types ---")
        self.test_list_event_types()
        self.test_create_event_type()
        self.test_get_event_type_by_id()

        return self.get_results_summary()


if __name__ == "__main__":
    test = MasterDataAPITest()
    summary = test.run_all_tests()
    print(f"\nTotal: {summary['total']}, Passed: {summary['passed']}, Failed: {summary['failed']}")
    print(f"Pass Rate: {summary['pass_rate']:.1f}%")
