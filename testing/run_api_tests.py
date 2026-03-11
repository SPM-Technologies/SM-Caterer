#!/usr/bin/env python3
"""
SM-Caterer API Test Runner
Run only API tests without browser dependencies
"""
import sys
import os
import json
from datetime import datetime

# Add directories to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "api_tests"))

from config import REPORT_DIR, BASE_URL


def check_server_availability():
    """Check if the server is running"""
    import requests
    try:
        response = requests.get(f"{BASE_URL}/actuator/health", timeout=5)
        return response.status_code == 200
    except:
        try:
            response = requests.get(f"{BASE_URL}/login", timeout=5)
            return response.status_code == 200
        except:
            return False


def main():
    print("=" * 70)
    print("SM-CATERER API TEST RUNNER")
    print("=" * 70)
    print(f"Target: {BASE_URL}")
    print(f"Started: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print()

    # Check server
    print("Checking server availability...")
    if not check_server_availability():
        print(f"ERROR: Server at {BASE_URL} is not responding!")
        print("Please ensure the application is running.")
        return 1

    print("Server is UP - Running tests...\n")

    all_results = {}
    total_tests = 0
    total_passed = 0
    total_failed = 0

    # Run Auth Tests
    try:
        from test_auth import AuthAPITest
        auth_test = AuthAPITest()
        result = auth_test.run_all_tests()
        all_results["auth"] = result
        total_tests += result["total"]
        total_passed += result["passed"]
        total_failed += result["failed"]
    except Exception as e:
        print(f"Auth tests error: {e}")
        all_results["auth"] = {"error": str(e)}

    # Run Customer Tests
    try:
        from test_customers import CustomerAPITest
        customer_test = CustomerAPITest()
        result = customer_test.run_all_tests()
        all_results["customers"] = result
        total_tests += result["total"]
        total_passed += result["passed"]
        total_failed += result["failed"]
    except Exception as e:
        print(f"Customer tests error: {e}")
        all_results["customers"] = {"error": str(e)}

    # Run Master Data Tests
    try:
        from test_master_data import MasterDataAPITest
        master_test = MasterDataAPITest()
        result = master_test.run_all_tests()
        all_results["master_data"] = result
        total_tests += result["total"]
        total_passed += result["passed"]
        total_failed += result["failed"]
    except Exception as e:
        print(f"Master Data tests error: {e}")
        all_results["master_data"] = {"error": str(e)}

    # Run Order Tests
    try:
        from test_orders import OrderAPITest
        order_test = OrderAPITest()
        result = order_test.run_all_tests()
        all_results["orders"] = result
        total_tests += result["total"]
        total_passed += result["passed"]
        total_failed += result["failed"]
    except Exception as e:
        print(f"Order tests error: {e}")
        all_results["orders"] = {"error": str(e)}

    # Run Payment Tests
    try:
        from test_payments import PaymentAPITest
        payment_test = PaymentAPITest()
        result = payment_test.run_all_tests()
        all_results["payments"] = result
        total_tests += result["total"]
        total_passed += result["passed"]
        total_failed += result["failed"]
    except Exception as e:
        print(f"Payment tests error: {e}")
        all_results["payments"] = {"error": str(e)}

    # Run Tenant Branding Tests
    try:
        from test_tenant_branding import TenantBrandingAPITest
        branding_test = TenantBrandingAPITest()
        result = branding_test.run_all_tests()
        all_results["tenant_branding"] = result
        total_tests += result["total"]
        total_passed += result["passed"]
        total_failed += result["failed"]
    except Exception as e:
        print(f"Tenant Branding tests error: {e}")
        all_results["tenant_branding"] = {"error": str(e)}

    # Print Summary
    print("\n" + "=" * 70)
    print("TEST RESULTS SUMMARY")
    print("=" * 70)

    for suite_name, result in all_results.items():
        if "error" in result:
            print(f"  {suite_name.upper():20} ERROR: {result['error']}")
        else:
            status = "PASS" if result["failed"] == 0 else "FAIL"
            print(f"  {suite_name.upper():20} {result['passed']}/{result['total']} passed [{status}]")

    print("-" * 70)
    pass_rate = (total_passed / total_tests * 100) if total_tests > 0 else 0
    print(f"  {'TOTAL':20} {total_passed}/{total_tests} passed ({pass_rate:.1f}%)")

    # List failed tests
    if total_failed > 0:
        print("\n" + "=" * 70)
        print("FAILED TESTS:")
        print("=" * 70)
        for suite_name, result in all_results.items():
            if "results" in result:
                for test in result["results"]:
                    if not test["passed"]:
                        print(f"  - [{suite_name}] {test['test_name']}: {test['message']}")

    # Save results
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    results_file = os.path.join(REPORT_DIR, f"api_test_results_{timestamp}.json")

    with open(results_file, 'w') as f:
        json.dump({
            "timestamp": datetime.now().isoformat(),
            "summary": {
                "total": total_tests,
                "passed": total_passed,
                "failed": total_failed,
                "pass_rate": pass_rate
            },
            "results": all_results
        }, f, indent=2, default=str)

    print(f"\nResults saved to: {results_file}")

    return 0 if total_failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
