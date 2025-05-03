package cz.savic.weatherevaluator.forecastwriter.persistence

import cz.savic.weatherevaluator.forecastwriter.config.DatabaseConfig
import cz.savic.weatherevaluator.forecastwriter.persistence.mappers.DailyForecastMapper
import cz.savic.weatherevaluator.forecastwriter.persistence.mappers.HourlyForecastMapper
import cz.savic.weatherevaluator.forecastwriter.persistence.typehandlers.LocalDateTimeTypeHandler
import cz.savic.weatherevaluator.forecastwriter.persistence.typehandlers.LocalDateTypeHandler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.mapping.Environment
import org.apache.ibatis.session.Configuration
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory
import java.util.Properties
import javax.sql.DataSource
import org.apache.ibatis.datasource.pooled.PooledDataSource

class MyBatisConfig(private val dbConfig: DatabaseConfig) {
    private val logger = KotlinLogging.logger {}

    fun createSqlSessionFactory(): SqlSessionFactory {
        logger.info { "Initializing MyBatis SqlSessionFactory" }

        val dataSource = createDataSource()
        val environment = Environment("development", JdbcTransactionFactory(), dataSource)
        val configuration = Configuration(environment)

        configuration.typeHandlerRegistry.register(LocalDateTimeTypeHandler::class.java)
        configuration.typeHandlerRegistry.register(LocalDateTypeHandler::class.java)

        configuration.addMapper(DailyForecastMapper::class.java)
        configuration.addMapper(HourlyForecastMapper::class.java)

        configuration.isMapUnderscoreToCamelCase = true
        configuration.isUseGeneratedKeys = true

        return SqlSessionFactoryBuilder().build(configuration)
    }

    private fun createDataSource(): DataSource {
        return PooledDataSource(
            "oracle.jdbc.OracleDriver",
            dbConfig.connectionString,
            dbConfig.username,
            dbConfig.password,
            Properties().apply {
                setProperty("poolMaximumActiveConnections", "10")
                setProperty("poolMaximumIdleConnections", "5")
                setProperty("poolTimeToWait", "30000")
            }
        )
    }
}