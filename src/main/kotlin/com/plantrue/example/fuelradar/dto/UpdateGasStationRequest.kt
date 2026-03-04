package com.plantrue.example.fuelradar.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive

@Schema(description = "주유소 수정 요청")
data class UpdateGasStationRequest(
    @field:Schema(description = "지역", example = "서울 송파구")
    val region: String? = null,
    @field:Schema(description = "주유소명", example = "송파제일주유소")
    val name: String? = null,
    @field:Schema(description = "주소", example = "서울 송파구 삼학사로 50")
    val address: String? = null,
    @field:Schema(description = "브랜드", example = "SK에너지")
    val brand: String? = null,
    @field:Schema(description = "셀프 여부", example = "true")
    val selfService: Boolean? = null,
    @field:Positive
    @field:Schema(description = "휘발유 가격", example = "1699")
    val gasolinePrice: Long? = null,
    @field:Positive
    @field:Schema(description = "경유 가격", example = "1599")
    val dieselPrice: Long? = null
)
