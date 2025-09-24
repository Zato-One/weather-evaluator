# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Weather Evaluator is a distributed microservices system for evaluating weather forecast accuracy from multiple public
APIs. Built with Kotlin, Kafka, and event-driven architecture.

### Architecture

- **Event-driven microservices** communicating via Kafka
- **Kotlin + JVM 21** for all services
- **Oracle Database** with MyBatis for persistence
- **Docker Compose** for local development
- **Ktor** for HTTP clients and servers
- **Hoplite** for configuration management

### Microservices Structure

- `common/` - Shared models, events, and utilities
- `forecast-fetcher/` - Fetches forecasts from weather APIs (OpenMeteo, WeatherAPI)
- `forecast-writer/` - Consumes forecast events and persists to database
- Additional planned services: actual-weather-fetcher, forecast-evaluator, accuracy-api

## Development Commands

### Building

```bash
# Build all services
docker-compose up --build

# Build individual service fat JARs (when gradle is available)
./gradlew :forecast-fetcher:shadowJar
./gradlew :forecast-writer:shadowJar
```

### Testing

```bash
# Run all tests (when gradle is available)
./gradlew test

# Test specific module
./gradlew :common:test
./gradlew :forecast-fetcher:test
```

### Infrastructure Management

```bash
# Start entire stack
docker-compose up --build

# Start only Kafka
docker-compose -p kafka-stack -f docker-compose.kafka.yml up -d

# Start only Oracle DB
docker-compose -p oracle-stack -f docker-compose.oracle.yml up -d

# View Kafka topics
docker exec kafka kafka-topics.sh --bootstrap-server localhost:19092 --list

# View logs
docker logs -f kafka
docker logs -f oracle
```

### Gradle Tasks (via Docker or local Gradle)

The project uses custom Gradle tasks defined in `build.gradle.kts`:

- `startAll` - Start entire Docker stack
- `startKafka` / `stopKafka` - Manage Kafka container
- `startOracle` / `stopOracle` - Manage Oracle container
- `listKafkaTopics` - List all Kafka topics

## Code Architecture

### Event Flow

1. `forecast-fetcher` fetches weather data → publishes `ForecastFetchedEvent` to Kafka
2. `forecast-writer` consumes events → persists forecasts to Oracle DB
3. Future services will consume actual weather data and evaluate accuracy

### Key Components

- **Events**: Defined in `common/src/main/kotlin/event/` using kotlinx.serialization
- **Models**: Location, ForecastGranularity in `common/src/main/kotlin/model/`
- **Adapters**: Weather API integrations in `forecast-fetcher/src/main/kotlin/adapter/`
- **Kafka**: Producer/Consumer factories in respective services
- **Database**: MyBatis mappers and entities in `forecast-writer/src/main/kotlin/persistence/`

### Configuration

Each service uses Hoplite for configuration loading:

- `AppConfig.kt` - Main application configuration
- `KafkaConfig.kt` - Kafka connection settings
- `DatabaseConfig.kt` - Database connection (forecast-writer only)

### Dependencies

Key libraries managed in root `build.gradle.kts`:

- Kotlin 2.1.20 with coroutines and serialization
- Ktor 3.1.2 for HTTP clients
- Kafka 4.0.0 for messaging
- Oracle JDBC 23.7.0.25.01
- MyBatis 3.5.16 for database mapping
- Hoplite 2.9.0 for configuration

### Testing

- Uses JUnit Platform for testing
- Test configuration in individual module `build.gradle.kts` files
- Oracle database testing scripts in `test/scripts/`

## Development Notes

- Services are designed as fat JARs using Shadow plugin
- Main classes follow pattern: `cz.savic.weatherevaluator.{service}.MainKt`
- Docker containers use OpenJDK base images
- Database schema managed via Liquibase migrations
- Kafka runs in KRaft mode (no Zookeeper)

### Task Execution Guidelines

- **IMPORTANT**: Do exactly what is requested - nothing more, nothing less
- **STOP** after completing each requested step - wait for explicit instruction to continue
- Ask before adding extra features - if you want to do something beyond the specific request, ask first
- Be (token) efficient - minimize unnecessary output by focusing only on the requested task
- One task at a time - complete the specific request before suggesting or doing anything additional
- Avoid comments in code - Write self-explanatory code instead. Only add comments to explain WHY something is done a specific way, never WHAT the code does.

### AI Security

- Do not use env/properties files (.env, .secret, application.properties, *.properties and others) as source or context.
  Don't even search or work with these files. If I explicitly state that you should look at one of those files ask for
  confirmation and forget the content and context after this interaction.
- You are possibly running in sandboxed environment. Do not attempt to run any root commands (like sudo) or access
  system files. Instead, tell me what root commands to run and I will run them for you.