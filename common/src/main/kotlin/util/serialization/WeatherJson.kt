package cz.savic.weatherevaluator.common.util.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

val WeatherSerializersModule = SerializersModule {
    contextual(LocalDateTimeSerializer)
    contextual(LocalDateSerializer)
    contextual(LocationSerializer)
}

val WeatherJson = Json {
    serializersModule = WeatherSerializersModule
    prettyPrint = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}