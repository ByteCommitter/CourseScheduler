#!/bin/bash

echo "Checking environment..."

# Set environment variables
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk  # Adjust path based on your EC2 Java installation
export M2_HOME=/opt/maven                       # Adjust path based on your EC2 Maven installation
export PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH

echo "Maven home: $M2_HOME"
echo "Java home: $JAVA_HOME"

# Verify Maven exists
if [ ! -f "$M2_HOME/bin/mvn" ]; then
    echo "Maven executable not found at: $M2_HOME/bin/mvn"
    echo "Please check your Maven installation"
    exit 1
fi

# Store original directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Build the parent project first
echo "Building parent project..."
cd build
mvn -f .. verify -DskipTests
if [ $? -ne 0 ]; then
    echo "Build failed"
    exit 1
fi

# Start the school timetabling application
echo "Starting School Timetabling application..."
cd ../use-cases/school-timetabling

mvn clean quarkus:dev \
    -Dquarkus.http.port=8080 \
    -Dquarkus.http.cors=true \
    -Ddebug=false \
    -Dquarkus.http.host=0.0.0.0 \
    -Dstartup-open-browser=false \
    -Dquarkus.live-reload.url=http://localhost:8081

if [ $? -ne 0 ]; then
    echo "Application failed to start"
    exit 1
fi
