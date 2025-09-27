package cz.savic.weatherevaluator.forecastevaluator.persistence

import cz.savic.weatherevaluator.forecastevaluator.config.DatabaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import java.sql.DriverManager
import java.util.Properties

private val logger = KotlinLogging.logger {}

object DatabaseInitializer {
    private var sqlSessionFactory: SqlSessionFactory? = null

    fun initialize(config: DatabaseConfig) {
        logger.info { "Initializing database connection for forecast-evaluator..." }
        logger.info { "Connection string: ${config.connectionString}" }

        // Run Liquibase migrations first
        runLiquibaseMigrations(config)

        val properties = Properties().apply {
            setProperty("driver", "oracle.jdbc.OracleDriver")
            setProperty("url", config.connectionString)
            setProperty("username", config.username)
            setProperty("password", config.password)
        }

        val inputStream = Resources.getResourceAsStream("mybatis-config.xml")
        sqlSessionFactory = SqlSessionFactoryBuilder().build(inputStream, properties)

        logger.info { "MyBatis SqlSessionFactory initialized successfully" }
    }

    private fun runLiquibaseMigrations(config: DatabaseConfig) {
        try {
            DriverManager.getConnection(
                config.connectionString,
                config.username,
                config.password
            ).use { connection ->
                val database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(JdbcConnection(connection))

                val liquibase = Liquibase(
                    "db/changelog-master.xml",
                    ClassLoaderResourceAccessor(),
                    database
                )

                logger.info { "Running Liquibase changelog for accuracy tables..." }
                liquibase.update()
                logger.info { "Database successfully initialized via Liquibase." }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to run Liquibase migrations" }
            throw e
        }
    }

    fun getSqlSessionFactory(): SqlSessionFactory {
        return sqlSessionFactory ?: throw IllegalStateException("Database not initialized. Call initialize() first.")
    }
}