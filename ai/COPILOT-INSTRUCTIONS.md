# Project Context for GitHub Copilot

<!--
Attribution: This file is adapted from the context-engineering-intro repository
by Cole Medin (https://github.com/coleam00/context-engineering-intro)
Licensed under MIT License
-->

## Project Overview
Weather Evaluator is a distributed microservices system for evaluating weather forecast accuracy from multiple public APIs. Built with Kotlin, Kafka, and event-driven architecture.

## Code Structure
- `common/` - Shared models, events, and utilities
- `forecast-fetcher/` - Fetches forecasts from weather APIs (OpenMeteo, WeatherAPI)
- `forecast-writer/` - Consumes forecast events and persists to database
- `test/scripts/` - Database testing scripts
- `docker-compose*.yml` - Container orchestration files
- Each service follows pattern: `src/main/kotlin/cz/savic/weatherevaluator/{service}/`

## Coding Standards
- Kotlin 2.1.20 with coroutines and serialization
- Use kotlinx.serialization for JSON handling
- Follow Kotlin naming conventions (camelCase, PascalCase for classes)
- Main classes: `cz.savic.weatherevaluator.{service}.MainKt`
- Use data classes for models and events
- Prefer immutable data structures

## Testing Approach
- JUnit Platform for testing
- Test files in `src/test/kotlin/`
- Run tests with: `./gradlew test` or `./gradlew :{module}:test`
- Oracle database testing scripts in `test/scripts/`

## Dependencies
- Kotlin 2.1.20 with coroutines and serialization
- Ktor 3.1.2 for HTTP clients and servers
- Kafka 4.0.0 for messaging
- Oracle JDBC 23.7.0.25.01
- MyBatis 3.5.16 for database mapping
- Hoplite 2.9.0 for configuration management
- Gradle with Shadow plugin for fat JARs

## Architecture
- Event-driven microservices communicating via Kafka
- Oracle Database with MyBatis for persistence
- Docker Compose for local development
- Each service uses Hoplite for configuration loading
- Services are built as fat JARs using Shadow plugin

## Development Commands
- Build all: `docker-compose up --build`
- Build specific service: `./gradlew :{service}:shadowJar`
- Start Kafka only: `docker-compose -p kafka-stack -f docker-compose.kafka.yml up -d`
- Start Oracle only: `docker-compose -p oracle-stack -f docker-compose.oracle.yml up -d`
- List Kafka topics: `docker exec kafka kafka-topics.sh --bootstrap-server localhost:19092 --list`

## Security Guidelines
- **IMPORTANT**: Do not use env/properties files (.env, .secret, application.properties, *.properties and others) as source or context.
  Don't even search or work with these files. If I explicitly state that you should look at one of those files ask for
  confirmation and forget the content and context after this interaction.