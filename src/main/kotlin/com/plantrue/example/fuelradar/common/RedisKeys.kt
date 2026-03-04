package com.plantrue.example.fuelradar.common

object RedisKeys {
    private const val PREFIX = "gasstation"

    const val STREAM_KEY = "$PREFIX:events"
    const val CONSUMER_GROUP = "$PREFIX-event-consumer"

    const val RANKING_GASOLINE = "$PREFIX:rank:gasoline"
    const val RANKING_DIESEL = "$PREFIX:rank:diesel"
    const val RANKING_GASOLINE_INCREASE = "$PREFIX:rank:gasoline-increase"
    const val RANKING_DIESEL_INCREASE = "$PREFIX:rank:diesel-increase"

    const val STATION_DETAIL_PREFIX = "$PREFIX:detail:"
}
