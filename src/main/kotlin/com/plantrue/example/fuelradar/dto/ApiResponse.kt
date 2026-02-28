package com.plantrue.example.fuelradar.dto

import java.time.LocalDateTime

data class ApiResponse<T>(
    val data: T,
    val timestamp: LocalDateTime
)

data class ErrorResponse(
    val error: ErrorDetail,
    val timestamp: LocalDateTime
)

data class ErrorDetail(
    val code: String,
    val message: String
)
