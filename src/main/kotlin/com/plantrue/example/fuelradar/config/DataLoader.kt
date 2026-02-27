package com.plantrue.example.fuelradar.config

import com.plantrue.example.fuelradar.domain.GasStation
import com.plantrue.example.fuelradar.repository.GasStationRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

@Component
class DataLoader(
    private val gasStationRepository: GasStationRepository
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
                .flatMap { gasStation -> gasStationRepository.save(gasStation) }
                .count()
                .doOnSuccess { count -> logger.info("Loaded $count gas station records") }
                .doOnError { logger.error("Error loading data", it) }
                .block()
        }
    }
}
