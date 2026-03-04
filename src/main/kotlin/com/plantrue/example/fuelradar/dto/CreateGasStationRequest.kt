package com.plantrue.example.fuelradar.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

@Schema(description = "주유소 생성 요청")
data class CreateGasStationRequest(
    @field:NotBlank
    @field:Schema(description = "주유소 코드", example = "A0000015")
    val stationCode: String,
    @field:Schema(description = "지역", example = "서울 송파구")
    val region: String,
    @field:Schema(description = "주유소명", example = "송파제일주유소")
    val name: String,
    @field:Schema(description = "주소", example = "서울 송파구 삼학사로 50")
    val address: String,
    @field:Schema(description = "브랜드", example = "SK에너지")
    val brand: String,
    @field:Schema(description = "셀프 여부", example = "true")
    val selfService: Boolean,
    @field:Positive
    @field:Schema(description = "휘발유 가격", example = "1699")
    val gasolinePrice: Long,
    @field:Positive
    @field:Schema(description = "경유 가격", example = "1599")
    val dieselPrice: Long,
    @field:Positive
    @field:Schema(description = "전일 휘발유 가격 (미입력 시 현재가 적용)", example = "1709")
    val gasolinePriceYesterday: Long? = null,
    @field:Positive
    @field:Schema(description = "전일 경유 가격 (미입력 시 현재가 적용)", example = "1609")
    val dieselPriceYesterday: Long? = null
)
