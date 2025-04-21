package cz.savic.weatherevaluator.forecastfetcher.util.serialization

import cz.savic.weatherevaluator.forecastfetcher.model.Location
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val SERIALIZATION_DELIMITER = ";"

object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }
    
    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

object LocalDateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }
    
    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}

object LocationSerializer : KSerializer<Location> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Location", PrimitiveKind.STRING)
    
    override fun serialize(encoder: Encoder, value: Location) {
        encoder.encodeString("${value.name}${SERIALIZATION_DELIMITER}${value.latitude}${SERIALIZATION_DELIMITER}${value.longitude}")
    }
    
    override fun deserialize(decoder: Decoder): Location {
        val locationString = decoder.decodeString()
        val parts = locationString.split(SERIALIZATION_DELIMITER)
        
        return Location(
            name = parts[0],
            latitude = parts[1].toDouble(),
            longitude = parts[2].toDouble()
        )
    }
}

val ForecastSerializersModule = SerializersModule {
    contextual(LocalDateTimeSerializer)
    contextual(LocalDateSerializer)
    contextual(LocationSerializer)
}

val ForecastJson = Json {
    serializersModule = ForecastSerializersModule
    prettyPrint = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}
