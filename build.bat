@echo off
echo Building Economy Plugin...
echo.

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven and try again
    pause
    exit /b 1
)

REM Clean and build the project
echo Cleaning previous build...
call mvn clean

echo Building project...
call mvn package

if errorlevel 1 (
    echo.
    echo Build failed! Check the error messages above.
    pause
    exit /b 1
)

echo.
echo Build successful!
echo.
echo The plugin JAR file is located at: target/economy-plugin-1.0.0.jar
echo.
echo To install:
echo 1. Copy the JAR file to your server's plugins folder
echo 2. Restart your server
echo 3. Configure the plugin using the generated config.yml
echo.
pause





