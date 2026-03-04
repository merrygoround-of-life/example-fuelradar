package com.plantrue.example.fuelradar.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class ApiResponse<T>(
    val data: T,
    val timestamp: LocalDateTime
)

@Schema(description = "페이징된 API 응답")
data class PagedApiResponse<T>(
    @field:Schema(description = "페이징된 데이터 목록")
    val data: List<T>,
    @field:Schema(description = "현재 페이지 번호 (1부터 시작)", example = "1")
    val page: Int,
    @field:Schema(description = "페이지 크기", example = "50")
    val size: Int,
    @field:Schema(description = "전체 항목 수", example = "10428")
    val totalItems: Int,
    @field:Schema(description = "전체 페이지 수", example = "209")
    val totalPages: Int,
    @field:Schema(description = "응답 생성 시간")
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
