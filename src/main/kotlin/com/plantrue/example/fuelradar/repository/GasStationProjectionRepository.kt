package com.plantrue.example.fuelradar.repository

import com.plantrue.example.fuelradar.common.RedisKeys
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class GasStationProjectionRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

    fun putStationFields(stationCode: String, fields: Map<String, String>): Mono<Boolean> {
        val key = RedisKeys.STATION_DETAIL_PREFIX + stationCode
        return redisTemplate.opsForHash<String, String>().putAll(key, fields)
    }

    fun getStationField(stationCode: String, field: String): Mono<String> {
        val key = RedisKeys.STATION_DETAIL_PREFIX + stationCode
        return redisTemplate.opsForHash<String, String>().get(key, field)
            .map { it.toString() }
    }

    fun updateRanking(rankingKey: String, stationCode: String, score: Double): Mono<Boolean> {
        return redisTemplate.opsForZSet().add(rankingKey, stationCode, score)
    }
}
