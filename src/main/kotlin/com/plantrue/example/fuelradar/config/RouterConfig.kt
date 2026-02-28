package com.plantrue.example.fuelradar.config

import com.plantrue.example.fuelradar.handler.GasStationCommandHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class RouterConfig {

    @Bean
    fun gasStationCommandRoutes(handler: GasStationCommandHandler): RouterFunction<ServerResponse> {
        return route()
            .POST("/api/stations", handler::createStation)
            .PUT("/api/stations/{stationCode}", handler::updateStation)
            .build()
    }
}
