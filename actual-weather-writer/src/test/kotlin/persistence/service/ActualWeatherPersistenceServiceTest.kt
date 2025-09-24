package cz.savic.weatherevaluator.actualweatherwriter.persistence.service

import cz.savic.weatherevaluator.actualweatherwriter.persistence.entity.ActualWeatherEntity
import cz.savic.weatherevaluator.actualweatherwriter.persistence.mapper.ActualWeatherMapper
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import cz.savic.weatherevaluator.common.model.Location
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class ActualWeatherPersistenceServiceTest {

    @Test
    fun `should persist weather observed events to database`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val mockSession = mockk<SqlSession>(relaxed = true)
        val mockMapper = mockk<ActualWeatherMapper>(relaxed = true)

        every { mockSessionFactory.openSession(true) } returns mockSession
        every { mockSession.getMapper(ActualWeatherMapper::class.java) } returns mockMapper

        val service = ActualWeatherPersistenceService(mockSessionFactory)

        val events = listOf(
            WeatherObservedEvent(
                source = "TestSource",
                location = Location("TestLocation", 50.0, 14.0),
                observedTimeUtc = LocalDateTime.of(2025, 1, 1, 12, 0),
                temperatureC = 20.0,
                precipitationMm = 5.0,
                windSpeedKph10m = 15.0
            )
        )

        service.persistBatch(events)

        verify { mockMapper.insert(any<ActualWeatherEntity>()) }
        verify { mockSession.flushStatements() }
        verify { mockSession.close() }
    }

    @Test
    fun `should handle empty event list`() {
        val mockSessionFactory = mockk<SqlSessionFactory>()
        val service = ActualWeatherPersistenceService(mockSessionFactory)

        service.persistBatch(emptyList())

        verify(exactly = 0) { mockSessionFactory.openSession(any()) }
    }
}