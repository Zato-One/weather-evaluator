FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY ../ .

# remove CR from Windows CRLF for Unix compatibility
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew

RUN ./gradlew :forecast-fetcher:build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/forecast-fetcher/build/libs/forecast-fetcher.jar forecast-fetcher.jar

ENTRYPOINT ["java", "-jar", "forecast-fetcher.jar"]