package com.plantrue.example.fuelradar.domain

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("gas_stations")
data class GasStation(
    @Id
    val id: Long? = null,
    val stationCode: String,
    val region: String,
    val name: String,
    val address: String,
    val brand: String,
    val selfService: Boolean,
    val gasolinePrice: Long,
    val dieselPrice: Long,
    val gasolinePriceYesterday: Long,
    val dieselPriceYesterday: Long
)
