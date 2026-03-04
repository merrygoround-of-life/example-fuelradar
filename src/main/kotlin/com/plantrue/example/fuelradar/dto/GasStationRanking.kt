package com.plantrue.example.fuelradar.dto

data class GasStationRanking(
    val stationCode: String,
    val name: String,
    val region: String,
    val brand: String,
    val selfService: Boolean,
    val gasolinePrice: Long,
    val dieselPrice: Long,
    val gasolinePriceYesterday: Long,
    val dieselPriceYesterday: Long,
    val rank: Int
)
