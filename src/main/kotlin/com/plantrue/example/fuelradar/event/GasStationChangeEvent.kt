package com.plantrue.example.fuelradar.event

import java.time.LocalDateTime

data class GasStationChangeEvent(
    val eventId: String,
    val stationCode: String,
    val eventType: GasStationChangeEventType,
    val changedFields: Map<String, Any>,
    val timestamp: LocalDateTime
)

enum class GasStationChangeEventType {
    CREATED,
    UPDATED
}
