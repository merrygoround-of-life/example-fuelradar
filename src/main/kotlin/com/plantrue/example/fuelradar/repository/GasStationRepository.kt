package com.plantrue.example.fuelradar.repository

import com.plantrue.example.fuelradar.domain.GasStation
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono

interface GasStationRepository : ReactiveCrudRepository<GasStation, Long> {
    fun findByStationCode(stationCode: String): Mono<GasStation>
}
