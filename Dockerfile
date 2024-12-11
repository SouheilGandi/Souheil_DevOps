FROM openjdk:8-jdk-alpine
WORKDIR /app
COPY target/eventsProject-1.0.0.jar /app/app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

