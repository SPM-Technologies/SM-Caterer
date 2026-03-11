#!/usr/bin/env python3
"""
SM-Caterer Comprehensive Test Runner
Executes all API and Frontend tests and generates a report
"""
import sys
import os
import json
from datetime import datetime
from typing import Dict, List

# Add directories to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "api_tests"))
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "frontend_tests"))

from config import REPORT_DIR


def run_api_tests() -> Dict:
    """Run all API tests"""
    print("\n" + "=" * 80)
    print("RUNNING API TESTS")
    print("=" * 80)

    results = {
        "auth": None,
        "customers": None,
        "master_data": None,
        "orders": None,
        "payments": None
    }

    try:
        from test_auth import AuthAPITest
        auth_test = AuthAPITest()
        results["auth"] = auth_test.run_all_tests()
    except Exception as e:
        print(f"Auth tests error: {e}")
        results["auth"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    try:
        from test_customers import CustomerAPITest
        customer_test = CustomerAPITest()
        results["customers"] = customer_test.run_all_tests()
    except Exception as e:
        print(f"Customer tests error: {e}")
        results["customers"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    try:
        from test_master_data import MasterDataAPITest
        master_test = MasterDataAPITest()
        results["master_data"] = master_test.run_all_tests()
    except Exception as e:
        print(f"Master Data tests error: {e}")
        results["master_data"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    try:
        from test_orders import OrderAPITest
        order_test = OrderAPITest()
        results["orders"] = order_test.run_all_tests()
    except Exception as e:
        print(f"Order tests error: {e}")
        results["orders"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    try:
        from test_payments import PaymentAPITest
        payment_test = PaymentAPITest()
        results["payments"] = payment_test.run_all_tests()
    except Exception as e:
        print(f"Payment tests error: {e}")
        results["payments"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    return results


def run_frontend_tests() -> Dict:
    """Run all frontend tests"""
    print("\n" + "=" * 80)
    print("RUNNING FRONTEND TESTS")
    print("=" * 80)

    results = {
        "login": None,
        "navigation": None,
        "order_wizard": None
    }

    try:
        from test_login_page import LoginPageTest
        login_test = LoginPageTest()
        results["login"] = login_test.run_all_tests()
    except Exception as e:
        print(f"Login tests error: {e}")
        results["login"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    try:
        from test_navigation import NavigationTest
        nav_test = NavigationTest()
        results["navigation"] = nav_test.run_all_tests()
    except Exception as e:
        print(f"Navigation tests error: {e}")
        results["navigation"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    try:
        from test_order_wizard import OrderWizardTest
        wizard_test = OrderWizardTest()
        results["order_wizard"] = wizard_test.run_all_tests()
    except Exception as e:
        print(f"Order Wizard tests error: {e}")
        results["order_wizard"] = {"total": 0, "passed": 0, "failed": 0, "error": str(e)}

    return results


def generate_html_report(api_results: Dict, frontend_results: Dict, filename: str):
    """Generate an HTML test report"""

    # Calculate totals
    api_total = sum(r.get("total", 0) for r in api_results.values() if r)
    api_passed = sum(r.get("passed", 0) for r in api_results.values() if r)
    api_failed = sum(r.get("failed", 0) for r in api_results.values() if r)

    fe_total = sum(r.get("total", 0) for r in frontend_results.values() if r)
    fe_passed = sum(r.get("passed", 0) for r in frontend_results.values() if r)
    fe_failed = sum(r.get("failed", 0) for r in frontend_results.values() if r)

    total = api_total + fe_total
    passed = api_passed + fe_passed
    failed = api_failed + fe_failed
    pass_rate = (passed / total * 100) if total > 0 else 0

    html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>SM-Caterer Test Report</title>
    <style>
        * {{ box-sizing: border-box; margin: 0; padding: 0; }}
        body {{ font-family: 'Segoe UI', Tahoma, sans-serif; background: #f5f5f5; padding: 20px; }}
        .container {{ max-width: 1200px; margin: 0 auto; }}
        h1 {{ text-align: center; color: #2c3e50; margin-bottom: 30px; }}
        .summary {{ display: grid; grid-template-columns: repeat(4, 1fr); gap: 20px; margin-bottom: 30px; }}
        .summary-card {{ background: white; padding: 20px; border-radius: 10px; text-align: center; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }}
        .summary-card h2 {{ font-size: 2em; margin-bottom: 10px; }}
        .summary-card p {{ color: #666; }}
        .total {{ border-left: 4px solid #3498db; }}
        .passed {{ border-left: 4px solid #27ae60; }}
        .failed {{ border-left: 4px solid #e74c3c; }}
        .rate {{ border-left: 4px solid #f39c12; }}
        .section {{ background: white; padding: 20px; border-radius: 10px; margin-bottom: 20px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }}
        .section h3 {{ color: #2c3e50; border-bottom: 2px solid #eee; padding-bottom: 10px; margin-bottom: 15px; }}
        table {{ width: 100%; border-collapse: collapse; }}
        th, td {{ padding: 12px; text-align: left; border-bottom: 1px solid #eee; }}
        th {{ background: #f8f9fa; font-weight: 600; }}
        .pass {{ color: #27ae60; font-weight: bold; }}
        .fail {{ color: #e74c3c; font-weight: bold; }}
        .badge {{ display: inline-block; padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: bold; }}
        .badge-pass {{ background: #d4edda; color: #155724; }}
        .badge-fail {{ background: #f8d7da; color: #721c24; }}
        .timestamp {{ text-align: center; color: #666; margin-top: 20px; }}
    </style>
</head>
<body>
    <div class="container">
        <h1>SM-Caterer Test Report</h1>

        <div class="summary">
            <div class="summary-card total">
                <h2>{total}</h2>
                <p>Total Tests</p>
            </div>
            <div class="summary-card passed">
                <h2>{passed}</h2>
                <p>Passed</p>
            </div>
            <div class="summary-card failed">
                <h2>{failed}</h2>
                <p>Failed</p>
            </div>
            <div class="summary-card rate">
                <h2>{pass_rate:.1f}%</h2>
                <p>Pass Rate</p>
            </div>
        </div>

        <div class="section">
            <h3>API Tests Summary</h3>
            <table>
                <tr>
                    <th>Test Suite</th>
                    <th>Total</th>
                    <th>Passed</th>
                    <th>Failed</th>
                    <th>Status</th>
                </tr>
    """

    for suite_name, result in api_results.items():
        if result:
            status_class = "badge-pass" if result.get("failed", 0) == 0 else "badge-fail"
            status_text = "PASS" if result.get("failed", 0) == 0 else "FAIL"
            html += f"""
                <tr>
                    <td>{suite_name.replace('_', ' ').title()}</td>
                    <td>{result.get('total', 0)}</td>
                    <td class="pass">{result.get('passed', 0)}</td>
                    <td class="fail">{result.get('failed', 0)}</td>
                    <td><span class="badge {status_class}">{status_text}</span></td>
                </tr>
            """

    html += """
            </table>
        </div>

        <div class="section">
            <h3>Frontend Tests Summary</h3>
            <table>
                <tr>
                    <th>Test Suite</th>
                    <th>Total</th>
                    <th>Passed</th>
                    <th>Failed</th>
                    <th>Status</th>
                </tr>
    """

    for suite_name, result in frontend_results.items():
        if result:
            status_class = "badge-pass" if result.get("failed", 0) == 0 else "badge-fail"
            status_text = "PASS" if result.get("failed", 0) == 0 else "FAIL"
            html += f"""
                <tr>
                    <td>{suite_name.replace('_', ' ').title()}</td>
                    <td>{result.get('total', 0)}</td>
                    <td class="pass">{result.get('passed', 0)}</td>
                    <td class="fail">{result.get('failed', 0)}</td>
                    <td><span class="badge {status_class}">{status_text}</span></td>
                </tr>
            """

    html += f"""
            </table>
        </div>

        <div class="section">
            <h3>Detailed Test Results</h3>
    """

    # Add detailed results for each test suite
    all_results = {**api_results, **frontend_results}
    for suite_name, result in all_results.items():
        if result and "results" in result:
            html += f"<h4 style='margin-top:20px;'>{suite_name.replace('_', ' ').title()}</h4>"
            html += """<table>
                <tr><th>Test Name</th><th>Status</th><th>Message</th></tr>
            """
            for test in result.get("results", []):
                status = "PASS" if test.get("passed") else "FAIL"
                status_class = "pass" if test.get("passed") else "fail"
                html += f"""
                    <tr>
                        <td>{test.get('test_name', 'Unknown')}</td>
                        <td class="{status_class}">{status}</td>
                        <td>{test.get('message', '')}</td>
                    </tr>
                """
            html += "</table>"

    html += f"""
        </div>

        <p class="timestamp">Report generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}</p>
    </div>
</body>
</html>
    """

    with open(filename, 'w', encoding='utf-8') as f:
        f.write(html)

    print(f"\nHTML Report saved to: {filename}")


def main():
    """Main test runner"""
    print("=" * 80)
    print("SM-CATERER COMPREHENSIVE TEST SUITE")
    print("=" * 80)
    print(f"Started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")

    # Check if we should run specific tests
    run_api = True
    run_frontend = True

    if len(sys.argv) > 1:
        if sys.argv[1] == "--api-only":
            run_frontend = False
        elif sys.argv[1] == "--frontend-only":
            run_api = False

    api_results = {}
    frontend_results = {}

    if run_api:
        api_results = run_api_tests()

    if run_frontend:
        frontend_results = run_frontend_tests()

    # Generate report
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    report_file = os.path.join(REPORT_DIR, f"test_report_{timestamp}.html")
    json_file = os.path.join(REPORT_DIR, f"test_results_{timestamp}.json")

    generate_html_report(api_results, frontend_results, report_file)

    # Save JSON results
    all_results = {
        "timestamp": datetime.now().isoformat(),
        "api_tests": api_results,
        "frontend_tests": frontend_results
    }

    with open(json_file, 'w') as f:
        json.dump(all_results, f, indent=2, default=str)

    print(f"JSON Results saved to: {json_file}")

    # Print summary
    print("\n" + "=" * 80)
    print("TEST EXECUTION COMPLETE")
    print("=" * 80)

    api_total = sum(r.get("total", 0) for r in api_results.values() if r)
    api_passed = sum(r.get("passed", 0) for r in api_results.values() if r)
    fe_total = sum(r.get("total", 0) for r in frontend_results.values() if r)
    fe_passed = sum(r.get("passed", 0) for r in frontend_results.values() if r)

    total = api_total + fe_total
    passed = api_passed + fe_passed
    failed = total - passed

    print(f"\nAPI Tests:      {api_passed}/{api_total} passed")
    print(f"Frontend Tests: {fe_passed}/{fe_total} passed")
    print(f"\nOVERALL: {passed}/{total} tests passed ({(passed/total*100) if total > 0 else 0:.1f}%)")

    if failed > 0:
        print(f"\nWARNING: {failed} tests FAILED - Check report for details")
        return 1
    else:
        print("\nSUCCESS: All tests passed!")
        return 0


if __name__ == "__main__":
    exit_code = main()
    sys.exit(exit_code)
