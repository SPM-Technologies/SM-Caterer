@echo off
REM ============================================
REM SM-Caterer Basic Test Agent (Windows)
REM Tests all pages and reports issues
REM ============================================

setlocal EnableDelayedExpansion

set SCRIPT_DIR=%~dp0
set PROJECT_ROOT=%SCRIPT_DIR%..\..
set CONFIG_FILE=%SCRIPT_DIR%config.json
set REPORT_DIR=%SCRIPT_DIR%reports
set COOKIE_FILE=%SCRIPT_DIR%.cookies

REM Create timestamp
for /f "tokens=2 delims==" %%a in ('wmic OS Get localdatetime /value') do set "dt=%%a"
set "TIMESTAMP=%dt:~0,8%_%dt:~8,6%"
set REPORT_FILE=%REPORT_DIR%\test_report_%TIMESTAMP%.txt

REM Counters
set TOTAL_TESTS=0
set PASSED_TESTS=0
set FAILED_TESTS=0

REM Base configuration
set BASE_URL=http://localhost:8080
set USERNAME=testuser
set PASSWORD=test123

echo.
echo ============================================
echo   SM-Caterer Basic Test Agent (Windows)
echo ============================================
echo.

REM Create reports directory
if not exist "%REPORT_DIR%" mkdir "%REPORT_DIR%"

REM Check if curl is available
where curl >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] curl is not available. Please install curl.
    exit /b 1
)

REM Check if application is running
echo [INFO] Checking if application is running...
curl -s -o nul -w "%%{http_code}" "%BASE_URL%/login" > temp_status.txt 2>nul
set /p HTTP_CODE=<temp_status.txt
del temp_status.txt

if not "%HTTP_CODE%"=="200" (
    echo [ERROR] Application is not running at %BASE_URL%
    echo [INFO] Please start the application first with: mvnw spring-boot:run
    exit /b 1
)
echo [PASS] Application is running

REM Login
echo [INFO] Logging in as %USERNAME%...

REM Get CSRF token
curl -s -c "%COOKIE_FILE%" "%BASE_URL%/login" > login_page.html 2>nul
for /f "tokens=4 delims==" %%a in ('findstr /i "name=\"_csrf\" value=" login_page.html') do (
    set CSRF_RAW=%%a
)
set CSRF_TOKEN=%CSRF_RAW:~1,-1%
set CSRF_TOKEN=%CSRF_TOKEN:"=%

REM Perform login
curl -s -c "%COOKIE_FILE%" -b "%COOKIE_FILE%" -L -d "username=%USERNAME%&password=%PASSWORD%&_csrf=%CSRF_TOKEN%" "%BASE_URL%/login" -o login_response.html 2>nul

REM Check login by accessing dashboard
curl -s -b "%COOKIE_FILE%" -o nul -w "%%{http_code}" "%BASE_URL%/dashboard" > temp_status.txt 2>nul
set /p DASH_CODE=<temp_status.txt
del temp_status.txt

if not "%DASH_CODE%"=="200" (
    echo [ERROR] Login failed
    exit /b 1
)
echo [PASS] Login successful

echo.
echo [INFO] Running page tests...
echo.

REM Initialize report
echo ============================================ > "%REPORT_FILE%"
echo SM-Caterer Basic Test Report >> "%REPORT_FILE%"
echo ============================================ >> "%REPORT_FILE%"
echo Date: %date% %time% >> "%REPORT_FILE%"
echo Base URL: %BASE_URL% >> "%REPORT_FILE%"
echo. >> "%REPORT_FILE%"

REM Test each page
call :test_page "Login Page" "/login" 200
call :test_page "Dashboard" "/dashboard" 200
call :test_page "Profile" "/profile" 200
call :test_page "Units List" "/masters/units" 200
call :test_page "Units New" "/masters/units/new" 200
call :test_page "Materials List" "/masters/materials" 200
call :test_page "Materials New" "/masters/materials/new" 200
call :test_page "Menus List" "/masters/menus" 200
call :test_page "Menus New" "/masters/menus/new" 200
call :test_page "Event Types List" "/masters/event-types" 200
call :test_page "Event Types New" "/masters/event-types/new" 200
call :test_page "Recipes List" "/masters/recipes" 200
call :test_page "Recipes New" "/masters/recipes/new" 200
call :test_page "UPI QR List" "/masters/upi-qr" 200
call :test_page "UPI QR New" "/masters/upi-qr/new" 200
call :test_page "Orders List" "/orders" 200
call :test_page "Order Wizard" "/orders/wizard/step1" 200
call :test_page "Customers List" "/customers" 200
call :test_page "Customers New" "/customers/new" 200
call :test_page "Payments List" "/payments" 200
call :test_page "Payments New" "/payments/new" 200
call :test_page "Reports Index" "/reports" 200
call :test_page "Order Report" "/reports/orders" 200
call :test_page "Payment Report" "/reports/payments" 200
call :test_page "Pending Balance" "/reports/pending-balance" 200

echo.
echo ============================================
echo   Test Summary
echo ============================================
echo.
echo Total: %TOTAL_TESTS% ^| Passed: %PASSED_TESTS% ^| Failed: %FAILED_TESTS%
echo.

echo. >> "%REPORT_FILE%"
echo SUMMARY >> "%REPORT_FILE%"
echo -------------------------------------------- >> "%REPORT_FILE%"
echo Total Tests: %TOTAL_TESTS% >> "%REPORT_FILE%"
echo Passed: %PASSED_TESTS% >> "%REPORT_FILE%"
echo Failed: %FAILED_TESTS% >> "%REPORT_FILE%"

echo [INFO] Report saved to: %REPORT_FILE%

REM Cleanup
del /q "%COOKIE_FILE%" 2>nul
del /q login_page.html 2>nul
del /q login_response.html 2>nul

if %FAILED_TESTS% gtr 0 (
    echo [ERROR] Some tests failed!
    exit /b 1
) else (
    echo [PASS] All tests passed!
    exit /b 0
)

REM ============================================
REM Test Page Function
REM ============================================
:test_page
set PAGE_NAME=%~1
set PAGE_PATH=%~2
set EXPECTED=%~3

set /a TOTAL_TESTS+=1

curl -s -b "%COOKIE_FILE%" -o nul -w "%%{http_code}" "%BASE_URL%%PAGE_PATH%" > temp_status.txt 2>nul
set /p ACTUAL=<temp_status.txt
del temp_status.txt

if "%ACTUAL%"=="%EXPECTED%" (
    echo [PASS] %PAGE_NAME% (%PAGE_PATH%): %ACTUAL%
    echo [PASS] %PAGE_NAME%: %ACTUAL% >> "%REPORT_FILE%"
    set /a PASSED_TESTS+=1
) else (
    echo [FAIL] %PAGE_NAME% (%PAGE_PATH%): Expected %EXPECTED%, got %ACTUAL%
    echo [FAIL] %PAGE_NAME%: Expected %EXPECTED%, got %ACTUAL% >> "%REPORT_FILE%"
    set /a FAILED_TESTS+=1
)
goto :eof
