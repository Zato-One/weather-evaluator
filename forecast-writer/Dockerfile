FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY ../ .

# remove CR from Windows CRLF for Unix compatibility
RUN sed -i 's/\r$//' ./gradlew
RUN chmod +x ./gradlew

RUN ./gradlew :forecast-writer:build -x test

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/forecast-writer/build/libs/forecast-writer.jar forecast-writer.jar

ENTRYPOINT ["java", "-jar", "forecast-writer.jar"]