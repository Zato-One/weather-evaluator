package cz.savic.weatherevaluator.forecastfetcher.util

object ApiResponseSamples {

    val openMeteoSuccessResponse = """
        {
          "daily": {
            "time": ["2024-01-15", "2024-01-16"],
            "temperature_2m_min": [2.5, 3.1],
            "temperature_2m_max": [8.2, 9.4],
            "temperature_2m_mean": [5.3, 6.2],
            "precipitation_sum": [0.1, 2.4],
            "wind_speed_10m_max": [12.5, 15.8]
          },
          "hourly": {
            "time": ["2024-01-15T00:00", "2024-01-15T01:00"],
            "temperature_2m": [5.2, 4.8],
            "precipitation": [0.0, 0.1],
            "wind_speed_10m": [10.2, 11.5]
          }
        }
    """.trimIndent()

    val openMeteoErrorResponse = """
        {
          "error": true,
          "reason": "Invalid coordinates"
        }
    """.trimIndent()

    val weatherApiSuccessResponse = """
        {
          "forecast": {
            "forecastday": [
              {
                "date": "2024-01-15",
                "day": {
                  "mintemp_c": 2.5,
                  "maxtemp_c": 8.2,
                  "avgtemp_c": 5.3,
                  "totalprecip_mm": 0.1,
                  "maxwind_kph": 12.5
                },
                "hour": [
                  {
                    "time": "2024-01-15 00:00",
                    "temp_c": 5.2,
                    "precip_mm": 0.0,
                    "wind_kph": 10.2
                  },
                  {
                    "time": "2024-01-15 01:00",
                    "temp_c": 4.8,
                    "precip_mm": 0.1,
                    "wind_kph": 11.5
                  }
                ]
              }
            ]
          }
        }
    """.trimIndent()

    val weatherApiLocationErrorResponse = """
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