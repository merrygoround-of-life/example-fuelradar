package com.plantrue.example.fuelradar.dto

import java.time.LocalDateTime

data class ApiResponse<T>(
    val data: T,
    val timestamp: LocalDateTime
)

data class PagedApiResponse<T>(
    val data: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Int,
    val totalPages: Int,
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
