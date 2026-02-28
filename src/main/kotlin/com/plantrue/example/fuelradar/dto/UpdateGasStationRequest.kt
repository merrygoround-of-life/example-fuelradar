package com.plantrue.example.fuelradar.dto

import jakarta.validation.constraints.Positive

data class UpdateGasStationRequest(
    val region: String? = null,
    val name: String? = null,
    val address: String? = null,
    val brand: String? = null,
    val selfService: Boolean? = null,
    @field:Positive val gasolinePrice: Long? = null,
    @field:Positive val dieselPrice: Long? = null
)
