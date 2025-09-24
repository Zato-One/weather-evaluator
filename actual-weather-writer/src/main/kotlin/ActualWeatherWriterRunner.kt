package cz.savic.weatherevaluator.actualweatherwriter

import cz.savic.weatherevaluator.actualweatherwriter.config.loadConfig
import cz.savic.weatherevaluator.actualweatherwriter.kafka.ActualWeatherEventConsumer
import cz.savic.weatherevaluator.actualweatherwriter.kafka.createKafkaConsumer
import cz.savic.weatherevaluator.actualweatherwriter.persistence.DatabaseInitializer
import cz.savic.weatherevaluator.actualweatherwriter.persistence.service.ActualWeatherBatchProcessor
import cz.savic.weatherevaluator.actualweatherwriter.persistence.service.ActualWeatherPersistenceService
import cz.savic.weatherevaluator.common.event.WeatherObservedEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.coroutineScope
import org.apache.ibatis.datasource.pooled.PooledDataSource
import org.apache.ibatis.io.Resources
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import java.util.concurrent.atomic.AtomicBoolean
import javax.sql.DataSource

class ActualWeatherWriterRunner : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val config = loadConfig()
    private val dbInitializer = DatabaseInitializer(config.database)
    private val closed = AtomicBoolean(false)

    init {
        logger.info { "Starting actual-weather-writer..." }

        logger.info { "Running Liquibase migrations..." }
        dbInitializer.initializeDatabase()
    }

    private val dataSource: DataSource = PooledDataSource(
        "oracle.jdbc.OracleDriver",
        config.database.connectionString,
        config.database.username,
        config.database.password
    )
    private val environment = Environment("dev", JdbcTransactionFactory(), dataSource)

    private val sqlSessionFactory: SqlSessionFactory = SqlSessionFactoryBuilder()
        .build(Resources.getResourceAsReader("mybatis-config.xml"))
        .apply { configuration.environment = environment }

    private val actualWeatherPersistenceService = ActualWeatherPersistenceService(sqlSessionFactory)

    private val batchProcessor = ActualWeatherBatchProcessor { events ->
        actualWeatherPersistenceService.persistBatch(events)
    }

    private val kafkaConsumer = createKafkaConsumer(config.kafka)
    private val eventConsumer = ActualWeatherEventConsumer(kafkaConsumer, config.kafka)

    suspend fun pollOnce() = coroutineScope {
        eventConsumer.poll { event ->
            batchProcessor.submit(event)
            logger.trace { "Received weather observed event: $event" }
        }
    }

    override fun close() {
        if (closed.getAndSet(true)) return

        logger.info { "Closing actual-weather-writer..." }
        eventConsumer.logStats()
        kafkaConsumer.close()
        logger.info { "Shutdown completed" }
    }
}