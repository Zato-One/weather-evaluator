package cz.savic.weatherevaluator.forecastevaluator.persistence.service

import cz.savic.weatherevaluator.forecastevaluator.accuracy.DailyAccuracyResult
import cz.savic.weatherevaluator.forecastevaluator.accuracy.HourlyAccuracyResult
import cz.savic.weatherevaluator.forecastevaluator.persistence.entity.DailyAccuracyEntity
import cz.savic.weatherevaluator.forecastevaluator.persistence.entity.HourlyAccuracyEntity
import cz.savic.weatherevaluator.forecastevaluator.persistence.mapper.AccuracyMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.session.SqlSession
import org.apache.ibatis.session.SqlSessionFactory

private val logger = KotlinLogging.logger {}

class AccuracyPersistenceService(private val sqlSessionFactory: SqlSessionFactory) {

    fun persistHourlyAccuracy(result: HourlyAccuracyResult) {
        val entity = HourlyAccuracyEntity.fromAccuracyResult(result)

        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(AccuracyMapper::class.java)
            mapper.insertHourlyAccuracy(entity)
            session.commit()
            logger.debug { "Persisted hourly accuracy for ${entity.locationName} at ${entity.targetDatetimeUtc}" }
        }
    }

    fun persistHourlyAccuracyBatch(results: List<HourlyAccuracyResult>) {
        if (results.isEmpty()) {
            logger.debug { "No hourly accuracy results to persist" }
            return
        }

        val entities = results.map { HourlyAccuracyEntity.fromAccuracyResult(it) }

        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(AccuracyMapper::class.java)
            entities.forEach { mapper.insertHourlyAccuracy(it) }
            session.commit()
            logger.info { "Persisted ${entities.size} hourly accuracy records" }
        }
    }

    fun persistDailyAccuracy(result: DailyAccuracyResult) {
        val entity = DailyAccuracyEntity.fromAccuracyResult(result)

        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(AccuracyMapper::class.java)
            mapper.insertDailyAccuracy(entity)
            session.commit()
            logger.debug { "Persisted daily accuracy for ${entity.locationName} on ${entity.targetDate}" }
        }
    }

    fun persistDailyAccuracyBatch(results: List<DailyAccuracyResult>) {
        if (results.isEmpty()) {
            logger.debug { "No daily accuracy results to persist" }
            return
        }

        val entities = results.map { DailyAccuracyEntity.fromAccuracyResult(it) }

        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(AccuracyMapper::class.java)
            entities.forEach { mapper.insertDailyAccuracy(it) }
            session.commit()
            logger.info { "Persisted ${entities.size} daily accuracy records" }
        }
    }

    fun getAccuracyStatistics(): AccuracyStatistics {
        sqlSessionFactory.openSession().use { session ->
            val mapper = session.getMapper(AccuracyMapper::class.java)
            val hourlyCount = mapper.countHourlyAccuracyRecords()
            val dailyCount = mapper.countDailyAccuracyRecords()

            return AccuracyStatistics(
                hourlyAccuracyRecords = hourlyCount,
                dailyAccuracyRecords = dailyCount
            )
        }
    }
}

data class AccuracyStatistics(
    val hourlyAccuracyRecords: Long,
    val dailyAccuracyRecords: Long
)