package cz.savic.weatherevaluator.actualweatherfetcher.util

object ApiResponseSamples {

    val openMeteoCurrentSuccessResponse = """
        {
          "current": {
            "time": "2024-01-15T14:30",
            "temperature_2m": 5.2,
            "precipitation": 0.1,
            "wind_speed_10m": 10.2
          }
        }
    """.trimIndent()

    val openMeteoCurrentErrorResponse = """
        {
          "error": true,
          "reason": "Invalid coordinates"
        }
    """.trimIndent()

    val weatherApiCurrentSuccessResponse = """
        {
          "current": {
            "temp_c": 5.2,
            "precip_mm": 0.1,
            "wind_kph": 10.2
          }
        }
    """.trimIndent()

    val weatherApiCurrentErrorResponse = """
        {
          "error": {
            "code": 1006,
            "message": "No matching location found."
          }
        }
    """.trimIndent()

    val weatherApiInvalidKeyErrorResponse = """
        {
          "error": {
            "code": 2006,
            "message": "API key provided is invalid."
          }
        }
    """.trimIndent()
}