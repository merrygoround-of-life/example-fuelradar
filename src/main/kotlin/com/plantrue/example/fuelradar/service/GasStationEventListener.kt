package com.plantrue.example.fuelradar.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.plantrue.example.fuelradar.common.RedisKeys
import com.plantrue.example.fuelradar.event.GasStationChangeEvent
import com.plantrue.example.fuelradar.event.GasStationChangeEventType
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Service
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDateTime
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.util.concurrent.atomic.AtomicReference

@Service
@Order(0)
class GasStationEventListener(
    private val redisTemplate: ReactiveRedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    environment: Environment
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(javaClass)
    private val disposableRef = AtomicReference<Disposable?>()
    private val consumerName = RedisKeys.CONSUMER_GROUP + "-" +
        InetAddress.getLocalHost().hostName + ":" +
        (environment.getProperty("server.port") ?: "8080")

    override fun run(args: ApplicationArguments) {
        val subscription = createConsumerGroupIfNotExists()
            .thenMany(startStreamReceiver())
            .doOnSubscribe { logger.info("Gas station event listener starting... (consumer: {})", consumerName) }
            .subscribe(
                { },
                { error -> logger.error("Stream receiver error", error) },
                { logger.info("Stream receiver completed") }
            )
        disposableRef.set(subscription)
    }

    private fun createConsumerGroupIfNotExists(): Mono<String> {
        return redisTemplate.opsForStream<String, String>()
            .createGroup(RedisKeys.STREAM_KEY, RedisKeys.CONSUMER_GROUP)
            .onErrorResume {
                logger.info("Consumer group already exists: {}", RedisKeys.CONSUMER_GROUP)
                Mono.just("OK")
            }
    }

    private fun startStreamReceiver(): Flux<Void> {
        val options = StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(Duration.ofSeconds(1))
            .build()

        return StreamReceiver.create(redisTemplate.connectionFactory, options)
            .receive(
                Consumer.from(RedisKeys.CONSUMER_GROUP, consumerName),
                StreamOffset.create(RedisKeys.STREAM_KEY, ReadOffset.lastConsumed())
            )
            .flatMap { record -> processEvent(record) }
            .onErrorContinue { error, _ ->
                logger.error("Error processing stream event", error)
            }
    }

    private fun processEvent(record: MapRecord<String, String, String>): Mono<Void> {
        return try {
            val eventData = record.value
            val event = parseGasStationChangeEvent(eventData)
            logger.debug("Processed event: {} for station {}", event.eventType, event.stationCode)

            acknowledgeMessage(record.id.value).then()
        } catch (e: Exception) {
            logger.error("Failed to parse event: {}", record.value, e)
            acknowledgeMessage(record.id.value).then()
        }
    }

    private fun parseGasStationChangeEvent(eventData: Map<String, String>): GasStationChangeEvent {
        return GasStationChangeEvent(
            eventId = eventData["eventId"] ?: throw IllegalArgumentException("Missing eventId"),
            stationCode = eventData["stationCode"] ?: throw IllegalArgumentException("Missing stationCode"),
            eventType = GasStationChangeEventType.valueOf(eventData["eventType"] ?: throw IllegalArgumentException("Missing eventType")),
            changedFields = parseChangedFields(eventData["changedFields"] ?: "{}"),
            timestamp = LocalDateTime.parse(eventData["timestamp"] ?: LocalDateTime.now().toString())
        )
    }

    private fun parseChangedFields(changedFieldsStr: String): Map<String, Any> {
        return try {
            objectMapper.readValue<Map<String, Any>>(changedFieldsStr)
        } catch (e: Exception) {
            logger.warn("Failed to parse changedFields: {}", changedFieldsStr, e)
            emptyMap()
        }
    }

    private fun acknowledgeMessage(messageId: String): Mono<Long> {
        return redisTemplate.opsForStream<String, String>()
            .acknowledge(RedisKeys.STREAM_KEY, RedisKeys.CONSUMER_GROUP, messageId)
    }

    @PreDestroy
    fun stop() {
        disposableRef.getAndSet(null)?.dispose()
        logger.info("Gas station event listener stopped (consumer: {})", consumerName)
    }
}
