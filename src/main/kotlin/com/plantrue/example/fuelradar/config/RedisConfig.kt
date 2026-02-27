package com.plantrue.example.fuelradar.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

@ConfigurationProperties(prefix = "redis.container")
data class RedisContainerProperties(
    var enabled: Boolean = true,
    var port: Int = 6379
)

@Configuration
@EnableConfigurationProperties(RedisContainerProperties::class)
class RedisConfig(private val containerProperties: RedisContainerProperties) {

    @Bean(initMethod = "start", destroyMethod = "stop")
    fun redisContainer(): GenericContainer<*> {
        return GenericContainer("redis:7-alpine")
            .withExposedPorts(containerProperties.port)
            .waitingFor(Wait.forListeningPort())
    }

    @Bean
    @DependsOn("redisContainer")
    fun reactiveRedisConnectionFactory(redisContainer: GenericContainer<*>): ReactiveRedisConnectionFactory {
        return LettuceConnectionFactory("localhost", redisContainer.getMappedPort(containerProperties.port))
    }
}
