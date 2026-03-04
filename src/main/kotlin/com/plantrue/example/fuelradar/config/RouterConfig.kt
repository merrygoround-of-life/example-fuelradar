package com.plantrue.example.fuelradar.config

import com.plantrue.example.fuelradar.dto.CreateGasStationRequest
import com.plantrue.example.fuelradar.dto.PagedApiResponse
import com.plantrue.example.fuelradar.dto.UpdateGasStationRequest
import com.plantrue.example.fuelradar.handler.GasStationCommandHandler
import com.plantrue.example.fuelradar.handler.GasStationQueryHandler
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springdoc.core.annotations.RouterOperation
import org.springdoc.core.annotations.RouterOperations
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class RouterConfig {

    @Bean
    @RouterOperations(
        RouterOperation(
            path = "/api/stations",
            method = [RequestMethod.POST],
            operation = Operation(
                operationId = "createStation",
                summary = "주유소 생성",
                description = "새로운 주유소를 등록합니다.",
                requestBody = RequestBody(
                    description = "생성할 주유소 정보",
                    required = true,
                    content = [Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = CreateGasStationRequest::class)
                    )]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "201",
                        description = "생성 성공",
                        content = [Content(schema = Schema(implementation = com.plantrue.example.fuelradar.dto.ApiResponse::class))]
                    ),
                    ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청 (이미 존재하는 주유소 코드 등)"
                    )
                ]
            )
        ),
        RouterOperation(
            path = "/api/stations/{stationCode}",
            method = [RequestMethod.PUT],
            operation = Operation(
                operationId = "updateStation",
                summary = "주유소 정보 수정",
                description = "기존 주유소의 가격, 지역, 브랜드 등을 수정합니다.",
                parameters = [
                    Parameter(
                        name = "stationCode",
                        `in` = ParameterIn.PATH,
                        required = true,
                        description = "주유소 코드",
                        schema = Schema(type = "string", example = "A0000015")
                    )
                ],
                requestBody = RequestBody(
                    description = "수정할 주유소 정보",
                    required = true,
                    content = [Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = UpdateGasStationRequest::class)
                    )]
                ),
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "수정 성공",
                        content = [Content(schema = Schema(implementation = com.plantrue.example.fuelradar.dto.ApiResponse::class))]
                    ),
                    ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청 (존재하지 않는 주유소 코드 등)"
                    )
                ]
            )
        )
    )
    fun gasStationCommandRoutes(handler: GasStationCommandHandler): RouterFunction<ServerResponse> {
        return route()
            .POST("/api/stations", handler::createStation)
            .PUT("/api/stations/{stationCode}", handler::updateStation)
            .build()
    }

    @Bean
    @RouterOperations(
        RouterOperation(
            path = "/api/stations/rankings/{tag}",
            method = [RequestMethod.GET],
            operation = Operation(
                operationId = "getRankings",
                summary = "주유소 순위 조회",
                description = "태그별 주유소 순위를 페이징으로 조회합니다.",
                parameters = [
                    Parameter(
                        name = "tag",
                        `in` = ParameterIn.PATH,
                        required = true,
                        description = "순위 태그 (gasoline: 휘발유 가격순, diesel: 경유 가격순, gasoline-increase: 휘발유 상승폭순, diesel-increase: 경유 상승폭순)",
                        schema = Schema(type = "string", allowableValues = ["gasoline", "diesel", "gasoline-increase", "diesel-increase"])
                    ),
                    Parameter(
                        name = "page",
                        `in` = ParameterIn.QUERY,
                        required = false,
                        description = "페이지 번호 (1부터 시작, 기본값: 1)",
                        schema = Schema(type = "integer", defaultValue = "1", minimum = "1")
                    ),
                    Parameter(
                        name = "size",
                        `in` = ParameterIn.QUERY,
                        required = false,
                        description = "페이지 크기 (1~100, 기본값: 50)",
                        schema = Schema(type = "integer", defaultValue = "50", minimum = "1", maximum = "100")
                    )
                ],
                responses = [
                    ApiResponse(
                        responseCode = "200",
                        description = "성공",
                        content = [Content(schema = Schema(implementation = PagedApiResponse::class))]
                    ),
                    ApiResponse(
                        responseCode = "400",
                        description = "잘못된 요청 (유효하지 않은 태그 또는 페이징 파라미터)"
                    )
                ]
            )
        )
    )
    fun gasStationQueryRoutes(handler: GasStationQueryHandler): RouterFunction<ServerResponse> {
        return route()
            .GET("/api/stations/rankings/{tag}", handler::getRankings)
            .build()
    }
}
