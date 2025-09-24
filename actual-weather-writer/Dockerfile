FROM openjdk:21-jdk-slim

WORKDIR /app

COPY actual-weather-writer/build/libs/actual-weather-writer.jar .

EXPOSE 8080

CMD ["java", "-jar", "actual-weather-writer.jar"]