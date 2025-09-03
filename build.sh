#!/bin/bash

echo "Building Economy Plugin..."
echo

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven and try again"
    exit 1
fi

# Clean and build the project
echo "Cleaning previous build..."
mvn clean

echo "Building project..."
mvn package

if [ $? -ne 0 ]; then
    echo
    echo "Build failed! Check the error messages above."
    exit 1
fi

echo
echo "Build successful!"
echo
echo "The plugin JAR file is located at: target/economy-plugin-1.0.0.jar"
echo
echo "To install:"
echo "1. Copy the JAR file to your server's plugins folder"
echo "2. Restart your server"
echo "3. Configure the plugin using the generated config.yml"
echo





