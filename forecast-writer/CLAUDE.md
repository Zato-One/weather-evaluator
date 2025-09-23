# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Service Overview

Forecast-writer is a Kotlin microservice that consumes forecast events from Kafka and persists them to Oracle database. It operates as a one-time execution job, polling messages once and then terminating.

## Development Commands

### Building

```bash
# Build fat JAR (from project root)
./gradlew :forecast-writer:shadowJar

# Build via Docker (from project root)
docker build -f forecast-writer/Dockerfile -t forecast-writer .

# Run locally (requires Kafka and Oracle running)
java -jar forecast-writer/build/libs/forecast-writer.jar
```

### Testing

```bash
# Run tests for this service only
./gradlew :forecast-writer:test

# Run specific test class
./gradlew :forecast-writer:test --tests "SpecificTestClass"
```

### Docker Development

```bash
# Run service in Docker Compose stack (from project root)
docker-compose up forecast-writer

# View service logs
docker logs -f weather-evaluator-forecast-writer-1

# Connect to Oracle DB for debugging
docker exec -it oracle sqlplus weather_evaluator/weather_evaluator@XEPDB1
```

## Service Architecture

### Core Components

**ForecastWriterRunner** - Main orchestrator that:
- Initializes database with Liquibase migrations on startup
- Creates MyBatis SqlSessionFactory with Oracle connection pooling
- Sets up Kafka consumer with batch processing capabilities
- Manages service lifecycle and cleanup

**Database Layer** - Oracle integration with MyBatis:
- `DatabaseInitializer` - Runs Liquibase migrations at startup
- `ForecastMapper` - MyBatis interface for SQL operations
- `ForecastPersistenceService` - Business logic for event-to-entity mapping
- Entity classes: `DailyForecastEntity`, `HourlyForecastEntity`

**Kafka Integration** - Event consumption:
- `ForecastEventConsumer` - Polls messages from `forecast.fetched` topic
- `ForecastBatchProcessor` - Batches events for efficient database writes
- Consumes `DailyForecastFetchedEvent` and `HourlyForecastFetchedEvent`

### Execution Flow

1. **Startup**: Load config → Run Liquibase migrations → Initialize MyBatis → Setup Kafka consumer
2. **Database Schema**: Create tables `forecast_daily` and `forecast_hourly` if not exist
3. **Message Processing**: Poll Kafka → Transform events to entities → Batch insert to Oracle
4. **Duplicate Handling**: Uses Oracle `IGNORE_ROW_ON_DUPKEY_INDEX` hint for idempotent inserts
5. **Cleanup**: Log statistics → Close Kafka consumer → Close database connections

### Database Integration Details

**Schema Management**: Liquibase changelog in `src/main/resources/db/`
**Connection Pooling**: Apache MyBatis PooledDataSource with Oracle JDBC driver
**SQL Mapping**: XML-based mappers in `src/main/resources/mapper/`
**Duplicate Prevention**: Unique constraints with Oracle-specific SQL hints
**Transaction Management**: Auto-commit enabled for batch inserts

## Configuration

### Required Files

- `application.conf` - Kafka and database settings
- `mybatis-config.xml` - MyBatis configuration
- `db/changelog-master.xml` - Liquibase database migrations

### Environment Variables

- `KAFKA_BOOTSTRAP_SERVERS` - Overrides default Kafka connection
- `ORACLE_CONNECTION_STRING` - Overrides default Oracle connection

### Database Setup

Default connection targets user `weather_evaluator` in Oracle container:
```
Host: localhost:1521
Service: XEPDB1
User: weather_evaluator/weather_evaluator
```

### Kafka Configuration

Consumer settings in `application.conf`:
```hocon
kafka {
  groupId = "forecast-writer"
  topics = ["forecast.fetched"]
  pollTimeoutMs = 100
}
```

## Dependencies

Key libraries specific to this service:
- **Oracle JDBC** - Database connectivity with connection pooling
- **MyBatis** - SQL mapping framework with XML-based configuration
- **Liquibase** - Database schema migration and versioning
- **Kafka Clients** - Consumer for polling forecast events
- **SLF4J Bridge** - Routes java.util.logging to SLF4J for unified logging

## Development Notes

- Service designed as one-shot execution, not long-running daemon
- Main class: `cz.savic.weatherevaluator.forecastwriter.MainKt`
- Fat JAR includes Oracle JDBC driver and all dependencies
- Database migrations run automatically on startup
- Uses Oracle-specific SQL hints for performance optimization
- Shutdown hook ensures clean resource closure
- TODO comment indicates future support for continuous polling mode