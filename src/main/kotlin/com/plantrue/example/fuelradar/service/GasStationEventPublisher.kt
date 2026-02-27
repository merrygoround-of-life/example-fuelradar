package com.plantrue.example.fuelradar.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.f4b6a3.uuid.UuidCreator
import com.plantrue.example.fuelradar.common.RedisKeys
import com.plantrue.example.fuelradar.event.GasStationChangeEvent
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GasStationEventPublisher(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) {

    fun publishEvent(event: GasStationChangeEvent): Mono<String> {
        val eventData = mapOf(
            "eventId" to event.eventId,
            "stationCode" to event.stationCode,
            "eventType" to event.eventType.name,
            "changedFields" to objectMapper.writeValueAsString(event.changedFields),
            "timestamp" to event.timestamp.toString()
        )

        return redisTemplate.opsForStream<String, String>()
            .add(RedisKeys.STREAM_KEY, eventData)
            .map { recordId -> recordId.value }
    }

    fun generateEventId(): String = UuidCreator.getTimeOrderedEpoch().toString()
}
