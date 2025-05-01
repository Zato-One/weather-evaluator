package cz.savic.weatherevaluator.forecastwriter.persistence

import cz.savic.weatherevaluator.forecastwriter.config.DatabaseConfig
import io.github.oshai.kotlinlogging.KotlinLogging
import java.sql.Connection
import java.sql.DriverManager

class DatabaseInitializer(private val config: DatabaseConfig) {
    private val logger = KotlinLogging.logger {}
    
    fun initializeTables() {
        var connection: Connection? = null
        try {
            connection = getConnection()
            
            val sqlScript = javaClass.getResourceAsStream("/db/init-tables.sql")?.bufferedReader()?.use { it.readText() }
                ?: throw IllegalStateException("Could not load initialization SQL script")
            
            val statements = sqlScript.split(";").filter { it.trim().isNotEmpty() }
            
            connection.createStatement().use { statement -> 
                for (sql in statements) {
                    statement.execute(sql.trim())
                }
                logger.info { "Database tables initialized successfully" }
            }
            
        } catch (e: Exception) {
            logger.error(e) { "Failed to initialize database tables" }
            throw e
        } finally {
            connection?.close()
        }
    }
    
    private fun getConnection(): Connection {
        return DriverManager.getConnection(
            config.connectionString,
            config.username,
            config.password
        )
    }
}