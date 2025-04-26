package cz.savic.weatherevaluator.common.model

import kotlinx.serialization.Serializable

@Serializable
data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double
)