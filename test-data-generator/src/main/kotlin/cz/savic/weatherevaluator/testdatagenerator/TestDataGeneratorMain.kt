package cz.savic.weatherevaluator.testdatagenerator

import cz.savic.weatherevaluator.testdatagenerator.config.GeneratorConfig
import cz.savic.weatherevaluator.testdatagenerator.generator.TestDataGenerator
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    logger.info { "Starting test data generation..." }

    try {
        val config = GeneratorConfig.load()
        val generator = TestDataGenerator(config.database, config.generation)

        generator.generateAllData()

        logger.info { "Test data generation completed successfully" }
    } catch (e: Exception) {
        logger.error(e) { "Test data generation failed" }
        throw e
    }
}