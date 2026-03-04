package com.plantrue.example.fuelradar.handler

import com.plantrue.example.fuelradar.dto.ErrorDetail
import com.plantrue.example.fuelradar.dto.ErrorResponse
import com.plantrue.example.fuelradar.dto.PagedApiResponse
import com.plantrue.example.fuelradar.service.GasStationQueryService
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class GasStationQueryHandler(
    private val queryService: GasStationQueryService
) {

    fun getRankings(request: ServerRequest): Mono<ServerResponse> {
        val tag = request.pathVariable("tag")
        val page = request.queryParam("page").orElse("1").toIntOrNull() ?: 1
        val size = request.queryParam("size").orElse("50").toIntOrNull() ?: 50

        if (page < 1) {
            return ServerResponse.badRequest()
                .bodyValue(ErrorResponse(ErrorDetail("INVALID_REQUEST", "Page must be at least 1"), LocalDateTime.now()))
        }
        if (size !in 1..100) {
            return ServerResponse.badRequest()
                .bodyValue(ErrorResponse(ErrorDetail("INVALID_REQUEST", "Size must be between 1 and 100"), LocalDateTime.now()))
        }

        return queryService.getRankings(tag, page - 1, size)
            .flatMap { result ->
                ServerResponse.ok().bodyValue(
                    PagedApiResponse(
                        data = result.rankings,
                        page = page,
                        size = size,
                        totalItems = result.totalItems,
                        totalPages = result.totalPages,
                        timestamp = LocalDateTime.now()
                    )
                )
            }
            .onErrorResume { error ->
                ServerResponse.badRequest()
                    .bodyValue(ErrorResponse(ErrorDetail("RANKING_FETCH_FAILED", error.message ?: "Unknown error"), LocalDateTime.now()))
            }
    }
}
