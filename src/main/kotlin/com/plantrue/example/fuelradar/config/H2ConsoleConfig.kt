package com.plantrue.example.fuelradar.config

import org.h2.tools.Server
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "h2.console")
data class H2ConsoleProperties(
    var port: Int = 8082,
    var enabled: Boolean = true
)

@Configuration
@EnableConfigurationProperties(H2ConsoleProperties::class)
class H2ConsoleConfig(private val properties: H2ConsoleProperties) {

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(name = ["h2.console.enabled"], havingValue = "true")
    fun h2WebServer(): Server {
        return Server.createWebServer("-web", "-webAllowOthers", "-webPort", properties.port.toString())
    }
}
