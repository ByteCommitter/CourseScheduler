FROM registry.access.redhat.com/ubi8/openjdk-11:latest

WORKDIR /deployments

COPY quarkus-app/lib/ /deployments/lib/
COPY quarkus-app/app/ /deployments/app/
COPY quarkus-app/quarkus/ /deployments/quarkus/
COPY quarkus-app/quarkus-run.jar /deployments/quarkus-run.jar

ENV JAVA_OPTIONS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/deployments/quarkus-run.jar"]
