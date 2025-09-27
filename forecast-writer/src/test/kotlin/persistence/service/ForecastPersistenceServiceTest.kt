package cz.savic.weatherevaluator.forecastwriter.persistence.service

import cz.savic.weatherevaluator.forecastwriter.persistence.entity.DailyForecastEntity
import cz.savic.weatherevaluator.forecastwriter.persistence.entity.HourlyForecastEntity
import cz.savic.weatherevaluator.forecastwriter.persistence.mapper.ForecastMapper
import cz.savic.weatherevaluator.common.event.DailyForecastFetchedEvent
import cz.savic.weatherevaluator.common.event.HourlyForecastFetchedEvent
import cz.savic.weatherevaluator.common.model.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ForecastPersistenceServiceTest {

    @Test
    fun `should persist daily forecast events to database`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val mockSession = mockk<SqlSession>(relaxed = true)
        val mockMapper = mockk<ForecastMapper>(relaxed = true)

        every { mockSessionFactory.openSession(true) } returns mockSession
        every { mockSession.getMapper(ForecastMapper::class.java) } returns mockMapper

        val service = ForecastPersistenceService(mockSessionFactory)

        val events = listOf(
            DailyForecastFetchedEvent(
                source = "TestSource",
                location = Location("TestLocation", 50.0, 14.0),
                forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
                targetDate = LocalDate.of(2025, 1, 2),
                temperatureMinC = 10.0,
                temperatureMaxC = 20.0,
                temperatureMeanC = 15.0,
                precipitationMmSum = 5.0,
                windSpeedKph10mMax = 25.0
            )
        )

        service.persistBatch(events)

        verify { mockMapper.insertDaily(any<DailyForecastEntity>()) }
        verify(exactly = 0) { mockMapper.insertHourly(any()) }
    }

    @Test
    fun `should persist hourly forecast events to database`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val mockSession = mockk<SqlSession>(relaxed = true)
        val mockMapper = mockk<ForecastMapper>(relaxed = true)

        every { mockSessionFactory.openSession(true) } returns mockSession
        every { mockSession.getMapper(ForecastMapper::class.java) } returns mockMapper

        val service = ForecastPersistenceService(mockSessionFactory)

        val events = listOf(
            HourlyForecastFetchedEvent(
                source = "TestSource",
                location = Location("TestLocation", 50.0, 14.0),
                forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
                targetDateTimeUtc = LocalDateTime.of(2025, 1, 2, 15, 0),
                temperatureC = 15.0,
                precipitationMm = 2.0,
                windSpeedKph10m = 20.0
            )
        )

        service.persistBatch(events)

        verify { mockMapper.insertHourly(any<HourlyForecastEntity>()) }
        verify(exactly = 0) { mockMapper.insertDaily(any()) }
    }

    @Test
    fun `should persist mixed daily and hourly forecast events`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val mockSession = mockk<SqlSession>(relaxed = true)
        val mockMapper = mockk<ForecastMapper>(relaxed = true)

        every { mockSessionFactory.openSession(true) } returns mockSession
        every { mockSession.getMapper(ForecastMapper::class.java) } returns mockMapper

        val service = ForecastPersistenceService(mockSessionFactory)

        val events = listOf(
            DailyForecastFetchedEvent(
                source = "TestSource",
                location = Location("TestLocation", 50.0, 14.0),
                forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
                targetDate = LocalDate.of(2025, 1, 2),
                temperatureMinC = 10.0,
                temperatureMaxC = 20.0,
                temperatureMeanC = 15.0,
                precipitationMmSum = 5.0,
                windSpeedKph10mMax = 25.0
            ),
            HourlyForecastFetchedEvent(
                source = "TestSource",
                location = Location("TestLocation", 50.0, 14.0),
                forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
                targetDateTimeUtc = LocalDateTime.of(2025, 1, 2, 15, 0),
                temperatureC = 15.0,
                precipitationMm = 2.0,
                windSpeedKph10m = 20.0
            )
        )

        service.persistBatch(events)

        verify { mockMapper.insertDaily(any<DailyForecastEntity>()) }
        verify { mockMapper.insertHourly(any<HourlyForecastEntity>()) }
    }

    @Test
    fun `should handle empty event list`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val mockSession = mockk<SqlSession>(relaxed = true)
        val mockMapper = mockk<ForecastMapper>(relaxed = true)

        every { mockSessionFactory.openSession(true) } returns mockSession
        every { mockSession.getMapper(ForecastMapper::class.java) } returns mockMapper

        val service = ForecastPersistenceService(mockSessionFactory)

        service.persistBatch(emptyList())

        verify { mockSessionFactory.openSession(true) }
        verify(exactly = 0) { mockMapper.insertDaily(any()) }
        verify(exactly = 0) { mockMapper.insertHourly(any()) }
    }

    @Test
    fun `should correctly map daily event fields to entity`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val mockSession = mockk<SqlSession>(relaxed = true)
        val mockMapper = mockk<ForecastMapper>(relaxed = true)

        every { mockSessionFactory.openSession(true) } returns mockSession
        every { mockSession.getMapper(ForecastMapper::class.java) } returns mockMapper

        val service = ForecastPersistenceService(mockSessionFactory)

        val event = DailyForecastFetchedEvent(
            source = "OpenMeteo",
            location = Location("Prague", 50.0755, 14.4378),
            forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 30),
            targetDate = LocalDate.of(2025, 1, 3),
            temperatureMinC = -5.0,
            temperatureMaxC = 25.0,
            temperatureMeanC = 10.0,
            precipitationMmSum = 15.5,
            windSpeedKph10mMax = 30.2
        )

        service.persistBatch(listOf(event))

        verify {
            mockMapper.insertDaily(match<DailyForecastEntity> { entity ->
                entity.source == "OpenMeteo" &&
                entity.locationName == "Prague" &&
                entity.latitude == 50.0755 &&
                entity.longitude == 14.4378 &&
                entity.forecastTimeUtc == LocalDateTime.of(2025, 1, 1, 12, 30) &&
                entity.targetDate == LocalDate.of(2025, 1, 3) &&
                entity.temperatureMinC == -5.0 &&
                entity.temperatureMaxC == 25.0 &&
                entity.temperatureMeanC == 10.0 &&
                entity.precipitationMmSum == 15.5 &&
                entity.windSpeedKph10mMax == 30.2
            })
        }
    }

    @Test
    fun `should correctly map hourly event fields to entity`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val mockSession = mockk<SqlSession>(relaxed = true)
        val mockMapper = mockk<ForecastMapper>(relaxed = true)

        every { mockSessionFactory.openSession(true) } returns mockSession
        every { mockSession.getMapper(ForecastMapper::class.java) } returns mockMapper

        val service = ForecastPersistenceService(mockSessionFactory)

        val event = HourlyForecastFetchedEvent(
            source = "WeatherAPI",
            location = Location("Brno", 49.1951, 16.6068),
            forecastTimeUtc = LocalDateTime.of(2025, 1, 1, 14, 45),
            targetDateTimeUtc = LocalDateTime.of(2025, 1, 2, 18, 0),
            temperatureC = 12.5,
            precipitationMm = 0.5,
            windSpeedKph10m = 18.7
        )

        service.persistBatch(listOf(event))

        verify {
            mockMapper.insertHourly(match<HourlyForecastEntity> { entity ->
                entity.source == "WeatherAPI" &&
                entity.locationName == "Brno" &&
                entity.latitude == 49.1951 &&
                entity.longitude == 16.6068 &&
                entity.forecastTimeUtc == LocalDateTime.of(2025, 1, 1, 14, 45) &&
                entity.targetDateTimeUtc == LocalDateTime.of(2025, 1, 2, 18, 0) &&
                entity.temperatureC == 12.5 &&
                entity.precipitationMm == 0.5 &&
                entity.windSpeedKph10m == 18.7
            })
        }
    }
}