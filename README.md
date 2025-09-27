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

## Oracle Setup – Database User

The `weather_evaluator` user is automatically created using an init script located at `docker/oracle-init/create-user.sql`.
No need to create the user or schema manually.

---

---

## 🙏 Acknowledgments

This project's AI development workflow and context engineering approach is inspired by and adapted from:

- **[context-engineering-intro](https://github.com/coleam00/context-engineering-intro)** by [Cole Medin](https://github.com/coleam00)
- **["A Complete Guide to Claude Code - Here are ALL the Best Strategies"](https://www.youtube.com/watch?v=amEUIuBKwvg)** video tutorial

The AI-related files in `ai/` and `.claude/` directories contain content adapted from Cole Medin's repository, which demonstrates context engineering techniques for AI coding assistants. Special thanks to Cole for sharing these valuable methodologies with the community.

---

## 🏗️ Architecture Principles Compliance

This project demonstrates adherence to microservice and event-driven architecture principles:

### Microservice Architecture ✅

| Principle | Implementation |
|-----------|----------------|
| **Single Responsibility** | Each service has one clear purpose (forecast-fetcher gets data, forecast-writer persists it) |
| **Decentralization** | Independent services with own configuration, no shared databases |
| **API First** | Well-defined interfaces (`ForecastProvider`, Kafka events) |
| **Failure Isolation** | Service failures don't cascade (adapter failures isolated in forecast-fetcher) |
| **Technology Independence** | Each service uses optimal tech stack (Ktor for HTTP, MyBatis for DB) |
| **Team Ownership** | Clear service boundaries with individual build configs and documentation |

### Event-Driven Architecture ✅

| Principle | Implementation |
|-----------|----------------|
| **Loose Coupling** | Services communicate only through Kafka events, no direct dependencies |
| **Asynchronous Communication** | Fire-and-forget messaging via Kafka producers/consumers |
| **Event Immutability** | Events are immutable data classes, never modified after creation |
| **Choreography** | Decentralized event flow, no central orchestrator |
| **Eventual Consistency** | Data consistency achieved through event processing |
| **Producer Independence** | Services run independently, don't know about consumers |

### Development Principles ✅

| Principle | Implementation                                                                                                                |
|-----------|-------------------------------------------------------------------------------------------------------------------------------|
| **SOLID - Single Responsibility** | Each class has one clear purpose (ForecastFetcherService fetches, ForecastEventProducer publishes, ForecastMapper handles DB) |
| **SOLID - Open/Closed** | ForecastProvider interface allows extending with new weather APIs without modifying existing code                             |
| **SOLID - Liskov Substitution** | OpenMeteoAdapter and WeatherApiAdapter are interchangeable through ForecastProvider interface                                 |
| **SOLID - Interface Segregation** | Clean interfaces like ForecastProvider with minimal, focused methods                                                          |
| **SOLID - Dependency Inversion** | High-level services depend on abstractions (ForecastProvider, ForecastEventProducer)                                          |
| **DRY (Don't Repeat Yourself)** | Common functionality extracted to shared modules (common events, serializers, configurations)                                 |
| **KISS (Keep It Simple)** | Straightforward class hierarchies, clear method names, minimal complexity                                                     |
| **YAGNI (You Aren't Gonna Need It)** | No over-engineering, only implemented features that are currently needed                                                      |
| **Composition over Inheritance** | Services use composition (ForecastFetcherService contains ForecastProvider) rather than deep inheritance                      |
| **Immutability** | Events and data models are immutable data classes with val properties                                                         |
| **Fail Fast** | Input validation and error handling at service boundaries                                                                     |
| **Separation of Concerns** | Clear layers: adapters for external APIs, services for business logic, persistence for data                                   |
| **Test Coverage** | Comprehensive coverage (unit, integration, configuration tests) |

**Strengths:**
- Clean separation of concerns between data fetching and persistence
- Robust error handling with failure isolation
- Proper use of domain events for inter-service communication
- Technology choices aligned with service requirements

**Minor Areas for Enhancement:**
- Add health checks and monitoring endpoints for production readiness
- Implement retry mechanisms with dead letter queues for resilience
- Add rate limiting for external API calls

---

## 💡 Ideas for improvement

- Event messages could be optimized using Protobuf for binary serialization
- ForecastGranularity currently supports HOURLY and DAILY forecasts. Additional granularities like THREE_HOURLY, SIX_HOURLY, and WEEKLY could be added, but not all weather API sources provide these granularities
- Replace Oracle-specific `IGNORE_ROW_ON_DUPKEY_INDEX` with database-agnostic upsert solution
- Add service health checks and monitoring endpoints
- Implement graceful shutdown handling for long-running message consumption
- Add retry logic and dead letter queue for failed message processing
- Implement rate limiting for external API calls to prevent quota exhaustion
- Implement contract testing between services via Kafka events