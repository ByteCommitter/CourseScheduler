FROM registry.access.redhat.com/ubi8/openjdk-11:latest

# Create app directory
WORKDIR /deployments

# Copy the pre-built application files
COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/
COPY target/quarkus-app/quarkus-run.jar /deployments/quarkus-run.jar

# Set environment variables
ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
