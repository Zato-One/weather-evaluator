package cz.savic.weatherevaluator.forecastwriter

import cz.savic.weatherevaluator.common.event.ForecastFetchedEvent
import cz.savic.weatherevaluator.forecastwriter.config.loadConfig
import cz.savic.weatherevaluator.forecastwriter.kafka.ForecastEventConsumer
import cz.savic.weatherevaluator.forecastwriter.kafka.createKafkaConsumer
import cz.savic.weatherevaluator.forecastwriter.persistence.DatabaseInitializer
import cz.savic.weatherevaluator.forecastwriter.persistence.service.ForecastBatchProcessor
import cz.savic.weatherevaluator.forecastwriter.persistence.service.ForecastPersistenceService
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

class ForecastWriterRunner : AutoCloseable {
    private val logger = KotlinLogging.logger {}
    private val config = loadConfig()
    private val dbInitializer = DatabaseInitializer(config.database)
    private val closed = AtomicBoolean(false)

    init {
        logger.info { "Starting forecast-writer..." }

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

    private val forecastPersistenceService = ForecastPersistenceService(sqlSessionFactory)

    private val batchProcessor = ForecastBatchProcessor { events ->
        forecastPersistenceService.persistBatch(events)
    }

    private val kafkaConsumer = createKafkaConsumer(config.kafka)
    private val eventConsumer = ForecastEventConsumer(kafkaConsumer, config.kafka)

    suspend fun pollOnce() = coroutineScope {
        eventConsumer.poll { event ->
            batchProcessor.submit(event)
            logger.trace { "Received forecast event: $event" }
        }
    }

    // TODO create method to poll periodically (remove pollOnce when it's created or keep it too?)

    override fun close() {
        if (closed.getAndSet(true)) return

        logger.info { "Closing forecast-writer..." }
        eventConsumer.logStats()
        kafkaConsumer.close()
        logger.info { "Shutdown completed" }
    }
}