#!/bin/bash
mvn clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
