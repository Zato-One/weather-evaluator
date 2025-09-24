FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY ../ .

# remove CR from Windows CRLF for Unix compatibility
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew

RUN ./gradlew :actual-weather-writer:build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/actual-weather-writer/build/libs/actual-weather-writer.jar actual-weather-writer.jar

ENTRYPOINT ["java", "-jar", "actual-weather-writer.jar"]