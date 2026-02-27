package com.plantrue.example.fuelradar.config

import com.plantrue.example.fuelradar.domain.GasStation
import com.plantrue.example.fuelradar.event.GasStationChangeEvent
import com.plantrue.example.fuelradar.event.GasStationChangeEventType
import com.plantrue.example.fuelradar.repository.GasStationRepository
import java.time.LocalDateTime
import com.plantrue.example.fuelradar.service.GasStationEventPublisher
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
@Order(1)
class DataLoader(
    private val gasStationRepository: GasStationRepository,
    private val eventPublisher: GasStationEventPublisher
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments) {
        logger.info("Loading gas station data from CSV...")

        val resource = ClassPathResource("data/sample-opinet.csv")

        resource.inputStream.bufferedReader().use { reader ->
            Flux.fromStream(reader.lines())
                .skip(1)    // Skip CSV header
                .map { line ->
                    val parts = line.split(",")
                    GasStation(
                        stationCode = parts[0],
                        region = parts[1],
                        name = parts[2],
                        address = parts[3],
                        brand = parts[4],
                        selfService = parts[5] == "셀프",
                        gasolinePrice = parts[6].toLong(),
                        dieselPrice = parts[7].toLong(),
                        gasolinePriceYesterday = parts[8].toLong(),
                        dieselPriceYesterday = parts[9].toLong()
                    )
                }
                .flatMap { gasStation ->
                    gasStationRepository.save(gasStation)
                        .flatMap { saved -> publishCreatedEvent(saved) }
                }
                .count()
                .doOnSuccess { count -> logger.info("Loaded {} gas station records and published initial events", count) }
                .doOnError { logger.error("Error loading data", it) }
                .block()
        }
    }

    private fun publishCreatedEvent(station: GasStation): Mono<String> {
        val event = GasStationChangeEvent(
            eventId = eventPublisher.generateEventId(),
            stationCode = station.stationCode,
            eventType = GasStationChangeEventType.CREATED,
            timestamp = LocalDateTime.now(),
            changedFields = mapOf(
                "region" to station.region,
                "name" to station.name,
                "address" to station.address,
                "brand" to station.brand,
                "selfService" to station.selfService,
                "gasolinePrice" to station.gasolinePrice,
                "dieselPrice" to station.dieselPrice,
                "gasolinePriceYesterday" to station.gasolinePriceYesterday,
                "dieselPriceYesterday" to station.dieselPriceYesterday
            )
        )
        return eventPublisher.publishEvent(event)
    }
}
