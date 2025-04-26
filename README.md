# 🌤️ Weather Evaluator

![Project Status](https://img.shields.io/badge/status-in%20progress-yellow.svg)

**⚠️ This project is currently in active development. Some services may be incomplete or not yet implemented.**

---

Distributed microservices system for evaluating the accuracy of weather forecasts from multiple public APIs.  
Built with Kotlin, Kafka, and an event-driven architecture.

This project is created purely for educational purposes, aimed at experimenting with and demonstrating various
technologies.

---

## 🧱 Architecture Overview

This project compares weather forecasts from various sources (e.g. OpenMeteo, YR.no, Windy)  
and evaluates how close each prediction was to the real observed weather.

Services communicate asynchronously via Kafka.

---

## 🔧 Microservices

| Service                    | Description                                                                                                                                                                   |
|----------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **forecast-fetcher**       | Periodically fetches weather forecasts (daily/hourly) for defined locations from various public APIs (e.g. OpenMeteo, WeatherAPI). Publishes `ForecastFetchedEvent` to Kafka. |
| **forecast-writer**        | Listens to forecast events from Kafka and stores them in a database.                                                                                                          |
| **actual-weather-fetcher** | Periodically collects real weather observations (current weather) from APIs. Publishes `WeatherObservedEvent` to Kafka.                                                       |
| **actual-weather-writer**  | Listens to observation events and stores them in a database.                                                                                                                  |
| **forecast-evaluator**     | Compares stored forecasts with actual weather data to calculate prediction accuracy. Reads and writes directly to the database (no Kafka).                                    |
| **accuracy-api**           | Provides aggregated statistics and forecast accuracy reports prepared by forecast-evaluator via REST API.                                                                     |

### *(Optional extensions)*

| Service                | Description                                                                                                          |
|------------------------|----------------------------------------------------------------------------------------------------------------------|
| **accuracy-writer**    | Alternative design: forecast-evaluator emits accuracy events to Kafka, and this service writes them to the database. |
| **alert-service**      | Sends notifications if forecast sources become unreliable based on accuracy trends.                                  |
| **metrics-service**    | Collects system and service health metrics for observability.                                                        |
| **report-service**     | Generates detailed reports on forecast performance and weather trends.                                               |
| **dashboard-frontend** | User-facing frontend to visualize forecasts, accuracies, and reports.                                                |
| **api-gateway**        | Central entry point for external consumers, routing requests to internal APIs (e.g. accuracy-api).                   |

---

## 🛠 Tech Stack

- Kotlin
- Kafka (event bus), in KRaft mode
- Ktor (HTTP client)
- kotlinx.serialization
- Hoplite (configuration loading)
- MyBatis + Oracle DB (persistence)
- Gradle + Docker

---

## 🚀 Local Development

Start all services + Kafka with Docker Compose:

```bash
docker-compose up --build
```

---

## 💡 Ideas for improvement

- Event messages could be optimized using Protobuf, for binary serialization
- ForecastGranularity could also have three-hourly and six-hourly, but not every API supports it 
