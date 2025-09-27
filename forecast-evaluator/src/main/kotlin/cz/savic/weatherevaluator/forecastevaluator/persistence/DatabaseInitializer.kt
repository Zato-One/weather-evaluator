package cz.savic.weatherevaluator.forecastevaluator.persistence

import cz.savic.weatherevaluator.forecastevaluator.config.DatabaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import java.util.Properties

private val logger = KotlinLogging.logger {}

object DatabaseInitializer {
    private var sqlSessionFactory: SqlSessionFactory? = null

    fun initialize(config: DatabaseConfig) {
        logger.info { "Initializing database connection for forecast-evaluator..." }
        logger.info { "Connection string: ${config.connectionString}" }

        val properties = Properties().apply {
            setProperty("driver", "oracle.jdbc.OracleDriver")
            setProperty("url", "jdbc:oracle:thin:${config.connectionString}")
            setProperty("username", config.username)
            setProperty("password", config.password)
        }

        val inputStream = Resources.getResourceAsStream("mybatis-config.xml")
        sqlSessionFactory = SqlSessionFactoryBuilder().build(inputStream, properties)

        logger.info { "MyBatis SqlSessionFactory initialized successfully" }
    }

    fun getSqlSessionFactory(): SqlSessionFactory {
        return sqlSessionFactory ?: throw IllegalStateException("Database not initialized. Call initialize() first.")
    }
}