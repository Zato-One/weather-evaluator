package cz.savic.weatherevaluator.common.util.serialization

import cz.savic.weatherevaluator.common.model.Location
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

const val SERIALIZATION_DELIMITER = ";"

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