# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Service Overview

Forecast-fetcher is a Kotlin microservice that fetches weather forecasts from multiple public APIs and publishes them to Kafka. It operates as a one-time execution job, fetching forecasts for configured locations and then terminating.

## Development Commands

### Building

```bash
# Build fat JAR (from project root)
./gradlew :forecast-fetcher:shadowJar

# Build via Docker (from project root)
docker build -f forecast-fetcher/Dockerfile -t forecast-fetcher .

# Run locally (requires Kafka running)
java -jar forecast-fetcher/build/libs/forecast-fetcher.jar
```

### Testing

```bash
# Run tests for this service only
./gradlew :forecast-fetcher:test

# Run specific test class
./gradlew :forecast-fetcher:test --tests "SpecificTestClass"
```

### Docker Development

```bash
# Run service in Docker Compose stack (from project root)
docker-compose up forecast-fetcher

# View service logs
docker logs -f weather-evaluator-forecast-fetcher-1
```

## Service Architecture

### Core Components

**ForecastFetcherRunner** - Main orchestrator that:
- Initializes HTTP client with Ktor CIO engine and JSON content negotiation
- Creates weather API adapters (OpenMeteo + WeatherAPI if key available)
- Coordinates concurrent fetching across all locations
- Manages Kafka producer lifecycle and cleanup

**Adapter Pattern** - Weather API integrations via `ForecastProvider` interface:
- `OpenMeteoAdapter` - Free API, no authentication
- `WeatherApiAdapter` - Commercial API requiring `weather-api.key` in secrets.properties

**Configuration System** - Uses Hoplite for HOCON configuration:
- Main config: `src/main/resources/application.conf`
- Secrets: `src/main/resources/secrets.properties` (not in git)
- Environment override: `KAFKA_BOOTSTRAP_SERVERS`

### Execution Flow

1. **Startup**: Load config → Initialize HTTP client → Create adapters → Setup Kafka producer
2. **Fetch Phase**: For each location, run all adapters concurrently using coroutines
3. **Processing**: Transform API responses to `ForecastResult` → Map to `ForecastFetchedEvent`
4. **Publishing**: Send events to Kafka topic `forecast.fetched`
5. **Cleanup**: Log statistics → Flush Kafka producer → Close resources

### API Integration Details

**Data Granularities**: Both adapters support daily and hourly forecasts
**Error Handling**: Per-adapter failure isolation - one failing adapter doesn't stop others
**Rate Limiting**: None implemented - relies on API quotas
**Response Mapping**: Custom response DTOs for each API → Common `ForecastResult` interface

## Configuration

### Required Files

- `application.conf` - Kafka settings and location list
- `secrets.properties` - API keys (create manually, see SecretLoader.kt:22)

### Environment Variables

- `KAFKA_BOOTSTRAP_SERVERS` - Overrides default Kafka connection

### Adding New Locations

Edit `application.conf`:
```hocon
locations = [
  { name = "NewCity", latitude = 48.1234, longitude = 17.5678 }
]
```

### Adding WeatherAPI Support

Create `src/main/resources/secrets.properties`:
```properties
weather-api.key=your_api_key_here
```

## Dependencies

Key libraries specific to this service:
- **Ktor Client** - HTTP client with CIO engine for async requests
- **kotlinx.serialization** - JSON handling for API responses
- **Hoplite** - HOCON configuration loading with environment variable support
- **Kafka Clients** - Producer for publishing forecast events

## Development Notes

- Service designed as one-shot execution, not long-running daemon
- Main class: `cz.savic.weatherevaluator.forecastfetcher.MainKt`
- Fat JAR includes all dependencies via Shadow plugin
- Uses structured logging with correlation of adapter execution
- Shutdown hook ensures clean Kafka producer closure