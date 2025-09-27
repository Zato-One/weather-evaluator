FROM openjdk:21-jdk-slim

WORKDIR /app

COPY forecast-evaluator/build/libs/forecast-evaluator.jar /app/forecast-evaluator.jar

EXPOSE 8080

CMD ["java", "-jar", "forecast-evaluator.jar"]