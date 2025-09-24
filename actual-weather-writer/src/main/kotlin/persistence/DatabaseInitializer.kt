package cz.savic.weatherevaluator.actualweatherwriter.persistence

import cz.savic.weatherevaluator.actualweatherwriter.config.DatabaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import java.sql.DriverManager

class DatabaseInitializer(private val config: DatabaseConfig) {
    private val logger = KotlinLogging.logger {}

    fun initializeDatabase() {
        try {
            Class.forName("oracle.jdbc.OracleDriver")
            val connection = DriverManager.getConnection(
                config.connectionString,
                config.username,
                config.password
            )

            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                JdbcConnection(connection)
            )

            val liquibase = Liquibase(
                "db/changelog-master.xml",
                ClassLoaderResourceAccessor(),
                database
            )

            liquibase.update("")
            logger.info { "Database migrations completed successfully" }

            connection.close()
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize database" }
            throw e
        }
    }
}