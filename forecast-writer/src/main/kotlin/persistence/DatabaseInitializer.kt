package cz.savic.weatherevaluator.forecastwriter.persistence

import cz.savic.weatherevaluator.forecastwriter.config.DatabaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager

class DatabaseInitializer(private val config: DatabaseConfig) {
    private val logger = KotlinLogging.logger {}

    fun initializeDatabase() {
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

            logger.info { "Running Liquibase changelog..." }
            liquibase.update()
            logger.info { "Database successfully initialized via Liquibase." }
        }
    }
}
