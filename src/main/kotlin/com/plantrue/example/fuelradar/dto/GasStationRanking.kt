package com.plantrue.example.fuelradar.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주유소 순위 정보")
data class GasStationRanking(
    @field:Schema(description = "주유소 코드", example = "A0000015")
    val stationCode: String,
    @field:Schema(description = "주유소명", example = "송파제일주유소")
    val name: String,
    @field:Schema(description = "지역", example = "서울 송파구")
    val region: String,
    @field:Schema(description = "브랜드", example = "SK에너지")
    val brand: String,
    @field:Schema(description = "셀프 여부", example = "true")
    val selfService: Boolean,
    @field:Schema(description = "휘발유 가격", example = "1699")
    val gasolinePrice: Long,
    @field:Schema(description = "경유 가격", example = "1599")
    val dieselPrice: Long,
    @field:Schema(description = "전일 휘발유 가격", example = "1709")
    val gasolinePriceYesterday: Long,
    @field:Schema(description = "전일 경유 가격", example = "1609")
    val dieselPriceYesterday: Long,
    @field:Schema(description = "순위", example = "1")
    val rank: Int
)
