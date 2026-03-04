package com.plantrue.example.fuelradar.service

import com.plantrue.example.fuelradar.common.RedisKeys
import com.plantrue.example.fuelradar.event.GasStationChangeEvent
import com.plantrue.example.fuelradar.event.GasStationChangeEventType
import com.plantrue.example.fuelradar.repository.GasStationProjectionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GasStationProjectionService(
    private val projectionRepository: GasStationProjectionRepository
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun project(event: GasStationChangeEvent): Mono<Boolean> {
        return when (event.eventType) {
            GasStationChangeEventType.CREATED -> projectCreated(event)
            GasStationChangeEventType.UPDATED -> projectUpdated(event)
        }.doOnSuccess {
            logger.debug("Projected {} event for station {}", event.eventType, event.stationCode)
        }
    }

    private fun projectCreated(event: GasStationChangeEvent): Mono<Boolean> {
        val fields = event.changedFields
        val gasolinePrice = toLongOrNull(fields["gasolinePrice"])
        val dieselPrice = toLongOrNull(fields["dieselPrice"])
        val gasolinePriceYesterday = toLongOrNull(fields["gasolinePriceYesterday"]) ?: gasolinePrice
        val dieselPriceYesterday = toLongOrNull(fields["dieselPriceYesterday"]) ?: dieselPrice

        val hashFields = fields.mapValues { it.value.toString() }.toMutableMap()
        hashFields["stationCode"] = event.stationCode
        hashFields["gasolinePriceYesterday"] = gasolinePriceYesterday.toString()
        hashFields["dieselPriceYesterday"] = dieselPriceYesterday.toString()

        return Mono.zip(
            projectionRepository.putStationFields(event.stationCode, hashFields),
            updatePriceRankings(event.stationCode, gasolinePrice, gasolinePriceYesterday, dieselPrice, dieselPriceYesterday)
        ).map { it.t1 && it.t2 }
    }

    private fun projectUpdated(event: GasStationChangeEvent): Mono<Boolean> {
        val fields = event.changedFields
        val newGasolinePrice = toLongOrNull(fields["gasolinePrice"])
        val newDieselPrice = toLongOrNull(fields["dieselPrice"])

        if (newGasolinePrice == null && newDieselPrice == null) {
            return projectionRepository.putStationFields(event.stationCode, fields.mapValues { it.value.toString() })
        }

        val stationCode = event.stationCode
        return Mono.zip(
            projectionRepository.getStationField(stationCode, "gasolinePrice").map { it.toLongOrNull() },
            projectionRepository.getStationField(stationCode, "dieselPrice").map { it.toLongOrNull() }
        ).flatMap { oldPrices ->
            val oldGasolinePrice = oldPrices.t1
            val oldDieselPrice = oldPrices.t2
            val hashFields = fields.mapValues { it.value.toString() }.toMutableMap()

            var gasolinePriceYesterday: Long? = null
            if (newGasolinePrice != null) {
                gasolinePriceYesterday = oldGasolinePrice
                hashFields["gasolinePriceYesterday"] = oldGasolinePrice.toString()
            }

            var dieselPriceYesterday: Long? = null
            if (newDieselPrice != null) {
                dieselPriceYesterday = oldDieselPrice
                hashFields["dieselPriceYesterday"] = oldDieselPrice.toString()
            }

            Mono.zip(
                projectionRepository.putStationFields(stationCode, hashFields),
                updatePriceRankings(stationCode, newGasolinePrice, gasolinePriceYesterday, newDieselPrice, dieselPriceYesterday)
            ).map { it.t1 && it.t2 }
        }
    }

    private fun updatePriceRankings(
        stationCode: String,
        gasolinePrice: Long?, gasolinePriceYesterday: Long?,
        dieselPrice: Long?, dieselPriceYesterday: Long?
    ): Mono<Boolean> {
        val ops = mutableListOf<Mono<Boolean>>()

        if (gasolinePrice != null) {
            ops.add(projectionRepository.updateRanking(RedisKeys.RANKING_GASOLINE, stationCode, gasolinePrice.toDouble()))
            val increase = gasolinePrice - (gasolinePriceYesterday ?: gasolinePrice)
            ops.add(projectionRepository.updateRanking(RedisKeys.RANKING_GASOLINE_INCREASE, stationCode, increase.toDouble()))
        }

        if (dieselPrice != null) {
            ops.add(projectionRepository.updateRanking(RedisKeys.RANKING_DIESEL, stationCode, dieselPrice.toDouble()))
            val increase = dieselPrice - (dieselPriceYesterday ?: dieselPrice)
            ops.add(projectionRepository.updateRanking(RedisKeys.RANKING_DIESEL_INCREASE, stationCode, increase.toDouble()))
        }

        return Mono.zip(ops) { results -> results.all { it as Boolean } }
    }

    private fun toLongOrNull(value: Any?): Long? {
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Number -> value.toLong()
            is String -> value.toLongOrNull()
            else -> null
        }
    }
}
