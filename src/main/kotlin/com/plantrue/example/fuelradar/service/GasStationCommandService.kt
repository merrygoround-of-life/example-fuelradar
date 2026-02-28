package com.plantrue.example.fuelradar.service

import com.plantrue.example.fuelradar.domain.GasStation
import com.plantrue.example.fuelradar.dto.CreateGasStationRequest
import com.plantrue.example.fuelradar.dto.UpdateGasStationRequest
import com.plantrue.example.fuelradar.event.GasStationChangeEvent
import com.plantrue.example.fuelradar.event.GasStationChangeEventType
import com.plantrue.example.fuelradar.repository.GasStationRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Service
class GasStationCommandService(
    private val gasStationRepository: GasStationRepository,
    private val eventPublisher: GasStationEventPublisher
) {

    fun createStation(request: CreateGasStationRequest): Mono<GasStation> {
        return gasStationRepository.findByStationCode(request.stationCode)
            .flatMap<GasStation> {
                Mono.error(RuntimeException("Station already exists: ${request.stationCode}"))
            }
            .switchIfEmpty(
                gasStationRepository.save(
                    GasStation(
                        stationCode = request.stationCode,
                        region = request.region,
                        name = request.name,
                        address = request.address,
                        brand = request.brand,
                        selfService = request.selfService,
                        gasolinePrice = request.gasolinePrice,
                        dieselPrice = request.dieselPrice,
                        gasolinePriceYesterday = request.gasolinePrice,
                        dieselPriceYesterday = request.dieselPrice
                    )
                )
            )
            .flatMap { saved ->
                val event = GasStationChangeEvent(
                    eventId = eventPublisher.generateEventId(),
                    stationCode = saved.stationCode,
                    eventType = GasStationChangeEventType.CREATED,
                    timestamp = LocalDateTime.now(),
                    changedFields = mapOf(
                        "region" to saved.region,
                        "name" to saved.name,
                        "address" to saved.address,
                        "brand" to saved.brand,
                        "selfService" to saved.selfService,
                        "gasolinePrice" to saved.gasolinePrice,
                        "dieselPrice" to saved.dieselPrice
                    )
                )
                eventPublisher.publishEvent(event).thenReturn(saved)
            }
    }

    fun updateStation(stationCode: String, request: UpdateGasStationRequest): Mono<GasStation> {
        return gasStationRepository.findByStationCode(stationCode)
            .switchIfEmpty(Mono.error(RuntimeException("Station not found: $stationCode")))
            .flatMap { station ->
                val changedFields = mutableMapOf<String, Any>()
                var updated = station

                request.region?.let {
                    updated = updated.copy(region = it)
                    changedFields["region"] = it
                }
                request.name?.let {
                    updated = updated.copy(name = it)
                    changedFields["name"] = it
                }
                request.address?.let {
                    updated = updated.copy(address = it)
                    changedFields["address"] = it
                }
                request.brand?.let {
                    updated = updated.copy(brand = it)
                    changedFields["brand"] = it
                }
                request.selfService?.let {
                    updated = updated.copy(selfService = it)
                    changedFields["selfService"] = it
                }
                request.gasolinePrice?.let {
                    updated = updated.copy(
                        gasolinePriceYesterday = station.gasolinePrice,
                        gasolinePrice = it
                    )
                    changedFields["gasolinePrice"] = it
                }
                request.dieselPrice?.let {
                    updated = updated.copy(
                        dieselPriceYesterday = station.dieselPrice,
                        dieselPrice = it
                    )
                    changedFields["dieselPrice"] = it
                }

                if (changedFields.isEmpty()) {
                    return@flatMap Mono.just(station)
                }

                gasStationRepository.save(updated)
                    .flatMap { saved ->
                        val event = GasStationChangeEvent(
                            eventId = eventPublisher.generateEventId(),
                            stationCode = saved.stationCode,
                            eventType = GasStationChangeEventType.UPDATED,
                            timestamp = LocalDateTime.now(),
                            changedFields = changedFields
                        )
                        eventPublisher.publishEvent(event).thenReturn(saved)
                    }
            }
    }
}
