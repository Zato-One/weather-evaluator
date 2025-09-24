package cz.savic.weatherevaluator.actualweatherwriter

import kotlinx.coroutines.runBlocking

fun main() {
    val runner = ActualWeatherWriterRunner()

    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutdown hook triggered...")
        runner.close()
    })

    runBlocking {
        runner.pollOnce()
    }
}