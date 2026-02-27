package com.plantrue.example.fuelradar.repository

import com.plantrue.example.fuelradar.domain.GasStation
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface GasStationRepository : ReactiveCrudRepository<GasStation, Long>
