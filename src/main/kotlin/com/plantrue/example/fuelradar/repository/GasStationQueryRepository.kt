package com.plantrue.example.fuelradar.repository

import com.plantrue.example.fuelradar.common.RedisKeys
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class GasStationQueryRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {

    fun getRankingRange(rankingKey: String, start: Long, end: Long): Flux<String> {
        return redisTemplate.opsForZSet()
            .range(rankingKey, Range.closed(start, end))
    }

    fun getRankingSize(rankingKey: String): Mono<Long> {
        return redisTemplate.opsForZSet().size(rankingKey)
    }

    fun getStationDetail(stationCode: String): Mono<Map<String, String>> {
        val key = RedisKeys.STATION_DETAIL_PREFIX + stationCode
        return redisTemplate.opsForHash<String, String>()
            .entries(key)
            .collectMap({ it.key }, { it.value })
    }
}
