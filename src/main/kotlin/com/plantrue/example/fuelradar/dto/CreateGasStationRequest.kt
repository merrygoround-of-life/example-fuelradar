package com.plantrue.example.fuelradar.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class CreateGasStationRequest(
    @field:NotBlank val stationCode: String,
    val region: String,
    val name: String,
    val address: String,
    val brand: String,
    val selfService: Boolean,
    @field:Positive val gasolinePrice: Long,
    @field:Positive val dieselPrice: Long
)
