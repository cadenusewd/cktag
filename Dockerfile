# Alpine Linux with OpenJDK JRE
FROM openjdk:8-jre-stretch

# copy JAR into image
COPY target/cktag-1.0.jar /app.jar
# run application with this command line
CMD ["/usr/bin/java", "-jar", "-Dspring.profiles.active=docker", "/app.jar"]
