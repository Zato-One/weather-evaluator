FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY ../ .

# remove CR from Windows CRLF for Unix compatibility
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew

RUN ./gradlew :actual-weather-fetcher:build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/actual-weather-fetcher/build/libs/actual-weather-fetcher.jar actual-weather-fetcher.jar

ENTRYPOINT ["java", "-jar", "actual-weather-fetcher.jar"]