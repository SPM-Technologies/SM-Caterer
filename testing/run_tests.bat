@echo off
REM SM-Caterer Test Runner for Windows
REM ===================================

echo ========================================
echo SM-CATERER TEST RUNNER
echo ========================================
echo.

REM Check if Python is available
where python >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    where py >nul 2>nul
    if %ERRORLEVEL% NEQ 0 (
        echo ERROR: Python is not installed or not in PATH
        echo Please install Python 3.8+ and try again
        pause
        exit /b 1
    )
    set PYTHON_CMD=py
) else (
    set PYTHON_CMD=python
)

echo Using Python: %PYTHON_CMD%
%PYTHON_CMD% --version
echo.

REM Check server availability
echo Checking if server is running at http://localhost:8080...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/login >temp_status.txt 2>nul
set /p HTTP_STATUS=<temp_status.txt
del temp_status.txt

if "%HTTP_STATUS%"=="200" (
    echo Server is UP [HTTP 200]
) else if "%HTTP_STATUS%"=="302" (
    echo Server is UP [HTTP 302 redirect]
) else (
    echo WARNING: Server may not be running [HTTP %HTTP_STATUS%]
    echo Make sure the application is started at http://localhost:8080
    echo.
    choice /C YN /M "Continue anyway"
    if errorlevel 2 exit /b 1
)

echo.
echo ========================================
echo RUNNING API TESTS
echo ========================================
echo.

cd /d "%~dp0"
cd api_tests

%PYTHON_CMD% -c "import requests" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo Installing required packages...
    %PYTHON_CMD% -m pip install requests
)

cd ..
%PYTHON_CMD% run_api_tests.py

echo.
echo ========================================
echo TEST EXECUTION COMPLETE
echo ========================================
echo.
echo Reports are saved in: %~dp0reports
echo.
pause
