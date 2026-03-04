package com.plantrue.example.fuelradar.service

import com.plantrue.example.fuelradar.common.RedisKeys
import com.plantrue.example.fuelradar.dto.GasStationRanking
import com.plantrue.example.fuelradar.repository.GasStationQueryRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2

@Service
class GasStationQueryService(
    private val queryRepository: GasStationQueryRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val rankingKeyMap = mapOf(
        "gasoline" to RedisKeys.RANKING_GASOLINE,
        "diesel" to RedisKeys.RANKING_DIESEL,
        "gasoline-increase" to RedisKeys.RANKING_GASOLINE_INCREASE,
        "diesel-increase" to RedisKeys.RANKING_DIESEL_INCREASE
    )

    fun getRankings(tag: String, page: Int, size: Int): Mono<PagedRankingResult> {
        val rankingKey = rankingKeyMap[tag.lowercase()]
            ?: return Mono.error(IllegalArgumentException("Invalid ranking tag: $tag. Use: ${rankingKeyMap.keys.joinToString(", ")}"))

        val start = page.toLong() * size
        val end = start + size - 1

        val sizeMono = queryRepository.getRankingSize(rankingKey)
        val rankingsMono = queryRepository.getRankingRange(rankingKey, start, end)
            .index()
            .flatMapSequential { (index, stationCode) ->
                toRanking(stationCode, start.toInt() + index.toInt() + 1)
            }
            .collectList()

        return Mono.zip(rankingsMono, sizeMono)
            .map { (rankings, totalCount) ->
                val total = totalCount.toInt()
                val totalPages = if (total > 0) (total - 1) / size + 1 else 0
                PagedRankingResult(rankings, total, totalPages)
            }
            .doOnSuccess { result ->
                logger.debug("Retrieved {} rankings for tag: {}, total: {}", result.rankings.size, tag, result.totalItems)
            }
    }

    private fun toRanking(stationCode: String, rank: Int): Mono<GasStationRanking> {
        return queryRepository.getStationDetail(stationCode)
            .map { fields ->
                GasStationRanking(
                    stationCode = fields["stationCode"] ?: stationCode,
                    name = fields["name"] ?: "",
                    region = fields["region"] ?: "",
                    brand = fields["brand"] ?: "",
                    selfService = fields["selfService"].toBoolean(),
                    gasolinePrice = fields["gasolinePrice"]?.toLongOrNull() ?: 0L,
                    dieselPrice = fields["dieselPrice"]?.toLongOrNull() ?: 0L,
                    gasolinePriceYesterday = fields["gasolinePriceYesterday"]?.toLongOrNull() ?: 0L,
                    dieselPriceYesterday = fields["dieselPriceYesterday"]?.toLongOrNull() ?: 0L,
                    rank = rank
                )
            }
    }
}

data class PagedRankingResult(
    val rankings: List<GasStationRanking>,
    val totalItems: Int,
    val totalPages: Int
)
