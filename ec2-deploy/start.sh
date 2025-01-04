#!/bin/bash

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Java not found. Please install Java 11 or higher"
    exit 1
fi

# Set memory options and other JVM arguments
JAVA_OPTS="-Xms128m -Xmx512m"

echo "Starting School Timetabling application..."
java $JAVA_OPTS \
    -Dquarkus.http.port=8080 \
    -Dquarkus.http.host=0.0.0.0 \
    -Djava.util.logging.manager=org.jboss.logmanager.LogManager \
    -jar quarkus-app/quarkus-run.jar
