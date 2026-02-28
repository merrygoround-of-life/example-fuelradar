package com.plantrue.example.fuelradar.handler

import com.plantrue.example.fuelradar.dto.ApiResponse
import com.plantrue.example.fuelradar.dto.CreateGasStationRequest
import com.plantrue.example.fuelradar.dto.ErrorDetail
import com.plantrue.example.fuelradar.dto.ErrorResponse
import com.plantrue.example.fuelradar.dto.UpdateGasStationRequest
import com.plantrue.example.fuelradar.service.GasStationCommandService
import jakarta.validation.Validator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Component
class GasStationCommandHandler(
    private val commandService: GasStationCommandService,
    private val validator: Validator
) {

    fun createStation(request: ServerRequest): Mono<ServerResponse> {
        return request.bodyToMono(CreateGasStationRequest::class.java)
            .flatMap { body ->
                validate(body)
                commandService.createStation(body)
            }
            .flatMap { station ->
                ServerResponse.status(201).bodyValue(ApiResponse(station, LocalDateTime.now()))
            }
            .onErrorResume { error ->
                ServerResponse.badRequest()
                    .bodyValue(ErrorResponse(ErrorDetail("STATION_CREATION_FAILED", error.message ?: "Unknown error"), LocalDateTime.now()))
            }
    }

    fun updateStation(request: ServerRequest): Mono<ServerResponse> {
        val stationCode = request.pathVariable("stationCode")
        return request.bodyToMono(UpdateGasStationRequest::class.java)
            .flatMap { body ->
                validate(body)
                commandService.updateStation(stationCode, body)
            }
            .flatMap { station ->
                ServerResponse.ok().bodyValue(ApiResponse(station, LocalDateTime.now()))
            }
            .onErrorResume { error ->
                ServerResponse.badRequest()
                    .bodyValue(ErrorResponse(ErrorDetail("STATION_UPDATE_FAILED", error.message ?: "Unknown error"), LocalDateTime.now()))
            }
    }

    private fun <T : Any> validate(body: T) {
        val violations = validator.validate(body)
        if (violations.isNotEmpty()) {
            val message = violations.joinToString(", ") { "${it.propertyPath}: ${it.message}" }
            throw IllegalArgumentException(message)
        }
    }
}
