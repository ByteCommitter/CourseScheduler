# Build stage
FROM maven:3.8.1-openjdk-11-slim as builder

# Copy the project files
WORKDIR /build
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Runtime stage
FROM registry.access.redhat.com/ubi8/openjdk-11:latest

# Create app directory
WORKDIR /deployments

# Copy only the necessary files
COPY --from=builder /build/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder /build/target/quarkus-app/app/ /deployments/app/
COPY --from=builder /build/target/quarkus-app/quarkus/ /deployments/quarkus/
COPY --from=builder /build/target/quarkus-app/quarkus-run.jar /deployments/quarkus-run.jar

# Set environment variables
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
