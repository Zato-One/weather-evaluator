# 🌤️ Weather Evaluator

![Project Status](https://img.shields.io/badge/status-in%20progress-yellow.svg)

**⚠️ This project is currently in active development. Some services may be incomplete or not yet implemented.**

---

Distributed microservices system for evaluating the accuracy of weather forecasts from multiple public APIs.  
Built with Kotlin, Kafka, and an event-driven architecture.

This project is created purely for educational purposes, aimed at experimenting with and demonstrating various technologies.

---

## 🧱 Architecture Overview

This project compares weather forecasts from various sources (e.g. OpenMeteo, YR.no, Windy)  
and evaluates how close each prediction was to the real observed weather.

Services communicate asynchronously via Kafka.

---

## 🔧 Microservices

| Service                | Description |
|------------------------|-------------|
| **forecast-fetcher**   | Periodically fetches weather forecasts for defined locations from a single API (e.g. OpenMeteo). Publishes `ForecastFetchedEvent` to Kafka. |
| **forecast-collector** | Listens to forecast events and stores them in a database. |
| **weather-observer**   | Collects real weather observations (what actually happened). Publishes `WeatherObservedEvent`. |
| **forecast-evaluator** | Compares past forecasts with actual weather to calculate prediction accuracy. |
| **stats-api**          | Provides aggregated statistics and forecast accuracy reports via REST API. |
| *(optional)* **notification-service** | Sends alerts if forecast sources become unreliable. |

---

## 🛠 Tech Stack

- Kotlin
- Kafka (event bus)
- Ktor (HTTP client)
- kotlinx.serialization
- MyBatis + Oracle DB (persistence)
- Gradle + Docker

---

## 🚀 Local Development

Start all services + Kafka with Docker Compose:

```bash
docker-compose up --build
